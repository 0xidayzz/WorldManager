package fr.oxidayzz.world.generator;

import org.bukkit.block.Biome;
import org.bukkit.generator.WorldInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FlexibleBiomeProviderTest {

    @Mock WorldInfo worldInfo;

    @Test
    void singleGroupReturnsThatGroupsBiomes() {
        FlexibleBiomeProvider provider = new FlexibleBiomeProvider(List.of(BiomeGroup.OCEAN));
        List<Biome> biomes = provider.getBiomes(worldInfo);

        assertTrue(biomes.contains(Biome.OCEAN));
        assertTrue(biomes.contains(Biome.DEEP_OCEAN));
        assertTrue(biomes.contains(Biome.WARM_OCEAN));
        assertEquals(3, biomes.size());
    }

    @Test
    void multipleGroupsCombineBiomes() {
        FlexibleBiomeProvider provider = new FlexibleBiomeProvider(
                Arrays.asList(BiomeGroup.OCEAN, BiomeGroup.DESERT));
        List<Biome> biomes = provider.getBiomes(worldInfo);

        assertTrue(biomes.contains(Biome.OCEAN));
        assertTrue(biomes.contains(Biome.DESERT));
        assertEquals(4, biomes.size()); // 3 ocean + 1 desert
    }

    @Test
    void emptyGroupListFallsBackToPlains() {
        FlexibleBiomeProvider provider = new FlexibleBiomeProvider(Collections.emptyList());
        List<Biome> biomes = provider.getBiomes(worldInfo);

        assertEquals(1, biomes.size());
        assertEquals(Biome.PLAINS, biomes.get(0));
    }

    @Test
    void getBiomeReturnsFirstAllowedBiome() {
        FlexibleBiomeProvider provider = new FlexibleBiomeProvider(List.of(BiomeGroup.OCEAN));
        Biome biome = provider.getBiome(worldInfo, 0, 64, 0);

        assertEquals(Biome.OCEAN, biome);
    }

    @Test
    void getBiomeWithEmptyGroupsReturnPlains() {
        FlexibleBiomeProvider provider = new FlexibleBiomeProvider(Collections.emptyList());
        Biome biome = provider.getBiome(worldInfo, 0, 64, 0);

        assertEquals(Biome.PLAINS, biome);
    }

    @Test
    void getBiomeReturnsSameValueRegardlessOfCoordinates() {
        FlexibleBiomeProvider provider = new FlexibleBiomeProvider(List.of(BiomeGroup.DESERT));
        Biome b1 = provider.getBiome(worldInfo, 0, 0, 0);
        Biome b2 = provider.getBiome(worldInfo, 1000, 128, -5000);

        assertEquals(b1, b2);
    }

    @Test
    void allBiomeGroupsCombinedHaveCorrectCount() {
        FlexibleBiomeProvider provider = new FlexibleBiomeProvider(
                Arrays.asList(BiomeGroup.values()));
        List<Biome> biomes = provider.getBiomes(worldInfo);

        int expectedTotal = 0;
        for (BiomeGroup bg : BiomeGroup.values()) {
            expectedTotal += bg.getBiomes().size();
        }
        assertEquals(expectedTotal, biomes.size());
    }

    @Test
    void getBiomesReturnsModifiableOrStableList() {
        FlexibleBiomeProvider provider = new FlexibleBiomeProvider(List.of(BiomeGroup.DESERT));
        List<Biome> biomes1 = provider.getBiomes(worldInfo);
        List<Biome> biomes2 = provider.getBiomes(worldInfo);

        assertEquals(biomes1, biomes2);
    }
}
