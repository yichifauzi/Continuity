package me.pepperbell.continuity.client.resource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import me.pepperbell.continuity.client.model.QuadProcessors;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class BakedModelManagerReloadExtension {
	private final CompletableFuture<CTMPropertiesLoader.LoadingResult> ctmLoadingResultFuture;
	private final AtomicBoolean wrapEmissiveModels = new AtomicBoolean();
	private volatile List<QuadProcessors.ProcessorHolder> processorHolders;

	public BakedModelManagerReloadExtension(ResourceManager resourceManager, Executor prepareExecutor) {
		ctmLoadingResultFuture = CompletableFuture.supplyAsync(() -> CTMPropertiesLoader.loadAllWithState(resourceManager), prepareExecutor);
		EmissiveSuffixLoader.load(resourceManager);
	}

	public void setContext() {
		SpriteLoaderLoadContext.THREAD_LOCAL.set(new SpriteLoaderInitContextImpl(ctmLoadingResultFuture.thenApply(CTMPropertiesLoader.LoadingResult::textureDependencies), wrapEmissiveModels));
	}

	public void clearContext() {
		SpriteLoaderLoadContext.THREAD_LOCAL.set(null);
	}

	public void beforeBaking(Map<Identifier, SpriteAtlasManager.AtlasPreparation> preparations, ModelLoader modelLoader) {
		CTMPropertiesLoader.LoadingResult result = ctmLoadingResultFuture.join();

		List<QuadProcessors.ProcessorHolder> processorHolders = CTMPropertiesLoader.createProcessorHolders(result.containers(), spriteId -> {
			SpriteAtlasManager.AtlasPreparation preparation = preparations.get(spriteId.getAtlasId());
			Sprite sprite = preparation.getSprite(spriteId.getTextureId());
			if (sprite != null) {
				return sprite;
			}
			return preparation.getMissingSprite();
		});

		this.processorHolders = processorHolders;

		((ModelLoaderExtension) modelLoader).continuity$setWrapCTM(!processorHolders.isEmpty());
		((ModelLoaderExtension) modelLoader).continuity$setWrapEmissive(wrapEmissiveModels.get());
	}

	public void apply() {
		List<QuadProcessors.ProcessorHolder> processorHolders = this.processorHolders;
		if (processorHolders != null) {
			QuadProcessors.reload(processorHolders);
		}
	}

	private static class SpriteLoaderInitContextImpl implements SpriteLoaderLoadContext {
		private final CompletableFuture<Map<Identifier, Set<Identifier>>> allExtraIdsFuture;
		private final Map<Identifier, CompletableFuture<Set<Identifier>>> extraIdsFutures = new Object2ObjectOpenHashMap<>();
		private final EmissiveControl blockAtlasEmissiveControl;
		private final AtomicReference<Map<Identifier, Identifier>> emissiveIdMapHolder = new AtomicReference<>();

		public SpriteLoaderInitContextImpl(CompletableFuture<Map<Identifier, Set<Identifier>>> allExtraIdsFuture, AtomicBoolean blockAtlasHasEmissivesHolder) {
			this.allExtraIdsFuture = allExtraIdsFuture;
			blockAtlasEmissiveControl = () -> blockAtlasHasEmissivesHolder.set(true);
		}

		@Override
		public CompletableFuture<@Nullable Set<Identifier>> getExtraIdsFuture(Identifier atlasId) {
			return extraIdsFutures.computeIfAbsent(atlasId, id -> allExtraIdsFuture.thenApply(allExtraIds -> allExtraIds.get(id)));
		}

		@Override
		@Nullable
		public EmissiveControl getEmissiveControl(Identifier atlasId) {
			if (atlasId.equals(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)) {
				return blockAtlasEmissiveControl;
			}
			return null;
		}

		@Override
		public AtomicReference<@Nullable Map<Identifier, Identifier>> getEmissiveIdMapHolder() {
			return emissiveIdMapHolder;
		}
	}
}
