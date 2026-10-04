package com.arnav.evenbettermoon;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Random;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gameoverse: the aurora, meteor showers and shooting stars, drawn in the sky. The original drew them as flat 2D
 * layers over the finished frame (Gui), so they covered walls and terrain and stayed fixed to the camera (seen
 * 2026-10-04). They're now geometry on the sky sphere, drawn from {@code SkyRenderer.renderSunMoonAndStars} like
 * vanilla's sunrise glow: before terrain, so terrain covers them, and fixed in the world (the aurora hangs over the
 * northern horizon). Same schedules as before ({@link SkyEventManager}); they fade with the stars (dusk, dawn, rain).
 * With an Iris shader pack the pipeline is assigned to the pack's sky program.
 */
public final class SkyEffects {
   private static final Logger LOGGER = LoggerFactory.getLogger("evenbettermoon");
   private static final float RADIUS = 100.0F;
   private static final int GREEN = 0x33FFAA;
   private static final int TEAL = 0x40E8C0;
   private static final int VIOLET = 0x9A33FF;
   private static final ByteBufferBuilder BYTES = new ByteBufferBuilder(1 << 16);
   private static RenderPipeline pipeline;

   private SkyEffects() {
   }

   private static RenderPipeline pipeline() {
      if (pipeline == null) {
         pipeline = RenderPipeline.builder()
            .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withLocation(Identifier.fromNamespaceAndPath("evenbettermoon", "pipeline/sky_effects"))
            .withVertexShader("core/position_color")
            .withFragmentShader("core/position_color")
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES)
            .build();
         assignToIris(pipeline);
      }
      return pipeline;
   }

   /** Iris draws unknown pipelines poorly or not at all under a shader pack; treat ours as basic sky geometry. */
   private static void assignToIris(RenderPipeline p) {
      if (!FabricLoader.getInstance().isModLoaded("iris")) {
         return;
      }
      try {
         Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
         Class<?> program = Class.forName("net.irisshaders.iris.api.v0.IrisProgram");
         Object instance = api.getMethod("getInstance").invoke(null);
         api.getMethod("assignPipeline", RenderPipeline.class, program).invoke(instance, p, program.getField("SKY_BASIC").get(null));
      } catch (ReflectiveOperationException | LinkageError e) {
         LOGGER.warn("[Even Better Moon & Sun] Couldn't register the sky effects with Iris", e);
      }
   }

   /** Called at the start of renderSunMoonAndStars, with the sky's pose before any celestial rotation. */
   public static void render(PoseStack poseStack, float starBrightness) {
      Minecraft mc = Minecraft.getInstance();
      ClientLevel level = mc.level;
      if (level == null || !level.dimension().equals(Level.OVERWORLD) || starBrightness <= 0.0F) {
         return;
      }
      // Vanilla stars peak at 0.5 brightness on a clear night
      float fade = Math.min(1.0F, starBrightness * 2.0F);
      float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
      long ticks = SkyTime.ticks(level);
      float anim = (level.getGameTime() % 1_000_000L) + partial;

      BufferBuilder builder = new BufferBuilder(BYTES, VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
      int vertices = 0;
      float aurora = SkyEventManager.auroraIntensity(ticks) * fade;
      if (aurora > 0.01F) {
         vertices += aurora(builder, ticks / 24000L, anim, aurora);
      }
      float meteors = SkyEventManager.meteorShowerProgress(ticks) * fade;
      if (meteors > 0.0F) {
         vertices += meteors(builder, ticks, partial, meteors);
      }
      vertices += shootingStars(builder, ticks, partial, fade);
      if (vertices == 0) {
         return;
      }

      try (MeshData mesh = builder.buildOrThrow()) {
         GpuDevice device = RenderSystem.getDevice();
         GpuBuffer buffer = device.createBuffer(() -> "Even Better Moon sky effects", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
         Matrix4fStack modelView = RenderSystem.getModelViewStack();
         modelView.pushMatrix();
         modelView.mul(poseStack.last().pose());
         GpuBufferSlice transform = RenderSystem.getDynamicUniforms()
            .writeTransform(modelView, new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
         RenderTarget target = mc.getMainRenderTarget();
         try (RenderPass pass = device.createCommandEncoder().createRenderPass(() -> "Even Better Moon sky effects",
            target.getColorTextureView(), OptionalInt.empty(), target.getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(pipeline());
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", transform);
            pass.setVertexBuffer(0, buffer);
            pass.draw(0, mesh.drawState().vertexCount());
         } finally {
            modelView.popMatrix();
            buffer.close();
         }
      }
   }

   // ---------------------------------------------------------------- aurora

   /** Curtains over the northern horizon: green at the foot, violet fading out at the top, gently waving. */
   private static int aurora(BufferBuilder b, long day, float anim, float intensity) {
      int written = 0;
      int columns = 40;
      for (int band = 0; band < 4; band++) {
         Random rng = new Random(day * 4801L + band * 733L + 1123L);
         float centre = (float)Math.toRadians(-70.0 + band * 47.0 + (rng.nextFloat() - 0.5F) * 20.0F);
         float span = (float)Math.toRadians(30.0 + rng.nextFloat() * 25.0);
         float baseElevation = (float)Math.toRadians(8.0 + rng.nextFloat() * 10.0);
         float height = (float)Math.toRadians(18.0 + rng.nextFloat() * 14.0);
         float speed = 0.004F + rng.nextFloat() * 0.004F;
         float phase = rng.nextFloat() * (float)(Math.PI * 2);
         float hueShift = (rng.nextFloat() - 0.5F) * 0.2F;
         float strength = intensity * (0.6F + 0.4F * rng.nextFloat()) * 0.75F;

         float[] az = new float[columns + 1];
         float[] foot = new float[columns + 1];
         float[] mid = new float[columns + 1];
         float[] top = new float[columns + 1];
         int[] alpha = new int[columns + 1];
         for (int i = 0; i <= columns; i++) {
            float t = (float)i / columns;
            float wave = (float)Math.sin(t * 6.0F + anim * speed + phase);
            az[i] = centre + (t - 0.5F) * span + (float)Math.toRadians(4.0) * wave;
            foot[i] = baseElevation + (float)Math.toRadians(2.0) * (float)Math.sin(t * 3.0F + anim * speed * 0.5F + phase * 2.0F);
            top[i] = foot[i] + height * (0.75F + 0.25F * (float)Math.sin(t * 5.0F + anim * speed * 1.3F + phase * 3.0F));
            mid[i] = foot[i] + (top[i] - foot[i]) * 0.35F;
            float edge = (float)Math.sin(Math.PI * t);
            float shimmer = 0.7F + 0.3F * (float)Math.sin(anim * 0.05F + t * 10.0F + band);
            alpha[i] = Math.min(255, Math.round(strength * edge * shimmer * 255.0F));
         }
         int footColour = mix(GREEN, TEAL, 0.5F + hueShift);
         int midColour = mix(TEAL, VIOLET, 0.35F + hueShift);
         for (int i = 0; i < columns; i++) {
            if (alpha[i] == 0 && alpha[i + 1] == 0) {
               continue;
            }
            // foot to middle: bright
            written += quad(b,
               az[i], foot[i], footColour, alpha[i],
               az[i + 1], foot[i + 1], footColour, alpha[i + 1],
               az[i + 1], mid[i + 1], midColour, alpha[i + 1],
               az[i], mid[i], midColour, alpha[i]);
            // middle to top: fading into violet
            written += quad(b,
               az[i], mid[i], midColour, alpha[i],
               az[i + 1], mid[i + 1], midColour, alpha[i + 1],
               az[i + 1], top[i + 1], VIOLET, 0,
               az[i], top[i], VIOLET, 0);
         }
      }
      return written;
   }

   private static int quad(BufferBuilder b, float az0, float el0, int c0, int a0, float az1, float el1, int c1, int a1,
                           float az2, float el2, int c2, int a2, float az3, float el3, int c3, int a3) {
      vertex(b, az0, el0, c0, a0);
      vertex(b, az1, el1, c1, a1);
      vertex(b, az2, el2, c2, a2);
      vertex(b, az0, el0, c0, a0);
      vertex(b, az2, el2, c2, a2);
      vertex(b, az3, el3, c3, a3);
      return 6;
   }

   /** A point on the sky sphere; azimuth 0 is north (-Z), positive towards east (+X). */
   private static void vertex(BufferBuilder b, float azimuth, float elevation, int rgb, int alpha) {
      float cos = (float)Math.cos(elevation);
      b.addVertex(RADIUS * cos * (float)Math.sin(azimuth), RADIUS * (float)Math.sin(elevation), -RADIUS * cos * (float)Math.cos(azimuth))
         .setColor(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, alpha);
   }

   private static int mix(int a, int b, float t) {
      t = Math.max(0.0F, Math.min(1.0F, t));
      int r = Math.round((a >> 16 & 0xFF) + ((b >> 16 & 0xFF) - (a >> 16 & 0xFF)) * t);
      int g = Math.round((a >> 8 & 0xFF) + ((b >> 8 & 0xFF) - (a >> 8 & 0xFF)) * t);
      int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * t);
      return r << 16 | g << 8 | bl;
   }

   // ------------------------------------------------- meteors and shooting stars

   /** A meteor shower night: 30 meteors (same timing as before), each crossing about 30 degrees of sky in 4 s. */
   private static int meteors(BufferBuilder b, long ticks, float partial, float intensity) {
      long day = ticks / 24000L;
      long tickOfDay = ticks % 24000L;
      if (tickOfDay < 13000L || tickOfDay >= 23000L) {
         return 0;
      }
      int written = 0;
      Random rng = new Random(day * 31337L + 7919L);
      for (int i = 0; i < 30; i++) {
         long start = 13000L + (long)(rng.nextFloat() * 9000.0F);
         float azimuth = rng.nextFloat() * (float)(Math.PI * 2);
         float elevation = (float)Math.toRadians(35.0 + rng.nextFloat() * 40.0);
         float drift = (float)Math.toRadians((rng.nextFloat() - 0.5F) * 30.0F);
         float speed = 1.0F + rng.nextFloat() * 1.5F;
         float t = (tickOfDay - start + partial) / 80.0F;
         if (t >= 0.0F && t < 1.0F) {
            float alpha = (t < 0.15F ? t / 0.15F : t > 0.75F ? (1.0F - t) / 0.25F : 1.0F) * intensity;
            float travel = (float)Math.toRadians(30.0) * speed * t;
            written += streak(b, azimuth + drift * t, elevation - travel, azimuth + drift * (t - 0.12F), elevation - travel + (float)Math.toRadians(6.0) * speed,
               (float)Math.toRadians(0.25), Math.round(alpha * 240.0F));
         }
      }
      return written;
   }

   /** Up to 8 shooting stars a night (same slots as before): short, fast, faint. */
   private static int shootingStars(BufferBuilder b, long ticks, float partial, float fade) {
      long day = ticks / 24000L;
      long tickOfDay = ticks % 24000L;
      if (tickOfDay < 13000L || tickOfDay >= 23000L) {
         return 0;
      }
      int written = 0;
      for (int slot = 0; slot < 8; slot++) {
         Random rng = new Random(day * 97L + slot * 13L + 4127L);
         if (rng.nextFloat() > 0.35F) {
            continue;
         }
         long start = 13000L + slot * 1250L;
         float t = (tickOfDay - start + partial) / 25.0F;
         if (t >= 0.0F && t < 1.0F) {
            float alpha = (t < 0.2F ? t / 0.2F : t > 0.7F ? (1.0F - t) / 0.3F : 1.0F) * fade * 0.65F;
            float azimuth = rng.nextFloat() * (float)(Math.PI * 2);
            float elevation = (float)Math.toRadians(30.0 + rng.nextFloat() * 45.0);
            float sweep = (float)Math.toRadians(15.0 + rng.nextFloat() * 15.0);
            float az = azimuth + sweep * t;
            float el = elevation - sweep * 0.5F * t;
            written += streak(b, az, el, az - (float)Math.toRadians(4.0), el + (float)Math.toRadians(2.0),
               (float)Math.toRadians(0.15), Math.round(alpha * 210.0F));
         }
      }
      return written;
   }

   /** A thin streak from a bright head to a faded tail, facing the viewer at the sky's centre. */
   private static int streak(BufferBuilder b, float headAz, float headEl, float tailAz, float tailEl, float halfWidth, int alpha) {
      if (alpha <= 0) {
         return 0;
      }
      Vector3f head = point(headAz, headEl);
      Vector3f tail = point(tailAz, tailEl);
      Vector3f side = new Vector3f(head).sub(tail).cross(head).normalize().mul(RADIUS * halfWidth);
      int a = Math.min(255, alpha);
      b.addVertex(tail.x - side.x, tail.y - side.y, tail.z - side.z).setColor(255, 255, 255, 0);
      b.addVertex(tail.x + side.x, tail.y + side.y, tail.z + side.z).setColor(255, 255, 255, 0);
      b.addVertex(head.x + side.x, head.y + side.y, head.z + side.z).setColor(255, 255, 255, a);
      b.addVertex(tail.x - side.x, tail.y - side.y, tail.z - side.z).setColor(255, 255, 255, 0);
      b.addVertex(head.x + side.x, head.y + side.y, head.z + side.z).setColor(255, 255, 255, a);
      b.addVertex(head.x - side.x, head.y - side.y, head.z - side.z).setColor(255, 255, 255, a);
      return 6;
   }

   private static Vector3f point(float azimuth, float elevation) {
      float cos = (float)Math.cos(elevation);
      return new Vector3f(RADIUS * cos * (float)Math.sin(azimuth), RADIUS * (float)Math.sin(elevation), -RADIUS * cos * (float)Math.cos(azimuth));
   }
}
