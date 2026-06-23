package fr.oxidayzz.world.generator;

import org.bukkit.block.Biome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BiomeGroupTest {

    @ParameterizedTest
    @EnumSource(BiomeGroup.class)
    void everyGroupHasNonEmptyKey(BiomeGroup group) {
        assertNotNull(group.getKey());
        assertFalse(group.getKey().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(BiomeGroup.class)
    void everyGroupHasAtLeastOneBiome(BiomeGroup group) {
        assertNotNull(group.getBiomes());
        assertFalse(group.getBiomes().isEmpty());
    }

    @ParameterizedTest
    @EnumSource(BiomeGroup.class)
    void fromKeyReturnsGroupForExactKey(BiomeGroup group) {
        assertEquals(group, BiomeGroup.fromKey(group.getKey()));
    }

    @ParameterizedTest
    @EnumSource(BiomeGroup.class)
    void fromKeyIsCaseInsensitive(BiomeGroup group) {
        assertEquals(group, BiomeGroup.fromKey(group.getKey().toUpperCase()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"unknown", "nope", "", "OCEAN2"})
    void fromKeyReturnsNullForUnknownKeys(String key) {
        assertNull(BiomeGroup.fromKey(key));
    }

    @Test
    void oceanGroupContainsExpectedBiomes() {
        BiomeGroup ocean = BiomeGroup.OCEAN;
        assertEquals("ocean", ocean.getKey());
        List<Biome> biomes = ocean.getBiomes();
        assertTrue(biomes.contains(Biome.OCEAN));
        assertTrue(biomes.contains(Biome.DEEP_OCEAN));
        assertTrue(biomes.contains(Biome.WARM_OCEAN));
        assertEquals(3, biomes.size());
    }

    @Test
    void desertGroupContainsExpectedBiome() {
        BiomeGroup desert = BiomeGroup.DESERT;
        assertEquals("desert", desert.getKey());
        assertEquals(List.of(Biome.DESERT), desert.getBiomes());
    }

    @Test
    void allEnumValuesHaveUniqueKeys() {
        BiomeGroup[] values = BiomeGroup.values();
        long uniqueKeys = java.util.Arrays.stream(values)
                .map(BiomeGroup::getKey)
                .distinct()
                .count();
        assertEquals(values.length, uniqueKeys);
    }

    @Test
    void plaineGroupContainsExpectedBiomes() {
        BiomeGroup plaine = BiomeGroup.PLAINE;
        assertEquals("plaine", plaine.getKey());
        assertTrue(plaine.getBiomes().contains(Biome.PLAINS));
        assertTrue(plaine.getBiomes().contains(Biome.SUNFLOWER_PLAINS));
    }

    @Test
    void grotteGroupContainsCaveBiomes() {
        BiomeGroup grotte = BiomeGroup.GROTTE;
        assertEquals("grotte", grotte.getKey());
        assertTrue(grotte.getBiomes().contains(Biome.LUSH_CAVES));
        assertTrue(grotte.getBiomes().contains(Biome.DRIPSTONE_CAVES));
    }
}
