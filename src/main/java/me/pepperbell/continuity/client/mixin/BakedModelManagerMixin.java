package me.pepperbell.continuity.client.mixin;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.pepperbell.continuity.client.resource.BakedModelManagerReloadExtension;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(BakedModelManager.class)
public class BakedModelManagerMixin {
	@Unique
	@Nullable
	private volatile BakedModelManagerReloadExtension continuity$reloadExtension;

	@Inject(method = "reload(Lnet/minecraft/resource/ResourceReloader$Synchronizer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
	private void continuity$onHeadReload(ResourceReloader.Synchronizer synchronizer, ResourceManager resourceManager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		continuity$reloadExtension = new BakedModelManagerReloadExtension(resourceManager, prepareExecutor);

		BakedModelManagerReloadExtension reloadExtension = this.continuity$reloadExtension;
		if (reloadExtension != null) {
			reloadExtension.setContext();
		}
	}

	@Inject(method = "reload(Lnet/minecraft/resource/ResourceReloader$Synchronizer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"), cancellable = true)
	private void continuity$onReturnReload(ResourceReloader.Synchronizer synchronizer, ResourceManager resourceManager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		BakedModelManagerReloadExtension reloadExtension = this.continuity$reloadExtension;
		if (reloadExtension != null) {
			reloadExtension.clearContext();
		}

		cir.setReturnValue(cir.getReturnValue().thenRun(() -> this.continuity$reloadExtension = null));
	}

	@Inject(method = "bake(Lnet/minecraft/util/profiler/Profiler;Ljava/util/Map;Lnet/minecraft/client/render/model/ModelLoader;)Lnet/minecraft/client/render/model/BakedModelManager$BakingResult;", at = @At("HEAD"))
	private void continuity$onHeadBake(Profiler profiler, Map<Identifier, SpriteAtlasManager.AtlasPreparation> preparations, ModelLoader modelLoader, CallbackInfoReturnable<?> cir) {
		BakedModelManagerReloadExtension reloadExtension = this.continuity$reloadExtension;
		if (reloadExtension != null) {
			reloadExtension.beforeBaking(preparations, modelLoader);
		}
	}

	@Inject(method = "upload(Lnet/minecraft/client/render/model/BakedModelManager$BakingResult;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
	private void continuity$onReturnUpload(@Coerce Object bakingResult, Profiler profiler, CallbackInfo ci) {
		BakedModelManagerReloadExtension reloadExtension = this.continuity$reloadExtension;
		if (reloadExtension != null) {
			reloadExtension.apply();
		}
	}
}
