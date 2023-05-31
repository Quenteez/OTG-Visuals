package ru.quenteez.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.quenteez.utils.CustomFog;

@Mixin(value = BackgroundRenderer.class, priority = 1500)
@Environment(EnvType.CLIENT)
public class BackgroundRendererMixin {

    @Inject(method = "applyFog", at=@At("RETURN"))
    private static void setFogFalloff(Camera camera, BackgroundRenderer.FogType fogType,
                                      float viewDistance, boolean thickFog, CallbackInfo ci) {
        FluidState cameraSubmersionType = camera.getSubmergedFluidState();
        Entity entity = camera.getFocusedEntity();
        if (fogType == BackgroundRenderer.FogType.FOG_SKY) {
            RenderSystem.fogStart(0.0f);
            RenderSystem.fogEnd(viewDistance);
        } else if (!cameraSubmersionType.isIn(FluidTags.WATER) &&
                !cameraSubmersionType.isIn(FluidTags.LAVA) &&
                !((entity instanceof LivingEntity) &&
                        ((LivingEntity)entity).hasStatusEffect(StatusEffects.BLINDNESS))) {
            CustomFog.renderFog(camera, fogType);
        }
    }

}
