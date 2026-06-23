package fr.oxidayzz.world.commands;

import fr.oxidayzz.world.WorldManager;
import fr.oxidayzz.world.generator.BiomeGroup;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WorldCommands implements CommandExecutor, TabCompleter {

    private static final int MAX_PREGEN_RADIUS = 100;
    private static final int MAX_SCAN_RADIUS = 10;

    private final WorldManager plugin;
    private final List<String> subCommands = Arrays.asList("create", "delete", "tp", "confirm", "cancel", "list", "scan", "pregen");

    public WorldCommands(WorldManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) return true;

        switch (args[0].toLowerCase()) {
            case "create":
                if (!player.hasPermission("worldmanager.create")) {
                    player.sendMessage("§cVous n'avez pas la permission.");
                    break;
                }
                if (args.length >= 9) {
                    try {
                        String worldName = args[1];
                        if (!isValidWorldName(worldName)) {
                            player.sendMessage("§cNom de monde invalide. Utilisez uniquement des lettres, chiffres, tirets et underscores.");
                            break;
                        }
                        plugin.getWorldService().askConfirmation(player, worldName, args[2].equalsIgnoreCase("flat"), 
                                args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), 
                                Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), 
                                (args.length == 10 && args[9].equalsIgnoreCase("nostruct")));
                    } catch (NumberFormatException e) { player.sendMessage("§cUsage: /rw create <nom> <biomes> <D%> <G%> <I%> <L%> <E%> <R%> [nostruct]"); }
                }
                break;

            case "pregen":
                if (!player.hasPermission("worldmanager.pregen")) {
                    player.sendMessage("§cVous n'avez pas la permission.");
                    break;
                }
                if (args.length >= 3) {
                    try {
                        int radius = Integer.parseInt(args[2]);
                        if (radius < 1 || radius > MAX_PREGEN_RADIUS) {
                            player.sendMessage("§cLe rayon doit etre entre 1 et " + MAX_PREGEN_RADIUS + ".");
                            break;
                        }
                        plugin.getWorldService().pregenWorld(player, args[1], radius);
                    } catch (NumberFormatException e) {
                        player.sendMessage("§cLe rayon doit etre un nombre valide.");
                    }
                } else player.sendMessage("§c/rw pregen <monde> <rayon>");
                break;

            case "scan":
                if (!player.hasPermission("worldmanager.scan")) {
                    player.sendMessage("§cVous n'avez pas la permission.");
                    break;
                }
                try {
                    int radius = args.length >= 2 ? Integer.parseInt(args[1]) : 1;
                    if (radius < 1 || radius > MAX_SCAN_RADIUS) {
                        player.sendMessage("§cLe rayon doit etre entre 1 et " + MAX_SCAN_RADIUS + ".");
                        break;
                    }
                    plugin.getWorldService().scanOres(player, radius);
                } catch (NumberFormatException e) {
                    player.sendMessage("§cLe rayon doit etre un nombre valide.");
                }
                break;

            case "tp":
                if (args.length >= 2) plugin.getWorldService().teleportPlayer(player, args[1]);
                break;

            case "delete":
                if (!player.hasPermission("worldmanager.delete")) {
                    player.sendMessage("§cVous n'avez pas la permission.");
                    break;
                }
                if (args.length >= 2) {
                    String worldName = args[1];
                    if (!isValidWorldName(worldName)) {
                        player.sendMessage("§cNom de monde invalide.");
                        break;
                    }
                    plugin.getWorldService().askDeleteConfirmation(player, worldName);
                }
                break;

            case "confirm": plugin.getWorldService().confirm(player); break;
            case "cancel": plugin.getWorldService().cancel(player); break;
            case "list":
                player.sendMessage("§6Mondes:");
                for (World w : Bukkit.getWorlds()) player.sendMessage("§8• §f" + w.getName());
                break;
        }
        return true;
    }

    private boolean isValidWorldName(String name) {
        return name != null && name.matches("[a-zA-Z0-9_\\-]+");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("pregen"))) {
            List<String> worlds = new ArrayList<>();
            for (World w : Bukkit.getWorlds()) worlds.add(w.getName());
            StringUtil.copyPartialMatches(args[1], worlds, completions);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            List<String> hints = new ArrayList<>(Collections.singletonList("flat"));
            for (BiomeGroup bg : BiomeGroup.values()) hints.add(bg.getKey());
            StringUtil.copyPartialMatches(args[2], hints, completions);
        }
        return completions;
    }
}