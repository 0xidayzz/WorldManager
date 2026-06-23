package fr.oxidayzz.world.util;

import java.util.function.BiConsumer;

/**
 * Shared chunk radius iteration utility.
 * Replaces the duplicated pattern:
 *   for (int x = -radius; x <= radius; x++)
 *       for (int z = -radius; z <= radius; z++) { ... }
 */
public final class ChunkIterator {

    private ChunkIterator() {}

    /**
     * Computes the total number of chunks in a square radius.
     */
    public static int totalChunks(int radius) {
        int side = radius * 2 + 1;
        return side * side;
    }

    /**
     * Iterates over all (x, z) chunk offsets within a given radius.
     */
    public static void forEachInRadius(int radius, BiConsumer<Integer, Integer> action) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                action.accept(x, z);
            }
        }
    }
}
