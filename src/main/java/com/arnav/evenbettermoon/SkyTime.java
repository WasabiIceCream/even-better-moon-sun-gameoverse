package com.arnav.evenbettermoon;

import net.gameoverse.skysync.RealSky;
import net.minecraft.world.level.Level;

/**
 * Gameoverse: the sky's calendar comes from the real date instead of the world's tick
 * count. A "day" is a real calendar day and the lunar day is the real moon's age, while
 * the time of day is still the world's own, warped to today's daylight length the same
 * way Gameoverse Sky Sync warps the sun, so night-only events line up with real dusk.
 */
public final class SkyTime {
   private SkyTime() {
   }

   /** Real day number times 24000, plus the world's (daylight-warped) tick of day. */
   public static long ticks(Level level) {
      long tickOfDay = Math.floorMod(RealSky.warpDayTicks(level.getOverworldClockTime()), 24000L);
      return RealSky.epochDay() * 24000L + tickOfDay;
   }

   /** Days since the March equinox, 0 to 365. */
   public static int dayOfYear() {
      return RealSky.dayOfSolarYear();
   }

   /** The real moon's age in whole days, 0 (new) to 28. */
   public static int lunarDay() {
      return Math.min(28, RealSky.lunarDay());
   }
}
