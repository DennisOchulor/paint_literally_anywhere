package io.github.dennisochulor.paint_literally_anywhere.item;

import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.joml.Vector3f;
import org.joml.Vector3fc;

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
        if (context.getLevel().isClientSide()) return InteractionResult.FAIL;

        ItemStack itemStack = context.getItemInHand();
        Integer argb = itemStack.get(ModComponents.RGB_COLOR);
        boolean emissive = itemStack.has(ModComponents.EMISSIVE);

        if (argb == null) return InteractionResult.FAIL;

        ServerLevel level = (ServerLevel) context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState state = level.getBlockState(blockPos);
        Direction hitDirection = context.getClickedFace();
        Vector3fc localHitPos = localize(context.getClickLocation(), hitDirection);
        Set<QuadTemplate> quads = ((BlockStateBaseExt) state).pla$quads(level, blockPos);

        QuadTemplate clippedQuad = null;
        for (QuadTemplate quad : quads) {
            if (quad.direction() == hitDirection && quad.clip(localHitPos)) {
                clippedQuad = quad;
                break;
            }
        }

        if (clippedQuad == null) {
            PLAMod.LOGGER.warn("{} at {}/ local: {} called useOn but somehow no quad was clipped!", state, blockPos, localHitPos);
            return InteractionResult.FAIL;
        }

        ChunkCanvasData.paintServer(level.getChunkAt(blockPos), clippedQuad, blockPos, localHitPos, argb, emissive);
        return InteractionResult.CONSUME;
    }

    private static Vector3fc localize(Vec3 vec, Direction hitDirection) {
        float x = (float) Math.abs((Math.abs(vec.x()) - Math.abs(Math.floor(vec.x()))));
        float y = (float) Math.abs((Math.abs(vec.y()) - Math.abs(Math.floor(vec.y()))));
        float z = (float) Math.abs((Math.abs(vec.z()) - Math.abs(Math.floor(vec.z()))));

        // Account for both most +ve and -ve of each axis being 0
        if (x == 0 && hitDirection == Direction.EAST) x = 1;
        if (y == 0 && hitDirection == Direction.UP) y = 1;
        if (z == 0 && hitDirection == Direction.SOUTH) z = 1;

        return new Vector3f(x, y, z);
    }
}
