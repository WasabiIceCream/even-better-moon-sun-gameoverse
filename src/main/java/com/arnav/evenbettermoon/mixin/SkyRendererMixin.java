package com.arnav.evenbettermoon.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
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
   private static final int LUNAR_CYCLE = 29;
   private static final int YEAR_LENGTH = 365;
   private static final float MAX_DECLINATION = 23.5F;

   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void overrideLunarCycle(ClientLevel level, float partialTick, Camera camera, SkyRenderState state, CallbackInfo ci) {
      if (state.skybox == Skybox.OVERWORLD) {
         long totalDays = level.getGameTime() / 24000L;
         int lunarDay = (int)(totalDays % 29L);
         state.moonPhase = lunarPhase(lunarDay);
      }
   }

   @Inject(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose:()V", ordinal = 1))
   private void addSeasonalDeclination(PoseStack poseStack, float f1, float f2, float f3, MoonPhase moonPhase, float f4, float f5, CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.level.dimension().equals(Level.OVERWORLD)) {
         long ticks = mc.level.getGameTime();
         float yearAngle = (float)(ticks / 8760000.0 * 2.0 * Math.PI);
         float declination = (float)(Math.sin(yearAngle) * 23.5);
         poseStack.mulPose(Axis.ZP.rotationDegrees(declination));
      }
   }

   private static MoonPhase lunarPhase(int day) {
      if (day == 0) {
         return MoonPhase.NEW_MOON;
      } else if (day <= 5) {
         return MoonPhase.WAXING_CRESCENT;
      } else if (day <= 7) {
         return MoonPhase.FIRST_QUARTER;
      } else if (day <= 13) {
         return MoonPhase.WAXING_GIBBOUS;
      } else if (day <= 15) {
         return MoonPhase.FULL_MOON;
      } else if (day <= 21) {
         return MoonPhase.WANING_GIBBOUS;
      } else {
         return day <= 23 ? MoonPhase.THIRD_QUARTER : MoonPhase.WANING_CRESCENT;
      }
   }
}
