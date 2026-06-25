package io.github.dennisochulor.playground.canvas;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class CanvasBlockEntity extends BlockEntity {
    private final int[] @Nullable [] sides = new int[6][];

    public CanvasBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(CanvasMod.CANVAS_BLOCK_ENTITY, worldPosition, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        for (Direction dir : Direction.values()) {
            int[] sideArr = sides[dir.ordinal()];

            if (sideArr != null) output.putIntArray(dir.getName(), sideArr);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (Direction dir : Direction.values()) {
            input.getIntArray(dir.getName()).ifPresent(side -> sides[dir.ordinal()] = side);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return saveWithoutMetadata(registryLookup); // initial sync
    }


    public void setPixel(Direction side, int x, int y, int color) {
        setPixel(side, index(x, y), color);
    }

    public void setPixel(Direction side, int index, int color) {
        int[] sideArr = sides[side.ordinal()];

        if (sideArr == null) {
            sideArr = new int[CanvasBlock.SIZE * CanvasBlock.SIZE];
            Arrays.fill(sideArr, CanvasBlock.DEFAULT_COLOR);
            sides[side.ordinal()] = sideArr;
        }

        sideArr[index] = color;

        if (!Objects.requireNonNull(level).isClientSide()) {
            this.setChanged();

            // incremental sync
            ClientboundCanvasUpdatePacket packet = new ClientboundCanvasUpdatePacket(this.getBlockPos(), side, index, color);
            PlayerLookup.tracking(this).forEach(player -> ServerPlayNetworking.send(player, packet));
        }
    }

    public int getPixel(Direction side, int x, int y) {
        return getPixel(side, index(x, y));
    }

    public int getPixel(Direction side, int index) {
        int[] sideArr = sides[side.ordinal()];

        return sideArr != null ? sideArr[index] : CanvasBlock.DEFAULT_COLOR;
    }

    public int @Nullable [] copyPixels(Direction dir) {
        int[] sideArr = sides[dir.ordinal()];
        return sideArr != null ? Arrays.copyOf(sideArr, sideArr.length) : null;
    }

    public int index(int x, int y) {
        return y * CanvasBlock.SIZE + x;
    }
}
