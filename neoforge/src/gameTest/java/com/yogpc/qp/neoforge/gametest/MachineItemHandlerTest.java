package com.yogpc.qp.neoforge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The quarry exposes its output as an extract-only item handler, so item pipes connect to it.
 */
public final class MachineItemHandlerTest {
    static List<TestFunction> tests() {
        var instance = new MachineItemHandlerTest();
        return List.of(
            TestFunction.create(QuarryPlus.modID, "MachineItemHandlerTestExtract", instance::extract),
            TestFunction.create(QuarryPlus.modID, "MachineItemHandlerTestNoInsert", instance::noInsert),
            TestFunction.create(QuarryPlus.modID, "MachineItemHandlerTestExtractOtherResource", instance::extractOtherResource)
        );
    }

    private static Pair<MachineStorage, ResourceHandler<ItemResource>> getHandler(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(pos, QuarryEntity.class);
        MachineStorage storage = MachineStorageHolder.getHolder(quarry).orElseThrow().getMachineStorage(quarry);

        var handler = helper.getLevel().getCapability(Capabilities.Item.BLOCK, helper.absolutePos(pos), null, quarry, null);
        assertNotNull(handler, "Quarry should expose an item handler");
        return Pair.of(storage, handler);
    }

    public void extract(GameTestHelper helper) {
        var pair = getHandler(helper, BlockPos.ZERO.above());
        MachineStorage storage = pair.getKey();
        var handler = pair.getValue();
        storage.addItem(new ItemStack(Items.COBBLESTONE, 10));

        assertEquals(1, handler.size());
        assertEquals(ItemResource.of(Items.COBBLESTONE), handler.getResource(0));
        assertEquals(10, handler.getAmountAsLong(0));

        try (var tx = Transaction.openRoot()) {
            assertEquals(4, handler.extract(0, ItemResource.of(Items.COBBLESTONE), 4, tx));
        }
        assertEquals(10, handler.getAmountAsLong(0), "Not committed, so nothing should be taken");

        try (var tx = Transaction.openRoot()) {
            assertEquals(4, handler.extract(0, ItemResource.of(Items.COBBLESTONE), 4, tx));
            tx.commit();
        }
        assertEquals(6, handler.getAmountAsLong(0));

        helper.succeed();
    }

    public void noInsert(GameTestHelper helper) {
        var pair = getHandler(helper, BlockPos.ZERO.above());
        MachineStorage storage = pair.getKey();
        var handler = pair.getValue();
        storage.addItem(new ItemStack(Items.COBBLESTONE, 10));

        assertFalse(handler.isValid(0, ItemResource.of(Items.COBBLESTONE)));
        try (var tx = Transaction.openRoot()) {
            assertEquals(0, handler.insert(0, ItemResource.of(Items.COBBLESTONE), 5, tx));
            assertEquals(0, handler.insert(ItemResource.of(Items.DIRT), 5, tx));
            tx.commit();
        }
        assertEquals(1, handler.size());
        assertEquals(10, handler.getAmountAsLong(0));

        helper.succeed();
    }

    public void extractOtherResource(GameTestHelper helper) {
        var pair = getHandler(helper, BlockPos.ZERO.above());
        MachineStorage storage = pair.getKey();
        var handler = pair.getValue();
        storage.addItem(new ItemStack(Items.COBBLESTONE, 10));

        try (var tx = Transaction.openRoot()) {
            assertEquals(0, handler.extract(0, ItemResource.of(Items.DIRT), 4, tx));
            tx.commit();
        }
        assertEquals(10, handler.getAmountAsLong(0));

        helper.succeed();
    }
}
