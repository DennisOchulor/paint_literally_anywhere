package io.github.dennisochulor.paint_literally_anywhere.item;

import io.github.dennisochulor.paint_literally_anywhere.ChunkCanvasData;
import io.github.dennisochulor.paint_literally_anywhere.network.ServerboundPaintPacket;
import io.github.dennisochulor.paint_literally_anywhere.shape.BlockStateBaseExt;
import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.Color;

public class PaintBrushItem extends Item {
    public PaintBrushItem(Properties properties) {
        super(properties);
    }

    public static void handlePacket(ServerboundPaintPacket packet, ServerPlayer player) {
        ServerLevel level = player.level();
        BlockPos pos = packet.pos();
        ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);

        // sanity checks - some are copied from vanilla's ServerboundUseItemOnPacket handling
        if (!player.connection.hasClientLoaded() ||
                !player.isWithinBlockInteractionRange(pos, 1.0F) ||
                !level.mayInteract(player, pos) ||
                !itemStack.is(ModItems.PAINT_BRUSH) ||
                properties.argb() == PaintBrushProperties.EMPTY_COLOR ||
                // ensure the clipped quad actually exist for the block state
                !((BlockStateBaseExt) level.getBlockState(pos)).pla$quads(level, pos).contains(packet.template())
        ) {
            return; // simply ignore nonsense packet
        }

        boolean success = ChunkCanvasData.paintServer(level.getChunkAt(pos), packet.template(), pos,
                QuadTemplate.localize(packet.hitPos(), pos, packet.template().direction()), properties.argb(), properties.emissive());

        if (success) {
            itemStack.hurtAndBreak(1, player, InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public Component getName(ItemStack itemStack) {
        PaintBrushProperties properties = itemStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT);
        return Component.translatable(
                properties.emissive() ? "item.paint_literally_anywhere.paint_brush.emissive" : "item.paint_literally_anywhere.paint_brush")
                .withColor(properties.argb() == PaintBrushProperties.EMPTY_COLOR ? Color.WHITE.getRGB() : properties.argb());
    }

    @Override
    public boolean canDestroyBlock(ItemStack itemStack, BlockState state, Level level, BlockPos pos, LivingEntity user) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.CONSUME;
    }
}
