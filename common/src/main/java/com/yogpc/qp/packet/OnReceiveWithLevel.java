package com.yogpc.qp.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface OnReceiveWithLevel {
    void onReceive(Level level, Player player);

    /**
     * Checks a client message that targets the machine at {@code pos}.
     * The chunk must already be loaded, so a message can't load or generate chunks,
     * and the player must be in the same 8 block range that the machine menus allow.
     */
    static boolean canAccess(Level level, Player player, BlockPos pos) {
        return level.isLoaded(pos) && pos.closerToCenterThan(player.position(), 8);
    }
}
