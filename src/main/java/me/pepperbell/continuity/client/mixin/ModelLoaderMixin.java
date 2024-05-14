package me.pepperbell.continuity.client.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import me.pepperbell.continuity.client.resource.ModelWrappingHandler;
import net.minecraft.client.render.model.ModelLoader;

@Mixin(ModelLoader.class)
abstract class ModelLoaderMixin implements ModelLoaderExtension {
	@Unique
	@Nullable
	private ModelWrappingHandler continuity$modelWrappingHandler;

	@Override
	@Nullable
	public ModelWrappingHandler continuity$getModelWrappingHandler() {
		return continuity$modelWrappingHandler;
	}

	@Override
	public void continuity$setModelWrappingHandler(@Nullable ModelWrappingHandler handler) {
		this.continuity$modelWrappingHandler = handler;
	}
}
