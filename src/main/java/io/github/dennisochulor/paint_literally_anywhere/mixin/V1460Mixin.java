package io.github.dennisochulor.paint_literally_anywhere.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import io.github.dennisochulor.paint_literally_anywhere.PLAMod;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V1460;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(V1460.class)
abstract class V1460Mixin {
    @Inject(method = "registerTypes", at = @At("TAIL"))
    private void registerShapeFile(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes, CallbackInfo ci) {
        schema.registerType(
                false,
                PLAMod.SHAPE_FILE_REFERENCE,
                () -> DSL.optionalFields(
                        "blockStates",
                        DSL.optionalFields("keys", DSL.list(References.BLOCK_STATE.in(schema)))
                )
        );
    }
}
