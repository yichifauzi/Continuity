package me.pepperbell.continuity.client.mixin;

import java.util.function.BiFunction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import me.pepperbell.continuity.client.resource.ModelWrappingHandler;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

@Mixin(targets = "net/minecraft/client/render/model/ModelLoader$BakerImpl")
public class ModelLoaderBakerImplMixin {
	@Unique
	private boolean continuity$wrapCTM;

	@Unique
	private boolean continuity$wrapEmissive;

	@Inject(method = "<init>(Lnet/minecraft/client/render/model/ModelLoader;Ljava/util/function/BiFunction;Lnet/minecraft/util/Identifier;)V", at = @At("TAIL"))
	private void continuity$onTailInit(ModelLoader modelLoader, BiFunction<Identifier, SpriteIdentifier, Sprite> spriteLoader, Identifier modelId, CallbackInfo ci) {
		continuity$wrapCTM = ((ModelLoaderExtension) modelLoader).continuity$getWrapCTM();
		continuity$wrapEmissive = ((ModelLoaderExtension) modelLoader).continuity$getWrapEmissive();
	}

	@Inject(method = "bake(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/ItemModelGenerator;create(Ljava/util/function/Function;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;")), at = @At(value = "RETURN", ordinal = 0), cancellable = true)
	private void continuity$onReturnBakeGenerated(Identifier id, ModelBakeSettings settings, CallbackInfoReturnable<BakedModel> cir) {
		BakedModel model = cir.getReturnValue();
		BakedModel wrappedModel = ModelWrappingHandler.wrap(model, id, continuity$wrapCTM, continuity$wrapEmissive);
		if (model != wrappedModel) {
			cir.setReturnValue(wrappedModel);
		}
	}

	@ModifyVariable(method = "bake(Lnet/minecraft/util/Identifier;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedModel;", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/render/model/UnbakedModel;bake(Lnet/minecraft/client/render/model/Baker;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/model/BakedModel;"))
	private BakedModel continuity$modifyBakedModel(BakedModel model, Identifier id, ModelBakeSettings settings) {
		return ModelWrappingHandler.wrap(model, id, continuity$wrapCTM, continuity$wrapEmissive);
	}
}
