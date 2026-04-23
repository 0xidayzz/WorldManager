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
        // Sécurité : Plaines par défaut si la liste est vide
        if (allowedBiomes.isEmpty()) {
            allowedBiomes.add(Biome.PLAINS);
        }
    }

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        // En retournant systématiquement le premier biome autorisé ici, 
        // on force le générateur à ignorer les suggestions d'océans par défaut.
        return allowedBiomes.get(0);
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        // Liste complète des biomes dans lesquels Minecraft peut piocher
        return allowedBiomes;
    }
}