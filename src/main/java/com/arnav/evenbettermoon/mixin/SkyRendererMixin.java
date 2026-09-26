package com.arnav.evenbettermoon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.gameoverse.skysync.RealSky;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

   // Gameoverse: no lunar-cycle override. Gameoverse Sky Sync already drives the vanilla
   // moon phase from the real moon on both sides, so the sky and the server agree.

   @Inject(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose:()V", ordinal = 1))
   private void addSeasonalDeclination(PoseStack poseStack, float f1, float f2, float f3, MoonPhase moonPhase, float f4, float f5, CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.level.dimension().equals(Level.OVERWORLD)) {
         float declination = (float)RealSky.solarDeclination();
         poseStack.mulPose(Axis.ZP.rotationDegrees(declination));
      }
   }
}
