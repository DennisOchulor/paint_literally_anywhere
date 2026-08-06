package io.github.dennisochulor.paint_literally_anywhere.client.mixin;

import io.github.dennisochulor.paint_literally_anywhere.client.model.generator.ShapeFileGenerator;
import io.github.dennisochulor.paint_literally_anywhere.shape.ShapeUtil;
import io.github.dennisochulor.paint_literally_anywhere.shape.parser.ShapeFileParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelManager.class)
public class ModelManagerMixin {
    @Inject(method = "apply", at = @At("TAIL"))
    private void afterApplyModels(CallbackInfo ci) {
        ShapeUtil.setParseResult(ShapeFileGenerator.generate(
                Minecraft.getInstance(), ShapeFileParser.parse(Minecraft.getInstance().gameDirectory.toPath())));
    }
}
