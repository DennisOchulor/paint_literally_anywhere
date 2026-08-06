package io.github.dennisochulor.paint_literally_anywhere.client.mixin;

import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(BlockStateModelSet.class)
public interface BlockStateModelSetAccessor {
    @Accessor("modelByState")
    Map<BlockState, BlockStateModel> pla$modelByState();
}
