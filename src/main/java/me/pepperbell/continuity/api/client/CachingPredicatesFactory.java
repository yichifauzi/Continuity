package me.pepperbell.continuity.api.client;

import java.util.function.Function;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;

public interface CachingPredicatesFactory<T extends CTMProperties> {
	CachingPredicates createPredicates(T properties, Function<SpriteIdentifier, Sprite> textureGetter);
}
