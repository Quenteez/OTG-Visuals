package ru.quenteez.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import ru.quenteez.network.BiomeSettingSyncWrapper;
import ru.quenteez.network.OTGClientSyncManager;

import java.util.Arrays;
import java.util.Optional;

public class CustomFog {
    private static float[][] fogDensityCache = new float[0][0];

    public static void renderFog(Camera camera, BackgroundRenderer.FogType fogType) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        World world = minecraft.world;
        GameOptions options = minecraft.options;
        BlockPos pos = camera.getBlockPos();
        Entity entity = camera.getFocusedEntity();

        Optional<RegistryKey<Biome>> key = world.getRegistryManager().get(Registry.BIOME_KEY)
                .getKey(world.getBiome(camera.getBlockPos()));
        if (!key.isPresent()) {
            return;
        }
        BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.get().getValue().toString());

        if (wrapper == null) {
            resetFogDistance(options, fogType);
            return;
        }

        double posX = pos.getX();
        double posZ = pos.getZ();

        int blockX = MathHelper.floor(posX);
        int blockZ = MathHelper.floor(posZ);

        int blendDistance = options.biomeBlendRadius;
        blendDistance = blendDistance == 0 ? 1 : blendDistance;

        int fogDensityCacheSize = blendDistance * 2 + 1;
        if (fogDensityCache.length < fogDensityCacheSize) {
            fogDensityCache = new float[fogDensityCacheSize][fogDensityCacheSize];
            for (float[] row : fogDensityCache) {
                Arrays.fill(row, -1f);
            }
        }

        boolean hasMoved = posX != lastX || posZ != lastZ;
        float biomeFogDistance = 0.0F;
        float weightBiomeFog = 0.0f;
        BlockPos.Mutable blockPos = new BlockPos.Mutable(0, 0, 0);
        float fogDensity;
        float densityWeight;

        int startX = -blendDistance;
        int endX = blendDistance;
        int startZ = -blendDistance;
        int endZ = blendDistance;

        for (int x = startX; x <= endX; ++x) {
            for (int z = startZ; z <= endZ; ++z) {
                blockPos.set(blockX + x, 0, blockZ + z);

                fogDensity = 1.0f - getFogDensity(x + blendDistance, z + blendDistance, wrapper, hasMoved);
                densityWeight = 1.0f;

                double differenceX = getDifference(entity.getX(), blockX, x, blendDistance);
                double differenceZ = getDifference(entity.getZ(), blockZ, z, blendDistance);

                if (differenceX >= 0.0) {
                    fogDensity *= differenceX;
                    densityWeight *= differenceX;
                }

                if (differenceZ >= 0.0) {
                    fogDensity *= differenceZ;
                    densityWeight *= differenceZ;
                }

                biomeFogDistance += fogDensity;
                weightBiomeFog += densityWeight;
            }
        }

        float weightMixed = (blendDistance * 2f) * (blendDistance * 2f);
        float weightDefault = weightMixed - weightBiomeFog;

        if (weightDefault < 0.0f) {
            weightDefault = 0.0f;
        }
        float farPlaneDistance = (float) (options.viewDistance * 16);

        float fogDistanceAvg = weightBiomeFog == 0.0f ? 0.0f : biomeFogDistance / weightBiomeFog;

        float fogDistance = (biomeFogDistance * 520f + farPlaneDistance * weightDefault) / weightMixed;
        float fogDistanceScaleBiome = (0.1f * (1.0f - fogDistanceAvg) + 0.75f * fogDistanceAvg);
        float fogDistanceScale = (fogDistanceScaleBiome * weightBiomeFog + weightDefault) / weightMixed;

        float finalFogDistance = Math.min(fogDistance, farPlaneDistance);
        float fogStart = fogType == BackgroundRenderer.FogType.FOG_SKY ? 0.0f : finalFogDistance * fogDistanceScale;

        lastX = posX;
        lastZ = posZ;

        RenderSystem.fogStart(fogStart);
        RenderSystem.fogEnd(finalFogDistance);
        RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
        RenderSystem.setupNvFogDistance();
    }


    private static void resetFogDistance(GameOptions options, BackgroundRenderer.FogType type) {
        float farPlaneDistance = (float) (options.viewDistance * 16);

        if (type == BackgroundRenderer.FogType.FOG_SKY) {
            RenderSystem.fogStart(0.0f);
        } else {
            RenderSystem.fogStart(farPlaneDistance * 0.75F);
        }
        RenderSystem.fogEnd(farPlaneDistance);

        Arrays.fill(fogDensityCache, new float[fogDensityCache.length]);
    }

    private static double lastX = Double.MIN_VALUE;
    private static double lastZ = Double.MIN_VALUE;

    private static double getDifference(double rawCoord, int blockCoord, int pos, int distance) {
        if (pos == -distance) {
            return 1.0f - (rawCoord - blockCoord);
        } else if (pos == distance) {
            return (rawCoord - blockCoord);
        }
        return -1.0f;
    }

    private static float getFogDensity(int x, int z, BiomeSettingSyncWrapper wrapper, boolean hasMoved) {
        float density = fogDensityCache[x][z];

        if (density != -1f && !hasMoved) {
            return density;
        }

        fogDensityCache[x][z] = wrapper.getFogDensity();
        return wrapper.getFogDensity();
    }
}
