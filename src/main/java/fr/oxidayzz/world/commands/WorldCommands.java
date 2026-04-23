package fr.oxidayzz.world.commands;

import fr.oxidayzz.world.WorldManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
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
            case "list":
                sender.sendMessage("§8§m----------------§r §6§lMondes Actifs §8§m----------------");
                // On boucle sur tous les mondes chargés par le serveur
                for (World w : Bukkit.getWorlds()) {
                    String environment = getEnvName(w.getEnvironment());
                    sender.sendMessage("§8- §f" + w.getName() + " §7(" + environment + "§7)");
                }
                sender.sendMessage("§8§m--------------------------------------------");
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
        sender.sendMessage("§6/rw list §8» §7Liste les mondes chargés.");
        sender.sendMessage("§6/rw info §8» §7Informations sur le plugin.");
        sender.sendMessage(" ");
        sender.sendMessage("§8§m--------------------------------------------");
    }
    private String getEnvName(World.Environment env) {
        return switch (env) {
            case NORMAL -> "§aOverworld";
            case NETHER -> "§cNether";
            case THE_END -> "§dEnd";
            case CUSTOM -> "§bCustom";
        };
    }
}