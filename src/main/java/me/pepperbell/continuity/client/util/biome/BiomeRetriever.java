package me.pepperbell.continuity.client.util.biome;

import org.jetbrains.annotations.Nullable;

import grondag.canvas.terrain.region.input.InputRegion;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;

public final class BiomeRetriever {
	private static final Provider PROVIDER = createProvider();

	private static Provider createProvider() {
		ClassLoader classLoader = BiomeRetriever.class.getClassLoader();

		if (FabricLoader.getInstance().isModLoaded("canvas")) {
			try {
				Class<?> inputRegionClass = Class.forName("grondag.canvas.terrain.region.input.InputRegion", false, classLoader);
				inputRegionClass.getMethod("getBiome", BlockPos.class);
				return BiomeRetriever::getBiomeByInputRegion;
			} catch (ClassNotFoundException | NoSuchMethodException e) {
				//
			}
		}

		return BiomeRetriever::getBiomeByAPI;
	}

	@Nullable
	public static Biome getBiome(BlockRenderView blockView, BlockPos pos) {
		return PROVIDER.getBiome(blockView, pos);
	}

	public static void init() {
	}

	@Nullable
	private static Biome getBiomeByAPI(BlockRenderView blockView, BlockPos pos) {
		if (blockView.hasBiomes()) {
			return blockView.getBiomeFabric(pos).value();
		}
		return null;
	}

	// Canvas
	@Nullable
	private static Biome getBiomeByInputRegion(BlockRenderView blockView, BlockPos pos) {
		if (blockView instanceof InputRegion inputRegion) {
			return inputRegion.getBiome(pos);
		}
		return getBiomeByAPI(blockView, pos);
	}

	private interface Provider {
		@Nullable
		Biome getBiome(BlockRenderView blockView, BlockPos pos);
	}
}
