package fr.oxidayzz.world;

import fr.oxidayzz.world.commands.WorldCommands;
import org.bukkit.Bukkit;
import org.bukkit.World;
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
            WorldCommands commands = new WorldCommands(this);
            getCommand("rw").setExecutor(commands);
            getCommand("rw").setTabCompleter(commands);
        } else {
            getLogger().severe("Commande 'rw' introuvable ! V\u00e9rifiez le plugin.yml.");
        }

        loadExistingWorlds();
        getLogger().info("WorldManager par Oxidayzz est active !");
    }

    private void loadExistingWorlds() {
        List<String> ignore = Arrays.asList("plugins", "logs", "cache", "world", "world_nether", "world_the_end");

        File container = Bukkit.getWorldContainer();
        File[] files = container.listFiles();

        if (files == null) {
            getLogger().warning("Impossible de lister les dossiers dans: " + container.getAbsolutePath());
            return;
        }

        for (File file : files) {
            if (file.isDirectory() && new File(file, "level.dat").exists() && !ignore.contains(file.getName())) {
                getLogger().info("Chargement automatique du monde : " + file.getName());
                try {
                    World world = Bukkit.createWorld(new WorldCreator(file.getName()));
                    if (world == null) {
                        getLogger().warning("\u00c9chec du chargement du monde : " + file.getName());
                    }
                } catch (Exception e) {
                    getLogger().severe("Erreur lors du chargement du monde '" + file.getName() + "': " + e.getMessage());
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