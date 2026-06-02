package com.example.hardcoretweaks.mixin;

import com.example.hardcoretweaks.HardcoreWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(method = "respawn", at = @At("RETURN"), cancellable = true)
    private void hardcoreTweaks$onRespawnReturn(ServerPlayer oldPlayer, boolean keepEverything, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer newPlayer = cir.getReturnValue();
        if (newPlayer != null && newPlayer.level().getServer().isHardcore()) {
            HardcoreWorldData data = HardcoreWorldData.get(newPlayer.level());
            if (data.spawnPenalty) {
                // Check if they are allowed to use set spawn and they have one
                boolean hasRespawnPos = oldPlayer.getRespawnConfig() != null && oldPlayer.getRespawnConfig().respawnData() != null;
                if (data.allowSetSpawn && hasRespawnPos) {
                    return; // Skip random spawn, they have a bed/anchor!
                }

                // Random spawn within range
                Random random = new Random();
                int range = data.spawnPenaltyRange;
                ServerLevel level = newPlayer.level();
                // getX() and getZ() from RespawnData pos
                int sx = level.getRespawnData().pos().getX();
                int sz = level.getRespawnData().pos().getZ();

                int dx = range == 0 ? 0 : random.nextInt(range * 2 + 1) - range;
                int dz = range == 0 ? 0 : random.nextInt(range * 2 + 1) - range;
                
                int rx = sx + dx;
                int rz = sz + dz;

                int ry = level.getMaxY() - 1;
                while (ry > level.getMinY()) {
                    BlockPos pos = new BlockPos(rx, ry, rz);
                    BlockState state = level.getBlockState(pos);
                    
                    boolean isSolid = !state.getCollisionShape(level, pos).isEmpty();
                    boolean isLiquid = !state.getFluidState().isEmpty();
                    
                    if (isSolid || isLiquid) {
                        ry++;
                        break;
                    }
                    ry--;
                }

                newPlayer.setPos(rx + 0.5, ry, rz + 0.5);
                newPlayer.connection.teleport(rx + 0.5, ry, rz + 0.5, newPlayer.getYRot(), newPlayer.getXRot());
            }
        }
    }
}
