package ru.quenteez.utils;

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
import net.minecraft.world.biome.Biome;
import ru.quenteez.OTGMain;
import ru.quenteez.network.BiomeSettingSyncWrapper;
import ru.quenteez.network.OTGClientSyncManager;

import java.util.Arrays;
import java.util.Optional;

public class ClientFogOld {
    private static double lastX = Double.MIN_VALUE;
    private static double lastZ = Double.MIN_VALUE;
    private static float[][] fogDensityCache = new float[0][0];
    private static boolean otgDidLastFogRender = false;

    public static float start;
    public static float end;

    private ClientFogOld()
    {
        for (float[] row : fogDensityCache)
        {
            Arrays.fill(row, -1f);
        }
    }

    public static void calculateValues(Camera camera, BackgroundRenderer.FogType fogType, Optional<RegistryKey<Biome>> key)
    {
        GameOptions options = MinecraftClient.getInstance().options;
        BlockPos pos = camera.getBlockPos();
        Entity entity = camera.getFocusedEntity();

        BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.get().getValue().toString());

        if (wrapper == null)
        {
            if (otgDidLastFogRender)
            {
                resetFogDistance(MinecraftClient.getInstance(), fogType);
            }
            return;
        }

        double posX = pos.getX();
        double posZ = pos.getZ();

        int blockX = MathHelper.floor(posX);
        int blockZ = MathHelper.floor(posZ);

        int blendDistance = options.biomeBlendRadius;
        blendDistance = blendDistance == 0 ? 1 : blendDistance;
        OTGMain.LOGGER.info("blendDistance: " + blendDistance);

        if (fogDensityCache.length < blendDistance * 2 + 1)
        {
            fogDensityCache = new float[(blendDistance * 2) + 1][(blendDistance * 2) + 1];
            for (float[] row : fogDensityCache)
            {
                Arrays.fill(row, -1f);
            }
        }

        boolean hasMoved = posX != lastX || posZ != lastZ;
        float biomeFogDistance = 0.0F;
        float weightBiomeFog = 0.0f;
        BlockPos.Mutable blockPos = new BlockPos.Mutable(0, 0, 0);
        float fogDensity;
        float densityWeight;
        double differenceX;
        double differenceZ;

        for (int x = -blendDistance; x <= blendDistance; ++x)
        {
            for (int z = -blendDistance; z <= blendDistance; ++z)
            {
                blockPos.set(blockX + x, 0, blockZ + z);

                fogDensity = 1.0f - getFogDensity(x + blendDistance, z + blendDistance, blockPos, hasMoved);
                densityWeight = 1.0f;

                differenceX = getDifference(entity.getX(), blockX, x, blendDistance);
                differenceZ = getDifference(entity.getZ(), blockZ, z, blendDistance);

                if (differenceX >= 0.0f)
                {
                    fogDensity *= differenceX;
                    densityWeight *= differenceX;
                }

                if (differenceZ >= 0.0f)
                {
                    fogDensity *= differenceZ;
                    densityWeight *= differenceZ;
                }

                biomeFogDistance += fogDensity;
                weightBiomeFog += densityWeight;

            }
        }

        float weightMixed = (blendDistance * 2f) * (blendDistance * 2f);
        float weightDefault = weightMixed - weightBiomeFog;

        if (weightDefault < 0.0f)
        {
            weightDefault = 0.0f;
        }
        float farPlaneDistance = (float) (options.viewDistance * 16);

        float fogDistanceAvg = weightBiomeFog == 0.0f ? 0.0f : biomeFogDistance / weightBiomeFog;

        float fogDistance = (biomeFogDistance * 520f + farPlaneDistance * weightDefault) / weightMixed;
        float fogDistanceScaleBiome = (0.1f * (1.0f - fogDistanceAvg) + 0.75f * fogDistanceAvg);
        float fogDistanceScale = (fogDistanceScaleBiome * weightBiomeFog + 1f * weightDefault) / weightMixed;

        float finalFogDistance = Math.min(fogDistance, farPlaneDistance);
        float fogStart = fogType == BackgroundRenderer.FogType.FOG_SKY ? 0.0f : finalFogDistance * fogDistanceScale;

        // set cache values
        lastX = posX;
        lastZ = posZ;

        otgDidLastFogRender = true;
        start = fogStart;
        end = finalFogDistance;
    }

    private static void resetFogDistance(MinecraftClient minecraft, BackgroundRenderer.FogType type)
    {
        if (otgDidLastFogRender)
        {
            // Non-OTG dims and OTG dims without fog settings don't properly reset
            // the fog start and end when players teleport between dimensions.
            // Reset the fog distance here.
            otgDidLastFogRender = false;
            float farPlaneDistance = (float) (minecraft.options.viewDistance * 16);

            if (type == BackgroundRenderer.FogType.FOG_SKY)
            {
                RenderSystem.fogStart(0.0f);
            } else
            {
                RenderSystem.fogStart(farPlaneDistance * 0.75F);
            }
            RenderSystem.fogEnd(farPlaneDistance);

            for (float[] row : fogDensityCache)
            {
                Arrays.fill(row, -1f);
            }
        }
    }

    // Get the difference between the raw coordinate and block coordinate
    private static double getDifference(double rawCoord, int blockCoord, int pos, int distance)
    {
        if (pos == -distance)
        {
            return 1.0f - (rawCoord - blockCoord);
        } else if (pos == distance)
        {
            return (rawCoord - blockCoord);
        }
        return -1.0f;
    }

    private static float getFogDensity(int x, int z, BlockPos.Mutable blockpos, boolean hasMoved)
    {
        float density = fogDensityCache[x][z];

        if (density != -1f && !hasMoved)
        {
            return density;
        }

        Optional<RegistryKey<Biome>> key = MinecraftClient.getInstance().world.getRegistryManager().get(Registry.BIOME_KEY)
                .getKey(MinecraftClient.getInstance().world.getBiome(blockpos));

        if (!key.isPresent())
            return 0;

        BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.get().getValue().toString());

        if (wrapper == null)
            return 0;

        fogDensityCache[x][z] = wrapper.getFogDensity();
        return wrapper.getFogDensity();
    }

}
