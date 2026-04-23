package fr.oxidayzz.world;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiListener implements Listener {

    private final WorldManager plugin;

    public GuiListener(WorldManager plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith("§8")) return;
        
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        WorldGUI.GuiSettings s = plugin.getWorldGui().getPlayerSettings().get(player.getUniqueId());
        if (s == null) return;

        int slot = event.getSlot();

        if (title.equals("§8Configuration: Arena")) {
            if (slot == 4) plugin.getWorldGui().openWorldsListGui(player);
            else if (slot == 11) { s.isFlat = !s.isFlat; plugin.getWorldGui().openMainGui(player); }
            else if (slot == 15) plugin.getWorldGui().openBiomeGui(player);
            else if (slot >= 19 && slot <= 24) {
                int mod = event.isRightClick() ? 10 : -10;
                if (slot == 19) s.d = Math.max(0, s.d + mod);
                else if (slot == 20) s.g = Math.max(0, s.g + mod);
                else if (slot == 21) s.i = Math.max(0, s.i + mod);
                else if (slot == 22) s.l = Math.max(0, s.l + mod);
                else if (slot == 23) s.e = Math.max(0, s.e + mod);
                else if (slot == 24) s.r = Math.max(0, s.r + mod);
                plugin.getWorldGui().openMainGui(player);
            }
            else if (slot == 40) {
                String biomes = s.selectedBiomes.isEmpty() ? "plaine" : String.join(",", s.selectedBiomes);
                plugin.getWorldService().askConfirmation(player, "Arena", s.isFlat, biomes, s.d, s.g, s.i, s.l, s.e, s.r, false);
                player.closeInventory();
            }
            else if (slot == 42) {
                plugin.getWorldService().pregenWorld(player, "Arena", 32);
                player.closeInventory();
            }
            else if (slot == 44) {
                plugin.getWorldService().askDeleteConfirmation(player, "Arena");
                player.closeInventory();
            }
        } 
        else if (title.equals("§8Sélection des Biomes")) {
            if (slot == 26) plugin.getWorldGui().openMainGui(player);
            else if (event.getCurrentItem().getType() == Material.PAPER) {
                String biomeName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);
                if (s.selectedBiomes.contains(biomeName)) s.selectedBiomes.remove(biomeName);
                else s.selectedBiomes.add(biomeName);
                plugin.getWorldGui().openBiomeGui(player);
            }
        } 
        else if (title.equals("§8Liste des Mondes")) {
            if (slot == 35) plugin.getWorldGui().openMainGui(player);
            else {
                String worldName = event.getCurrentItem().getItemMeta().getDisplayName().substring(2);
                plugin.getWorldService().teleportPlayer(player, worldName);
                player.closeInventory();
            }
        }
    }
}