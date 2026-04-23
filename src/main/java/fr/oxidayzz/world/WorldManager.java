package fr.oxidayzz.world;

import fr.oxidayzz.world.commands.WorldCommands;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldManager extends JavaPlugin {

    private WorldService worldService;

    @Override
    public void onEnable() {
        // Initialisation du service avec l'instance du plugin
        this.worldService = new WorldService(this);

        // Enregistrement de la commande principale
        if (getCommand("rw") != null) {
            getCommand("rw").setExecutor(new WorldCommands(this));
        }

        getLogger().info("WorldManager par Oxidayzz est active !");
    }

    // Le getter indispensable pour WorldCommands
    public WorldService getWorldService() {
        return worldService;
    }

    @Override
    public void onDisable() {
        getLogger().info("WorldManager s'arrete...");
    }
}