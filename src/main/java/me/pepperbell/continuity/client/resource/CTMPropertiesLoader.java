package me.pepperbell.continuity.client.resource;

import java.io.InputStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.pepperbell.continuity.api.client.CTMLoader;
import me.pepperbell.continuity.api.client.CTMLoaderRegistry;
import me.pepperbell.continuity.api.client.CTMProperties;
import me.pepperbell.continuity.api.client.CachingPredicates;
import me.pepperbell.continuity.api.client.QuadProcessor;
import me.pepperbell.continuity.client.ContinuityClient;
import me.pepperbell.continuity.client.model.QuadProcessors;
import me.pepperbell.continuity.client.util.BooleanState;
import me.pepperbell.continuity.client.util.biome.BiomeHolderManager;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

public final class CTMPropertiesLoader {
	public static LoadingResult loadAllWithState(ResourceManager resourceManager) {
		// TODO: move these to the very beginning of resource reload
		BiomeHolderManager.clearCache();

		LoadingResult result = loadAll(resourceManager);

		// TODO: move these to the very end of resource reload
		BiomeHolderManager.refreshHolders();

		return result;
	}

	public static LoadingResult loadAll(ResourceManager resourceManager) {
		List<LoadingContainer<?>> containers = new ObjectArrayList<>();
		Map<Identifier, Set<Identifier>> textureDependencies = new Object2ObjectOpenHashMap<>();
		LoadingResult result = new LoadingResult(containers, textureDependencies);

		int packPriority = 0;
		Iterator<ResourcePack> iterator = resourceManager.streamResourcePacks().iterator();
		BooleanState invalidIdentifierState = InvalidIdentifierStateHolder.get();
		invalidIdentifierState.enable();
		while (iterator.hasNext()) {
			ResourcePack pack = iterator.next();
			loadAll(pack, packPriority, resourceManager, result);
			packPriority++;
		}
		invalidIdentifierState.disable();
		containers.sort(Comparator.reverseOrder());

		return result;
	}

	private static void loadAll(ResourcePack pack, int packPriority, ResourceManager resourceManager, LoadingResult result) {
		for (String namespace : pack.getNamespaces(ResourceType.CLIENT_RESOURCES)) {
			pack.findResources(ResourceType.CLIENT_RESOURCES, namespace, "optifine/ctm", (id, inputSupplier) -> {
				if (id.getPath().endsWith(".properties")) {
					try (InputStream stream = inputSupplier.get()) {
						Properties properties = new Properties();
						properties.load(stream);
						load(properties, id, pack, packPriority, resourceManager, result);
					} catch (Exception e) {
						ContinuityClient.LOGGER.error("Failed to load CTM properties from file '" + id + "' in pack '" + pack.getName() + "'", e);
					}
				}
			});
		}
	}

	private static void load(Properties properties, Identifier id, ResourcePack pack, int packPriority, ResourceManager resourceManager, LoadingResult result) {
		String method = properties.getProperty("method", "ctm").trim();
		CTMLoader<?> loader = CTMLoaderRegistry.get().getLoader(method);
		if (loader != null) {
			load(loader, properties, id, pack, packPriority, resourceManager, method, result);
		} else {
			ContinuityClient.LOGGER.error("Unknown 'method' value '" + method + "' in file '" + id + "' in pack '" + pack.getName() + "'");
		}
	}

	private static <T extends CTMProperties> void load(CTMLoader<T> loader, Properties properties, Identifier id, ResourcePack pack, int packPriority, ResourceManager resourceManager, String method, LoadingResult result) {
		T ctmProperties = loader.getPropertiesFactory().createProperties(properties, id, pack, packPriority, resourceManager, method);
		if (ctmProperties != null) {
			LoadingContainer<T> container = new LoadingContainer<>(loader, ctmProperties);
			result.containers().add(container);
			for (SpriteIdentifier spriteId : ctmProperties.getTextureDependencies()) {
				Set<Identifier> atlasTextureDependencies = result.textureDependencies().computeIfAbsent(spriteId.getAtlasId(), id1 -> new ObjectOpenHashSet<>());
				atlasTextureDependencies.add(spriteId.getTextureId());
			}
		}
	}

	public static List<QuadProcessors.ProcessorHolder> createProcessorHolders(List<LoadingContainer<?>> containers, Function<SpriteIdentifier, Sprite> textureGetter) {
		List<QuadProcessors.ProcessorHolder> processorHolders = new ObjectArrayList<>();
		for (LoadingContainer<?> container : containers) {
			processorHolders.add(container.toProcessorHolder(textureGetter));
		}
		return processorHolders;
	}

	public record LoadingContainer<T extends CTMProperties>(CTMLoader<T> loader, T properties) implements Comparable<LoadingContainer<?>> {
		public QuadProcessors.ProcessorHolder toProcessorHolder(Function<SpriteIdentifier, Sprite> textureGetter) {
			QuadProcessor processor = loader.getProcessorFactory().createProcessor(properties, textureGetter);
			CachingPredicates predicates = loader.getPredicatesFactory().createPredicates(properties, textureGetter);
			return new QuadProcessors.ProcessorHolder(processor, predicates);
		}

		@Override
		public int compareTo(@NotNull LoadingContainer<?> o) {
			return properties.compareTo(o.properties);
		}
	}

	public record LoadingResult(List<LoadingContainer<?>> containers, Map<Identifier, Set<Identifier>> textureDependencies) {
	}
}
