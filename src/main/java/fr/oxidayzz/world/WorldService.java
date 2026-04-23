package fr.oxidayzz.world;

import fr.oxidayzz.world.generator.BiomeGroup;
import fr.oxidayzz.world.generator.FlexibleBiomeProvider;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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
    public record PendingAction(String name, boolean isFlat, String biomeList, ActionType type) {}

    public void askConfirmation(Player player, String name, boolean isFlat, String biomeList) {
        File worldFolder = new File(Bukkit.getWorldContainer(), name);
        if (Bukkit.getWorld(name) == null && !worldFolder.exists()) {
            executeCreation(player, name, isFlat, biomeList);
            return;
        }

        pendingActions.put(player.getUniqueId(), new PendingAction(name, isFlat, biomeList, ActionType.CREATE));
        player.sendMessage("§6§lATTENTION ! §eLe monde §b" + name + " §eexiste déjà.");
        player.sendMessage("§7Voulez-vous le §cremplacer §7? (§a/rw confirm §7ou §c/rw cancel§7)");
    }

    public void askDeleteConfirmation(Player player, String name) {
        World world = Bukkit.getWorld(name);
        if (world == null && !new File(Bukkit.getWorldContainer(), name).exists()) {
            player.sendMessage("§cCe monde n'existe pas.");
            return;
        }

        if (name.equalsIgnoreCase("world") || name.toLowerCase().contains("nether") || name.toLowerCase().contains("end")) {
            player.sendMessage("§cImpossible de supprimer un monde principal.");
            return;
        }

        pendingActions.put(player.getUniqueId(), new PendingAction(name, false, null, ActionType.DELETE));
        player.sendMessage("§6§lDANGER ! §eVous allez supprimer §b" + name + "§e.");
        player.sendMessage("§7Action irréversible. (§a/rw confirm §7ou §c/rw cancel§7)");
    }

    public void confirm(Player player) {
        PendingAction pending = pendingActions.remove(player.getUniqueId());
        if (pending == null) return;

        if (pending.type() == ActionType.DELETE) {
            executeDeletion(player, pending.name());
        } else {
            World old = Bukkit.getWorld(pending.name());
            if (old != null) {
                // On décharge SANS sauvegarder pour éviter les erreurs de fichiers occupés
                Bukkit.unloadWorld(old, false);
            }
            
            // On attend 5 ticks (1/4 de sec) pour laisser Windows/Linux libérer le dossier
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                deleteWorldFolder(new File(Bukkit.getWorldContainer(), pending.name()));
                executeCreation(player, pending.name(), pending.isFlat(), pending.biomeList());
            }, 5L);
        }
    }

    public void cancel(Player player) {
        if (pendingActions.remove(player.getUniqueId()) != null) player.sendMessage("§cAnnulé.");
    }

    public void teleportPlayer(Player player, String worldName) {
        World target = Bukkit.getWorld(worldName);
        if (target == null) {
            player.sendMessage("§cMonde introuvable.");
            return;
        }
        player.teleport(target.getSpawnLocation());
        player.sendMessage("§aTéléporté !");
    }

    private void executeDeletion(CommandSender sender, String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            World fallback = Bukkit.getWorlds().get(0);
            for (Player p : world.getPlayers()) p.teleport(fallback.getSpawnLocation());
            Bukkit.unloadWorld(world, false);
        }
        
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (deleteWorldFolder(new File(Bukkit.getWorldContainer(), name))) {
                sender.sendMessage("§aSupprimé avec succès.");
            }
        }, 10L);
    }

    private void executeCreation(Player player, String name, boolean isFlat, String biomeList) {
        new BukkitRunnable() {
            int progress = 0;
            @Override
            public void run() {
                if (progress > 100) { this.cancel(); return; }
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§eCréation: §b" + progress + "%"));
                progress += 10;
            }
        }.runTaskTimer(plugin, 0L, 5L);

        WorldCreator creator = new WorldCreator(name);
        if (isFlat) {
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
        }

        if (biomeList != null && !biomeList.isEmpty()) {
            List<BiomeGroup> selectedGroups = new ArrayList<>();
            for (String key : biomeList.split(",")) {
                BiomeGroup bg = BiomeGroup.fromKey(key.trim());
                if (bg != null) selectedGroups.add(bg);
            }
            if (!selectedGroups.isEmpty()) {
                creator.biomeProvider(new FlexibleBiomeProvider(selectedGroups));
            }
        }

        World world = Bukkit.createWorld(creator);
        if (world != null) {
            // Désactive les raids pour éviter l'erreur de fichier raids.dat au prochain unload
            world.setGameRule(GameRule.DISABLE_RAIDS, true);
        }
        player.sendMessage("§a§lSUCCÈS ! §7Monde §f" + name + " §7prêt.");
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