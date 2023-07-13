package me.pepperbell.continuity.client.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.pepperbell.continuity.api.client.CachingPredicates;
import me.pepperbell.continuity.api.client.QuadProcessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.Sprite;

public final class QuadProcessors {
	private static ProcessorHolder[] processorHolders = new ProcessorHolder[0];
	private static final BlockStateKeyCache CACHE = new BlockStateKeyCache();

	public static Function<Sprite, Slice> getCache(BlockState state) {
		return CACHE.apply(state);
	}

	private static Slice computeSlice(BlockState state, Sprite sprite) {
		List<QuadProcessor> processorList = new ObjectArrayList<>();
		List<QuadProcessor> multipassProcessorList = new ObjectArrayList<>();

		for (ProcessorHolder holder : processorHolders) {
			QuadProcessor processor = holder.processor();
			CachingPredicates predicates = holder.predicates();
			if (!predicates.affectsBlockStates() || predicates.affectsBlockState(state)) {
				if (predicates.affectsSprites()) {
					if (predicates.affectsSprite(sprite)) {
						processorList.add(processor);
						if (predicates.isValidForMultipass()) {
							multipassProcessorList.add(processor);
						}
					}
				} else {
					processorList.add(processor);
				}
			}
		}

		QuadProcessor[] processors = processorList.toArray(QuadProcessor[]::new);
		QuadProcessor[] multipassProcessors = multipassProcessorList.toArray(QuadProcessor[]::new);
		return new Slice(processors, multipassProcessors);
	}

	@ApiStatus.Internal
	public static void reload(List<QuadProcessors.ProcessorHolder> processorHolders) {
		QuadProcessors.processorHolders = processorHolders.toArray(ProcessorHolder[]::new);
		CACHE.clear();
	}

	public record ProcessorHolder(QuadProcessor processor, CachingPredicates predicates) {
	}

	public record Slice(QuadProcessor[] processors, QuadProcessor[] multipassProcessors) {
	}

	private static class BlockStateKeyCache implements Function<BlockState, SpriteKeyCache> {
		private final Map<BlockState, SpriteKeyCache> map = new Object2ObjectOpenHashMap<>();
		private final StampedLock lock = new StampedLock();

		@Override
		public SpriteKeyCache apply(BlockState state) {
			SpriteKeyCache innerCache;
			long readStamp = lock.readLock();
			try {
				innerCache = map.get(state);
			} finally {
				lock.unlockRead(readStamp);
			}
			if (innerCache == null) {
				long writeStamp = lock.writeLock();
				try {
					innerCache = new SpriteKeyCache(state);
					map.put(state, innerCache);
				} finally {
					lock.unlockWrite(writeStamp);
				}
			}
			return innerCache;
		}

		public void clear() {
			long writeStamp = lock.writeLock();
			try {
				map.values().forEach(SpriteKeyCache::clear);
			} finally {
				lock.unlockWrite(writeStamp);
			}
		}
	}

	private static class SpriteKeyCache implements Function<Sprite, Slice> {
		private final Map<Sprite, Slice> map = new Object2ObjectOpenHashMap<>(4, Hash.FAST_LOAD_FACTOR);
		private final StampedLock lock = new StampedLock();
		private final BlockState state;

		public SpriteKeyCache(BlockState state) {
			this.state = state;
		}

		@Override
		public Slice apply(Sprite sprite) {
			Slice slice;
			long readStamp = lock.readLock();
			try {
				slice = map.get(sprite);
			} finally {
				lock.unlockRead(readStamp);
			}
			if (slice == null) {
				long writeStamp = lock.writeLock();
				try {
					slice = computeSlice(state, sprite);
					map.put(sprite, slice);
				} finally {
					lock.unlockWrite(writeStamp);
				}
			}
			return slice;
		}

		public void clear() {
			long writeStamp = lock.writeLock();
			try {
				map.clear();
			} finally {
				lock.unlockWrite(writeStamp);
			}
		}
	}
}
