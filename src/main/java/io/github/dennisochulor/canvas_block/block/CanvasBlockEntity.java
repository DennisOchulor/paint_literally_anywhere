package io.github.dennisochulor.canvas_block.block;

import io.github.dennisochulor.canvas_block.CanvasMod;
import io.github.dennisochulor.canvas_block.network.ClientboundCanvasUpdatePacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class CanvasBlockEntity extends BlockEntity {
    // int is ARGB, where alpha represents emissive (opaque) or non-emissive (transparent)
    // during rendering, the actual alpha is always opaque
    private final int[] @Nullable [] sides = new int[6][];

    public CanvasBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.CANVAS_BLOCK_ENTITY, worldPosition, blockState);
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

    @Override
    public void applyImplicitComponents(DataComponentGetter components) { // for loading pixel data from item
        CustomData customData = components.get(DataComponents.CUSTOM_DATA);
        if (customData == null) return;

        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), CanvasMod.LOGGER)) {
            HolderLookup.Provider registries = Objects.requireNonNull(level).registryAccess();
            loadAdditional(TagValueInput.create(reporter, registries, customData.copyTag()));
        }
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) { // for saving pixel data to item when block is mined
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), CanvasMod.LOGGER)) {
            HolderLookup.Provider registries = Objects.requireNonNull(level).registryAccess();
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);

            if (!output.isEmpty()) {
                components.set(DataComponents.ITEM_NAME, Component.translatable("block.canvas_block.canvas.as_item_with_data"));
                components.set(DataComponents.CUSTOM_DATA, CustomData.of(output.buildResult()));
            }
        }
    }

    public void setPixel(Direction side, int x, int y, int color, boolean emissive) {
        setPixel(side, index(x, y), color, emissive);
    }

    public void setPixel(Direction side, int index, int color, boolean emissive) {
        int[] sideArr = sides[side.ordinal()];
        if (sideArr == null) {
            sideArr = new int[CanvasBlock.SIZE * CanvasBlock.SIZE];
            Arrays.fill(sideArr, CanvasBlock.DEFAULT_COLOR);
            sides[side.ordinal()] = sideArr;
        }

        sideArr[index] = CanvasBlock.withEmissiveData(color, emissive);

        if (!Objects.requireNonNull(level).isClientSide()) {
            this.setChanged();

            // incremental sync
            ClientboundCanvasUpdatePacket packet = new ClientboundCanvasUpdatePacket(this.getBlockPos(), side, index, color, emissive);
            PlayerLookup.tracking(this).forEach(player -> ServerPlayNetworking.send(player, packet));
        }
    }

    /**
     * @return the ARGB color, with alpha opaque meaning emissive and
     * alpha transparent meaning non-emissive.
     */
    public int getPixelColor(Direction side, int x, int y) {
        return getPixelColor(side, index(x, y));
    }

    /**
     * @return the ARGB color, with alpha opaque meaning emissive and
     * alpha transparent meaning non-emissive.
     */
    public int getPixelColor(Direction side, int index) {
        int[] sideArr = sides[side.ordinal()];
        return sideArr != null ? sideArr[index] : CanvasBlock.DEFAULT_COLOR;
    }

    public int @Nullable [] copyPixelColors(Direction dir) {
        int[] sideArr = sides[dir.ordinal()];
        return sideArr != null ? Arrays.copyOf(sideArr, sideArr.length) : null;
    }

    public int index(int x, int y) {
        return y * CanvasBlock.SIZE + x;
    }
}
