package io.github.dennisochulor.paint_literally_anywhere.item;

import io.github.dennisochulor.paint_literally_anywhere.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.Color;
import java.util.*;

public class PaletteMenu extends ItemCombinerMenu {
    public static final Color STARTING_COLOR = Color.RED;

    public static final int INVENTORY_START_X = 8;
    public static final int INVENTORY_START_Y = 160;

    public static final int INPUT_SLOT_START_X = INVENTORY_START_X + AbstractContainerMenu.SLOT_SIZE;
    public static final int INPUT_SLOT_START_Y = 123;
    public static final int INPUT_SLOT_INDEX = 0;

    public static final int RESULT_SLOT_START_X = INVENTORY_START_X + (AbstractContainerMenu.SLOT_SIZE * 7);
    public static final int RESULT_SLOT_START_Y = INPUT_SLOT_START_Y;
    public static final int RESULT_SLOT_INDEX = 1;

    public static final int FIRST_DYE_SLOT_START_X = (INPUT_SLOT_START_X + RESULT_SLOT_START_X) / 2 - (AbstractContainerMenu.SLOT_SIZE / 2);
    public static final int FIRST_DYE_SLOT_START_Y = INPUT_SLOT_START_Y + 15;
    public static final int FIRST_DYE_SLOT_INDEX = 2;

    public static final int SECOND_DYE_SLOT_START_X = FIRST_DYE_SLOT_START_X + AbstractContainerMenu.SLOT_SIZE;
    public static final int SECOND_DYE_SLOT_START_Y = FIRST_DYE_SLOT_START_Y;
    public static final int SECOND_DYE_SLOT_INDEX = 3;

    private static final Map<Color, Item> COLOR_TO_DYE_MAP;

    static {
        // DyeColor.VALUES.textColor is RGB with max alpha
        Map<Color, Item> map = new HashMap<>(DyeColor.VALUES.size());
        DyeColor.VALUES.forEach(dyeColor -> {
            Color color = new Color(dyeColor.getTextColor());
            map.put(color, Items.DYE.pick(dyeColor));
        });
        COLOR_TO_DYE_MAP = Map.copyOf(map);
    }


    private final ContainerLevelAccess access;
    private Color requestedColor = STARTING_COLOR; // not using DataSlot cause it does not do C2S syncing
    private final Container dyeSlots = new SimpleContainer(4) {
        // 4 because it is indexed 2 and 3 in ItemCombinerMenu's container
        @Override
        public void setChanged() {
            super.setChanged();
            PaletteMenu.this.slotsChanged(this);
        }
    };

    public PaletteMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public PaletteMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenuTypes.PALETTE_MENU, containerId, inventory, access, createMenuSlotDefinitions());

        // remove the inventory slots added by above super call as they are positioned incorrectly
        this.slots.removeIf(slot -> slot.container == inventory);
        this.addStandardInventorySlots(inventory, INVENTORY_START_X, INVENTORY_START_Y);

        this.access = access;
        this.addSlot(new Slot(dyeSlots, FIRST_DYE_SLOT_INDEX, FIRST_DYE_SLOT_START_X, FIRST_DYE_SLOT_START_Y) {
            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }
        });
        this.addSlot(new Slot(dyeSlots, SECOND_DYE_SLOT_INDEX, SECOND_DYE_SLOT_START_X, SECOND_DYE_SLOT_START_Y) {
            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }
        });
    }

    private static ItemCombinerMenuSlotDefinition createMenuSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(INPUT_SLOT_INDEX, INPUT_SLOT_START_X, INPUT_SLOT_START_Y, itemStack -> itemStack.is(ModItems.PAINT_BRUSH))
                .withResultSlot(RESULT_SLOT_INDEX, RESULT_SLOT_START_X, RESULT_SLOT_START_Y)
                .build();
    }

    public void setRequestedColor(Color requestedColor) {
        this.requestedColor = requestedColor;
        access.evaluate((level, _) -> {
            if (!level.isClientSide()) {
                createResult();
            }
            return Optional.empty();
        });
    }

    public Color getRequestedColor() {
        return requestedColor;
    }

    public Container getDyeSlots() {
        return dyeSlots;
    }

    private void updateDyes() {
        if (inputSlots.isEmpty()) {
            dyeSlots.setItem(FIRST_DYE_SLOT_INDEX, ItemStack.EMPTY);
            dyeSlots.setItem(SECOND_DYE_SLOT_INDEX, ItemStack.EMPTY);
            return;
        }

        // sort from closest to furthest from requestedColor
        List<Color> sortedDyeColors = COLOR_TO_DYE_MAP.keySet().stream()
                .sorted(Comparator.comparingDouble(color -> approxDistanceBetweenRGBValues(color, requestedColor))).toList();
        Color firstDyeColor = sortedDyeColors.getFirst();
        Color secondDyeColor = sortedDyeColors.get(1);

        dyeSlots.setItem(FIRST_DYE_SLOT_INDEX, new ItemStack(COLOR_TO_DYE_MAP.get(firstDyeColor)));
        boolean needsSecondDye = approxDistanceBetweenRGBValues(firstDyeColor, requestedColor) > 150;
        dyeSlots.setItem(SECOND_DYE_SLOT_INDEX, needsSecondDye ? new ItemStack(COLOR_TO_DYE_MAP.get(secondDyeColor)) : ItemStack.EMPTY);
    }

    // https://stackoverflow.com/a/9085524
    private static double approxDistanceBetweenRGBValues(Color color1, Color color2) {
        long rmean = ( (long) color1.getRed() + (long) color2.getRed() ) / 2;
        long r = (long) color1.getRed() - (long) color2.getRed();
        long g = (long) color1.getGreen() - (long) color2.getGreen();
        long b = (long) color1.getBlue() - (long) color2.getBlue();
        return Math.sqrt((((512+rmean)*r*r)>>8) + 4*g*g + (((767-rmean)*b*b)>>8));
    }

    @Override
    protected void onTake(Player player, ItemStack carried) {
        if (!player.hasInfiniteMaterials()) {
            Inventory inventory = player.getInventory();
            ItemStack firstDye = dyeSlots.getItem(FIRST_DYE_SLOT_INDEX);
            ItemStack secondDye = dyeSlots.getItem(SECOND_DYE_SLOT_INDEX);

            if (!firstDye.isEmpty()) {
                int slot = inventory.findSlotMatchingItem(firstDye);
                if (slot == -1) throw new IllegalStateException("Could not find matching dye in player's inventory!");
                inventory.removeItem(slot, 1);
            }
            if (!secondDye.isEmpty()) {
                int slot = inventory.findSlotMatchingItem(secondDye);
                if (slot == -1) throw new IllegalStateException("Could not find matching dye in player's inventory!");
                inventory.removeItem(slot, 1);
            }
        }

        inputSlots.setItem(INPUT_SLOT_INDEX, ItemStack.EMPTY);
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasItem) {
        if (player.hasInfiniteMaterials()) return true;

        ItemStack firstDye = dyeSlots.getItem(FIRST_DYE_SLOT_INDEX);
        ItemStack secondDye = dyeSlots.getItem(SECOND_DYE_SLOT_INDEX);
        Inventory inventory = player.getInventory();

        boolean firstMatch = firstDye.isEmpty() || inventory.contains(itemStack -> itemStack.is(firstDye.getItem()) && !itemStack.isEmpty());
        boolean secondMatch = secondDye.isEmpty() || inventory.contains(itemStack -> itemStack.is(secondDye.getItem()) && !itemStack.isEmpty());
        return firstMatch && secondMatch;
    }

    @Override
    public void createResult() {
        updateDyes();

        ItemStack inputStack = inputSlots.getItem(INPUT_SLOT_INDEX);

        if (inputStack.isEmpty() || inputStack.getItem() != ModItems.PAINT_BRUSH) {
            resultSlots.setItem(RESULT_SLOT_INDEX, ItemStack.EMPTY);
        }
        else {
            ItemStack resultStack = inputStack.copy();
            PaintBrushProperties properties = resultStack.getOrDefault(ModComponents.PAINT_BRUSH, PaintBrushProperties.DEFAULT).withArgb(requestedColor.getRGB());
            resultStack.set(ModComponents.PAINT_BRUSH, properties);
            resultSlots.setItem(RESULT_SLOT_INDEX, resultStack);
        }
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return true;
    }
}
