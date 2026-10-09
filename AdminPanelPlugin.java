package com.yourname.adminpanel;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AdminPanelPlugin extends JavaPlugin implements CommandExecutor, Listener {

    private final Set<UUID> staffModePlayers = new HashSet<>();
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final Set<UUID> godModePlayers = new HashSet<>();
    private boolean globalChatMuted = false;

    private final String PREFIX = "§8[§b§lADMIN§8] §7";

    @Override
    public void onEnable() {
        getCommand("staffmode").setExecutor(this);
        getCommand("adminpanel").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AdminPanel v2.0 (Design Edition) successfully loaded!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("staffmode")) {
            if (staffModePlayers.contains(player.getUniqueId())) {
                staffModePlayers.remove(player.getUniqueId());
                player.sendMessage(PREFIX + "§cStaff Mode has been §lDISABLED§c.");
                if (vanishedPlayers.contains(player.getUniqueId())) toggleVanish(player);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f);
            } else {
                staffModePlayers.add(player.getUniqueId());
                player.sendMessage(PREFIX + "§aStaff Mode has been §lENABLED§a.");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("adminpanel")) {
            if (!staffModePlayers.contains(player.getUniqueId())) {
                player.sendMessage(PREFIX + "§cYou must enable §e/staffmode §cfirst!");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return true;
            }
            openMainGUI(player);
            return true;
        }
        return false;
    }

    private void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§8» §c§lAdmin Control Panel §8«");

        // Fill background with gray glass
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) {
            gui.setItem(i, filler);
        }

        // Section Dividers
        ItemStack redDivider = createItem(Material.RED_STAINED_GLASS_PANE, " ");
        gui.setItem(4, redDivider);
        gui.setItem(13, redDivider);
        gui.setItem(22, redDivider);
        gui.setItem(31, redDivider);
        gui.setItem(40, redDivider);
        gui.setItem(49, redDivider);

        // --- LEFT SIDE: PLAYER ACTIONS ---
        gui.setItem(10, createItem(Material.GOLDEN_APPLE, "§a§lHeal Player", "§8» §7Restore max health and remove effects"));
        gui.setItem(11, createItem(Material.COOKED_BEEF, "§6§lFeed Player", "§8» §7Restore max hunger and saturation"));
        
        gui.setItem(19, createItem(Material.FEATHER, "§b§lToggle Flight", "§8» §7Enable or disable fly mode"));
        gui.setItem(20, createItem(Material.GLASS, "§d§lToggle Vanish", "§8» §7Become completely invisible to players"));

        gui.setItem(28, createItem(Material.NETHER_STAR, "§e§lToggle God Mode", "§8» §7Become immune to all damage"));
        gui.setItem(29, createItem(Material.SUGAR, "§f§lSpeed Boost", "§8» §7Grants you Speed II permanently"));
        
        gui.setItem(37, createItem(Material.DIAMOND_CHESTPLATE, "§b§lCreative Mode", "§8» §7Switch to Creative GameMode"));
        gui.setItem(38, createItem(Material.IRON_CHESTPLATE, "§a§lSurvival Mode", "§8» §7Switch to Survival GameMode"));

        // --- RIGHT SIDE: SERVER & MODERATION ---
        gui.setItem(15, createItem(Material.CHEST, "§6§lInventory See", "§8» §7View/Edit an online player's inventory"));
        gui.setItem(16, createItem(Material.ENDER_CHEST, "§5§lEnderchest See", "§8» §7View/Edit an online player's enderchest"));

        gui.setItem(24, createItem(Material.PAPER, "§c§lClear Chat", "§8» §7Spam empty lines to clear global chat"));
        gui.setItem(25, createItem(Material.BARRIER, "§4§lMute Global Chat", "§8» §7Prevent non-staff from typing in chat"));

        gui.setItem(33, createItem(Material.SUNFLOWER, "§e§lSet Time: Day", "§8» §7Sets the world time to Morning"));
        gui.setItem(34, createItem(Material.CAMPFIRE, "§8§lSet Time: Night", "§8» §7Sets the world time to Midnight"));

        gui.setItem(42, createItem(Material.DIAMOND_SWORD, "§4§lKill All Hostile Mobs", "§8» §7Removes all monsters in the current world"));
        gui.setItem(43, createItem(Material.COMPASS, "§d§lTeleport to Spawn", "§8» §7Teleports you to the world's spawn point"));

        // Utility at the bottom
        gui.setItem(45, createItem(Material.LAVA_BUCKET, "§c§lTrash Can", "§8» §7Clear YOUR own inventory instantly"));
        gui.setItem(53, createItem(Material.ANVIL, "§e§lRepair Hand Item", "§8» §7Fully repairs the item in your main hand"));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);
    }

    private void openPlayerSelector(Player player, String title) {
        Inventory selector = Bukkit.createInventory(null, 54, title);
        for (Player online : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(online);
                meta.setDisplayName("§e§l" + online.getName());
                meta.setLore(Arrays.asList("§8» §7Click to inspect"));
                head.setItemMeta(meta);
            }
            selector.addItem(head);
        }
        player.openInventory(selector);
    }

    private ItemStack createItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        Player player = (Player) event.getWhoClicked();

        if (title.equals("§8» §c§lAdmin Control Panel §8«")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta() || clicked.getType().toString().contains("GLASS_PANE")) return;

            String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

            switch (itemName) {
                case "Heal Player":
                    player.setHealth(20.0);
                    for (PotionEffect effect : player.getActivePotionEffects()) player.removePotionEffect(effect.getType());
                    player.sendMessage(PREFIX + "§aYour health has been restored!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
                    break;
                case "Feed Player":
                    player.setFoodLevel(20);
                    player.setSaturation(20f);
                    player.sendMessage(PREFIX + "§6Your hunger has been satisfied!");
                    player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1f, 1f);
                    break;
                case "Toggle Flight":
                    player.setAllowFlight(!player.getAllowFlight());
                    player.sendMessage(PREFIX + "§bFlight Mode is now " + (player.getAllowFlight() ? "§a§lENABLED" : "§c§lDISABLED") + "§b.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f);
                    break;
                case "Toggle Vanish":
                    toggleVanish(player);
                    break;
                case "Toggle God Mode":
                    if (godModePlayers.contains(player.getUniqueId())) {
                        godModePlayers.remove(player.getUniqueId());
                        player.sendMessage(PREFIX + "§cGod Mode §lDISABLED§c.");
                    } else {
                        godModePlayers.add(player.getUniqueId());
                        player.sendMessage(PREFIX + "§aGod Mode §lENABLED§a.");
                    }
                    player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1f, 1f);
                    break;
                case "Speed Boost":
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1, false, false, false));
                    player.sendMessage(PREFIX + "§fSpeed II Boost applied!");
                    break;
                case "Creative Mode":
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage(PREFIX + "§3GameMode set to §bCreative§3.");
                    break;
                case "Survival Mode":
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage(PREFIX + "§3GameMode set to §aSurvival§3.");
                    break;
                case "Inventory See":
                    openPlayerSelector(player, "§8» §6Select Player (Invsee) §8«");
                    return; // Don't close inventory yet
                case "Enderchest See":
                    openPlayerSelector(player, "§8» §5Select Player (E-Chest) §8«");
                    return; // Don't close inventory yet
                case "Trash Can":
                    player.getInventory().clear();
                    player.sendMessage(PREFIX + "§cYour inventory has been wiped clean.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1f, 1f);
                    break;
                case "Repair Hand Item":
                    ItemStack inHand = player.getInventory().getItemInMainHand();
                    if (inHand.getType() != Material.AIR && inHand.getItemMeta() instanceof Damageable) {
                        Damageable meta = (Damageable) inHand.getItemMeta();
                        meta.setDamage(0);
                        inHand.setItemMeta(meta);
                        player.sendMessage(PREFIX + "§aItem in your hand has been fully repaired!");
                        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1f, 1f);
                    } else {
                        player.sendMessage(PREFIX + "§cYou are not holding a repairable item.");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                    }
                    break;
                case "Teleport to Spawn":
                    player.teleport(player.getWorld().getSpawnLocation());
                    player.sendMessage(PREFIX + "§dTeleported to the world spawn point.");
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
                    break;
                case "Set Time: Day":
                    player.getWorld().setTime(1000);
                    player.sendMessage(PREFIX + "§eTime has been changed to Day.");
                    break;
                case "Set Time: Night":
                    player.getWorld().setTime(13000);
                    player.sendMessage(PREFIX + "§8Time has been changed to Night.");
                    break;
                case "Clear Chat":
                    for (int i = 0; i < 100; i++) Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(" ");
                    Bukkit.broadcastMessage("§8[§c§l!§8] §c§lGLOBAL CHAT HAS BEEN CLEARED BY A STAFF MEMBER.");
                    Bukkit.broadcastMessage(" ");
                    break;
                case "Mute Global Chat":
                    globalChatMuted = !globalChatMuted;
                    if (globalChatMuted) {
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage("§8[§c§l!§8] §c§lGLOBAL CHAT HAS BEEN MUTED BY STAFF.");
                        Bukkit.broadcastMessage(" ");
                    } else {
                        Bukkit.broadcastMessage(" ");
                        Bukkit.broadcastMessage("§8[§a§l!§8] §a§lGLOBAL CHAT HAS BEEN UNMUTED.");
                        Bukkit.broadcastMessage(" ");
                    }
                    break;
                case "Kill All Hostile Mobs":
                    int count = 0;
                    for (Entity ent : player.getWorld().getEntities()) {
                        if (ent instanceof Monster) {
                            ent.remove();
                            count++;
                        }
                    }
                    player.sendMessage(PREFIX + "§4Slaughtered §c" + count + " §4hostile mobs in this world.");
                    break;
            }
            player.closeInventory();
        }

        if (title.equals("§8» §6Select Player (Invsee) §8«")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
                String targetName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                Player target = Bukkit.getPlayerExact(targetName);
                if (target != null) {
                    player.openInventory(target.getInventory());
                    player.sendMessage(PREFIX + "§eOpening inventory of §6" + target.getName() + "§e.");
                } else {
                    player.sendMessage(PREFIX + "§cPlayer is no longer online.");
                    player.closeInventory();
                }
            }
        }

        if (title.equals("§8» §5Select Player (E-Chest) §8«")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
                String targetName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                Player target = Bukkit.getPlayerExact(targetName);
                if (target != null) {
                    player.openInventory(target.getEnderChest());
                    player.sendMessage(PREFIX + "§eOpening enderchest of §6" + target.getName() + "§e.");
                } else {
                    player.sendMessage(PREFIX + "§cPlayer is no longer online.");
                    player.closeInventory();
                }
            }
        }
    }

    private void toggleVanish(Player player) {
        if (vanishedPlayers.contains(player.getUniqueId())) {
            vanishedPlayers.remove(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) p.showPlayer(this, player);
            player.sendMessage(PREFIX + "§cYou are no longer in vanish mode. You are §lVISIBLE§c.");
        } else {
            vanishedPlayers.add(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("adminpanel.use")) p.hidePlayer(this, player);
            }
            player.sendMessage(PREFIX + "§aYou are now in vanish mode. You are §lHIDDEN§a from normal players.");
        }
        player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 1f, 1f);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (godModePlayers.contains(player.getUniqueId())) event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (globalChatMuted && !event.getPlayer().hasPermission("adminpanel.use")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§8[§c§l!§8] §cThe global chat is currently muted by a staff member!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        for (UUID uuid : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished != null && !joiner.hasPermission("adminpanel.use")) {
                joiner.hidePlayer(this, vanished);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        staffModePlayers.remove(id);
        vanishedPlayers.remove(id);
        godModePlayers.remove(id);
    }
}
