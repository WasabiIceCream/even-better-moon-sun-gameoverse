package com.arnav.evenbettermoon;

import java.util.List;
import net.minecraft.client.Minecraft;

public final class SkyCommandHandler {
   private static final String PRE = "[Even Better Moon & Sun] ";

   public static List<String> handle(String rawMsg) {
      String[] parts = rawMsg.trim().split("\\s+");
      if (parts.length == 0) {
         return null;
      } else {
         String var2 = parts[0].toLowerCase();

         return switch (var2) {
            case "/eclipse" -> eclipse(parts);
            case "/bloodmoon" -> bloodmoon(parts);
            case "/meteorshower" -> meteorshower(parts);
            case "/year" -> year(parts);
            case "/aurora" -> aurora(parts);
            case "/astro" -> astro();
            default -> null;
         };
      }
   }

   private static List<String> eclipse(String[] p) {
      if (p.length < 2) {
         return List.of("[Even Better Moon & Sun] Usage: /eclipse set <days>  |  /eclipse reset");
      } else {
         SkyEventConfig cfg = SkyEventConfig.get();
         if (p[1].equalsIgnoreCase("reset")) {
            cfg.eclipseInterval = 174;
            cfg.save();
            return List.of("[Even Better Moon & Sun] Eclipse interval reset to 174 days.");
         } else if (p[1].equalsIgnoreCase("set")) {
            if (p.length < 3) {
               return List.of("[Even Better Moon & Sun] Usage: /eclipse set <days>");
            } else {
               try {
                  int days = Integer.parseInt(p[2]);
                  if (days < 0) {
                     return List.of("[Even Better Moon & Sun] Days must be >= 0 (0 = disable).");
                  } else {
                     cfg.eclipseInterval = days;
                     cfg.save();
                     return List.of(
                        days == 0 ? "[Even Better Moon & Sun] Solar eclipse disabled." : "[Even Better Moon & Sun] Eclipse: every " + days + " days."
                     );
                  }
               } catch (NumberFormatException var3) {
                  return List.of("[Even Better Moon & Sun] Not a number. Usage: /eclipse set <days>");
               }
            }
         } else {
            return List.of("[Even Better Moon & Sun] Usage: /eclipse set <days>  |  /eclipse reset");
         }
      }
   }

   private static List<String> bloodmoon(String[] p) {
      if (p.length < 2) {
         return List.of("[Even Better Moon & Sun] Usage: /bloodmoon set <days>  |  /bloodmoon reset");
      } else {
         SkyEventConfig cfg = SkyEventConfig.get();
         if (p[1].equalsIgnoreCase("reset")) {
            cfg.bloodMoonInterval = -1;
            cfg.save();
            return List.of("[Even Better Moon & Sun] Blood moon reset to default (every full moon night).");
         } else if (p[1].equalsIgnoreCase("set")) {
            if (p.length < 3) {
               return List.of("[Even Better Moon & Sun] Usage: /bloodmoon set <days>");
            } else {
               try {
                  int days = Integer.parseInt(p[2]);
                  if (days < 0) {
                     return List.of("[Even Better Moon & Sun] Days must be >= 0 (0 = disable). Use /bloodmoon reset to restore lunar default.");
                  } else {
                     cfg.bloodMoonInterval = days;
                     cfg.save();
                     return List.of(
                        days == 0 ? "[Even Better Moon & Sun] Blood moon disabled." : "[Even Better Moon & Sun] Blood moon: every " + days + " days."
                     );
                  }
               } catch (NumberFormatException var3) {
                  return List.of("[Even Better Moon & Sun] Not a number. Usage: /bloodmoon set <days>");
               }
            }
         } else {
            return List.of("[Even Better Moon & Sun] Usage: /bloodmoon set <days>  |  /bloodmoon reset");
         }
      }
   }

   private static List<String> meteorshower(String[] p) {
      if (p.length < 2) {
         return List.of("[Even Better Moon & Sun] Usage: /meteorshower set <days>  |  /meteorshower reset");
      } else {
         SkyEventConfig cfg = SkyEventConfig.get();
         if (p[1].equalsIgnoreCase("reset")) {
            cfg.meteorShowerInterval = 14;
            cfg.save();
            return List.of("[Even Better Moon & Sun] Meteor shower interval reset to 14 days.");
         } else if (p[1].equalsIgnoreCase("set")) {
            if (p.length < 3) {
               return List.of("[Even Better Moon & Sun] Usage: /meteorshower set <days>");
            } else {
               try {
                  int days = Integer.parseInt(p[2]);
                  if (days < 0) {
                     return List.of("[Even Better Moon & Sun] Days must be >= 0 (0 = disable).");
                  } else {
                     cfg.meteorShowerInterval = days;
                     cfg.save();
                     return List.of(
                        days == 0 ? "[Even Better Moon & Sun] Meteor shower disabled." : "[Even Better Moon & Sun] Meteor shower: every " + days + " days."
                     );
                  }
               } catch (NumberFormatException var3) {
                  return List.of("[Even Better Moon & Sun] Not a number. Usage: /meteorshower set <days>");
               }
            }
         } else {
            return List.of("[Even Better Moon & Sun] Usage: /meteorshower set <days>  |  /meteorshower reset");
         }
      }
   }

   private static List<String> year(String[] p) {
      if (p.length < 2) {
         return List.of("[Even Better Moon & Sun] Usage: /year set <days>  |  /year reset");
      } else {
         SkyEventConfig cfg = SkyEventConfig.get();
         if (p[1].equalsIgnoreCase("reset")) {
            cfg.yearLength = 365;
            cfg.save();
            return List.of("[Even Better Moon & Sun] Year length reset to 365 days (91 days per season).");
         } else if (p[1].equalsIgnoreCase("set")) {
            if (p.length < 3) {
               return List.of("[Even Better Moon & Sun] Usage: /year set <days>");
            } else {
               try {
                  int days = Integer.parseInt(p[2]);
                  if (days < 8) {
                     return List.of("[Even Better Moon & Sun] Year length must be at least 8 days.");
                  } else {
                     cfg.yearLength = days;
                     cfg.save();
                     return List.of("[Even Better Moon & Sun] Year length set to " + days + " days (" + days / 4 + " days per season).");
                  }
               } catch (NumberFormatException var3) {
                  return List.of("[Even Better Moon & Sun] Not a number. Usage: /year set <days>");
               }
            }
         } else {
            return List.of("[Even Better Moon & Sun] Usage: /year set <days>  |  /year reset");
         }
      }
   }

   private static List<String> aurora(String[] p) {
      if (p.length < 2) {
         return List.of("[Even Better Moon & Sun] Usage: /aurora set <multiplier>  |  /aurora reset");
      } else {
         SkyEventConfig cfg = SkyEventConfig.get();
         if (p[1].equalsIgnoreCase("reset")) {
            cfg.auroraIntensityMultiplier = 1.0;
            cfg.save();
            return List.of("[Even Better Moon & Sun] Aurora intensity reset to default (1.0).");
         } else if (p[1].equalsIgnoreCase("set")) {
            if (p.length < 3) {
               return List.of("[Even Better Moon & Sun] Usage: /aurora set <multiplier>");
            } else {
               try {
                  double mult = Double.parseDouble(p[2]);
                  if (mult < 0.0) {
                     return List.of("[Even Better Moon & Sun] Multiplier must be >= 0 (0 = disable).");
                  } else {
                     cfg.auroraIntensityMultiplier = mult;
                     cfg.save();
                     return List.of(
                        mult == 0.0 ? "[Even Better Moon & Sun] Aurora Borealis disabled." : "[Even Better Moon & Sun] Aurora intensity: " + mult + "x."
                     );
                  }
               } catch (NumberFormatException var4) {
                  return List.of("[Even Better Moon & Sun] Not a number. Usage: /aurora set <multiplier>");
               }
            }
         } else {
            return List.of("[Even Better Moon & Sun] Usage: /aurora set <multiplier>  |  /aurora reset");
         }
      }
   }

   private static List<String> astro() {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) {
         return List.of("[Even Better Moon & Sun] No world loaded.");
      } else {
         long ticks = mc.level.getGameTime();
         long totalDays = ticks / 24000L;
         int yearLength = SkyEventConfig.get().yearLength;
         int seasonLength = yearLength / 4;
         int dayOfYear = (int)(totalDays % yearLength);
         int lunarDay = (int)(totalDays % 29L);
         String season = dayOfYear < seasonLength ? "Spring" : (dayOfYear < seasonLength * 2 ? "Summer" : (dayOfYear < seasonLength * 3 ? "Autumn" : "Winter"));
         int bm = SkyEventManager.daysUntilNextBloodMoon(ticks);
         int ec = SkyEventManager.daysUntilNextEclipse(ticks);
         int ms = SkyEventManager.daysUntilNextMeteorShower(ticks);
         boolean auroraTonight = SkyEventManager.isAuroraActiveTonight(ticks);
         return List.of(
            "[Even Better Moon & Sun] Day " + totalDays + "  |  " + season + "  |  Lunar day " + (lunarDay + 1) + " / 29",
            "  Blood moon:    " + countdown(bm),
            "  Solar eclipse: " + countdown(ec),
            "  Meteor shower: " + countdown(ms),
            "  Aurora tonight: " + (SkyEventConfig.get().auroraIntensityMultiplier <= 0.0 ? "disabled" : (auroraTonight ? "yes" : "no"))
         );
      }
   }

   private static String countdown(int days) {
      if (days < 0) {
         return "disabled";
      } else {
         return days == 0 ? "tonight!" : "in " + days + " day" + (days == 1 ? "" : "s");
      }
   }
}
