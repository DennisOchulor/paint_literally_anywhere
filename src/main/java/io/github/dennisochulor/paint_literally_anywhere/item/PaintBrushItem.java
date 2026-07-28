package io.github.dennisochulor.paint_literally_anywhere.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

        ServerLevel level = (ServerLevel) context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        Vec3 hitPos = context.getClickLocation();
    }
}
