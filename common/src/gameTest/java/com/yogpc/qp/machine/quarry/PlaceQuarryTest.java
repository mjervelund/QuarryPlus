package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.gametest.GameTestFunctions;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.misc.QuarryChunkLoader;
import com.yogpc.qp.machine.module.ModuleInventoryHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.TagValueInput;

import static org.junit.jupiter.api.Assertions.*;

public final class PlaceQuarryTest {
    static final BlockPos base = BlockPos.ZERO.above();

    public static void place(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        assertInstanceOf(QuarryBlock.class, helper.getBlockState(base).getBlock());
        assertInstanceOf(QuarryEntity.class, helper.getBlockEntity(base, QuarryEntity.class));
        helper.succeed();
    }

    public static void checkDropNormal(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base, QuarryEntity.class));
        assertFalse(drop.isEmpty());
        var quarryStack = drop.getFirst();
        assertInstanceOf(QuarryItem.class, quarryStack.getItem());
        assertFalse(quarryStack.isEnchanted());
        helper.succeed();
    }

    public static void checkDropEnchanted(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 3);
        helper.getBlockEntity(base, QuarryEntity.class).setEnchantments(mutable.toImmutable());

        var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base, QuarryEntity.class));
        assertFalse(drop.isEmpty());
        var quarryStack = drop.getFirst();
        assertInstanceOf(QuarryItem.class, quarryStack.getItem());
        assertTrue(quarryStack.isEnchanted());
        assertEquals(1, quarryStack.getEnchantments().size());
        assertEquals(3, quarryStack.getEnchantments().getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        helper.succeed();
    }

    public static void checkDropEnchanted2(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 5);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING), 3);
        helper.getBlockEntity(base, QuarryEntity.class).setEnchantments(mutable.toImmutable());

        var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base, QuarryEntity.class));
        var quarryStack = drop.getFirst();
        assertInstanceOf(QuarryItem.class, quarryStack.getItem());
        assertTrue(quarryStack.isEnchanted());
        assertEquals(2, quarryStack.getEnchantments().size());
        assertEquals(5, quarryStack.getEnchantments().getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        assertEquals(3, quarryStack.getEnchantments().getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING)));
        helper.succeed();
    }

    public static void saveEnchantment(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(base, QuarryEntity.class);

        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 5);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING), 3);
        quarry.setEnchantments(mutable.toImmutable());

        var saved = quarry.saveWithFullMetadata(helper.getLevel().registryAccess());
        quarry.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), saved));

        var enchantments = quarry.getEnchantments();
        assertEquals(5, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        assertEquals(3, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING)));
        assertEquals(0, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.FORTUNE)));

        helper.succeed();
    }

    public static void placeQuarry1(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.placeAt(player, stack, base.below(), Direction.UP);
        helper.assertBlockPresent(PlatformAccess.getAccess().registerObjects().quarryBlock().get(), base);

        QuarryEntity quarry = helper.getBlockEntity(base, QuarryEntity.class);
        var enchantments = quarry.getEnchantments();
        assertTrue(enchantments.isEmpty());
        helper.succeed();
    }

    public static void placeQuarry2(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        stack.enchant(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 3);
        stack.enchant(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.placeAt(player, stack, base.below(), Direction.UP);
        helper.assertBlockPresent(PlatformAccess.getAccess().registerObjects().quarryBlock().get(), base);

        QuarryEntity quarry = helper.getBlockEntity(base, QuarryEntity.class);
        var enchantments = quarry.getEnchantments();
        assertFalse(enchantments.isEmpty());
        assertEquals(3, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        assertEquals(3, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING)));
        assertEquals(0, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.SHARPNESS)));

        assertEquals(PlatformAccess.config().powerMap().quarry().maxEnergy() * 4 * PowerEntity.ONE_FE, quarry.getMaxEnergy());

        helper.succeed();
    }

    public static void accessModuleInventory(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(base, QuarryEntity.class);
        var moduleInv = ModuleInventoryHolder.getFromObject(quarry);
        assertTrue(moduleInv.isPresent());
        assertTrue(moduleInv.get().getModules().isEmpty());

        helper.succeed();
    }

    public static void handOverChunkLoading(GameTestHelper helper) {
        var level = helper.getLevel();
        var firstPos = base.east();
        var secondPos = (helper.absolutePos(firstPos).getX() & 15) == 15 ? base : base.east(2);
        var chunkPos = ChunkPos.containing(helper.absolutePos(firstPos));
        assertEquals(chunkPos, ChunkPos.containing(helper.absolutePos(secondPos)));
        // The test instance block entity force-loads every chunk of the structure (26.x), so this
        // chunk is already in the forced set, which is exactly the situation the handover fixes:
        // a quarry that starts working in an already force-loaded chunk gets None.
        assertTrue(level.getForceLoadedChunks().contains(chunkPos.pack()));

        helper.setBlock(firstPos, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        helper.setBlock(secondPos, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity first = helper.getBlockEntity(firstPos, QuarryEntity.class);
        QuarryEntity second = helper.getBlockEntity(secondPos, QuarryEntity.class);

        first.setState(QuarryState.MOVE_HEAD, helper.getBlockState(firstPos));
        second.setState(QuarryState.MOVE_HEAD, helper.getBlockState(secondPos));
        assertEquals(QuarryChunkLoader.None.INSTANCE, first.getChunkLoader());
        assertEquals(QuarryChunkLoader.None.INSTANCE, second.getChunkLoader());
        // Give the first quarry the chunk load, as if it had forced the chunk when it started
        first.setChunkLoader(new QuarryChunkLoader.Load(helper.absolutePos(firstPos)));

        // The first one finishes: the chunk load is handed to the second one instead of being removed
        first.setState(QuarryState.FINISHED, helper.getBlockState(firstPos));
        assertTrue(level.getForceLoadedChunks().contains(chunkPos.pack()));
        assertEquals(new QuarryChunkLoader.Load(helper.absolutePos(secondPos)), second.getChunkLoader());

        // The second one finishes: no working machine is left, the forced load is removed
        second.setState(QuarryState.FINISHED, helper.getBlockState(secondPos));
        assertFalse(level.getForceLoadedChunks().contains(chunkPos.pack()));
        // Restore the test structure's forced chunk: tests in this batch whose structure shares the
        // chunk stop ticking without it and the batch never finishes
        level.setChunkForced(chunkPos.x(), chunkPos.z(), true);
        helper.succeed();
    }
}
