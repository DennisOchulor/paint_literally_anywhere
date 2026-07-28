package io.github.dennisochulor.paint_literally_anywhere.shape;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ShapeUtil {
    private ShapeUtil() {}

    private static final Map<QuadTemplate, QuadTemplate> TEMPLATE_CACHE = new HashMap<>();
    private static final QuadTemplate[] BLOCK_TEMPLATES = voxelShapeToQuadTemplates(Shapes.block());

    public static QuadTemplate[] voxelShapeToQuadTemplates(VoxelShape voxelShape) {
        if (voxelShape == Shapes.block()) return BLOCK_TEMPLATES;

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
            quads.add(new QuadTemplate(v4, v5, v6, v7));
            quads.add(new QuadTemplate(v1, v0, v3, v2));
            quads.add(new QuadTemplate(v0, v4, v7, v3));
            quads.add(new QuadTemplate(v5, v1, v2, v6));
            quads.add(new QuadTemplate(v3, v7, v6, v2));
            quads.add(new QuadTemplate(v0, v1, v5, v4));
        });

        QuadTemplate[] quadsArr = new QuadTemplate[quads.size()];
        for (int i = 0; i < quads.size(); i++) {
            QuadTemplate newTemplate = quads.get(i);
            QuadTemplate cached = TEMPLATE_CACHE.get(newTemplate);

            if (cached != null) {
                quadsArr[i] = cached;
            }
            else {
                quadsArr[i] = newTemplate;
                TEMPLATE_CACHE.put(newTemplate, newTemplate);
            }
        }

        return quadsArr;
    }

    /**
     * @return the cached QuadTemplate, which may be the one passed in or a previously cached one.
     */
    public static QuadTemplate cache(QuadTemplate template) {
        return TEMPLATE_CACHE.computeIfAbsent(template, _ -> template);
    }
}
