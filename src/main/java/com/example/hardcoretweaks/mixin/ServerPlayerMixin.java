package com.example.hardcoretweaks.mixin;

import com.example.hardcoretweaks.HardcoreTweaks;
import com.example.hardcoretweaks.HardcoreWorldData;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @Shadow public ServerGamePacketListenerImpl connection;
    @Shadow public abstract boolean setGameMode(net.minecraft.world.level.GameType gameMode);

    @Unique
    private int hardcoreTweaks$respawnTimer = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void hardcoreTweaks$autoRespawnTick(CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        if (self.isDeadOrDying() && self.level().getServer().isHardcore()) {
            hardcoreTweaks$respawnTimer++;
            if (hardcoreTweaks$respawnTimer >= 100) { // 5 seconds (20 ticks/sec * 5)
                if (this.connection != null) {
                    this.connection.handleClientCommand(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.PERFORM_RESPAWN));
                }
            }
        } else {
            hardcoreTweaks$respawnTimer = 0;
        }
    }

    @Inject(method = "die", at = @At("TAIL"))
    private void hardcoreTweaks$onDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        if (self.level().getServer().isHardcore()) {
            HardcoreWorldData data = HardcoreWorldData.get(self.level());
            data.addPlayerDeath(self.getUUID());
            
            if (data.deathLimitActive) {
                int currentDeaths = data.getPlayerDeaths(self.getUUID());
                int threshold = data.deathLimitBase + data.deathLimitValue;
                if (currentDeaths >= threshold) {
                    self.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                }
            }
            
            HardcoreTweaks.syncDataToPlayer(self); // Sync the new death count!
        }
    }
}
