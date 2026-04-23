package fr.oxidayzz.world;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldService {

    private final WorldManager plugin;
    // On stocke maintenant une "Action" en attente (soit CREATE, soit DELETE)
    private final Map<UUID, PendingAction> pendingActions = new HashMap<>();

    public WorldService(WorldManager plugin) {
        this.plugin = plugin;
    }

    // Enum pour différencier l'action à confirmer
    public enum ActionType { CREATE, DELETE }
    public record PendingAction(String name, boolean isFlat, ActionType type) {}

    public void askConfirmation(Player player, String name, boolean isFlat) {
        File worldFolder = new File(Bukkit.getWorldContainer(), name);
        
        if (Bukkit.getWorld(name) == null && !worldFolder.exists()) {
            executeCreation(player, name, isFlat);
            return;
        }

        pendingActions.put(player.getUniqueId(), new PendingAction(name, isFlat, ActionType.CREATE));
        player.sendMessage("§6§lATTENTION ! §eLe monde §b" + name + " §eexiste déjà.");
        player.sendMessage("§7Voulez-vous le §cremplacer §7? (§a/rw confirm §7ou §c/rw cancel§7)");
    }

    // Nouvelle méthode pour demander la suppression
    public void askDeleteConfirmation(Player player, String name) {
        World world = Bukkit.getWorld(name);
        File worldFolder = new File(Bukkit.getWorldContainer(), name);

        if (world == null && !worldFolder.exists()) {
            player.sendMessage("§cCe monde n'existe pas.");
            return;
        }

        // Protection des mondes par défaut
        if (isDefaultWorld(name)) {
            player.sendMessage("§cImpossible de supprimer un monde principal du serveur.");
            return;
        }

        pendingActions.put(player.getUniqueId(), new PendingAction(name, false, ActionType.DELETE));
        player.sendMessage("§6§lDANGER ! §eVous allez supprimer le monde §b" + name + "§e.");
        player.sendMessage("§7Cette action est irréversible. (§a/rw confirm §7ou §c/rw cancel§7)");
    }

    public void confirm(Player player) {
        PendingAction pending = pendingActions.remove(player.getUniqueId());
        if (pending == null) {
            player.sendMessage("§cAucune action en attente.");
            return;
        }

        if (pending.type() == ActionType.DELETE) {
            executeDeletion(player, pending.name());
        } else {
            // C'est un remplacement (CREATE)
            World oldWorld = Bukkit.getWorld(pending.name());
            if (oldWorld != null) {
                Bukkit.unloadWorld(oldWorld, false);
            }
            deleteWorldFolder(new File(Bukkit.getWorldContainer(), pending.name()));
            executeCreation(player, pending.name(), pending.isFlat());
        }
    }

    public void cancel(Player player) {
        if (pendingActions.remove(player.getUniqueId()) != null) {
            player.sendMessage("§cAction annulée.");
        } else {
            player.sendMessage("§cRien à annuler.");
        }
    }

    private void executeDeletion(CommandSender sender, String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            // On téléporte les joueurs avant de décharger
            World fallback = Bukkit.getWorlds().get(0);
            for (Player p : world.getPlayers()) {
                p.teleport(fallback.getSpawnLocation());
                p.sendMessage("§7Le monde a été supprimé, retour au spawn.");
            }
            Bukkit.unloadWorld(world, false);
        }
        deleteWorldFolder(new File(Bukkit.getWorldContainer(), name));
        sender.sendMessage("§a§lSUCCÈS ! §7Le monde §f" + name + " §7a été supprimé.");
    }

    private void executeCreation(Player player, String name, boolean isFlat) {
        new BukkitRunnable() {
            int progress = 0;
            @Override
            public void run() {
                if (progress > 100) {
                    this.cancel();
                    return;
                }
                String bar = renderProgressBar(progress);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                    new TextComponent("§eCréation: " + bar + " §b" + progress + "%"));
                progress += 10;
            }
        }.runTaskTimer(plugin, 0L, 5L);

        WorldCreator creator = new WorldCreator(name);
        if (isFlat) {
            creator.type(WorldType.FLAT);
            creator.generateStructures(false);
        }
        
        Bukkit.createWorld(creator);
        player.sendMessage("§a§lSUCCÈS ! §7Le monde §f" + name + " §7est prêt.");
    }

    private String renderProgressBar(int percent) {
        StringBuilder bar = new StringBuilder("§8[");
        int completed = percent / 10;
        for (int i = 0; i < 10; i++) {
            bar.append(i < completed ? "§a■" : "§7■");
        }
        return bar.append("§8]").toString();
    }

    private void deleteWorldFolder(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) deleteWorldFolder(f);
                    else f.delete();
                }
            }
            path.delete();
        }
    }

    private boolean isDefaultWorld(String name) {
        return name.equalsIgnoreCase("world") || name.equalsIgnoreCase("world_nether") || name.equalsIgnoreCase("world_the_end");
    }
}