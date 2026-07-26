package io.github.dennisochulor.paint_literally_anywhere.client.model;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public class ModModelLoadingPlugin implements ModelLoadingPlugin {
    @Override
    public void initialize(Context pluginContext) {
        pluginContext.modifyBlockModelAfterBake().register((model, _) -> {
            return new CanvasBlockStateModel(model);
        });
    }
}
