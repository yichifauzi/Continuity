package me.pepperbell.continuity.api.client;

import java.util.Collection;

import net.minecraft.client.util.SpriteIdentifier;

public interface CTMProperties extends Comparable<CTMProperties> {
	Collection<SpriteIdentifier> getTextureDependencies();
}
