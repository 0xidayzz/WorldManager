package fr.oxidayzz.world.commands;

import fr.oxidayzz.world.WorldManager;
import fr.oxidayzz.world.generator.BiomeGroup;
import fr.oxidayzz.world.util.MessageUtil;
import fr.oxidayzz.world.util.WorldUtil;
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
                if (args.length >= 9) {
                    try {
                        plugin.getWorldService().askConfirmation(player, args[1], args[2].equalsIgnoreCase("flat"), 
                                args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), 
                                Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), 
                                (args.length == 10 && args[9].equalsIgnoreCase("nostruct")));
                    } catch (Exception e) {
                        MessageUtil.sendError(player, "Usage: /rw create <nom> <biomes> <D%> <G%> <I%> <L%> <E%> <R%> [nostruct]");
                    }
                }
                break;

            case "pregen":
                if (args.length >= 3) {
                    plugin.getWorldService().pregenWorld(player, args[1], Integer.parseInt(args[2]));
                } else {
                    MessageUtil.sendError(player, "/rw pregen <monde> <rayon>");
                }
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
                MessageUtil.sendInfo(player, "Mondes:");
                for (String name : WorldUtil.getWorldNames()) {
                    player.sendMessage("\u00A78\u2022 \u00A7f" + name);
                }
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
            StringUtil.copyPartialMatches(args[1], WorldUtil.getWorldNames(), completions);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            List<String> hints = new ArrayList<>(Collections.singletonList("flat"));
            for (BiomeGroup bg : BiomeGroup.values()) hints.add(bg.getKey());
            StringUtil.copyPartialMatches(args[2], hints, completions);
        }
        return completions;
    }
}
