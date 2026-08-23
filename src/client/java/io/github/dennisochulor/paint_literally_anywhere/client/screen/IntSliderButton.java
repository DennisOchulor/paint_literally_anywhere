package io.github.dennisochulor.paint_literally_anywhere.client.screen;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

public class IntSliderButton extends AbstractSliderButton {
    private int actualValue;
    private final int[] values;
    private final String label;

    public IntSliderButton(int x, int y, int width, int height, String label, int initialValue, int[] values) {
        int index = -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == initialValue) {
                index = i;
                break;
            }
        }

        if (index == -1) {
            throw new IllegalArgumentException("Provided initial value %d not in values array!".formatted(initialValue));
        }

        super(x, y, width, height, Component.literal(label), (double) index / (values.length - 1));
        this.actualValue = initialValue;
        this.values = values;
        this.label = label;
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(label + ": " + actualValue));
    }

    @Override
    protected void applyValue() {
        int index = (int) Math.round((values.length - 1) * value);
        actualValue = values[index];
    }

    public int getActualValue() {
        return actualValue;
    }
}
