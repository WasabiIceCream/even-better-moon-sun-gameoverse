package com.arnav.evenbettermoon.mixin;

import com.arnav.evenbettermoon.SkyEventConfig;
import com.arnav.evenbettermoon.SkyEventManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DebugScreenOverlay.class)
public abstract class DebugScreenMixin {
   private static final int LUNAR_CYCLE = 29;

   @Inject(method = "getGameInformation", at = @At("RETURN"), cancellable = true)
   private void addAstroInfo(CallbackInfoReturnable<List<String>> cir) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level != null) {
         long totalTicks = com.arnav.evenbettermoon.SkyTime.ticks(mc.level);
         long totalDays = totalTicks / 24000L;
         int yearLength = 365;
         int seasonLength = yearLength / 4;
         int dayOfYear = com.arnav.evenbettermoon.SkyTime.dayOfYear();
         int lunarDay = com.arnav.evenbettermoon.SkyTime.lunarDay();
         String season = getSeason(dayOfYear, seasonLength);
         int dayInSeason = dayOfYear % seasonLength + 1;
         String moonPhase = getMoonPhaseName(lunarDay);
         float eclipse = SkyEventManager.eclipseProgress(totalTicks);
         float harvestMoon = SkyEventManager.harvestMoonAlpha(totalTicks);
         float bloodMoon = SkyEventManager.bloodMoonAlpha(totalTicks);
         float meteorShower = SkyEventManager.meteorShowerProgress(totalTicks);
         float aurora = SkyEventManager.auroraIntensity(totalTicks);
         int daysBloodMoon = SkyEventManager.daysUntilNextBloodMoon(totalTicks);
         int daysEclipse = SkyEventManager.daysUntilNextEclipse(totalTicks);
         int daysMeteor = SkyEventManager.daysUntilNextMeteorShower(totalTicks);
         List<String> info = new ArrayList<>((Collection<? extends String>)cir.getReturnValue());
         info.add("");
         info.add(java.time.LocalDate.now() + "  |  " + season + " (day " + dayInSeason + ")");
         info.add("Lunar cycle: " + moonPhase + " (" + (lunarDay + 1) + " / 29)");
         if (eclipse > 0.0F) {
            info.add("Solar Eclipse (" + Math.round(eclipse * 100.0F) + "%)");
         } else if (harvestMoon > 0.0F) {
            info.add("Harvest Moon");
         } else if (bloodMoon > 0.0F) {
            info.add("Blood Moon");
         }

         if (meteorShower > 0.0F) {
            info.add("Meteor Shower");
         }

         if (aurora > 0.0F) {
            info.add("Aurora Borealis (" + Math.round(aurora * 100.0F) + "%)");
         }

         info.add("");
         if (daysBloodMoon > 0) {
            info.add("Next blood moon:    " + daysBloodMoon + " day" + (daysBloodMoon == 1 ? "" : "s"));
         } else if (daysBloodMoon < 0) {
            info.add("Blood moon: disabled");
         }

         if (daysEclipse > 0) {
            info.add("Next eclipse:       " + daysEclipse + " day" + (daysEclipse == 1 ? "" : "s"));
         } else if (daysEclipse < 0) {
            info.add("Eclipse: disabled");
         }

         if (daysMeteor > 0) {
            info.add("Next meteor shower: " + daysMeteor + " day" + (daysMeteor == 1 ? "" : "s"));
         } else if (daysMeteor < 0) {
            info.add("Meteor shower: disabled");
         }

         cir.setReturnValue(info);
      }
   }

   private static String getSeason(int dayOfYear, int seasonLength) {
      if (dayOfYear < seasonLength) {
         return "Spring";
      } else if (dayOfYear < seasonLength * 2) {
         return "Summer";
      } else {
         return dayOfYear < seasonLength * 3 ? "Autumn" : "Winter";
      }
   }

   private static String getMoonPhaseName(int lunarDay) {
      if (lunarDay == 0) {
         return "New Moon";
      } else if (lunarDay <= 5) {
         return "Waxing Crescent";
      } else if (lunarDay <= 7) {
         return "First Quarter";
      } else if (lunarDay <= 13) {
         return "Waxing Gibbous";
      } else if (lunarDay <= 15) {
         return "Full Moon";
      } else if (lunarDay <= 21) {
         return "Waning Gibbous";
      } else {
         return lunarDay <= 23 ? "Third Quarter" : "Waning Crescent";
      }
   }
}
