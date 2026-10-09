package com.deinname.adminpanel;

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

    @Override
    public void onEnable() {
        getCommand("staffmode").setExecutor(this);
        getCommand("adminpanel").setExecutor(this);
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("AdminPanel (Verbessert) geladen!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (command.getName().equalsIgnoreCase("staffmode")) {
            if (staffModePlayers.contains(player.getUniqueId())) {
                staffModePlayers.remove(player.getUniqueId());
                player.sendMessage(ChatColor.RED + "Staff-Modus deaktiviert.");
                // Disable vanish if leaving staff mode
                if (vanishedPlayers.contains(player.getUniqueId())) toggleVanish(player);
            } else {
                staffModePlayers.add(player.getUniqueId());
                player.sendMessage(ChatColor.GREEN + "Staff-Modus aktiviert.");
            }
            return true;
        }

        if (command.getName().equalsIgnoreCase("adminpanel")) {
            if (!staffModePlayers.contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "Du musst im /staffmode sein, um das Admin-Panel zu öffnen!");
                return true;
            }
            openMainGUI(player);
            return true;
        }
        return false;
    }

    private void openMainGUI(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§4§lAdmin Panel");

        // Row 1: Player States
        gui.setItem(10, createItem(Material.GOLDEN_APPLE, "§aHeilen", "§7Stellt volles Leben her"));
        gui.setItem(11, createItem(Material.COOKED_BEEF, "§6Füttern", "§7Füllt Hunger auf"));
        gui.setItem(12, createItem(Material.FEATHER, "§bFlug-Modus", "§7Fliegen an/aus"));
        gui.setItem(13, createItem(Material.GLASS, "§dVanish", "§7Versteckt dich vor Spielern"));
        gui.setItem(14, createItem(Material.NETHER_STAR, "§eGod Mode", "§7Macht dich unbesiegbar"));
        gui.setItem(15, createItem(Material.ENDER_PEARL, "§5Enderchest ansehen", "§7Öffnet Spieler-Enderchest"));
        gui.setItem(16, createItem(Material.RABBIT_FOOT, "§fSpeed Boost", "§7Gibt dir Schnelligkeit"));

        // Row 2: Gamemodes
        gui.setItem(19, createItem(Material.DIAMOND_BLOCK, "§bKreativ", "§7Setzt Spielmodus"));
        gui.setItem(20, createItem(Material.DIRT, "§aSurvival", "§7Setzt Spielmodus"));
        gui.setItem(21, createItem(Material.ENDER_EYE, "§7Zuschauer", "§7Setzt Spielmodus"));
        gui.setItem(22, createItem(Material.CHEST, "§6Invsee", "§7Spielerinventar ansehen"));
        gui.setItem(23, createItem(Material.LAVA_BUCKET, "§cInventar leeren", "§7Leert DEIN Inventar"));
        gui.setItem(24, createItem(Material.ANVIL, "§eItem Reparieren", "§7Repariert Item in der Hand"));
        gui.setItem(25, createItem(Material.COMPASS, "§dZum Spawn", "§7Teleportiert zum Spawn"));

        // Row 3: Server & World
        gui.setItem(28, createItem(Material.SUNFLOWER, "§eTag", "§7Zeit auf Tag"));
        gui.setItem(29, createItem(Material.CAMPFIRE, "§8Nacht", "§7Zeit auf Nacht"));
        gui.setItem(30, createItem(Material.LIGHT_BLUE_STAINED_GLASS, "§bSonne", "§7Wetter auf Klar"));
        gui.setItem(31, createItem(Material.WATER_BUCKET, "§9Regen", "§7Wetter auf Regen"));
        gui.setItem(32, createItem(Material.PAPER, "§cChat leeren", "§7Leert Chat für alle"));
        gui.setItem(33, createItem(Material.BARRIER, "§4Chat Muten", "§7Globaler Chat an/aus"));
        gui.setItem(34, createItem(Material.DIAMOND_SWORD, "§4Mobs Töten", "§7Tötet feindliche Mobs"));

        player.openInventory(gui);
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1f, 1f);
    }

    private void openPlayerSelector(Player player, String title) {
        Inventory selector = Bukkit.createInventory(null, 54, title);
        for (Player online : Bukkit.getOnlinePlayers()) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(online);
                meta.setDisplayName("§e" + online.getName());
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

        if (title.equals("§4§lAdmin Panel")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

            switch (itemName) {
                case "Heilen":
                    player.setHealth(20.0);
                    player.sendMessage("§aDu wurdest geheilt.");
                    break;
                case "Füttern":
                    player.setFoodLevel(20);
                    player.sendMessage("§6Du wurdest gefüttert.");
                    break;
                case "Flug-Modus":
                    player.setAllowFlight(!player.getAllowFlight());
                    player.sendMessage("§bFlug-Modus " + (player.getAllowFlight() ? "Aktiviert" : "Deaktiviert") + ".");
                    break;
                case "Vanish":
                    toggleVanish(player);
                    break;
                case "God Mode":
                    if (godModePlayers.contains(player.getUniqueId())) {
                        godModePlayers.remove(player.getUniqueId());
                        player.sendMessage("§cGod Mode deaktiviert.");
                    } else {
                        godModePlayers.add(player.getUniqueId());
                        player.sendMessage("§aGod Mode aktiviert.");
                    }
                    break;
                case "Speed Boost":
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 999999, 1));
                    player.sendMessage("§fSpeed Boost angewendet.");
                    break;
                case "Kreativ":
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage("§bSpielmodus: Kreativ.");
                    break;
                case "Survival":
                    player.setGameMode(GameMode.SURVIVAL);
                    player.sendMessage("§aSpielmodus: Survival.");
                    break;
                case "Zuschauer":
                    player.setGameMode(GameMode.SPECTATOR);
                    player.sendMessage("§7Spielmodus: Zuschauer.");
                    break;
                case "Invsee":
                    openPlayerSelector(player, "§8Spieler wählen (Invsee)");
                    break;
                case "Enderchest ansehen":
                    openPlayerSelector(player, "§5Spieler wählen (E-Chest)");
                    break;
                case "Inventar leeren":
                    player.getInventory().clear();
                    player.sendMessage("§cDein Inventar wurde geleert.");
                    break;
                case "Item Reparieren":
                    ItemStack inHand = player.getInventory().getItemInMainHand();
                    if (inHand.getType() != Material.AIR && inHand.getItemMeta() instanceof Damageable) {
                        Damageable meta = (Damageable) inHand.getItemMeta();
                        meta.setDamage(0);
                        inHand.setItemMeta(meta);
                        player.sendMessage("§aItem repariert!");
                    } else {
                        player.sendMessage("§cDu hältst kein reparierbares Item.");
                    }
                    break;
                case "Zum Spawn":
                    player.teleport(player.getWorld().getSpawnLocation());
                    player.sendMessage("§dZum Spawn teleportiert.");
                    break;
                case "Tag":
                    player.getWorld().setTime(1000);
                    player.sendMessage("§eZeit auf Tag gesetzt.");
                    break;
                case "Nacht":
                    player.getWorld().setTime(13000);
                    player.sendMessage("§8Zeit auf Nacht gesetzt.");
                    break;
                case "Sonne":
                    player.getWorld().setStorm(false);
                    player.sendMessage("§bWetter ist nun klar.");
                    break;
                case "Regen":
                    player.getWorld().setStorm(true);
                    player.sendMessage("§9Wetter auf Regen gesetzt.");
                    break;
                case "Chat leeren":
                    for (int i = 0; i < 100; i++) Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage("§cDer Chat wurde von einem Teammitglied geleert.");
                    break;
                case "Chat Muten":
                    globalChatMuted = !globalChatMuted;
                    Bukkit.broadcastMessage(globalChatMuted ? "§cGlobaler Chat wurde stummgeschaltet." : "§aGlobaler Chat ist wieder offen.");
                    break;
                case "Mobs Töten":
                    int count = 0;
                    for (Entity ent : player.getWorld().getEntities()) {
                        if (ent instanceof Monster) {
                            ent.remove();
                            count++;
                        }
                    }
                    player.sendMessage("§4" + count + " feindliche Mobs entfernt.");
                    break;
            }
            if (itemName.equals("Invsee") || itemName.equals("Enderchest ansehen")) return;
            player.closeInventory();
        }

        if (title.startsWith("§8Spieler wählen (Invsee)")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
                String targetName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                Player target = Bukkit.getPlayerExact(targetName);
                if (target != null) {
                    player.openInventory(target.getInventory());
                } else {
                    player.sendMessage("§cSpieler offline.");
                }
            }
        }

        if (title.startsWith("§5Spieler wählen (E-Chest)")) {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
                String targetName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                Player target = Bukkit.getPlayerExact(targetName);
                if (target != null) {
                    player.openInventory(target.getEnderChest());
                } else {
                    player.sendMessage("§cSpieler offline.");
                }
            }
        }
    }

    private void toggleVanish(Player player) {
        if (vanishedPlayers.contains(player.getUniqueId())) {
            vanishedPlayers.remove(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) p.showPlayer(this, player);
            player.sendMessage("§cVanish deaktiviert. Du bist sichtbar.");
        } else {
            vanishedPlayers.add(player.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("adminpanel.use")) p.hidePlayer(this, player);
            }
            player.sendMessage("§aVanish aktiviert. Unsichtbar für normale Spieler.");
        }
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
            event.getPlayer().sendMessage("§cDer globale Chat ist derzeit gestummt.");
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
