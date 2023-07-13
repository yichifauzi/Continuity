package me.pepperbell.continuity.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import me.pepperbell.continuity.client.mixinterface.ModelLoaderExtension;
import net.minecraft.client.render.model.ModelLoader;

@Mixin(ModelLoader.class)
public class ModelLoaderMixin implements ModelLoaderExtension {
	@Unique
	private boolean continuity$wrapCTM;

	@Unique
	private boolean continuity$wrapEmissive;

	@Override
	public boolean continuity$getWrapCTM() {
		return continuity$wrapCTM;
	}

	@Override
	public void continuity$setWrapCTM(boolean wrapCTM) {
		this.continuity$wrapCTM = wrapCTM;
	}

	@Override
	public boolean continuity$getWrapEmissive() {
		return continuity$wrapEmissive;
	}

	@Override
	public void continuity$setWrapEmissive(boolean wrapEmissive) {
		this.continuity$wrapEmissive = wrapEmissive;
	}
}
