package fr.oxidayzz.world;

import fr.oxidayzz.world.generator.BiomeGroup;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class WorldGUI {

    private final WorldManager plugin;
    private final Map<UUID, GuiSettings> playerSettings = new HashMap<>();

    public WorldGUI(WorldManager plugin) {
        this.plugin = plugin;
    }

    public static class GuiSettings {
        public boolean isFlat = false;
        public int d = 100, g = 100, i = 100, l = 100, e = 100, r = 100;
        public Set<String> selectedBiomes = new HashSet<>();
    }

    public void openMainGui(Player player) {
        GuiSettings s = playerSettings.computeIfAbsent(player.getUniqueId(), k -> new GuiSettings());
        Inventory inv = Bukkit.createInventory(null, 54, "§8Configuration: Arena");

        inv.setItem(4, createGuiItem(Material.MAP, "§6Mondes existants", "§eCliquez pour voir la liste"));
        inv.setItem(11, createGuiItem(s.isFlat ? Material.GRASS_BLOCK : Material.DIRT, 
            "§6Type: " + (s.isFlat ? "§aPlat" : "§7Normal"), "§eChanger le relief"));
        inv.setItem(15, createGuiItem(Material.OAK_SAPLING, "§6Sélection des Biomes", "§7Actuels: §f" + s.selectedBiomes.size()));

        inv.setItem(19, createGuiItem(Material.DIAMOND_ORE, "§bDiamant: §f" + s.d + "%", "§7G: -10% | D: +10%"));
        inv.setItem(20, createGuiItem(Material.GOLD_ORE, "§eOr: §f" + s.g + "%", "§7G: -10% | D: +10%"));
        inv.setItem(21, createGuiItem(Material.IRON_ORE, "§fFer: §f" + s.i + "%", "§7G: -10% | D: +10%"));
        inv.setItem(22, createGuiItem(Material.LAPIS_ORE, "§9Lapis: §f" + s.l + "%", "§7G: -10% | D: +10%"));
        inv.setItem(23, createGuiItem(Material.EMERALD_ORE, "§aEmeraude: §f" + s.e + "%", "§7G: -10% | D: +10%"));
        inv.setItem(24, createGuiItem(Material.REDSTONE_ORE, "§cRedstone: §f" + s.r + "%", "§7G: -10% | D: +10%"));

        inv.setItem(40, createGuiItem(Material.LIME_CONCRETE, "§a§lCRÉER ARENA", "§7Lancer la confirmation"));
        inv.setItem(42, createGuiItem(Material.BEACON, "§b§lPREGEN ARENA", "§7Lancer la pré-génération (Rayon 32)"));
        inv.setItem(44, createGuiItem(Material.BARRIER, "§c§lSUPPRIMER ARENA", "§7Supprimer le monde Arena"));

        player.openInventory(inv);
    }

    public void openBiomeGui(Player player) {
        GuiSettings s = playerSettings.get(player.getUniqueId());
        Inventory inv = Bukkit.createInventory(null, 27, "§8Sélection des Biomes");

        int slot = 0;
        for (BiomeGroup bg : BiomeGroup.values()) {
            boolean selected = s.selectedBiomes.contains(bg.getKey());
            ItemStack item = createGuiItem(Material.PAPER, (selected ? "§a" : "§7") + bg.getKey(), "§eCliquez pour basculer");
            if (selected) {
                ItemMeta m = item.getItemMeta();
                if (m != null) {
                    m.addEnchant(Enchantment.MENDING, 1, true);
                    m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.setItemMeta(m);
                }
            }
            inv.setItem(slot++, item);
        }
        inv.setItem(26, createGuiItem(Material.ARROW, "§cRetour", ""));
        player.openInventory(inv);
    }

    public void openWorldsListGui(Player player) {
        Inventory inv = Bukkit.createInventory(null, 36, "§8Liste des Mondes");
        int slot = 0;
        for (World w : Bukkit.getWorlds()) {
            if (slot >= 35) break; 
            Material icon = w.getWorldType() == WorldType.FLAT ? Material.GRASS_BLOCK : Material.DIRT;
            inv.setItem(slot++, createGuiItem(icon, "§b" + w.getName(), "§eCliquez pour vous TP"));
        }
        inv.setItem(35, createGuiItem(Material.ARROW, "§cRetour", ""));
        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material mat, String name, String lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Collections.singletonList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }

    public Map<UUID, GuiSettings> getPlayerSettings() { return playerSettings; }
}