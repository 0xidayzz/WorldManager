package fr.oxidayzz.world.util;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Shared world lookup, listing, and filesystem utilities.
 */
public final class WorldUtil {

    private WorldUtil() {}

    /**
     * Looks up a world by name, sending an error message if not found.
     * Replaces the duplicated pattern:
     *   World w = Bukkit.getWorld(name);
     *   if (w == null) { player.sendMessage("...Monde introuvable."); return; }
     */
    public static Optional<World> getWorldOrNotify(CommandSender sender, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            MessageUtil.sendWorldNotFound(sender);
            return Optional.empty();
        }
        return Optional.of(world);
    }

    /**
     * Returns the list of currently loaded world names.
     * Replaces duplicated Bukkit.getWorlds() -> getName() iteration.
     */
    public static List<String> getWorldNames() {
        List<String> names = new ArrayList<>();
        for (World w : Bukkit.getWorlds()) {
            names.add(w.getName());
        }
        return names;
    }

    /**
     * Recursively deletes a directory (used for world folder cleanup).
     * Extracted from WorldService to be reusable.
     */
    public static boolean deleteFolder(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteFolder(f);
                    } else {
                        f.delete();
                    }
                }
            }
            return path.delete();
        }
        return true;
    }
}
