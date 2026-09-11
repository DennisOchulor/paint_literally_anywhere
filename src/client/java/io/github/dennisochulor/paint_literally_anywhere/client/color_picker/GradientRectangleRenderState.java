package io.github.dennisochulor.paint_literally_anywhere.client.color_picker;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

// vanilla's ColoredRectangleRenderState only goes vertical, we need to be able to specify a color for each corner
public record GradientRectangleRenderState(
        GuiGraphicsExtractor graphics,
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2fc pose,
        @Nullable ScreenRectangle scissorArea,
        int x0, int y0, int x1, int y1,
        int x0y0col, int x1y0col, int x1y1col, int x0y1col
) implements GuiElementRenderState {
    GradientRectangleRenderState(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1,
                                 int x0y0col, int x1y0col, int x1y1col, int x0y1col)
    {
        this(graphics, RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()), graphics.scissorStack.peek(),
                x0, y0, x1, y1, x0y0col, x1y0col, x1y1col, x0y1col);
    }

    public void submit() {
        graphics.guiRenderState.addGuiElement(this);
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        vertexConsumer.addVertexWith2DPose(pose, x0, y0).setColor(x0y0col);
        vertexConsumer.addVertexWith2DPose(pose, x0, y1).setColor(x0y1col);
        vertexConsumer.addVertexWith2DPose(pose, x1, y1).setColor(x1y1col);
        vertexConsumer.addVertexWith2DPose(pose, x1, y0).setColor(x1y0col);
    }

    @Override
    public RenderPipeline pipeline() {
        return pipeline;
    }

    @Override
    public TextureSetup textureSetup() {
        return textureSetup;
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return scissorArea;
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
