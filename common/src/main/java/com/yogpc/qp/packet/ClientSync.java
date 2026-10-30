package com.yogpc.qp.packet;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ClientSync {
    void fromClientTag(ValueInput input);

    ValueOutput toClientTag(ValueOutput output);

    @SuppressWarnings("unchecked") // Checked. Safe
    default <T extends BlockEntity & ClientSync> void syncToClient() {
        if (this instanceof BlockEntity entity) {
            var level = entity.getLevel();
            if (level instanceof ServerLevel serverLevel) {
                // Only players tracking this chunk have the block entity loaded, so only they can use the message.
                // Quarries sync every tick while moving the head, so sending to the whole dimension does not scale.
                var players = serverLevel.getChunkSource().chunkMap.getPlayers(ChunkPos.containing(entity.getBlockPos()), false);
                if (players.isEmpty()) {
                    return;
                }
                var clientSyncMessage = new ClientSyncMessage((T) this);
                for (var player : players) {
                    PlatformAccess.getAccess().packetHandler().sendToClientPlayer(clientSyncMessage, player);
                }
            }
        }
    }
}
