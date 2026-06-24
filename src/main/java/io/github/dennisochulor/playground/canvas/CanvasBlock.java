package io.github.dennisochulor.playground.canvas;

import com.mojang.serialization.MapCodec;
import io.github.dennisochulor.playground.Playground;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.SelectableSlotContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.OptionalInt;

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
        if (itemStack.getItem() != Items.STICK) return InteractionResult.PASS;

        if (level.getBlockEntity(pos) instanceof CanvasBlockEntity canvas) {
            Direction dir = hitResult.getDirection();
            OptionalInt pixel = this.getHitSlot(hitResult, dir);

            if (pixel.isEmpty()) return InteractionResult.PASS;

            int color = Color.BLACK.getRGB();
            canvas.setPixel(dir, pixel.getAsInt(), color);
            Playground.LOGGER.info("Set side {} at {} to {}", dir, pixel.getAsInt(), color);

            return InteractionResult.SUCCESS_SERVER;
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
