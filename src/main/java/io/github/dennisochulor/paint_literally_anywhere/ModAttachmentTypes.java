package io.github.dennisochulor.paint_literally_anywhere;

import io.github.dennisochulor.paint_literally_anywhere.shape.QuadInstance;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ModAttachmentTypes {
    private ModAttachmentTypes() {}

    public static final AttachmentType<ChunkCanvasData> CHUNK_CANVAS_DATA =
            AttachmentRegistry.create(PLAMod.id("chunk_canvas_data"),
                     builder ->
                             builder.persistent(ChunkCanvasData.CODEC)
            );

    public static final AttachmentType<List<QuadInstance>> QUAD_INSTANCES =
            AttachmentRegistry.create(PLAMod.id("quad_instances"),
                    builder ->
                            builder.persistent(QuadInstance.CODEC.listOf().xmap(ArrayList::new, List::copyOf))
            );

    public static final AttachmentType<Set<String>> INACCURATE_NAMESPACES =
            AttachmentRegistry.create(PLAMod.id("inaccurate_namespaces"),
                    builder ->
                            builder.syncWith(ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), AttachmentSyncPredicate.all())
            );


    public static void init() {}
}
