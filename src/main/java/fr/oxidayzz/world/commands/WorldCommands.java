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
                    // On envoie la liste des biomes au service (vide si c'est un monde plat)
                    plugin.getWorldService().askConfirmation(player, args[1], isFlat, isFlat ? "" : biomeList);
                } else {
                    sender.sendMessage("§cUsage: /rw create <nom> [flat|groupe1,groupe2...]");
                }
                break;

            case "delete":
                if (sender instanceof Player player && args.length >= 2) {
                    plugin.getWorldService().askDeleteConfirmation(player, args[1]);
                } else {
                    sender.sendMessage("§cUsage: /rw delete <nom>");
                }
                break;

            case "tp":
                if (sender instanceof Player player) {
                    if (args.length >= 2) {
                        plugin.getWorldService().teleportPlayer(player, args[1]);
                    } else {
                        player.sendMessage("§cUsage: /rw tp <nom>");
                    }
                } else {
                    sender.sendMessage("§cSeul un joueur peut se téléporter.");
                }
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

            default:
                sender.sendMessage("§cCommande inconnue.");
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
        } 
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("delete")) {
                List<String> worldNames = new ArrayList<>();
                for (World w : Bukkit.getWorlds()) {
                    worldNames.add(w.getName());
                }
                StringUtil.copyPartialMatches(args[1], worldNames, completions);
            }
        } 
        else if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            List<String> hints = new ArrayList<>();
            hints.add("flat");
            for (BiomeGroup bg : BiomeGroup.values()) {
                hints.add(bg.getKey());
            }
            // Note: Pour les listes (ex: or,jungle), le TabCompleter de base est limité, 
            // mais ici il proposera les groupes individuellement.
            StringUtil.copyPartialMatches(args[2], hints, completions);
        }

        Collections.sort(completions);
        return completions;
    }

    private void sendList(CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage("§8§m----------§r §6§lMONDES CHARGÉS §8§m----------");
        
        for (World w : Bukkit.getWorlds()) {
            String name = w.getName();
            String envTag;
            
            switch (w.getEnvironment()) {
                case NORMAL -> envTag = "§aOverworld";
                case NETHER -> envTag = "§cNether";
                case THE_END -> envTag = "§dEnd";
                default -> envTag = "§7Inconnu";
            }

            String customTag = "";
            if (!name.equalsIgnoreCase("world") && 
                !name.equalsIgnoreCase("world_nether") && 
                !name.equalsIgnoreCase("world_the_end")) {
                customTag = " §8[§b§lCustom§8]";
            }

            sender.sendMessage(" §8• §f" + name + " §8[§7" + envTag + "§8]" + customTag);
        }
        
        sender.sendMessage("§8§m------------------------------------");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------§r §6§lWorldManager §8§m----------------");
        sender.sendMessage(" ");
        sender.sendMessage("§6/rw help §8» §7Affiche ce menu d'aide.");
        sender.sendMessage("§6/rw create <nom> [flat|biomes] §8» §7Créer avec biomes spécifiques.");
        sender.sendMessage("§6/rw delete <nom> §8» §cSupprimer définitivement.");
        sender.sendMessage("§6/rw tp <nom> §8» §7Se téléporter au monde.");
        sender.sendMessage("§6/rw confirm §8» §aConfirmer l'action.");
        sender.sendMessage("§6/rw list §8» §7Afficher les mondes actifs.");
        sender.sendMessage(" ");
        sender.sendMessage("§7Exemple biomes: §f/rw create MonMonde or,jungle,riviere");
        sender.sendMessage("§8§m--------------------------------------------");
    }
}