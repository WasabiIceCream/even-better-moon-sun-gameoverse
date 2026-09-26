package com.arnav.evenbettermoon.mixin;

import com.arnav.evenbettermoon.SkyEventManager;
import java.util.Random;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class SkyOverlayMixin {
   @Inject(method = "extractRenderState", at = @At("TAIL"))
   private void drawSkyOverlay(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null && mc.level.dimension().equals(Level.OVERWORLD)) {
         long ticks = com.arnav.evenbettermoon.SkyTime.ticks(mc.level);
         float meteorShower = SkyEventManager.meteorShowerProgress(ticks);
         if (meteorShower > 0.0F) {
            renderMeteors(graphics, ticks, meteorShower);
         }

         renderShootingStars(graphics, ticks);
         float aurora = SkyEventManager.auroraIntensity(ticks);
         if (aurora > 0.0F) {
            renderAurora(graphics, ticks, mc.level.getGameTime(), aurora);
         }

         float eclipse = SkyEventManager.eclipseProgress(ticks);
         if (eclipse > 0.0F) {
            int alpha = (int)(eclipse * 220.0F);
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24);
         } else {
            float harvestMoon = SkyEventManager.harvestMoonAlpha(ticks);
            if (harvestMoon > 0.0F) {
               int alpha = (int)(harvestMoon * 255.0F);
               graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24 | 16747520);
            } else {
               float bloodMoon = SkyEventManager.bloodMoonAlpha(ticks);
               if (bloodMoon > 0.0F) {
                  int alpha = (int)(bloodMoon * 255.0F);
                  graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24 | 0xFF0000);
               }
            }
         }
      }
   }

   private static void renderMeteors(GuiGraphicsExtractor graphics, long ticks, float intensity) {
      long day = ticks / 24000L;
      long tickOfDay = ticks % 24000L;
      if (tickOfDay >= 13000L && tickOfDay < 23000L) {
         int W = graphics.guiWidth();
         int H = graphics.guiHeight();
         Random rng = new Random(day * 31337L + 7919L);
         int count = 30;

         for (int i = 0; i < count; i++) {
            long startTick = 13000L + (long)(rng.nextFloat() * 9000.0F);
            float sx = rng.nextFloat() * 1.3F - 0.15F;
            float sy = rng.nextFloat() * 0.35F;
            float speed = 1.0F + rng.nextFloat() * 1.5F;
            long delta = tickOfDay - startTick;
            if (delta >= 0L && delta < 80L) {
               float t = (float)delta / 80.0F;
               float alpha = t < 0.15F ? t / 0.15F : (t > 0.75F ? (1.0F - t) / 0.25F : 1.0F);
               alpha *= intensity;
               float cx = sx * W + speed * 120.0F * t;
               float cy = sy * H + speed * 65.0F * t;

               for (int j = 0; j < 8; j++) {
                  float tx = cx - j * speed * 1.7F;
                  float ty = cy - j * speed * 0.92F;
                  float tailAlpha = alpha * (1.0F - j / 8.0F);
                  int a = (int)(tailAlpha * 240.0F);
                  if (a > 0 && !(tx < 0.0F) && !(tx > W) && !(ty < 0.0F) && !(ty > H)) {
                     graphics.fill(Math.round(tx), Math.round(ty), Math.round(tx) + 2, Math.round(ty) + 2, a << 24 | 16777215);
                  }
               }
            }
         }
      }
   }

   private static void renderShootingStars(GuiGraphicsExtractor graphics, long ticks) {
      long day = ticks / 24000L;
      long tickOfDay = ticks % 24000L;
      if (tickOfDay >= 13000L && tickOfDay < 23000L) {
         int W = graphics.guiWidth();
         int H = graphics.guiHeight();

         for (int slot = 0; slot < 8; slot++) {
            Random rng = new Random(day * 97L + slot * 13L + 4127L);
            if (!(rng.nextFloat() > 0.35F)) {
               long starStart = 13000L + slot * 1250L;
               long delta = tickOfDay - starStart;
               if (delta >= 0L && delta < 25L) {
                  float t = (float)delta / 25.0F;
                  float alpha = t < 0.2F ? t / 0.2F : (t > 0.7F ? (1.0F - t) / 0.3F : 1.0F);
                  float speed = 5.0F + rng.nextFloat() * 7.0F;
                  float cx = (rng.nextFloat() * 1.2F - 0.1F) * W + speed * 35.0F * t;
                  float cy = rng.nextFloat() * 0.45F * H + speed * 18.0F * t;

                  for (int j = 0; j < 4; j++) {
                     float tx = cx - j * speed * 0.8F;
                     float ty = cy - j * speed * 0.42F;
                     int a = (int)(alpha * (1.0F - j / 4.0F) * 0.65F * 210.0F);
                     if (a > 0 && !(tx < 0.0F) && !(tx > W) && !(ty < 0.0F) && !(ty > H)) {
                        graphics.fill(Math.round(tx), Math.round(ty), Math.round(tx) + 1, Math.round(ty) + 1, a << 24 | 16777215);
                     }
                  }
               }
            }
         }
      }
   }

   private static void renderAurora(GuiGraphicsExtractor graphics, long ticks, long animTicks, float intensity) {
      long day = ticks / 24000L;
      int W = graphics.guiWidth();
      int H = graphics.guiHeight();
      int bandCount = 4;
      int curtainHeight = Math.round(H * 0.42F);
      int sliceHeight = 3;

      for (int band = 0; band < bandCount; band++) {
         Random rng = new Random(day * 4801L + band * 733L + 1123L);
         float bandCenterX = (0.15F + band * 0.24F) * W + (rng.nextFloat() - 0.5F) * 40.0F;
         float bandWidth = 60.0F + rng.nextFloat() * 50.0F;
         float waveSpeed = 3.5E-4F + rng.nextFloat() * 3.0E-4F;
         float wavePhase = rng.nextFloat() * (float) (Math.PI * 2);
         float hueSeed = rng.nextFloat();

         for (int y = 0; y < curtainHeight; y += sliceHeight) {
            float rowFrac = (float)y / curtainHeight;
            float wave = (float)Math.sin((float)(animTicks % 1000000L) * waveSpeed + rowFrac * 6.0F + wavePhase) * bandWidth * 0.5F * (0.4F + rowFrac);
            float x = bandCenterX + wave;
            float shimmer = 0.7F + 0.3F * (float)Math.sin((animTicks % 1000000L) * 0.02 + rowFrac * 10.0F + band);
            float alpha = intensity * shimmer * (1.0F - rowFrac * 0.6F) * 0.55F;
            if (!(alpha <= 0.02F)) {
               int color = auroraColor(rowFrac, hueSeed);
               int a = Math.min(255, Math.round(alpha * 255.0F));
               float halfWidth = bandWidth * (0.3F + 0.2F * rowFrac) / 2.0F;
               int x0 = Math.round(x - halfWidth);
               int x1 = Math.round(x + halfWidth);
               if (x1 > 0 && x0 < W) {
                  x0 = Math.max(0, x0);
                  x1 = Math.min(W, x1);
                  if (x1 > x0) {
                     graphics.fill(x0, y, x1, y + sliceHeight, a << 24 | color & 16777215);
                  }
               }
            }
         }
      }
   }

   private static int auroraColor(float rowFrac, float hueSeed) {
      int green = 3407786;
      int violet = 10105855;
      float t = 1.0F - rowFrac;
      t = Math.max(0.0F, Math.min(1.0F, t + (hueSeed - 0.5F) * 0.2F));
      int r = lerpChannel(green >> 16 & 0xFF, violet >> 16 & 0xFF, t);
      int g = lerpChannel(green >> 8 & 0xFF, violet >> 8 & 0xFF, t);
      int b = lerpChannel(green & 0xFF, violet & 0xFF, t);
      return r << 16 | g << 8 | b;
   }

   private static int lerpChannel(int a, int b, float t) {
      return Math.round(a + (b - a) * t);
   }
}
