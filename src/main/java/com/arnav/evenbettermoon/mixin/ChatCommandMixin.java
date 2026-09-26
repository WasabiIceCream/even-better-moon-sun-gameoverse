package com.arnav.evenbettermoon.mixin;

import com.arnav.evenbettermoon.SkyCommandHandler;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatCommandMixin {
   @Inject(method = "handleChatInput", at = @At("HEAD"), cancellable = true)
   private void interceptSkyCommands(String msg, boolean addToRecent, CallbackInfo ci) {
      String t = msg.trim().toLowerCase();
      if (t.equals("/eclipse")
         || t.startsWith("/eclipse ")
         || t.equals("/bloodmoon")
         || t.startsWith("/bloodmoon ")
         || t.equals("/meteorshower")
         || t.startsWith("/meteorshower ")
         || t.equals("/year")
         || t.startsWith("/year ")
         || t.equals("/aurora")
         || t.startsWith("/aurora ")
         || t.equals("/astro")) {
         List<String> lines = SkyCommandHandler.handle(msg.trim());
         if (lines != null) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui != null) {
               for (String line : lines) {
                  mc.gui.getChat().addClientSystemMessage(Component.literal(line));
               }
            }

            ci.cancel();
         }
      }
   }
}
