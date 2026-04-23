package fr.oxidayzz.world.generator;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class FlexibleBiomeProvider extends BiomeProvider {

    private final List<Biome> allowedBiomes;

    public FlexibleBiomeProvider(List<BiomeGroup> enabledGroups) {
        this.allowedBiomes = new ArrayList<>();
        for (BiomeGroup group : enabledGroups) {
            allowedBiomes.addAll(group.getBiomes());
        }
        if (allowedBiomes.isEmpty()) {
            allowedBiomes.add(Biome.PLAINS);
        }
    }

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        // On retourne le premier biome par défaut, 
        // le moteur de Minecraft choisira parmi la liste fournie par getBiomes()
        return allowedBiomes.get(0);
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return allowedBiomes;
    }
}