package com.arnav.evenbettermoon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.gameoverse.skysync.RealSky;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType.Skybox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
   // Gameoverse: Gameoverse Sky Sync already drives the vanilla moon phase from the real
   // moon on both sides, so the sky and the server agree; no phase override here.

   /**
    * Gameoverse: the moon's position follows the real moon instead of always sitting
    * opposite the sun. It trails the sun by its elongation (a new moon rises with the
    * sun, a full moon at sunset) and stays up for as long as its own declination allows.
    */
   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void realMoonPosition(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
      if (state.skybox == Skybox.OVERWORLD) {
         double ticks = level.getDefaultClockTime() + partialTick;
         state.moonAngle = (float)Math.toRadians(RealSky.moonAngleDegrees(ticks));
      }
   }

   /** Tilts the sun's arc north or south by today's solar declination (sun only). */
   @Inject(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 1, shift = At.Shift.AFTER))
   private void addSeasonalDeclination(PoseStack poseStack, float f1, float f2, float f3, MoonPhase moonPhase, float f4, float f5, CallbackInfo ci) {
      if (inOverworld()) {
         poseStack.mulPose(Axis.ZP.rotationDegrees((float)RealSky.solarDeclination()));
      }
   }

   /** Gameoverse: tilts the moon's arc by the moon's own declination. */
   @Inject(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", ordinal = 2, shift = At.Shift.AFTER))
   private void addLunarDeclination(PoseStack poseStack, float f1, float f2, float f3, MoonPhase moonPhase, float f4, float f5, CallbackInfo ci) {
      if (inOverworld()) {
         poseStack.mulPose(Axis.ZP.rotationDegrees((float)RealSky.moonDeclination()));
      }
   }

   private static boolean inOverworld() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level != null && mc.level.dimension().equals(Level.OVERWORLD);
   }
}
