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
    private final List<String> subCommands = Arrays.asList("create", "delete", "tp", "confirm", "cancel", "list");

    public WorldCommands(WorldManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) return true;

        switch (args[0].toLowerCase()) {
            case "create":
                // Usage: /rw create <nom> <biomes|flat> <d%> <g%> <i%> <l%> <e%> <r%> [nostruct]
                if (args.length >= 9) {
                    try {
                        String name = args[1];
                        String biomes = args[2];
                        int d = Integer.parseInt(args[3]);
                        int g = Integer.parseInt(args[4]);
                        int i = Integer.parseInt(args[5]);
                        int l = Integer.parseInt(args[6]);
                        int e = Integer.parseInt(args[7]);
                        int r = Integer.parseInt(args[8]);
                        
                        boolean noStruct = false;
                        if (args.length == 10 && args[9].equalsIgnoreCase("nostruct")) {
                            noStruct = true;
                        }

                        plugin.getWorldService().askConfirmation(player, name, biomes.equalsIgnoreCase("flat"), 
                                                               biomes, d, g, i, l, e, r, noStruct);
                    } catch (NumberFormatException ex) {
                        player.sendMessage("§cErreur: Les taux de minerais doivent être des nombres.");
                    }
                } else {
                    player.sendMessage("§cUsage: /rw create <nom> <biomes> <D%> <G%> <I%> <L%> <E%> <R%> [nostruct]");
                }
                break;

            case "tp":
                if (args.length >= 2) plugin.getWorldService().teleportPlayer(player, args[1]);
                break;

            case "delete":
                if (args.length >= 2) plugin.getWorldService().askDeleteConfirmation(player, args[1]);
                break;

            case "confirm":
                plugin.getWorldService().confirm(player);
                break;

            case "cancel":
                plugin.getWorldService().cancel(player);
                break;

            case "list":
                player.sendMessage("§6§lMONDES:");
                for (World w : Bukkit.getWorlds()) player.sendMessage(" §8• §f" + w.getName());
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
            List<String> hints = new ArrayList<>(Collections.singletonList("flat"));
            for (BiomeGroup bg : BiomeGroup.values()) hints.add(bg.getKey());
            StringUtil.copyPartialMatches(args[2], hints, completions);
        } else if (args.length >= 4 && args.length <= 9 && args[0].equalsIgnoreCase("create")) {
            completions.add("100");
            completions.add("200");
        } else if (args.length == 10 && args[0].equalsIgnoreCase("create")) {
            completions.add("nostruct");
        }
        return completions;
    }
}