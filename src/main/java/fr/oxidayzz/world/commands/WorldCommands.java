package fr.oxidayzz.world.commands;

import fr.oxidayzz.world.WorldManager;
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
                    boolean isFlat = (args.length >= 3 && args[2].equalsIgnoreCase("flat"));
                    plugin.getWorldService().askConfirmation(player, args[1], isFlat);
                } else {
                    sender.sendMessage("§cUsage: /rw create <nom> [flat]");
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
            // Complétion pour /rw <sous-commande>
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
        } 
        else if (args.length == 2) {
            // Complétion pour les noms de mondes après /rw tp ou /rw delete
            if (args[0].equalsIgnoreCase("tp") || args[0].equalsIgnoreCase("delete")) {
                List<String> worldNames = new ArrayList<>();
                for (World w : Bukkit.getWorlds()) {
                    worldNames.add(w.getName());
                }
                StringUtil.copyPartialMatches(args[1], worldNames, completions);
            }
        } 
        else if (args.length == 3) {
            // Complétion pour [flat] après /rw create <nom>
            if (args[0].equalsIgnoreCase("create")) {
                StringUtil.copyPartialMatches(args[2], Collections.singletonList("flat"), completions);
            }
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
            String customTag = (!name.equalsIgnoreCase("world") && !name.equalsIgnoreCase("world_nether") && !name.equalsIgnoreCase("world_the_end")) ? " §8[§b§lCustom§8]" : "";
            sender.sendMessage(" §8• §f" + name + " §8[§7" + envTag + "§8]" + customTag);
        }
        sender.sendMessage("§8§m------------------------------------");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------§r §6§lWorldManager §8§m----------------");
        sender.sendMessage(" ");
        sender.sendMessage("§6/rw help §8» §7Affiche ce menu d'aide.");
        sender.sendMessage("§6/rw create <nom> [flat] §8» §7Créer/Remplacer un monde.");
        sender.sendMessage("§6/rw delete <nom> §8» §cSupprimer définitivement.");
        sender.sendMessage("§6/rw tp <nom> §8» §7Se téléporter dans un monde.");
        sender.sendMessage("§6/rw confirm §8» §aConfirmer l'action en attente.");
        sender.sendMessage("§6/rw cancel §8» §cAnnuler l'action en attente.");
        sender.sendMessage("§6/rw list §8» §7Afficher les mondes actifs.");
        sender.sendMessage(" ");
        sender.sendMessage("§8§m--------------------------------------------");
    }
}