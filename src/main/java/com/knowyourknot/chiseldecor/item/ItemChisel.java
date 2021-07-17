package com.knowyourknot.chiseldecor.item;

import com.knowyourknot.chiseldecor.ChiselDecorEntryPoint;
import com.knowyourknot.chiseldecor.block.ChiselGroupLookup;
import com.knowyourknot.chiseldecor.gui.ChiselScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.TagGroup;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.List;

public class ItemChisel extends Item implements NamedScreenHandlerFactory {
    private ChiselGroupLookup.ChiselGroup group;
    private int index = 0;


    public ItemChisel(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        Item item = state.getBlock().asItem();
        if (!(item instanceof BlockItem)) {
            ChiselDecorEntryPoint.LOGGER.info("How is " + item.getName().getString() +" not an blockItem?");
            return ActionResult.PASS;
        }


        TagGroup<Item> itemTags = ServerTagManagerHolder.getTagManager().getOrCreateTagGroup(Registry.ITEM_KEY);
        ChiselGroupLookup.ChiselGroup group = ChiselGroupLookup.getGroup(item, itemTags);
        List<Item> chiselBlocks = ChiselGroupLookup.getBlocksInGroup(item, itemTags);
        if (context.getPlayer().isSneaking()) {
            ChiselDecorEntryPoint.LOGGER.debug("saving group " + item.getName().getString());
            this.group = group;
            index = chiselBlocks.indexOf(item);
            return ActionResult.CONSUME;
        }

        if (this.group == group) {
            item = chiselBlocks.get(index);
            ChiselDecorEntryPoint.LOGGER.debug("Applying group " + item.getName().getString());

            ItemPlacementContext ctx = new ItemPlacementContext(
                    context.getPlayer(),
                    Hand.MAIN_HAND,
                    new ItemStack(item),
                    new BlockHitResult(context.getHitPos(), Direction.DOWN, context.getBlockPos(), false)
            );
            ctx.canReplaceExisting = true;
            ((BlockItem)item).place(ctx);

            return ActionResult.CONSUME;
        }

        return ActionResult.PASS;
    }

    @Override
    public boolean canMine(BlockState state, World world, BlockPos pos, PlayerEntity miner) {
        if (!world.isClient) {
            Item item = state.getBlock().asItem();
            if (!(item instanceof BlockItem)) {
                ChiselDecorEntryPoint.LOGGER.info("How is " + item.getName().getString() +" not an blockItem?");
                return true;
            }
            TagGroup<Item> itemTags = ServerTagManagerHolder.getTagManager().getOrCreateTagGroup(Registry.ITEM_KEY);
            List<Item> chiselBlocks = ChiselGroupLookup.getBlocksInGroup(item, itemTags);

            if (chiselBlocks.isEmpty()) {
                return true;
            }

            int index = chiselBlocks.indexOf(item);
            boolean direction = miner.isSneaking();
            if (!miner.isSneaking()) {
                index += 1;
            } else {
                index -= 1;
            }
            if (index >= chiselBlocks.size()) {
                index = 0;
            } else if (index < 0) {
                index = chiselBlocks.size() - 1;
            }

            item = chiselBlocks.get(index);
            ChiselDecorEntryPoint.LOGGER.debug("replace with: " + item.getName().getString());
            ItemPlacementContext ctx = new ItemPlacementContext(
                    miner,
                    Hand.MAIN_HAND,
                    new ItemStack(item),
                    new BlockHitResult(miner.getPos(), Direction.DOWN, pos, false)
            );
            ctx.canReplaceExisting = true;
            ((BlockItem)item).place(ctx);
        }
        return false;
    }

    @Override
    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        Item item = state.getBlock().asItem();
        if (!(item instanceof BlockItem)) {
            ChiselDecorEntryPoint.LOGGER.info("How is " + item.getName().getString() +" not an blockItem?");
            return 1.0f;
        }
        TagGroup<Item> itemTags = ServerTagManagerHolder.getTagManager().getOrCreateTagGroup(Registry.ITEM_KEY);
        List<Item> chiselBlocks = ChiselGroupLookup.getBlocksInGroup(item, itemTags);
        if (chiselBlocks.isEmpty()) {
            return 1.0f;
        }
        return state.getBlock().getHardness() * 50.0f;
    }


    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (user.isSneaking()) {
            this.group = null;
            return TypedActionResult.consume(user.getStackInHand(hand));
        }
        super.use(world, user, hand);
        user.openHandledScreen(this);
        return TypedActionResult.pass(user.getStackInHand(hand));
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ChiselScreenHandler(syncId, inv);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("gui.chiseldecor.chisel");
    }
    
}
