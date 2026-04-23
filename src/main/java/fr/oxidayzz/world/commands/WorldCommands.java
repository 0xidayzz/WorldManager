package fr.oxidayzz.world.commands;

import fr.oxidayzz.world.WorldManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WorldCommands implements CommandExecutor {

    private final WorldManager plugin;

    public WorldCommands(WorldManager plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args) {
        
        // 1. Si le joueur tape juste /rw ou /rw help
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        // 2. Gestion des autres sous-commandes
        switch (args[0].toLowerCase()) {
            case "info":
                sender.sendMessage("§a§l[WorldManager] §7Version: §f" + plugin.getDescription().getVersion());
                sender.sendMessage("§a§l[WorldManager] §7Auteur: §fOxidayzz");
                break;

            default:
                sender.sendMessage("§cCommande inconnue. Tapez §6/rw help");
                break;
        }

        return true;
    }

    // Petite méthode pour afficher un joli menu
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m----------------§r §6§lWorldManager §8§m----------------");
        sender.sendMessage(" ");
        sender.sendMessage("§6/rw help §8» §7Affiche ce menu d'aide.");
        sender.sendMessage("§6/rw tp <monde> §8» §7Se téléporter ou créer un monde.");
        sender.sendMessage("§6/rw info §8» §7Informations sur le plugin.");
        sender.sendMessage(" ");
        sender.sendMessage("§8§m--------------------------------------------");
    }
}