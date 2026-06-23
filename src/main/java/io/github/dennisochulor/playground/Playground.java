package io.github.dennisochulor.playground;

import io.github.dennisochulor.playground.canvas.CanvasMod;
import io.github.dennisochulor.playground.mirror.MirrorMod;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

public class Playground implements ModInitializer {
	public static final String MOD_ID = "playground";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initialising Playground!");

		MixinEnvironment.getCurrentEnvironment().audit();

		MirrorMod.init();
		CanvasMod.init();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
