package com.arnav.evenbettermoon.mixin;

import com.arnav.evenbettermoon.SkyEventManager;
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
         // Gameoverse: the aurora, meteor showers and shooting stars are drawn in the sky now (SkyEffects), not
         // over the finished frame, where they covered walls and terrain

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
}
