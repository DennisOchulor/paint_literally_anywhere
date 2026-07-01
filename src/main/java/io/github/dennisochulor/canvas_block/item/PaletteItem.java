package io.github.dennisochulor.canvas_block.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class PaletteItem extends Item {
    public PaletteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, inventory, _) -> {
                        return new PaletteMenu(containerId, inventory, ContainerLevelAccess.create(level, player.blockPosition()));
                    },
                    Component.translatable("item.canvas_block.palette")
            ));
        }

        return InteractionResult.CONSUME;
    }
}
