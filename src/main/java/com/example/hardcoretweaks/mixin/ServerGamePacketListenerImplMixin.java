package com.example.hardcoretweaks.mixin;

import com.example.hardcoretweaks.HardcoreTweaks;
import com.example.hardcoretweaks.HardcoreWorldData;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Inject(method = "handleClientCommand", at = @At("TAIL"))
    private void hardcoreTweaks$onRespawn(ServerboundClientCommandPacket packet, CallbackInfo ci) {
        if (packet.getAction() == ServerboundClientCommandPacket.Action.PERFORM_RESPAWN) {
            if (this.player.level().getServer().isHardcore()) {
                HardcoreWorldData data = HardcoreWorldData.get(this.player.level());
                int deaths = data.getPlayerDeaths(this.player.getUUID());
                
                boolean shouldSpectate = true;
                if (data.deathLimitActive) {
                    if (deaths < data.deathLimitBase + data.deathLimitValue) {
                        shouldSpectate = false;
                    }
                } else {
                    shouldSpectate = false; // No limit, infinite lives
                }

                if (!shouldSpectate) {
                    // Vanilla just set them to spectator. Let's revert them!
                    this.player.setGameMode(this.player.level().getServer().getDefaultGameType());
                    // Re-sync data to ensure they see their new death count
                    HardcoreTweaks.syncDataToPlayer(this.player);
                }
            }
        }
    }
}
