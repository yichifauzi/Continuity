package me.pepperbell.continuity.api.client;

import java.util.Properties;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

public interface CTMPropertiesFactory<T extends CTMProperties> {
	@Nullable
	T createProperties(Properties properties, Identifier id, ResourcePack pack, int packPriority, ResourceManager resourceManager, String method);
}
