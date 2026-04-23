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
    private final List<String> subCommands = Arrays.asList("help", "create", "delete", "tp", "confirm", "cancel", "list");

    public WorldCommands(WorldManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                if (sender instanceof Player player && args.length >= 2) {
                    String biomeList = (args.length >= 3) ? args[2] : "";
                    boolean isFlat = biomeList.equalsIgnoreCase("flat");
                    
                    // Analyse des options supplémentaires
                    boolean boost = false;
                    boolean noStruct = false;
                    
                    for (int i = 3; i < args.length; i++) {
                        if (args[i].equalsIgnoreCase("boost")) boost = true;
                        if (args[i].equalsIgnoreCase("nostruct")) noStruct = true;
                    }

                    plugin.getWorldService().askConfirmation(player, args[1], isFlat, isFlat ? "" : biomeList, boost, noStruct);
                } else {
                    sender.sendMessage("§cUsage: /rw create <nom> [biomes|flat] [boost] [nostruct]");
                }
                break;

            case "delete":
                if (sender instanceof Player player && args.length >= 2) plugin.getWorldService().askDeleteConfirmation(player, args[1]);
                break;

            case "tp":
                if (sender instanceof Player player && args.length >= 2) plugin.getWorldService().teleportPlayer(player, args[1]);
                break;

            case "confirm":
                if (sender instanceof Player player) plugin.getWorldService().confirm(player);
                break;

            case "cancel":
                if (sender instanceof Player player) plugin.getWorldService().cancel(player);
                break;

            case "list":
                sendList(sender);
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            List<String> hints = new ArrayList<>();
            hints.add("flat");
            for (BiomeGroup bg : BiomeGroup.values()) hints.add(bg.getKey());
            StringUtil.copyPartialMatches(args[2], hints, completions);
        } else if (args.length >= 4 && args[0].equalsIgnoreCase("create")) {
            List<String> options = Arrays.asList("boost", "nostruct");
            StringUtil.copyPartialMatches(args[args.length - 1], options, completions);
        }
        Collections.sort(completions);
        return completions;
    }

    // --- Les méthodes sendHelp et sendList restent les mêmes que précédemment ---
    private void sendList(CommandSender sender) {
        sender.sendMessage("§6§lMONDES CHARGÉS:");
        for (World w : Bukkit.getWorlds()) sender.sendMessage(" §8• §f" + w.getName());
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6/rw create <nom> [biomes] [boost] [nostruct]");
        sender.sendMessage("§7Exemple: /rw create MaMap or,plaine boost nostruct");
    }
}