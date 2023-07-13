package me.pepperbell.continuity.client.mixinterface;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public interface ChunkRendererRegionExtension {
	RegistryEntry<Biome> continuity$getBiome(BlockPos pos);
}
