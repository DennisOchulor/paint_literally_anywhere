package io.github.dennisochulor.paint_literally_anywhere.client.model.generator;

import io.github.dennisochulor.paint_literally_anywhere.shape.QuadTemplate;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

// Forgive me, for I have sinned.
@SuppressWarnings("UnstableApiUsage")
class FakeQuadEmitter extends MutableQuadViewImpl {
    private final List<QuadTemplate> templates = new ArrayList<>();

    FakeQuadEmitter() {
        this.data = new int[EncodingFormat.TOTAL_STRIDE];
        this.clear();
    }

    @Override
    protected void emitDirectly() {
        computeGeometry();

        templates.add(QuadTemplate.create(
                new Vector3f(x(0), y(0), z(0)),
                new Vector3f(x(1), y(1), z(1)),
                new Vector3f(x(2), y(2), z(2)),
                new Vector3f(x(3), y(3), z(3)),
                lightFace()) // equivalent to BakedQuad.direction()
        );
    }

    List<QuadTemplate> getAndClearTemplates() {
        List<QuadTemplate> list = List.copyOf(templates);
        templates.clear();
        return list;
    }
}
