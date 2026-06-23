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
    
    public record PendingAction(String name, boolean isFlat, String biomeList, 
                                int d, int g, int i, int l, int e, int r, 
                                boolean noStructures, ActionType type) {}

    // --- GESTION DES CONFIRMATIONS ---

    public void askConfirmation(Player player, String name, boolean isFlat, String biomeList, 
                                int d, int g, int i, int l, int e, int r, boolean noStructures) {
        
        pendingActions.put(player.getUniqueId(), new PendingAction(name, isFlat, biomeList, d, g, i, l, e, r, noStructures, ActionType.CREATE));
        
        player.sendMessage("§6§lREMPLACEMENT §7- Monde: §b" + name);
        player.sendMessage("§eBoosts: §fD:" + d + "% G:" + g + "% I:" + i + "% L:" + l + "% E:" + e + "% R:" + r + "%");
        if (noStructures) player.sendMessage("§c⚠ Structures désactivées");
        player.sendMessage("§7Tapez §a/rw confirm §7pour valider.");
    }

    public void askDeleteConfirmation(Player player, String name) {
        pendingActions.put(player.getUniqueId(), new PendingAction(name, false, null, 0,0,0,0,0,0, false, ActionType.DELETE));
        player.sendMessage("§6§lSUPPRESSION §7- Monde: §b" + name);
        player.sendMessage("§7Tapez §a/rw confirm §7pour supprimer.");
    }

    public void confirm(Player player) {
        PendingAction pending = pendingActions.remove(player.getUniqueId());
        if (pending == null) {
            player.sendMessage("§cAucune action en attente de confirmation.");
            return;
        }

        if (pending.type() == ActionType.DELETE) {
            executeDeletion(player, pending.name());
        } else {
            World old = Bukkit.getWorld(pending.name());
            if (old != null) {
                boolean unloaded = Bukkit.unloadWorld(old, false);
                if (!unloaded) {
                    player.sendMessage("§cImpossible de décharger le monde §f" + pending.name() + "§c. Annulation.");
                    return;
                }
            }
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!deleteWorldFolder(new File(Bukkit.getWorldContainer(), pending.name()))) {
                    player.sendMessage("§c⚠ Suppression partielle du dossier monde §f" + pending.name() + "§c.");
                }
                executeCreation(player, pending);
            }, 5L);
        }
    }

    public void cancel(Player player) {
        pendingActions.remove(player.getUniqueId());
        player.sendMessage("§cAction annulée.");
    }

    // --- ACTIONS DE CRÉATION ET SUPPRESSION ---

    private void executeCreation(Player player, PendingAction pa) {
        WorldCreator creator = new WorldCreator(pa.name());
        if (pa.isFlat()) creator.type(WorldType.FLAT);
        creator.generateStructures(!pa.noStructures());

        if (pa.biomeList() != null && !pa.biomeList().isEmpty()) {
            List<BiomeGroup> selectedGroups = new ArrayList<>();
            for (String key : pa.biomeList().split(",")) {
                BiomeGroup bg = BiomeGroup.fromKey(key.trim());
                if (bg != null) selectedGroups.add(bg);
            }
            if (!selectedGroups.isEmpty()) creator.biomeProvider(new FlexibleBiomeProvider(selectedGroups));
        }

        World world;
        try {
            world = Bukkit.createWorld(creator);
        } catch (Exception e) {
            plugin.getLogger().severe("Erreur lors de la création du monde '" + pa.name() + "': " + e.getMessage());
            player.sendMessage("§c§lERREUR ! §7Impossible de créer le monde §f" + pa.name() + "§7.");
            return;
        }

        if (world == null) {
            plugin.getLogger().warning("Création du monde '" + pa.name() + "' a retourné null.");
            player.sendMessage("§c§lERREUR ! §7La création du monde §f" + pa.name() + " §7a échoué.");
            return;
        }

        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.getPopulators().add(new OrePopulator(pa.d(), pa.g(), pa.i(), pa.l(), pa.e(), pa.r()));

        Location spawn = new Location(world, 0.5, 100, 0.5);
        world.setSpawnLocation(spawn);
        spawn.clone().subtract(0, 1, 0).getBlock().setType(Material.GLASS);

        player.sendMessage("§a§lSUCCÈS ! §7Le monde §f" + pa.name() + " §7est créé.");
    }

    private void executeDeletion(CommandSender sender, String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            for (Player p : world.getPlayers()) p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            boolean unloaded = Bukkit.unloadWorld(world, false);
            if (!unloaded) {
                sender.sendMessage("§cImpossible de décharger le monde §f" + name + "§c. Suppression annulée.");
                return;
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (deleteWorldFolder(new File(Bukkit.getWorldContainer(), name))) {
                sender.sendMessage("§aSupprimé.");
            } else {
                sender.sendMessage("§cÉchec de la suppression du dossier monde §f" + name + "§c.");
            }
        }, 10L);
    }

    // --- SYSTÈME DE PREGEN (ASYNC) ---

    public void pregenWorld(Player player, String worldName, int radiusInChunks) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage("§cMonde introuvable.");
            return;
        }

        int total = (int) Math.pow((radiusInChunks * 2 + 1), 2);
        player.sendMessage("§e[Pregen] §7Génération de §b" + total + " §7chunks...");

        int[] count = {0};
        int[] errors = {0};
        long startTime = System.currentTimeMillis();

        for (int x = -radiusInChunks; x <= radiusInChunks; x++) {
            for (int z = -radiusInChunks; z <= radiusInChunks; z++) {
                world.getChunkAtAsync(x, z).thenAccept(chunk -> {
                    count[0]++;

                    if (count[0] % 50 == 0 || count[0] == total) {
                        double progress = (double) count[0] / total * 100;
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            new TextComponent(String.format("§ePregen: §b%.1f%% §7(%d/%d)", progress, count[0], total)));
                    }

                    if (count[0] == total) {
                        long duration = (System.currentTimeMillis() - startTime) / 1000;
                        String msg = "§a§lTERMINÉ ! §7" + total + " chunks générés en " + duration + "s.";
                        if (errors[0] > 0) {
                            msg += " §c(" + errors[0] + " erreurs)";
                        }
                        player.sendMessage(msg);
                        world.save();
                    }
                }).exceptionally(ex -> {
                    errors[0]++;
                    count[0]++;
                    plugin.getLogger().warning("Erreur pregen chunk: " + ex.getMessage());
                    if (count[0] == total) {
                        long duration = (System.currentTimeMillis() - startTime) / 1000;
                        player.sendMessage("§e[Pregen] §7Terminé en " + duration + "s avec §c" + errors[0] + " erreur(s)§7.");
                        world.save();
                    }
                    return null;
                });
            }
        }
    }

    // --- SYSTÈME DE SCAN ---

    public void scanOres(Player player, int radius) {
        player.sendMessage("§e[Scan] §7Analyse...");
        Map<Material, Integer> c = new HashMap<>();
        List<Material> target = Arrays.asList(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE);
        target.forEach(m -> c.put(m, 0));

        Chunk center = player.getLocation().getChunk();
        int chunks = 0;
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                Chunk ck = player.getWorld().getChunkAt(center.getX()+x, center.getZ()+z);
                chunks++;
                for (int bx=0; bx<16; bx++) for (int bz=0; bz<16; bz++) for (int by=-64; by<80; by++) {
                    Material type = ck.getBlock(bx, by, bz).getType();
                    if (c.containsKey(type)) c.put(type, c.get(type)+1);
                }
            }
        }
        player.sendMessage("§6Résultats: §bDia:" + (c.get(Material.DIAMOND_ORE)+c.get(Material.DEEPSLATE_DIAMOND_ORE))/chunks + "/ch | §eOr:" + (c.get(Material.GOLD_ORE)+c.get(Material.DEEPSLATE_GOLD_ORE))/chunks + "/ch");
    }

    // --- UTILITAIRES ---

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
                    if (f.isDirectory()) {
                        if (!deleteWorldFolder(f)) {
                            plugin.getLogger().warning("Impossible de supprimer le dossier: " + f.getAbsolutePath());
                        }
                    } else {
                        if (!f.delete()) {
                            plugin.getLogger().warning("Impossible de supprimer le fichier: " + f.getAbsolutePath());
                        }
                    }
                }
            }
            return path.delete();
        }
        return true;
    }
}