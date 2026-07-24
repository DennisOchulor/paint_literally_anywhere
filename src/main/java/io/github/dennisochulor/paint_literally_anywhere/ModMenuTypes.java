package io.github.dennisochulor.paint_literally_anywhere;

import io.github.dennisochulor.paint_literally_anywhere.item.PaletteMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public final class ModMenuTypes {
    private ModMenuTypes() {}

    public static final MenuType<PaletteMenu> PALETTE_MENU = register(
            "palette",
            PaletteMenu::new
    );



    public static void init() {}

    private static <T extends AbstractContainerMenu> MenuType<T> register(
            String name,
            MenuType.MenuSupplier<T> constructor
    )
    {
        return Registry.register(
                BuiltInRegistries.MENU,
                PLAMod.id("menu/" + name),
                new MenuType<>(constructor, FeatureFlagSet.of()));
    }
}
