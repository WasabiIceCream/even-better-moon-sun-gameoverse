package com.arnav.evenbettermoon;

import java.util.Random;

public final class SkyEventManager {
   public static final int LUNAR_CYCLE = 29;

   public static float eclipseProgress(long totalTicks) {
      int interval = SkyEventConfig.get().eclipseInterval;
      if (interval <= 0) {
         return 0.0F;
      } else {
         long day = totalTicks / 24000L;
         if (day % interval != 0L) {
            return 0.0F;
         } else {
            long tickOfDay = totalTicks % 24000L;
            long relToNoon = tickOfDay - 6000L;
            if (Math.abs(relToNoon) > 1500L) {
               return 0.0F;
            } else {
               float t = 1.0F - (float)Math.abs(relToNoon) / 1500.0F;
               return t * t * t;
            }
         }
      }
   }

   public static float bloodMoonAlpha(long totalTicks) {
      int interval = SkyEventConfig.get().bloodMoonInterval;
      if (interval == 0) {
         return 0.0F;
      } else if (harvestMoonAlpha(totalTicks) > 0.0F) {
         return 0.0F;
      } else {
         long day = totalTicks / 24000L;
         long tickOfDay = totalTicks % 24000L;
         if (interval < 0) {
            int lunarDay = SkyTime.lunarDay();
            if (lunarDay < 14 || lunarDay > 15) {
               return 0.0F;
            }
         } else if (day % interval != 0L) {
            return 0.0F;
         }

         if (tickOfDay < 13000L) {
            return 0.0F;
         } else if (tickOfDay < 14000L) {
            return 0.15F * (float)(tickOfDay - 13000L) / 1000.0F;
         } else if (tickOfDay < 23000L) {
            return 0.15F;
         } else {
            return tickOfDay < 24000L ? 0.15F * (1.0F - (float)(tickOfDay - 23000L) / 1000.0F) : 0.0F;
         }
      }
   }

   public static float meteorShowerProgress(long totalTicks) {
      int interval = SkyEventConfig.get().meteorShowerInterval;
      if (interval <= 0) {
         return 0.0F;
      } else {
         long day = totalTicks / 24000L;
         if (day % interval != 0L) {
            return 0.0F;
         } else {
            long tickOfDay = totalTicks % 24000L;
            if (tickOfDay < 13000L || tickOfDay > 23000L) {
               return 0.0F;
            } else if (tickOfDay < 13500L) {
               return (float)(tickOfDay - 13000L) / 500.0F;
            } else {
               return tickOfDay > 22500L ? (float)(23000L - tickOfDay) / 500.0F : 1.0F;
            }
         }
      }
   }

   public static float harvestMoonAlpha(long totalTicks) {
      // Gameoverse: bloodMoonInterval 0 also turns off the harvest moon tint, since
      // Enhanced Celestials runs the real Blood and Harvest Moon events here.
      if (SkyEventConfig.get().bloodMoonInterval == 0) {
         return 0.0F;
      }

      int yearLength = 365;
      long day = totalTicks / 24000L;
      long tickOfDay = totalTicks % 24000L;
      int dayOfYear = SkyTime.dayOfYear();
      int lunarDay = SkyTime.lunarDay();
      if (lunarDay >= 14 && lunarDay <= 15) {
         int equinox = yearLength / 2;
         if (Math.abs(dayOfYear - equinox) > 21) {
            return 0.0F;
         } else if (tickOfDay < 13000L) {
            return 0.0F;
         } else if (tickOfDay < 14000L) {
            return 0.12F * (float)(tickOfDay - 13000L) / 1000.0F;
         } else if (tickOfDay < 23000L) {
            return 0.12F;
         } else {
            return tickOfDay < 24000L ? 0.12F * (1.0F - (float)(tickOfDay - 23000L) / 1000.0F) : 0.0F;
         }
      } else {
         return 0.0F;
      }
   }

   public static int daysUntilNextBloodMoon(long totalTicks) {
      int interval = SkyEventConfig.get().bloodMoonInterval;
      if (interval == 0) {
         return -1;
      } else {
         long day = totalTicks / 24000L;
         if (interval < 0) {
            int lunarDay = SkyTime.lunarDay();
            if (lunarDay >= 14 && lunarDay <= 15) {
               return 0;
            } else {
               return lunarDay < 14 ? 14 - lunarDay : 29 - lunarDay + 14;
            }
         } else {
            long rem = day % interval;
            return rem == 0L ? 0 : (int)(interval - rem);
         }
      }
   }

   public static int daysUntilNextEclipse(long totalTicks) {
      int interval = SkyEventConfig.get().eclipseInterval;
      if (interval <= 0) {
         return -1;
      } else {
         long day = totalTicks / 24000L;
         long rem = day % interval;
         return rem == 0L ? 0 : (int)(interval - rem);
      }
   }

   public static int daysUntilNextMeteorShower(long totalTicks) {
      int interval = SkyEventConfig.get().meteorShowerInterval;
      if (interval <= 0) {
         return -1;
      } else {
         long day = totalTicks / 24000L;
         long rem = day % interval;
         return rem == 0L ? 0 : (int)(interval - rem);
      }
   }

   public static float auroraIntensity(long totalTicks) {
      double multiplier = SkyEventConfig.get().auroraIntensityMultiplier;
      if (multiplier <= 0.0) {
         return 0.0F;
      } else {
         long day = totalTicks / 24000L;
         long tickOfDay = totalTicks % 24000L;
         if (tickOfDay >= 13000L && tickOfDay < 23000L) {
            int yearLength = 365;
            int dayOfYear = SkyTime.dayOfYear();
            float seasonalWeight = equinoxWeight(dayOfYear, yearLength);
            if (seasonalWeight <= 0.0F) {
               return 0.0F;
            } else {
               Random rng = new Random(day * 6271L + 90210L);
               float baseChance = 0.55F;
               float chance = baseChance * seasonalWeight * (float)multiplier;
               if (rng.nextFloat() > chance) {
                  return 0.0F;
               } else {
                  float nightStrength = 0.4F + rng.nextFloat() * 0.6F;
                  long windowStart = 13000L;
                  long windowEnd = 23000L;
                  float progress = (float)(tickOfDay - windowStart) / (float)(windowEnd - windowStart);
                  float fade = progress < 0.15F ? progress / 0.15F : (progress > 0.85F ? (1.0F - progress) / 0.15F : 1.0F);
                  return Math.min(1.0F, nightStrength * fade * seasonalWeight * (float)multiplier);
               }
            }
         } else {
            return 0.0F;
         }
      }
   }

   public static boolean isAuroraActiveTonight(long totalTicks) {
      double multiplier = SkyEventConfig.get().auroraIntensityMultiplier;
      if (multiplier <= 0.0) {
         return false;
      } else {
         long day = totalTicks / 24000L;
         int yearLength = 365;
         int dayOfYear = SkyTime.dayOfYear();
         float seasonalWeight = equinoxWeight(dayOfYear, yearLength);
         if (seasonalWeight <= 0.0F) {
            return false;
         } else {
            Random rng = new Random(day * 6271L + 90210L);
            float chance = 0.55F * seasonalWeight * (float)multiplier;
            return rng.nextFloat() <= chance;
         }
      }
   }

   private static float equinoxWeight(int dayOfYear, int yearLength) {
      double halfPeriod = yearLength / 2.0;
      double phase = dayOfYear * ((Math.PI * 2) / halfPeriod);
      double raw = Math.cos(phase);
      double sharpened = Math.pow(Math.max(0.0, raw), 3.0);
      return (float)sharpened;
   }
}
