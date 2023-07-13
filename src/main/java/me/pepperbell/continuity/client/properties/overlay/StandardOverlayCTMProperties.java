package me.pepperbell.continuity.client.properties.overlay;

import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import me.pepperbell.continuity.client.properties.ConnectingCTMProperties;
import me.pepperbell.continuity.client.properties.PropertiesParsingHelper;
import me.pepperbell.continuity.client.resource.ResourceRedirectHandler;
import net.minecraft.block.BlockState;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

public class StandardOverlayCTMProperties extends ConnectingCTMProperties implements OverlayPropertiesSection.Provider {
	protected OverlayPropertiesSection overlaySection;
	protected Set<Identifier> connectTilesSet;
	protected Predicate<BlockState> connectBlocksPredicate;

	public StandardOverlayCTMProperties(Properties properties, Identifier id, ResourcePack pack, int packPriority, ResourceManager resourceManager, String method) {
		super(properties, id, pack, packPriority, resourceManager, method);
		overlaySection = new OverlayPropertiesSection(properties, id, packName);
	}

	@Override
	public void init() {
		super.init();
		overlaySection.init();
		parseConnectTiles();
		parseConnectBlocks();
	}

	@Override
	public OverlayPropertiesSection getOverlayPropertiesSection() {
		return overlaySection;
	}

	protected void parseConnectTiles() {
		connectTilesSet = PropertiesParsingHelper.parseMatchTiles(properties, "connectTiles", id, packName, ResourceRedirectHandler.get(resourceManager));
	}

	protected void parseConnectBlocks() {
		connectBlocksPredicate = PropertiesParsingHelper.parseBlockStates(properties, "connectBlocks", id, packName);
	}

	public Set<Identifier> getConnectTilesSet() {
		return connectTilesSet;
	}

	public Predicate<BlockState> getConnectBlocksPredicate() {
		return connectBlocksPredicate;
	}
}
