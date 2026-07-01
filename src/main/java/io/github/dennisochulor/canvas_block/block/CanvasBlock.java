package io.github.dennisochulor.canvas_block.block;

import com.mojang.serialization.MapCodec;
import io.github.dennisochulor.canvas_block.CanvasMod;
import io.github.dennisochulor.canvas_block.item.ModComponents;
import io.github.dennisochulor.canvas_block.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SelectableSlotContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.awt.Color;

public class CanvasBlock extends BaseEntityBlock implements SelectableSlotContainer {
    public static final int SIZE = 16;
    public static final int DEFAULT_COLOR = Color.WHITE.getRGB();

    public CanvasBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(CanvasBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new CanvasBlockEntity(worldPosition, blockState);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.PASS;
        if (itemStack.getItem() != ModItems.PAINT_BRUSH) return InteractionResult.PASS;

        if (level.getBlockEntity(pos) instanceof CanvasBlockEntity canvas) {
            Direction dir = hitResult.getDirection();
            int pixelIndex = this.getHitSlot(hitResult, dir).orElse(-1);

            if (pixelIndex == -1) {
                CanvasMod.LOGGER.warn("Attempt to paint non-existant pixel!\n{} / {} / {}", canvas, dir, hitResult);
                return InteractionResult.PASS;
            }

            Integer color = itemStack.getComponents().get(ModComponents.RGB_COLOR);
            boolean emissive = itemStack.has(ModComponents.EMISSIVE);

            if (color == null) {
                return InteractionResult.PASS;
            }

            if (color == canvas.getPixelColor(dir, pixelIndex) && emissive == canvas.isEmissive(dir, pixelIndex)) {
                return InteractionResult.PASS;
            }

            canvas.setPixel(dir, pixelIndex, color, emissive);
            itemStack.hurtAndBreak(1, player, hand);
            //CanvasMod.LOGGER.info("Set side {} at {} to {} {}", dir, pixelIndex, color, emissive);

            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public int getRows() {
        return SIZE;
    }

    @Override
    public int getColumns() {
        return SIZE;
    }
}
