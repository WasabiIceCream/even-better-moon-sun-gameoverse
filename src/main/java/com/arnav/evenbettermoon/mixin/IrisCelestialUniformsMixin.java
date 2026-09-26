package com.arnav.evenbettermoon.mixin;

import com.mojang.math.Axis;
import net.gameoverse.skysync.RealSky;
import net.irisshaders.iris.uniforms.CapturedRenderingState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gameoverse: shaders that draw their own sun and moon (Eclipse, Complementary Unbound's own
 * style) place them from Iris's sunPosition/moonPosition uniforms, which Iris builds from
 * the time of day and the pack's sun angle alone. That missed the real-sky tilt this mod adds
 * to the vanilla sun, and always put the moon opposite the sun. This rebuilds the same matrix
 * Iris does (modelView, Y -90, Z pack angle, X celestial angle) with the solar or lunar
 * declination added to the Z rotation, exactly where the vanilla sun and moon get it, and the
 * real moon angle for the moon. Iris derives shadowLightPosition from these, so shadows follow.
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.uniforms.CelestialUniforms", remap = false)
public abstract class IrisCelestialUniformsMixin {
   @Shadow @Final private float sunPathRotation;

   @Inject(method = "getCelestialPosition", at = @At("HEAD"), cancellable = true, remap = false)
   private void gameoverse$realSkyPosition(boolean sun, float unused, CallbackInfoReturnable<Vector4f> cir) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null || !mc.level.dimension().equals(Level.OVERWORLD)) {
         return;
      }
      float tickDelta = CapturedRenderingState.INSTANCE.getTickDelta();
      float angle;
      double declination;
      if (sun) {
         angle = mc.gameRenderer.getMainCamera().attributeProbe().getValue(EnvironmentAttributes.SUN_ANGLE, tickDelta);
         declination = RealSky.solarDeclination();
      } else {
         angle = (float)RealSky.moonAngleDegrees(mc.level.getDefaultClockTime() + tickDelta);
         declination = RealSky.moonDeclination();
      }
      Matrix4f matrix = new Matrix4f(CapturedRenderingState.INSTANCE.getGbufferModelView());
      matrix.rotate(Axis.YP.rotationDegrees(-90.0F));
      matrix.rotate(Axis.ZP.rotationDegrees(this.sunPathRotation + (float)declination));
      matrix.rotate(Axis.XP.rotationDegrees(angle));
      cir.setReturnValue(matrix.transform(new Vector4f(0.0F, 100.0F, 0.0F, 1.0F)));
   }
}
