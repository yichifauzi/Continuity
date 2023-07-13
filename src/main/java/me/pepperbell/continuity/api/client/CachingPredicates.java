package me.pepperbell.continuity.api.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;

public interface CachingPredicates {
	boolean affectsSprites();

	boolean affectsSprite(Sprite sprite);

	boolean affectsBlockStates();

	boolean affectsBlockState(BlockState state);

	boolean isValidForMultipass();
}
