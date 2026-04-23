package fr.oxidayzz.world;

import fr.oxidayzz.world.generator.BiomeGroup;
import fr.oxidayzz.world.generator.FlexibleBiomeProvider;
import fr.oxidayzz.world.generator.OrePopulator;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class WorldService {

    private final WorldManager plugin;
    private final Map<UUID, PendingAction> pendingActions = new HashMap<>();

    public WorldService(WorldManager plugin) {
        this.plugin = plugin;
    }

    public enum ActionType { CREATE, DELETE }
    
    // Record incluant les 6 taux de minerais (en %) et l'option structures
    public record PendingAction(String name, boolean isFlat, String biomeList, 
                                int d, int g, int i, int l, int e, int r, 
                                boolean noStructures, ActionType type) {}

    public void askConfirmation(Player player, String name, boolean isFlat, String biomeList, 
                                int d, int g, int i, int l, int e, int r, boolean noStructures) {
        
        pendingActions.put(player.getUniqueId(), new PendingAction(name, isFlat, biomeList, d, g, i, l, e, r, noStructures, ActionType.CREATE));
        
        player.sendMessage("§6§lREMPLACEMENT §7- Monde: §b" + name);
        player.sendMessage("§eBoosts: §fDiamant: " + d + "% | Or: " + g + "% | Fer: " + i + "%");
        player.sendMessage("§fLapis: " + l + "% | Emeraude: " + e + "% | Redstone: " + r + "%");
        if (noStructures) player.sendMessage("§c⚠ Structures désactivées");
        player.sendMessage("§7Tapez §a/rw confirm §7pour valider.");
    }

    public void askDeleteConfirmation(Player player, String name) {
        pendingActions.put(player.getUniqueId(), new PendingAction(name, false, null, 0, 0, 0, 0, 0, 0, false, ActionType.DELETE));
        player.sendMessage("§6§lSUPPRESSION §7- Monde: §b" + name);
        player.sendMessage("§7Tapez §a/rw confirm §7pour supprimer définitivement.");
    }

    public void confirm(Player player) {
        PendingAction pending = pendingActions.remove(player.getUniqueId());
        if (pending == null) return;

        if (pending.type() == ActionType.DELETE) {
            executeDeletion(player, pending.name());
        } else {
            World old = Bukkit.getWorld(pending.name());
            if (old != null) Bukkit.unloadWorld(old, false);
            
            // Délai pour libérer les fichiers (raids.dat etc)
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                deleteWorldFolder(new File(Bukkit.getWorldContainer(), pending.name()));
                executeCreation(player, pending);
            }, 5L);
        }
    }

    public void cancel(Player player) {
        pendingActions.remove(player.getUniqueId());
        player.sendMessage("§cAction annulée.");
    }

    private void executeCreation(Player player, PendingAction pa) {
    // 1. Affichage de la barre de progression
    new BukkitRunnable() {
        int progress = 0;
        @Override
        public void run() {
            if (progress > 100) { this.cancel(); return; }
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§eGénération: §b" + progress + "%"));
            progress += 10;
        }
    }.runTaskTimer(plugin, 0L, 5L);

    // 2. Configuration du créateur (SANS toucher au .generator())
    WorldCreator creator = new WorldCreator(pa.name());
    creator.generateStructures(!pa.noStructures());
    if (pa.isFlat()) creator.type(WorldType.FLAT);

    // 3. Gestion des biomes (on garde notre FlexibleBiomeProvider)
    if (pa.biomeList() != null && !pa.biomeList().isEmpty()) {
        List<BiomeGroup> selectedGroups = new ArrayList<>();
        for (String key : pa.biomeList().split(",")) {
            BiomeGroup bg = BiomeGroup.fromKey(key.trim());
            if (bg != null) selectedGroups.add(bg);
        }
        if (!selectedGroups.isEmpty()) {
            creator.biomeProvider(new FlexibleBiomeProvider(selectedGroups));
        }
    }

    // 4. Création du monde
    World world = Bukkit.createWorld(creator);

    if (world != null) {
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        
        // 5. C'est ICI qu'on ajoute les minerais sans casser le terrain !
        // On ajoute notre OrePopulator à la liste des populators existants du monde
        world.getPopulators().add(new OrePopulator(pa.d(), pa.g(), pa.i(), pa.l(), pa.e(), pa.r()));

        // 6. Sécurisation du spawn
        Location spawn = new Location(world, 0.5, 100, 0.5);
        world.setSpawnLocation(spawn);
        spawn.clone().subtract(0, 1, 0).getBlock().setType(Material.GLASS);
    }
    
    player.sendMessage("§a§lSUCCÈS ! §7Le monde §f" + pa.name() + " §7est généré avec relief et minerais.");
  }

    private void executeDeletion(CommandSender sender, String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            for (Player p : world.getPlayers()) p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            Bukkit.unloadWorld(world, false);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (deleteWorldFolder(new File(Bukkit.getWorldContainer(), name))) {
                sender.sendMessage("§aMonde supprimé.");
            }
        }, 10L);
    }

    public void teleportPlayer(Player player, String worldName) {
        World target = Bukkit.getWorld(worldName);
        if (target != null) {
            player.teleport(target.getSpawnLocation());
            player.sendMessage("§aTéléporté !");
        } else player.sendMessage("§cMonde introuvable.");
    }

    private boolean deleteWorldFolder(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteWorldFolder(f);
                    else f.delete();
                }
            }
            return path.delete();
        }
        return true;
    }
}