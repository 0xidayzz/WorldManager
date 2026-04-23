package fr.oxidayzz.world;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldService {

    private final WorldManager plugin;
    private final Map<UUID, PendingWorld> pendingCreations = new HashMap<>();

    public WorldService(WorldManager plugin) {
        this.plugin = plugin;
    }

    public record PendingWorld(String name, boolean isFlat) {}

    public void askConfirmation(Player player, String name, boolean isFlat) {
        File worldFolder = new File(Bukkit.getWorldContainer(), name);
        
        // Si le monde n'existe pas du tout, on crée direct
        if (Bukkit.getWorld(name) == null && !worldFolder.exists()) {
            executeCreation(player, name, isFlat);
            return;
        }

        // Sinon, on met en attente
        pendingCreations.put(player.getUniqueId(), new PendingWorld(name, isFlat));
        player.sendMessage("§6§lATTENTION ! §eLe monde §b" + name + " §eexiste déjà.");
        player.sendMessage("§7Voulez-vous le §cremplacer §7? (§a/rw confirm §7ou §c/rw cancel§7)");
    }

    public void confirm(Player player) {
        PendingWorld pending = pendingCreations.remove(player.getUniqueId());
        if (pending == null) {
            player.sendMessage("§cAucune création en attente.");
            return;
        }

        // Nettoyage avant recréation
        World oldWorld = Bukkit.getWorld(pending.name());
        if (oldWorld != null) {
            Bukkit.unloadWorld(oldWorld, false);
        }
        deleteWorldFolder(new File(Bukkit.getWorldContainer(), pending.name()));
        
        executeCreation(player, pending.name(), pending.isFlat());
    }

    public void cancel(Player player) {
        if (pendingCreations.remove(player.getUniqueId()) != null) {
            player.sendMessage("§cAction annulée.");
        } else {
            player.sendMessage("§cRien à annuler.");
        }
    }

    private void executeCreation(Player player, String name, boolean isFlat) {
        // Lancement de l'animation Action Bar
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

        // Création technique
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
}