package io.github.dennisochulor.paint_literally_anywhere.shape;

import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParseResult;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.*;

public final class ShapeUtil {
    private ShapeUtil() {}

    private static final Map<QuadTemplate, QuadTemplate> TEMPLATE_CACHE = new HashMap<>();
    private static final Set<QuadTemplate> BLOCK_TEMPLATES = voxelShapeToQuadTemplates(Shapes.block());
    private static ShapeFileParseResult parseResult = ShapeFileParseResult.EMPTY;

    public static Set<QuadTemplate> getQuadTemplates(BlockState state, VoxelShape shape) {
        if (parseResult.get(state) instanceof Set<QuadTemplate> templates) {
            return templates;
        }

        return voxelShapeToQuadTemplates(shape);
    }

    public static void setParseResult(ShapeFileParseResult result) {
        parseResult = result;
    }

    /**
     * @return the cached QuadTemplate, which may be the one passed in or a previously cached one.
     */
    public static QuadTemplate cache(QuadTemplate template) {
        return TEMPLATE_CACHE.computeIfAbsent(template, _ -> template);
    }

    private static Set<QuadTemplate> voxelShapeToQuadTemplates(VoxelShape voxelShape) {
        if (voxelShape == Shapes.block() && BLOCK_TEMPLATES != null) {
            return BLOCK_TEMPLATES;
        }

        List<QuadTemplate> quads = new ArrayList<>();

        // obtain 6 quads from each box
        voxelShape.forAllBoxes((x1, y1, z1, x2, y2, z2) -> {
            float xMin = (float) x1;
            float yMin = (float) y1;
            float zMin = (float) z1;
            float xMax = (float) x2;
            float yMax = (float) y2;
            float zMax = (float) z2;

            // define 8 vertices of the box
            Vector3fc v0 = new Vector3f(xMin, yMin, zMin);
            Vector3fc v1 = new Vector3f(xMax, yMin, zMin);
            Vector3fc v2 = new Vector3f(xMax, yMax, zMin);
            Vector3fc v3 = new Vector3f(xMin, yMax, zMin);
            Vector3fc v4 = new Vector3f(xMin, yMin, zMax);
            Vector3fc v5 = new Vector3f(xMax, yMin, zMax);
            Vector3fc v6 = new Vector3f(xMax, yMax, zMax);
            Vector3fc v7 = new Vector3f(xMin, yMax, zMax);

            // define 6 faces from those 8 vertices
            quads.add(QuadTemplate.create(v4, v5, v6, v7, Direction.SOUTH));
            quads.add(QuadTemplate.create(v1, v0, v3, v2, Direction.NORTH));
            quads.add(QuadTemplate.create(v0, v4, v7, v3, Direction.WEST));
            quads.add(QuadTemplate.create(v5, v1, v2, v6, Direction.EAST));
            quads.add(QuadTemplate.create(v3, v7, v6, v2, Direction.UP));
            quads.add(QuadTemplate.create(v0, v1, v5, v4, Direction.DOWN));
        });

        return Set.copyOf(quads);
    }
}
