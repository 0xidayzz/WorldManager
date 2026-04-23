package fr.oxidayzz.world;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Classe principale du plugin WorldManager.
 * Gère le cycle de vie du plugin (activation/désactivation).
 */
public class WorldManager extends JavaPlugin {

    @Override
    public void onEnable() {
        // Message envoyé dans la console lors du démarrage
        getLogger().info("========================================");
        getLogger().info("WorldManager est active !");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("========================================");

        // C'est ici que nous enregistrerons les commandes et les évènements plus tard
    }

    @Override
    public void onDisable() {
        // Message envoyé dans la console lors de l'arrêt
        getLogger().info("WorldManager s'arrete, fermeture des mondes...");
    }
}