package fr.oxidayzz.world.generator;

import org.bukkit.Material;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import java.util.Random;

public class OrePopulator extends BlockPopulator {

    private final double dMult, gMult, iMult, lMult, eMult, rMult;

    public OrePopulator(int dPct, int gPct, int iPct, int lPct, int ePct, int rPct) {
        this.dMult = dPct / 100.0;
        this.gMult = gPct / 100.0;
        this.iMult = iPct / 100.0;
        this.lMult = lPct / 100.0;
        this.eMult = ePct / 100.0;
        this.rMult = rPct / 100.0;
    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion region) {
        // Quantité de base * multiplicateur
        generateOre(region, random, Material.DIAMOND_ORE, (int)(4 * dMult), -64, 16);
        generateOre(region, random, Material.GOLD_ORE, (int)(6 * gMult), -64, 32);
        generateOre(region, random, Material.IRON_ORE, (int)(10 * iMult), -16, 80);
        generateOre(region, random, Material.LAPIS_ORE, (int)(3 * lMult), -64, 32);
        generateOre(region, random, Material.EMERALD_ORE, (int)(2 * eMult), 32, 120);
        generateOre(region, random, Material.REDSTONE_ORE, (int)(8 * rMult), -64, 16);
    }

    private void generateOre(LimitedRegion region, Random random, Material mat, int amount, int minY, int maxY) {
        for (int i = 0; i < amount; i++) {
            int x = random.nextInt(16);
            int z = random.nextInt(16);
            int y = random.nextInt(maxY - minY) + minY;
            Material current = region.getType(x, y, z);
            if (current == Material.STONE || current == Material.DEEPSLATE || current == Material.TUFF) {
                region.setType(x, y, z, mat);
            }
        }
    }
}