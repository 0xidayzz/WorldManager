package fr.oxidayzz.world.generator;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class FlexibleBiomeProvider extends BiomeProvider {

    private final List<Biome> allowed;
    private final Biome fallback;

    public FlexibleBiomeProvider(List<BiomeGroup> groups) {
        this.allowed = new ArrayList<>();
        for (BiomeGroup g : groups) {
            allowed.addAll(g.getBiomes());
        }
        
        if (allowed.isEmpty()) {
            allowed.add(Biome.PLAINS);
        }
        
        // On définit le biome par défaut (le premier de ta liste)
        this.fallback = allowed.get(0);
    }

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        // C'est ici que la magie opère : 
        // Si Minecraft essaie de mettre un biome qui n'est pas dans notre liste 
        // (comme un océan car le terrain est bas), on le force avec notre fallback (ex: plaine)
        // Cela transforme les fonds marins en plaines terrestres.
        return fallback; 
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return allowed;
    }
}