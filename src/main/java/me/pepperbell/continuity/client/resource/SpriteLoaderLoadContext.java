package me.pepperbell.continuity.client.resource;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

public interface SpriteLoaderLoadContext {
	ThreadLocal<SpriteLoaderLoadContext> THREAD_LOCAL = new ThreadLocal<>();

	CompletableFuture<@Nullable Set<Identifier>> getExtraIdsFuture(Identifier atlasId);

	@Nullable
	EmissiveControl getEmissiveControl(Identifier atlasId);

	AtomicReference<@Nullable Map<Identifier, Identifier>> getEmissiveIdMapHolder();

	interface EmissiveControl {
		void markHasEmissives();
	}
}
