package com.arnav.evenbettermoon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SkyEventConfig {
   public int bloodMoonInterval = 0;
   public int eclipseInterval = 174;
   public int meteorShowerInterval = 14;
   public int yearLength = 365;
   public double auroraIntensityMultiplier = 1.0;
   private static SkyEventConfig instance;
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static final Logger LOGGER = LoggerFactory.getLogger("evenbettermoon");

   public static SkyEventConfig get() {
      if (instance == null) {
         instance = doLoad();
      }

      return instance;
   }

   private static SkyEventConfig doLoad() {
      try {
         Path p = path();
         if (Files.exists(p)) {
            SkyEventConfig c = (SkyEventConfig)GSON.fromJson(Files.readString(p), SkyEventConfig.class);
            if (c != null) {
               return c;
            }
         }
      } catch (Exception var2) {
      }

      return new SkyEventConfig();
   }

   public void save() {
      try {
         Path p = path();
         Files.createDirectories(p.getParent());
         Files.writeString(p, GSON.toJson(this));
      } catch (IOException var2) {
         LOGGER.error("[Even Better Moon & Sun] Failed to save config: {}", var2.getMessage());
      }
   }

   private static Path path() {
      return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("evenbettermoon.json");
   }
}
