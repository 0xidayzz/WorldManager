package fr.oxidayzz.world.generator;

import org.bukkit.Material;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrePopulatorTest {

    @Mock WorldInfo worldInfo;
    @Mock LimitedRegion region;

    @Test
    void constructorAcceptsZeroPercentages() {
        assertDoesNotThrow(() -> new OrePopulator(0, 0, 0, 0, 0, 0));
    }

    @Test
    void constructorAcceptsHundredPercentages() {
        assertDoesNotThrow(() -> new OrePopulator(100, 100, 100, 100, 100, 100));
    }

    @Test
    void constructorAcceptsLargeBoostValues() {
        assertDoesNotThrow(() -> new OrePopulator(500, 300, 200, 400, 150, 250));
    }

    @Test
    void populateWithZeroMultipliersDoesNotPlaceOres() {
        OrePopulator populator = new OrePopulator(0, 0, 0, 0, 0, 0);
        Random random = new Random(42);

        populator.populate(worldInfo, random, 0, 0, region);

        verify(region, never()).setType(anyInt(), anyInt(), anyInt(), any(Material.class));
    }

    @Test
    void populateWithHighMultipliersPlacesOres() {
        OrePopulator populator = new OrePopulator(200, 200, 200, 200, 200, 200);
        Random random = new Random(42);

        when(region.getType(anyInt(), anyInt(), anyInt())).thenReturn(Material.STONE);

        populator.populate(worldInfo, random, 0, 0, region);

        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), any(Material.class));
    }

    @Test
    void populateReplacesStone() {
        OrePopulator populator = new OrePopulator(100, 0, 0, 0, 0, 0);
        Random random = new Random(42);

        when(region.getType(anyInt(), anyInt(), anyInt())).thenReturn(Material.STONE);

        populator.populate(worldInfo, random, 0, 0, region);

        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.DIAMOND_ORE));
    }

    @Test
    void populateReplacesDeepslate() {
        OrePopulator populator = new OrePopulator(100, 0, 0, 0, 0, 0);
        Random random = new Random(1);

        when(region.getType(anyInt(), anyInt(), anyInt())).thenReturn(Material.DEEPSLATE);

        populator.populate(worldInfo, random, 5, 5, region);

        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.DIAMOND_ORE));
    }

    @Test
    void populateReplacesTuff() {
        OrePopulator populator = new OrePopulator(100, 0, 0, 0, 0, 0);
        Random random = new Random(1);

        when(region.getType(anyInt(), anyInt(), anyInt())).thenReturn(Material.TUFF);

        populator.populate(worldInfo, random, 3, 3, region);

        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.DIAMOND_ORE));
    }

    @Test
    void populateDoesNotReplaceDirt() {
        OrePopulator populator = new OrePopulator(100, 100, 100, 100, 100, 100);
        Random random = new Random(42);

        when(region.getType(anyInt(), anyInt(), anyInt())).thenReturn(Material.DIRT);

        populator.populate(worldInfo, random, 0, 0, region);

        verify(region, never()).setType(anyInt(), anyInt(), anyInt(), any(Material.class));
    }

    @Test
    void populateHandlesIndexOutOfBoundsGracefully() {
        OrePopulator populator = new OrePopulator(100, 100, 100, 100, 100, 100);
        Random random = new Random(42);

        when(region.getType(anyInt(), anyInt(), anyInt())).thenThrow(new IndexOutOfBoundsException("test"));

        assertDoesNotThrow(() -> populator.populate(worldInfo, random, 0, 0, region));
    }

    @Test
    void populateHandlesIllegalArgumentGracefully() {
        OrePopulator populator = new OrePopulator(100, 100, 100, 100, 100, 100);
        Random random = new Random(42);

        when(region.getType(anyInt(), anyInt(), anyInt())).thenThrow(new IllegalArgumentException("test"));

        assertDoesNotThrow(() -> populator.populate(worldInfo, random, 0, 0, region));
    }

    @Test
    void populatePlacesAllOreTypesWithHighBoost() {
        OrePopulator populator = new OrePopulator(200, 200, 200, 200, 200, 200);
        Random random = new Random(42);

        when(region.getType(anyInt(), anyInt(), anyInt())).thenReturn(Material.STONE);

        populator.populate(worldInfo, random, 0, 0, region);

        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.DIAMOND_ORE));
        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.GOLD_ORE));
        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.IRON_ORE));
        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.LAPIS_ORE));
        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.EMERALD_ORE));
        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.REDSTONE_ORE));
    }

    @Test
    void populateWithOnlyDiamondBoostPlacesOnlyDiamonds() {
        OrePopulator populator = new OrePopulator(100, 0, 0, 0, 0, 0);
        Random random = new Random(42);

        when(region.getType(anyInt(), anyInt(), anyInt())).thenReturn(Material.STONE);

        populator.populate(worldInfo, random, 0, 0, region);

        verify(region, atLeastOnce()).setType(anyInt(), anyInt(), anyInt(), eq(Material.DIAMOND_ORE));
        verify(region, never()).setType(anyInt(), anyInt(), anyInt(), eq(Material.GOLD_ORE));
        verify(region, never()).setType(anyInt(), anyInt(), anyInt(), eq(Material.IRON_ORE));
    }
}
