package com.example.hardcoretweaks.mixin.client;

import com.example.hardcoretweaks.HardcoreSettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {
   @Shadow
   private Button disconnectButton;

   @Inject(method = "init", at = @At("TAIL"))
   private void hardcoreTweaks$addSettingsButton(CallbackInfo ci) {
      if (this.disconnectButton == null) {
          return;
      }
      
      Minecraft client = Minecraft.getInstance();
      // We now show the button in all worlds, but gray out options in HardcoreSettingsScreen if not hardcore.
      if (client.level == null) {
          return;
      }

      Screen self = (Screen) (Object) this;
      int btnW = 204;
      int btnH = 20;
      int btnX = (self.width - btnW) / 2;
      int origDisconnectY = this.disconnectButton.getY();
      
      // Safely shift the disconnect button down to make room for our button
      this.disconnectButton.setY(origDisconnectY + 24);
      
      ((ScreenInvoker)self)
         .invokeAddRenderableWidget(
            Button.builder(
                  Component.literal("Hardcore Settings"),
                  btn -> client.setScreen(new HardcoreSettingsScreen(self))
               )
               .bounds(btnX, origDisconnectY, btnW, btnH)
               .build()
         );
   }
}
