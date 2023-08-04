package me.pepperbell.continuity.client.resource;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableMap;

import me.pepperbell.continuity.client.model.CTMBakedModel;
import me.pepperbell.continuity.client.model.EmissiveBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class ModelWrappingHandler {
	private static final ImmutableMap<ModelIdentifier, BlockState> BLOCK_STATE_MODEL_IDS = createBlockStateModelIdMap();

	private static ImmutableMap<ModelIdentifier, BlockState> createBlockStateModelIdMap() {
		ImmutableMap.Builder<ModelIdentifier, BlockState> builder = ImmutableMap.builder();
		// Match code of BakedModelManager#bake
		for (Block block : Registries.BLOCK) {
			Identifier blockId = block.getRegistryEntry().registryKey().getValue();
			for (BlockState state : block.getStateManager().getStates()) {
				ModelIdentifier modelId = BlockModels.getModelId(blockId, state);
				builder.put(modelId, state);
			}
		}
		return builder.build();
	}

	public static BakedModel wrap(@Nullable BakedModel model, Identifier modelId, boolean wrapCTM, boolean wrapEmissive) {
		if (model != null && !model.isBuiltin() && !modelId.equals(ModelLoader.MISSING_ID)) {
			if (wrapCTM) {
				if (modelId instanceof ModelIdentifier) {
					BlockState state = BLOCK_STATE_MODEL_IDS.get(modelId);
					if (state != null) {
						model = new CTMBakedModel(model, state);
					}
				}
			}
			if (wrapEmissive) {
				model = new EmissiveBakedModel(model);
			}
		}
		return model;
	}
}
