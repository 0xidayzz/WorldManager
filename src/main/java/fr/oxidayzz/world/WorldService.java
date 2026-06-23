package fr.oxidayzz.world;

import fr.oxidayzz.world.generator.BiomeGroup;
import fr.oxidayzz.world.generator.FlexibleBiomeProvider;
import fr.oxidayzz.world.generator.OrePopulator;
import fr.oxidayzz.world.util.ChunkIterator;
import fr.oxidayzz.world.util.MessageUtil;
import fr.oxidayzz.world.util.WorldUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        
        player.sendMessage("\u00A76\u00A7lREMPLACEMENT \u00A77- Monde: \u00A7b" + name);
        player.sendMessage("\u00A7eBoosts: \u00A7fD:" + d + "% G:" + g + "% I:" + i + "% L:" + l + "% E:" + e + "% R:" + r + "%");
        if (noStructures) player.sendMessage("\u00A7c\u26A0 Structures d\u00E9sactiv\u00E9es");
        player.sendMessage("\u00A77Tapez \u00A7a/rw confirm \u00A77pour valider.");
    }

    public void askDeleteConfirmation(Player player, String name) {
        pendingActions.put(player.getUniqueId(), new PendingAction(name, false, null, 0,0,0,0,0,0, false, ActionType.DELETE));
        player.sendMessage("\u00A76\u00A7lSUPPRESSION \u00A77- Monde: \u00A7b" + name);
        player.sendMessage("\u00A77Tapez \u00A7a/rw confirm \u00A77pour supprimer.");
    }

    public void confirm(Player player) {
        PendingAction pending = pendingActions.remove(player.getUniqueId());
        if (pending == null) return;

        if (pending.type() == ActionType.DELETE) {
            executeDeletion(player, pending.name());
        } else {
            World old = Bukkit.getWorld(pending.name());
            if (old != null) Bukkit.unloadWorld(old, false);
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                WorldUtil.deleteFolder(new File(Bukkit.getWorldContainer(), pending.name()));
                executeCreation(player, pending);
            }, 5L);
        }
    }

    public void cancel(Player player) {
        pendingActions.remove(player.getUniqueId());
        MessageUtil.sendError(player, "Action annul\u00E9e.");
    }

    // --- ACTIONS DE CR\u00C9ATION ET SUPPRESSION ---

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

        World world = Bukkit.createWorld(creator);
        if (world != null) {
            world.setGameRule(GameRule.DISABLE_RAIDS, true);
            world.getPopulators().add(new OrePopulator(pa.d(), pa.g(), pa.i(), pa.l(), pa.e(), pa.r()));
            
            Location spawn = new Location(world, 0.5, 100, 0.5);
            world.setSpawnLocation(spawn);
            spawn.clone().subtract(0, 1, 0).getBlock().setType(Material.GLASS);
        }
        MessageUtil.sendSuccess(player, "SUCC\u00C8S ! \u00A77Le monde \u00A7f" + pa.name() + " \u00A77est cr\u00E9\u00E9.");
    }

    private void executeDeletion(CommandSender sender, String name) {
        Optional<World> opt = WorldUtil.getWorldOrNotify(sender, name);
        if (opt.isPresent()) {
            World world = opt.get();
            for (Player p : world.getPlayers()) p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
            Bukkit.unloadWorld(world, false);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (WorldUtil.deleteFolder(new File(Bukkit.getWorldContainer(), name))) {
                MessageUtil.sendSuccess(sender, "Supprim\u00E9.");
            }
        }, 10L);
    }

    // --- SYST\u00C8ME DE PREGEN (ASYNC) ---

    public void pregenWorld(Player player, String worldName, int radiusInChunks) {
        Optional<World> opt = WorldUtil.getWorldOrNotify(player, worldName);
        if (opt.isEmpty()) return;
        World world = opt.get();

        int total = ChunkIterator.totalChunks(radiusInChunks);
        player.sendMessage("\u00A7e[Pregen] \u00A77G\u00E9n\u00E9ration de \u00A7b" + total + " \u00A77chunks...");

        int[] count = {0};
        long startTime = System.currentTimeMillis();

        ChunkIterator.forEachInRadius(radiusInChunks, (x, z) -> {
            world.getChunkAtAsync(x, z).thenAccept(chunk -> {
                count[0]++;
                
                if (count[0] % 50 == 0 || count[0] == total) {
                    double progress = (double) count[0] / total * 100;
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
                        new TextComponent(String.format("\u00A7ePregen: \u00A7b%.1f%% \u00A77(%d/%d)", progress, count[0], total)));
                }

                if (count[0] == total) {
                    long duration = (System.currentTimeMillis() - startTime) / 1000;
                    MessageUtil.sendSuccess(player, "TERMIN\u00C9 ! \u00A77" + total + " chunks g\u00E9n\u00E9r\u00E9s en " + duration + "s.");
                    world.save();
                }
            });
        });
    }

    // --- SYST\u00C8ME DE SCAN ---

    public void scanOres(Player player, int radius) {
        player.sendMessage("\u00A7e[Scan] \u00A77Analyse...");
        Map<Material, Integer> c = new HashMap<>();
        List<Material> target = Arrays.asList(Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE, Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE);
        target.forEach(m -> c.put(m, 0));

        Chunk center = player.getLocation().getChunk();
        int[] chunks = {0};

        ChunkIterator.forEachInRadius(radius, (x, z) -> {
            Chunk ck = player.getWorld().getChunkAt(center.getX() + x, center.getZ() + z);
            chunks[0]++;
            for (int bx = 0; bx < 16; bx++) {
                for (int bz = 0; bz < 16; bz++) {
                    for (int by = -64; by < 80; by++) {
                        Material type = ck.getBlock(bx, by, bz).getType();
                        if (c.containsKey(type)) c.put(type, c.get(type) + 1);
                    }
                }
            }
        });

        int totalChunks = chunks[0];
        player.sendMessage("\u00A76R\u00E9sultats: \u00A7bDia:" + (c.get(Material.DIAMOND_ORE) + c.get(Material.DEEPSLATE_DIAMOND_ORE)) / totalChunks + "/ch | \u00A7eOr:" + (c.get(Material.GOLD_ORE) + c.get(Material.DEEPSLATE_GOLD_ORE)) / totalChunks + "/ch");
    }

    // --- UTILITAIRES ---

    public void teleportPlayer(Player player, String worldName) {
        WorldUtil.getWorldOrNotify(player, worldName).ifPresent(world -> {
            player.teleport(world.getSpawnLocation());
            MessageUtil.sendSuccess(player, "T\u00E9l\u00E9port\u00E9 !");
        });
    }
}
