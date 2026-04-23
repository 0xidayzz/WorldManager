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

    private final WorldManager plugin;
    private final List<String> subCommands = Arrays.asList("create", "delete", "tp", "confirm", "cancel", "list", "scan", "pregen", "gui");

    public WorldCommands(WorldManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (args.length == 0) return true;

        switch (args[0].toLowerCase()) {
            case "create":
                if (args.length >= 9) {
                    try {
                        plugin.getWorldService().askConfirmation(player, args[1], args[2].equalsIgnoreCase("flat"), 
                                args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), 
                                Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), 
                                (args.length == 10 && args[9].equalsIgnoreCase("nostruct")));
                    } catch (Exception e) { player.sendMessage("§cUsage: /rw create <nom> <biomes> <D%> <G%> <I%> <L%> <E%> <R%> [nostruct]"); }
                }
                break;
            case "gui":
              plugin.getWorldGui().openMainGui(player);
              break;
            case "pregen":
                if (args.length >= 3) {
                    plugin.getWorldService().pregenWorld(player, args[1], Integer.parseInt(args[2]));
                } else player.sendMessage("§c/rw pregen <monde> <rayon>");
                break;

            case "scan":
                plugin.getWorldService().scanOres(player, args.length >= 2 ? Integer.parseInt(args[1]) : 1);
                break;

            case "tp":
                if (args.length >= 2) plugin.getWorldService().teleportPlayer(player, args[1]);
                break;

            case "delete":
                if (args.length >= 2) plugin.getWorldService().askDeleteConfirmation(player, args[1]);
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