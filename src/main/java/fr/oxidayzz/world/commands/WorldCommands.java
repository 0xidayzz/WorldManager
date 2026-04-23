package fr.oxidayzz.world.commands;

import fr.oxidayzz.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WorldCommands implements CommandExecutor {

    private final WorldManager plugin;

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

            case "confirm":
                if (sender instanceof Player player) plugin.getWorldService().confirm(player);
                break;

            case "cancel":
                if (sender instanceof Player player) plugin.getWorldService().cancel(player);
                break;

            case "list":
                sender.sendMessage("§6§lMondes Actifs:");
                for (World w : Bukkit.getWorlds()) {
                    sender.sendMessage(" §8- §f" + w.getName());
                }
                break;

            default:
                sender.sendMessage("§cCommande inconnue.");
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------§r §6§lWorldManager §8§m----------------");
        sender.sendMessage(" ");
        sender.sendMessage("§6/rw help §8» §7Affiche ce menu d'aide.");
        sender.sendMessage("§6/rw create <nom> [flat] §8» §7Créer ou remplacer un monde.");
        sender.sendMessage("§6/rw delete <nom> §8» §cSupprimer définitivement un monde.");
        sender.sendMessage("§6/rw confirm §8» §aConfirmer l'écrasement d'un monde.");
        sender.sendMessage("§6/rw cancel §8» §cAnnuler la création en cours.");
        sender.sendMessage("§6/rw list §8» §7Afficher la liste des mondes chargés.");
        sender.sendMessage(" ");
        sender.sendMessage("§8§m--------------------------------------------");
    }
}