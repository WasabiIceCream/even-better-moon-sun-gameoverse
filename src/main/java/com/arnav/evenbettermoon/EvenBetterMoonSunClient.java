package com.arnav.evenbettermoon;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class EvenBetterMoonSunClient implements ClientModInitializer {
   /**
    * Gameoverse: the sky commands are real client commands (autocomplete, no red "unknown
    * command" while typing). The original caught them by intercepting chat messages.
    */
   public void onInitializeClient() {
      ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
         dispatcher.register(literal("astro").executes(c -> run(c, "/astro")));
         dispatcher.register(literal("eclipse").executes(c -> run(c, "/eclipse")));
         dispatcher.register(literal("year").executes(c -> run(c, "/year")));
         dispatcher.register(intervalCommand("bloodmoon", "days"));
         dispatcher.register(intervalCommand("meteorshower", "days"));
         dispatcher.register(literal("aurora")
            .executes(c -> run(c, "/aurora"))
            .then(literal("reset").executes(c -> run(c, "/aurora reset")))
            .then(literal("set").then(argument("multiplier", DoubleArgumentType.doubleArg(0))
               .executes(c -> run(c, "/aurora set " + DoubleArgumentType.getDouble(c, "multiplier"))))));
      });
   }

   private static LiteralArgumentBuilder<FabricClientCommandSource> intervalCommand(String name, String arg) {
      return literal(name)
         .executes(c -> run(c, "/" + name))
         .then(literal("reset").executes(c -> run(c, "/" + name + " reset")))
         .then(literal("set").then(argument(arg, IntegerArgumentType.integer(0))
            .executes(c -> run(c, "/" + name + " set " + IntegerArgumentType.getInteger(c, arg)))));
   }

   private static int run(CommandContext<FabricClientCommandSource> context, String command) {
      var lines = SkyCommandHandler.handle(command);
      if (lines != null) {
         for (String line : lines) {
            context.getSource().sendFeedback(Component.literal(line));
         }
      }
      return 1;
   }
}
