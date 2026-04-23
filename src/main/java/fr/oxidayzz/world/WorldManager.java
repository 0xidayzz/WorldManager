package fr.oxidayzz.world;

import fr.oxidayzz.world.commands.WorldCommands;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class WorldManager extends JavaPlugin {

    private WorldService worldService;

    @Override
    public void onEnable() {
        this.worldService = new WorldService(this);

        if (getCommand("rw") != null) {
            getCommand("rw").setExecutor(new WorldCommands(this));
        }

        // Chargement des mondes existants au démarrage
        loadExistingWorlds();

        getLogger().info("WorldManager par Oxidayzz est active !");
    }

    private void loadExistingWorlds() {
        // Liste des dossiers à ne pas charger comme des mondes
        List<String> ignore = Arrays.asList("plugins", "logs", "cache", "world", "world_nether", "world_the_end");
        
        File container = Bukkit.getWorldContainer();
        File[] files = container.listFiles();

        if (files != null) {
            for (File file : files) {
                // Si c'est un dossier qui contient un level.dat et qui n'est pas ignoré
                if (file.isDirectory() && new File(file, "level.dat").exists() && !ignore.contains(file.getName())) {
                    getLogger().info("Chargement automatique du monde : " + file.getName());
                    Bukkit.createWorld(new WorldCreator(file.getName()));
                }
            }
        }
    }

    public WorldService getWorldService() {
        return worldService;
    }

    @Override
    public void onDisable() {
        getLogger().info("WorldManager s'arrete...");
    }
}