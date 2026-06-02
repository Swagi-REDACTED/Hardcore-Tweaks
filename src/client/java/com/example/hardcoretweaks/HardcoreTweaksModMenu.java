package com.example.hardcoretweaks;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HardcoreTweaksModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            Minecraft client = Minecraft.getInstance();
            if (client.level != null && client.level.getLevelData().isHardcore()) {
                if (HardcoreTweaksClient.LATEST_SYNC_DATA != null) {
                    return new HardcoreSettingsScreen(parent);
                } else {
                    return new ErrorScreen(parent, "Still syncing data with server...");
                }
            } else {
                return new ErrorScreen(parent, "Not in Game. Please open in a Hardcore world.");
            }
        };
    }

    private static class ErrorScreen extends Screen {
        private final Screen parent;
        private final String message;

        protected ErrorScreen(Screen parent, String message) {
            super(Component.literal("Error"));
            this.parent = parent;
            this.message = message;
        }

        public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
            gfx.centeredText(this.font, this.message, this.width / 2, this.height / 2, 0xFFFF5555);
            super.extractRenderState(gfx, mouseX, mouseY, delta);
        }

        @Override
        public void onClose() {
            if (this.minecraft != null) {
                this.minecraft.setScreen(this.parent);
            }
        }
    }
}
