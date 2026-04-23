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
    private WorldGUI worldGui; // Instance pour gérer les menus

    @Override
    public void onEnable() {
        // 1. Initialisation des services
        this.worldService = new WorldService(this);
        this.worldGui = new WorldGUI(this);

        // 2. Configuration des commandes
        if (getCommand("rw") != null) {
            WorldCommands commands = new WorldCommands(this);
            getCommand("rw").setExecutor(commands);
            getCommand("rw").setTabCompleter(commands);
        }

        // 3. Enregistrement du Listener pour le GUI
        // Sans cette ligne, cliquer dans l'inventaire ne fera rien
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);

        // 4. Chargement des mondes existants au démarrage
        loadExistingWorlds();

        getLogger().info("§aWorldManager par Oxidayzz est active !");
    }

    private void loadExistingWorlds() {
        // Liste des dossiers à ne pas charger comme des mondes (système Minecraft)
        List<String> ignore = Arrays.asList("plugins", "logs", "cache", "world", "world_nether", "world_the_end");
        
        File container = Bukkit.getWorldContainer();
        File[] files = container.listFiles();

        if (files != null) {
            for (File file : files) {
                // Si c'est un dossier qui contient un level.dat et qui n'est pas dans la liste ignore
                if (file.isDirectory() && new File(file, "level.dat").exists() && !ignore.contains(file.getName())) {
                    getLogger().info("Chargement automatique du monde : " + file.getName());
                    Bukkit.createWorld(new WorldCreator(file.getName()));
                }
            }
        }
    }

    // Getters pour accéder aux services depuis les commandes et le GUI
    public WorldService getWorldService() {
        return worldService;
    }

    public WorldGUI getWorldGui() {
        return worldGui;
    }

    @Override
    public void onDisable() {
        getLogger().info("WorldManager s'arrete...");
    }
}