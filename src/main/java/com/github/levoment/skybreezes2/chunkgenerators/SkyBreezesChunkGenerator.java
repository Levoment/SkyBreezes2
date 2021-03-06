package com.github.levoment.skybreezes2.chunkgenerators;

import com.github.levoment.skybreezes2.mixin.ChunkNoiseSamplerAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.math.random.CheckedRandom;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SkyBreezesChunkGenerator extends ChunkGenerator {

    public static final Codec<SkyBreezesChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            SkyBreezesChunkGenerator.createStructureSetRegistryGetter(instance).and(instance.group(
                    RegistryOps.createRegistryCodec(Registry.NOISE_KEY).forGetter(generator -> generator.noiseRegistry),
                    (BiomeSource.CODEC.fieldOf("biome_source")).forGetter(generator -> generator.biomeSource),
                    (ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings")).forGetter(generator -> generator.settings))
            ).apply(instance, instance.stable(SkyBreezesChunkGenerator::new)));

    private final Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry;
    protected final RegistryEntry<ChunkGeneratorSettings> settings;
    private final AquiferSampler.FluidLevelSampler fluidLevelSampler;

    public SkyBreezesChunkGenerator(Registry<StructureSet> structureSetRegistry, Registry<DoublePerlinNoiseSampler.NoiseParameters> noiseRegistry, BiomeSource populationSource, RegistryEntry<ChunkGeneratorSettings> registryEntry) {
        super(structureSetRegistry, Optional.empty(), populationSource);
        this.noiseRegistry = noiseRegistry;
        this.settings = registryEntry;
        this.fluidLevelSampler = (x, y, z) -> new AquiferSampler.FluidLevel(-1000000, Blocks.AIR.getDefaultState());
    }

    @Override
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public CompletableFuture<Chunk> populateBiomes(Registry<Biome> biomeRegistry, Executor executor, NoiseConfig noiseConfig, Blender blender, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.supplyAsync(Util.debugSupplier("init_biomes", () -> {
            this.populateBiomes(blender, noiseConfig, structureAccessor, chunk);
            return chunk;
        }), Util.getMainWorkerExecutor());
    }

    private void populateBiomes(Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk2) {
        ChunkNoiseSampler chunkNoiseSampler = chunk2.getOrCreateChunkNoiseSampler(chunk -> this.method_41537(chunk, structureAccessor, blender, noiseConfig));
        BiomeSupplier biomeSupplier = BelowZeroRetrogen.getBiomeSupplier(blender.getBiomeSupplier(this.biomeSource), chunk2);
        chunk2.populateBiomes(biomeSupplier, ((ChunkNoiseSamplerAccessor)chunkNoiseSampler).callCreateMultiNoiseSampler(noiseConfig.getNoiseRouter(), this.settings.value().spawnTarget()));
    }

    private ChunkNoiseSampler method_41537(Chunk chunk, StructureAccessor structureAccessor, Blender blender, NoiseConfig noiseConfig) {
        return ChunkNoiseSampler.create(chunk, noiseConfig, StructureWeightSampler.method_42695(structureAccessor, chunk.getPos()), this.settings.value(), this.fluidLevelSampler, blender);
    }

    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess world, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {

    }

    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {

    }

    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
//            // Get the start and end positions of the chunk
//            int startX = chunk.getPos().getStartX();
//            int startZ = chunk.getPos().getStartZ();
//
//            boolean mainIsland = false;
//            boolean endPortal = false;
//            boolean cactusIslandChunk = false;
//            boolean secondIsland = false;
//
//            boolean chunksHaveSameValuesWithoutSigns = Math.abs(startX) == Math.abs(startZ);
//
//            if (chunksHaveSameValuesWithoutSigns && (startX & 1023) == 0) {
//                mainIsland = true;
//            }
//
//            if ((chunksHaveSameValuesWithoutSigns && ((startX % 1026) == 0) && startX != 0) || (startX == 32 && startZ == 32)) {
//                System.out.println(startX);
//                endPortal = true;
//            }
//
//            if (chunksHaveSameValuesWithoutSigns && (((startX % 1021) == 0 && startX != 0) || (startX == -48 && startZ == -48))) {
//                cactusIslandChunk = true;
//            }
//
//            if (((startX % 1022) == 0 && (startZ & 1023) == 0 && startX != 0) || (startX == -32 && startZ == 0)) {
//                secondIsland = true;
//            }
//
//
//
//            if (mainIsland) {
//                // Build the spawn island
//                for (int y = 0; y < 8; y++) {
//                    for (int x = 0 + y; x < 17 - y - (y > 0 ? 1 : 0) ; x++) {
//                        for (int z = 0 + y; z < 17 - y - (y > 0 ? 1 : 0) ; z++) {
//                            BlockPos blockPos = new BlockPos((startX + x), 70 - y, (startZ + z));
//                            if (y == 0) {
//
//                                chunk.setBlockState(blockPos, Blocks.GRASS_BLOCK.getDefaultState(), true);
//                            } else if (y == 7) {
//                                chunk.setBlockState(blockPos, Blocks.BEDROCK.getDefaultState(), true);
//                            } else {
//                                chunk.setBlockState(blockPos, Blocks.DIRT.getDefaultState(), true);
//                            }
//                        }
//                    }
//                }
//
//                // Plant a tree
//                chunk.setBlockState(new BlockPos(startX + 8, 71, startZ + 8), Blocks.OAK_SAPLING.getDefaultState(), false);
//                BlockPos chestPosition = new BlockPos(startX + 2, 71, startZ + 8);
//                // Place a chest
//                chunk.setBlockState(chestPosition, Blocks.CHEST.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(Properties.CHEST_TYPE, ChestType.SINGLE), true);
//                // Get the chest blockstate
//                BlockState chestBlockState = chunk.getBlockState(chestPosition);
//                chunk.setBlockEntity(new ChestBlockEntity(chestPosition, chestBlockState));
//                // Try to get the chest entity
//                BlockEntity chestBlockEntity = chunk.getBlockEntity(new BlockPos(startX + 2, 71, startZ + 8));
//                // If the the entity is not null and is of type ChestBlockEntity
//                if (chestBlockEntity != null) {
//                    // Place 2 water buckets in the chest
//                    ((ChestBlockEntity)chestBlockEntity).setStack(0, new ItemStack(Items.WATER_BUCKET));
//                    ((ChestBlockEntity)chestBlockEntity).setStack(1, new ItemStack(Items.WATER_BUCKET));
//                    // Place 2 lava buckets in the chest
//                    ((ChestBlockEntity)chestBlockEntity).setStack(2, new ItemStack(Items.LAVA_BUCKET));
//                    ((ChestBlockEntity)chestBlockEntity).setStack(3, new ItemStack(Items.LAVA_BUCKET));
//                    // Place 32 of bonemeal in the chest
//                    ((ChestBlockEntity)chestBlockEntity).setStack(4, new ItemStack(Items.BONE_MEAL, 32));
//                    // Place another sapling in case the tree doesn't drop any saplings
//                    ((ChestBlockEntity)chestBlockEntity).setStack(5, new ItemStack(Items.OAK_SAPLING));
//                }
//            }
//
//            if(endPortal) {
//                // Spawn an End Portal nearby
//                Block theBlock = Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH).getBlock();
//
//                chunk.setBlockState(new BlockPos(startX + 7, 6, startZ + 6), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH), false);
//                chunk.setBlockState(new BlockPos(startX + 8, 6, startZ + 6), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH), false);
//                chunk.setBlockState(new BlockPos(startX + 9, 6, startZ + 6), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.SOUTH), false);
//
//                chunk.setBlockState(new BlockPos(startX + 7, 6, startZ + 10), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH), false);
//                chunk.setBlockState(new BlockPos(startX + 8, 6, startZ + 10), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH), false);
//                chunk.setBlockState(new BlockPos(startX + 9, 6, startZ + 10), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH), false);
//
//                chunk.setBlockState(new BlockPos(startX + 6, 6, startZ + 7), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST), false);
//                chunk.setBlockState(new BlockPos(startX + 6, 6, startZ + 8), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST), false);
//                chunk.setBlockState(new BlockPos(startX + 6, 6, startZ + 9), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST), false);
//
//                chunk.setBlockState(new BlockPos(startX + 10, 6, startZ + 7), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST), false);
//                chunk.setBlockState(new BlockPos(startX + 10, 6, startZ + 8), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST), false);
//                chunk.setBlockState(new BlockPos(startX + 10, 6, startZ + 9), Blocks.END_PORTAL_FRAME.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.WEST), false);
//            }
//
//            if (cactusIslandChunk) {
//                // Make an island with cactus on it
//                int desertIslandStartX = startX ;
//                int desertIslandStartZ = startZ;
//                for (int x = 0; x <= 2; x++) {
//                    for (int z = 0; z <= 2; z++) {
//                        for (int y = 0; y <= 5; y++) {
//                            chunk.setBlockState(new BlockPos(desertIslandStartX + x, 70 + y, desertIslandStartZ + z), Blocks.SAND.getDefaultState(), false);
//                        }
//                    }
//                }
//                chunk.setBlockState(new BlockPos(desertIslandStartX, 70 + 6, desertIslandStartZ), Blocks.CACTUS.getDefaultState(), false);
//            }
//
//            if (secondIsland) {
//                // Make an island with different saplings on it
//                for (int y = 0; y < 8; y++) {
//                    for (int x = 0 + y; x < 17 - y - (y > 0 ? 1 : 0) ; x++) {
//                        for (int z = 0 + y; z < 17 - y - (y > 0 ? 1 : 0) ; z++) {
//                            if (y == 0) {
//                                chunk.setBlockState(new BlockPos((startX + x), 70 - y, (startZ + z)), Blocks.GRASS_BLOCK.getDefaultState(), false);
//                            } else {
//                                chunk.setBlockState(new BlockPos((startX + x), 70 - y, (startZ + z)), Blocks.DIRT.getDefaultState(), false);
//                            }
//
//                        }
//                    }
//                }
//                // Plant an acacia tree
//                chunk.setBlockState(new BlockPos(startX, 71, startZ), Blocks.ACACIA_SAPLING.getDefaultState(), false);
//                // Plant a birch tree
//                chunk.setBlockState(new BlockPos(startX + 16, 71, startZ), Blocks.BIRCH_SAPLING.getDefaultState(), false);
//                // Plant a spruce tree
//                chunk.setBlockState(new BlockPos(startX + 16, 71, startZ + 16), Blocks.SPRUCE_SAPLING.getDefaultState(), false);
//
//                // Plant a Dark oak tree
//                chunk.setBlockState(new BlockPos(startX, 71, startZ + 16), Blocks.DARK_OAK_SAPLING.getDefaultState(), false);
//                chunk.setBlockState(new BlockPos(startX, 71, startZ + 15), Blocks.DARK_OAK_SAPLING.getDefaultState(), false);
//                chunk.setBlockState(new BlockPos(startX + 1, 71, startZ + 16), Blocks.DARK_OAK_SAPLING.getDefaultState(), false);
//                chunk.setBlockState(new BlockPos(startX + 1, 71, startZ + 15), Blocks.DARK_OAK_SAPLING.getDefaultState(), false);
//
//                // Plant a jungle tree
//                chunk.setBlockState(new BlockPos(startX + 8, 71, startZ + 8), Blocks.JUNGLE_SAPLING.getDefaultState(), false);
//                chunk.setBlockState(new BlockPos(startX + 9, 71, startZ + 8), Blocks.JUNGLE_SAPLING.getDefaultState(), false);
//                chunk.setBlockState(new BlockPos(startX + 8, 71, startZ + 9), Blocks.JUNGLE_SAPLING.getDefaultState(), false);
//                chunk.setBlockState(new BlockPos(startX + 9, 71, startZ + 9), Blocks.JUNGLE_SAPLING.getDefaultState(), false);
//
//                // Place a chest
//                chunk.setBlockState(new BlockPos(startX + 2, 71, startZ + 4), Blocks.CHEST.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(Properties.CHEST_TYPE, ChestType.SINGLE), true);
//                // Try to get the chest entity
//                BlockEntity chestWithSeedsAndPlants = chunk.getBlockEntity(new BlockPos(startX + 2, 71, startZ + 4));
//                // If the the entity is not null and is of type ChestBlockEntity
//                if (chestWithSeedsAndPlants != null) {
//                    // Place sugar cane
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(1, new ItemStack(Items.SUGAR_CANE));
//                    // Place kelp
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(2, new ItemStack(Items.KELP));
//                    // Place Watermelon
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(6, new ItemStack(Items.MELON_SEEDS));
//                    // Place Pumpkin
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(7, new ItemStack(Items.PUMPKIN_SEEDS));
//                    // Place cocoa beans
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(8, new ItemStack(Items.COCOA_BEANS));
//                    // Place one sapling of each tree in case they don't drop any saplings
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(9, new ItemStack(Items.ACACIA_SAPLING));
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(12, new ItemStack(Items.BIRCH_SAPLING));
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(13, new ItemStack(Items.SPRUCE_SAPLING));
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(19, new ItemStack(Items.DARK_OAK_SAPLING));
//                    ((ChestBlockEntity)chestWithSeedsAndPlants).setStack(26, new ItemStack(Items.JUNGLE_SAPLING));
//
//                }
//            }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        return new VerticalBlockSample(this.getMinimumY(), new BlockState[0]); // Never pass null. Will crash everyone and vanilla too.
    }

    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {

    }

    @Override
    public int getWorldHeight() {
        return this.settings.value().generationShapeConfig().height();
    }

    @Override
    public int getSeaLevel() {
        return this.settings.value().seaLevel();
    }

    @Override
    public int getMinimumY() {
        return this.settings.value().generationShapeConfig().minimumY();
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return 0;
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        if (this.settings.value().mobGenerationDisabled()) {
            return;
        }
        ChunkPos chunkPos = region.getCenterPos();
        RegistryEntry<Biome> registryEntry = region.getBiome(chunkPos.getStartPos().withY(region.getTopY() - 1));
        ChunkRandom chunkRandom = new ChunkRandom(new CheckedRandom(RandomSeed.getSeed()));
        chunkRandom.setPopulationSeed(region.getSeed(), chunkPos.getStartX(), chunkPos.getStartZ());
        SpawnHelper.populateEntities(region, registryEntry, chunkPos, chunkRandom);
    }
}
