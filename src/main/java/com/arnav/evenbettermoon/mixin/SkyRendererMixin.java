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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.joml.Vector4f;
import org.joml.Vector4fc;
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

   /**
    * Gameoverse: the aurora, meteor showers and shooting stars are drawn here, in the sky, before any celestial
    * rotation (so they stay fixed in the world) and before terrain (so terrain covers them). The last parameter is
    * the stars' brightness, which they fade with. See SkyEffects.
    */
   @Inject(method = "renderSunMoonAndStars", at = @At("HEAD"))
   private void skyEffects(PoseStack poseStack, float sunAngle, float moonAngle, float starAngle, MoonPhase moonPhase, float sunMoonAlpha, float starBrightness, CallbackInfo ci) {
      com.arnav.evenbettermoon.SkyEffects.render(poseStack, starBrightness);
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

   /** Gameoverse: the sun fades as the moon covers it during a solar eclipse. */
   @ModifyArg(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(FLcom/mojang/blaze3d/vertex/PoseStack;)V"), index = 0)
   private float eclipseSun(float alpha) {
      return inOverworld() ? alpha * (float)(1.0 - RealSky.solarCoverage()) : alpha;
   }

   /**
    * Gameoverse: the moon dims in Earth's penumbra and turns a dim coppery red in its
    * umbra during a lunar eclipse, deepest at totality.
    */
   @ModifyArg(method = "renderMoon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4fc;Lorg/joml/Vector4fc;Lorg/joml/Vector3fc;Lorg/joml/Matrix4fc;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"), index = 1)
   private Vector4fc eclipseMoon(Vector4fc color) {
      if (!inOverworld()) {
         return color;
      }
      double umbral = RealSky.umbralMagnitude();
      double penumbral = RealSky.penumbralMagnitude();
      if (penumbral <= 0) {
         return color;
      }
      float r = 1, g = 1, b = 1;
      float dim = (float)(1 - 0.25 * Math.min(1, penumbral));
      if (umbral > 0) {
         float t = (float)Math.min(1, umbral);
         r = lerp(dim, 0.62F, t);
         g = lerp(dim, 0.24F, t);
         b = lerp(dim, 0.16F, t);
      } else {
         r = g = b = dim;
      }
      return new Vector4f(color.x() * r, color.y() * g, color.z() * b, color.w());
   }

   private static float lerp(float from, float to, float t) {
      return from + (to - from) * t;
   }

   private static boolean inOverworld() {
      Minecraft mc = Minecraft.getInstance();
      return mc.level != null && mc.level.dimension().equals(Level.OVERWORLD);
   }
}
