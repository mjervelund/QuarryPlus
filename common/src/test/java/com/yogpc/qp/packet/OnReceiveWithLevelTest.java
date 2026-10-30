package com.yogpc.qp.packet;

import com.yogpc.qp.BeforeMC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnReceiveWithLevelTest extends BeforeMC {
    private static final BlockPos MACHINE = new BlockPos(10, 64, 10);

    private static Level level(boolean loaded) {
        Level level = mock(Level.class);
        when(level.isLoaded(any())).thenReturn(loaded);
        when(level.dimension()).thenReturn(Level.OVERWORLD);
        return level;
    }

    private static Player playerAt(double x, double y, double z) {
        Player player = mock(Player.class);
        when(player.position()).thenReturn(new Vec3(x, y, z));
        return player;
    }

    @Test
    void acceptsLoadedMachineInReach() {
        assertTrue(OnReceiveWithLevel.canAccess(level(true), playerAt(12.5, 64, 12.5), MACHINE));
    }

    @Test
    void rejectsFarAwayMachine() {
        assertFalse(OnReceiveWithLevel.canAccess(level(true), playerAt(10.5, 64, 30.5), MACHINE));
    }

    @Test
    void rejectsUnloadedMachine() {
        assertFalse(OnReceiveWithLevel.canAccess(level(false), playerAt(10.5, 64, 10.5), MACHINE));
    }

    @Test
    void farMessageNeverLooksUpTheBlockEntity() {
        // Level.getBlockEntity loads (and generates) the chunk, so a rejected message must not reach it
        Level level = level(false);
        new YSetterMessage(new BlockPos(1_000_000, 64, 1_000_000), Level.OVERWORLD, 0).onReceive(level, playerAt(0, 64, 0));
        verify(level, never()).getBlockEntity(any());
    }
}
