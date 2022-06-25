package com.github.levoment.skybreezes2;

import com.github.levoment.skybreezes2.chunkgenerators.SkyBreezesChunkGenerator;
import com.github.levoment.skybreezes2.features.SkyBreezesIslandFeatures;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.HeightRangePlacementModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class SkyBreezes2 implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("skybreezes2");

	// Mod id
	public static final String MOD_ID = "skybreezes2";

	// Sky Breezes island features
	private static SkyBreezesIslandFeatures SKY_BREEZES_ISLAND_FEATURE;
	private static RegistryEntry<ConfiguredFeature<DefaultFeatureConfig, ?>> SKY_BREEZES_CONFIGURED_FEATURE;
	private static RegistryEntry<PlacedFeature> SKY_BREEZES_PLACED_FEATURE;

	@Override
	public void onInitialize() {
		// Store the raw feature in a variable
		SkyBreezesIslandFeatures skyBreezesIslandFeature = new SkyBreezesIslandFeatures(DefaultFeatureConfig.CODEC);
		// Register the raw feature for the islands
		SKY_BREEZES_ISLAND_FEATURE = Registry.register(Registry.FEATURE, new Identifier(MOD_ID, "island_feature"), skyBreezesIslandFeature);
		// Register the configured feature for the islands
		SKY_BREEZES_CONFIGURED_FEATURE = ConfiguredFeatures.register(new Identifier(MOD_ID, "island_feature").toString(), SKY_BREEZES_ISLAND_FEATURE, DefaultFeatureConfig.DEFAULT);
		// Register the placed feature for the islands
		SKY_BREEZES_PLACED_FEATURE = PlacedFeatures.register(new Identifier(MOD_ID, "island_placed_feature").toString(), SKY_BREEZES_CONFIGURED_FEATURE, List.of(
				HeightRangePlacementModifier.uniform(YOffset.aboveBottom(0), YOffset.belowTop(90)),
				BiomePlacementModifier.of()));
		// Register my custom chunk generator
		Registry.register(Registry.CHUNK_GENERATOR, new Identifier("skybreezes2", "sky_breezes_chunk_generator"), SkyBreezesChunkGenerator.CODEC);
	}
}
