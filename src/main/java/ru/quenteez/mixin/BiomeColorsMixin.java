package ru.quenteez.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.level.ColorResolver;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.quenteez.network.BiomeSettingSyncWrapper;
import ru.quenteez.network.OTGClientSyncManager;

import java.util.Optional;

@Mixin(BiomeColors.class)
public class BiomeColorsMixin {

    @Mutable
    @Shadow @Final public static ColorResolver GRASS_COLOR;
    @Mutable
    @Shadow @Final public static ColorResolver FOLIAGE_COLOR;
    @Mutable
    @Shadow @Final public static ColorResolver WATER_COLOR;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyGrassColor(CallbackInfo ci) {
        ColorResolver grassColorResolver = BiomeColors.GRASS_COLOR;
        GRASS_COLOR = (biome, posX, posZ) ->
        {
            Optional<RegistryKey<Biome>> key = MinecraftClient.getInstance().world.getRegistryManager()
                    .get(Registry.BIOME_KEY).getKey(biome);

            if (!key.isPresent())
            {
                return grassColorResolver.getColor(biome, posX, posZ);
            }

            BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

            if (wrapper == null)
            {
                return grassColorResolver.getColor(biome, posX, posZ);
            }

            double noise = Biome.FOLIAGE_NOISE.sample(posX * 0.0225D, posZ * 0.0225D, false);
            return wrapper.getGrassColorControl().getColor(noise, biome.getGrassColorAt(posX, posZ));

        };
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyFoliageColor(CallbackInfo ci) {
        ColorResolver foliageColorResolver = BiomeColors.FOLIAGE_COLOR;
        FOLIAGE_COLOR = (biome, posX, posZ) ->
        {
            Optional<RegistryKey<Biome>> key = MinecraftClient.getInstance().world.getRegistryManager()
                    .get(Registry.BIOME_KEY).getKey(biome);

            if (!key.isPresent())
            {
                return foliageColorResolver.getColor(biome, posX, posZ);
            }

            BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

            if (wrapper == null)
            {
                return foliageColorResolver.getColor(biome, posX, posZ);
            }

            double noise = Biome.FOLIAGE_NOISE.sample(posX * 0.0225D, posZ * 0.0225D, false);
            return wrapper.getFoliageColorControl().getColor(noise, biome.getFoliageColor());

        };
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void modifyWaterColor(CallbackInfo ci) {
        ColorResolver waterColorResolver = BiomeColors.WATER_COLOR;
        WATER_COLOR = (biome, posX, posZ) ->
        {
            Optional<RegistryKey<Biome>> key = MinecraftClient.getInstance().world.getRegistryManager()
                    .get(Registry.BIOME_KEY).getKey(biome);

            if (!key.isPresent())
            {
                return waterColorResolver.getColor(biome, posX, posZ);
            }

            BiomeSettingSyncWrapper wrapper = OTGClientSyncManager.getSyncedData().get(key.toString());

            if (wrapper == null)
            {
                return waterColorResolver.getColor(biome, posX, posZ);
            }

            double noise = Biome.FOLIAGE_NOISE.sample(posX * 0.0225D, posZ * 0.0225D, false);
            return wrapper.getWaterColorControl().getColor(noise, biome.getWaterColor());

        };
    }



}
