package me.pepperbell.continuity.client.mixin;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import me.pepperbell.continuity.client.resource.ModelWrappingHandler;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

@Mixin(targets = "net/minecraft/client/render/model/ModelLoader$BakerImpl")
abstract class ModelLoaderBakerImplMixin {
	@Unique
	@Nullable
	private ModelWrappingHandler continuity$modelWrappingHandler;

	@Inject(method = "<init>(Lnet/minecraft/client/render/model/ModelLoader;Ljava/util/function/BiFunction;Lnet/minecraft/util/Identifier;)V", at = @At("TAIL"))
	private void continuity$onTailInit(ModelLoader modelLoader, BiFunction<Identifier, SpriteIdentifier, Sprite> spriteLoader, Identifier modelId, CallbackInfo ci) {
		continuity$modelWrappingHandler = ((ModelLoaderExtension) modelLoader).continuity$getModelWrappingHandler();
	}

	@ModifyExpressionValue(method = "bake(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;", at = { @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;"), @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/UnbakedModel;bake(Lnet/minecraft/client/render/model/Baker;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/model/BakedModel;") })
	private BakedModel continuity$modifyBakedModel(BakedModel original, Identifier id, ModelBakeSettings settings) {
		if (continuity$modelWrappingHandler != null) {
			return continuity$modelWrappingHandler.wrap(original, id);
		}
		return original;
	}
}
