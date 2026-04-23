package fr.oxidayzz.world.generator;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class OrePopulator extends BlockPopulator {

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        // Exemple pour le Diamant : on tente de poser 5 filons supplémentaires par chunk
        generateOre(limitedRegion, random, Material.DIAMOND_ORE, 5, -64, 16);
        
        // Exemple pour l'Or : 8 filons supplémentaires
        generateOre(limitedRegion, random, Material.GOLD_ORE, 8, -64, 32);
        
        // Exemple pour le Fer : 12 filons
        generateOre(limitedRegion, random, Material.IRON_ORE, 12, -64, 80);
    }

    private void generateOre(LimitedRegion region, Random random, Material material, int amount, int minY, int maxY) {
        for (int i = 0; i < amount; i++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int y = random.nextInt(maxY - minY) + minY;

            // On ne pose le minerai que si c'est de la pierre (pour ne pas en mettre dans les airs)
            if (region.getType(x, y, z) == Material.STONE || region.getType(x, y, z) == Material.DEEPSLATE) {
                region.setType(x, y, z, material);
            }
        }
    }
}