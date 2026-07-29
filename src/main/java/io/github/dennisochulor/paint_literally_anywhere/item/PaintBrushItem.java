package io.github.dennisochulor.paint_literally_anywhere.item;

import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
import java.util.Set;

public class PaintBrushItem extends Item {
    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        int rgb = itemStack.getComponents().getOrDefault(ModComponents.RGB_COLOR, Color.WHITE.getRGB());
        boolean emissive = itemStack.has(ModComponents.EMISSIVE);
        return Component.translatable(
                emissive ? "item.paint_literally_anywhere.paint_brush.emissive" : "item.paint_literally_anywhere.paint_brush")
                .withColor(rgb);
    }

    @Override
    public boolean canDestroyBlock(ItemStack itemStack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) return InteractionResult.PASS;

        ItemStack itemStack = context.getItemInHand();
        Integer argb = itemStack.get(ModComponents.RGB_COLOR);
        boolean emissive = itemStack.has(ModComponents.EMISSIVE);

        if (argb == null) return InteractionResult.PASS;

        ServerLevel level = (ServerLevel) context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState state = level.getBlockState(blockPos);
        Vec3 hitPos = context.getClickLocation();
        Set<QuadTemplate> quads = ((BlockStateBaseExt) state).pla$quads(level, blockPos);

        QuadTemplate clippedQuad = null;
        for (QuadTemplate quad : quads) {
            if (quad.clip(hitPos)) {
                clippedQuad = quad;
                break;
            }
        }

        if (clippedQuad == null) {
            PLAMod.LOGGER.warn("Block {} at {}/{} called useOn but somehow no quad was clipped!", state, blockPos, hitPos);
            return InteractionResult.PASS;
        }

        ChunkCanvasData.paintServer(level.getChunkAt(blockPos), clippedQuad, blockPos, hitPos, argb, emissive);
        return InteractionResult.CONSUME;
    }
}
