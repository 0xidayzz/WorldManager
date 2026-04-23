package fr.oxidayzz.world.generator;

import org.bukkit.block.Biome;
import java.util.Arrays;
import java.util.List;

public enum BiomeGroup {
    OCEAN("ocean", Arrays.asList(Biome.OCEAN, Biome.DEEP_OCEAN, Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN)),
    RIVIERE("riviere", Arrays.asList(Biome.RIVER, Biome.FROZEN_RIVER)),
    MONTAGNE("montagne", Arrays.asList(Biome.JAGGED_PEAKS, Biome.STONY_PEAKS, Biome.FROZEN_PEAKS, Biome.MEADOW, Biome.GROVE)),
    NOIR("noir", Arrays.asList(Biome.DARK_FOREST)),
    CHENE("chene", Arrays.asList(Biome.FOREST, Biome.FLOWER_FOREST)),
    BOULOT("boulot", Arrays.asList(Biome.BIRCH_FOREST, Biome.OLD_GROWTH_BIRCH_FOREST)),
    JUNGLE("jungle", Arrays.asList(Biome.JUNGLE, Biome.BAMBOO_JUNGLE, Biome.SPARSE_JUNGLE)),
    DESERT("desert", Arrays.asList(Biome.DESERT)),
    TAIGA("taiga", Arrays.asList(Biome.TAIGA, Biome.SNOWY_TAIGA)),
    GRANDE_TAIGA("grande_taiga", Arrays.asList(Biome.OLD_GROWTH_PINE_TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA)),
    OR("or", Arrays.asList(Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.WOODED_BADLANDS));

    private final String key;
    private final List<Biome> biomes;

    BiomeGroup(String key, List<Biome> biomes) {
        this.key = key;
        this.biomes = biomes;
    }

    public String getKey() { return key; }
    public List<Biome> getBiomes() { return biomes; }

    public static BiomeGroup fromKey(String key) {
        for (BiomeGroup bg : values()) {
            if (bg.key.equalsIgnoreCase(key)) return bg;
        }
        return null;
    }
}