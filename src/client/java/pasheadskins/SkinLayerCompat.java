package pasheadskins;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public final class SkinLayerCompat {
	private static final boolean SKIN_LAYERS_LOADED = FabricLoader.getInstance().isModLoaded("skinlayers3d");

	private SkinLayerCompat() {
	}

	public static void apply(HeadStandModel model, Identifier texture, boolean slim) {
		if (!SKIN_LAYERS_LOADED || texture == null) {
			return;
		}

		try {
			Holder.apply(model, texture, slim);
		} catch (Throwable ignored) {
		}
	}

	private static final class Holder {
		private static void apply(HeadStandModel model, Identifier texture, boolean slim) {
			SkinLayerSupport.apply(model, texture, slim);
		}
	}
}
