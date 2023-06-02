package ru.quenteez.utils;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import ru.quenteez.network.BiomeSettingSyncWrapper;

public class CustomFog {

    private static void resetFogDistance() {
        RenderSystem.fogMode(GlStateManager.FogMode.LINEAR);
        RenderSystem.setupNvFogDistance();
    }

    public static void renderFog(BiomeSettingSyncWrapper wrapper) {
        if (wrapper == null) {
            resetFogDistance();
            return;
        }

//        float farPlaneDistance = (float) (options.viewDistance * 16);
//        float densityWeight = 1.0f;

        float wrapperDensity = wrapper.getFogDensity();

        RenderSystem.fogDensity(wrapperDensity * 0.1f);
        RenderSystem.fogMode(GlStateManager.FogMode.EXP2);
        RenderSystem.setupNvFogDistance();

//        float fogDensity = (1.0f - wrapperDensity) * 0.1f;
//
//        float biomeFogDistance = 0.0F;
//        float weightBiomeFog = 0.0f;
//
//        biomeFogDistance += fogDensity;
//        weightBiomeFog += densityWeight;
//
//        float weightMixed = 1.0f;
//        float weightDefault = weightMixed - weightBiomeFog;
//
//        if (weightDefault < 0.0f) {
//            weightDefault = 0.0f;
//        }
//
//        float fogDistanceAvg = weightBiomeFog == 0.0f ? 0.0f : biomeFogDistance / weightBiomeFog;
//        float fogDistance = (biomeFogDistance * 520f + farPlaneDistance * weightDefault) / weightMixed;
//        float fogDistanceScaleBiome = (0.1f * (1.0f - fogDistanceAvg) + 0.75f * fogDistanceAvg);
//        float fogDistanceScale = (fogDistanceScaleBiome * weightBiomeFog + weightDefault) / weightMixed;
//
//        float finalFogDistance = Math.min(fogDistance, farPlaneDistance) * 2.0f;
//        float fogStart = fogType == BackgroundRenderer.FogType.FOG_SKY ? 0.0f : finalFogDistance * fogDistanceScale;
//
//        RenderSystem.fogStart(fogStart);
//        RenderSystem.fogEnd(finalFogDistance);
    }


}
