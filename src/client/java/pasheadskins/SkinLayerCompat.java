package pasheadskins;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.model.geom.ModelPart;

public final class SkinLayerCompat {
	private static final boolean SKIN_LAYERS_LOADED = FabricLoader.getInstance().isModLoaded("skinlayers3d");

	private SkinLayerCompat() {
	}

	public static void applyToStand(Object standModel, Object nameInfo) {
		if (!SKIN_LAYERS_LOADED) {
			return;
		}

		SkinLayerPasSupport.apply(standModel, nameInfo);
	}

	public static void snapshotInjector(Object state, ModelPart part) {
		if (!SKIN_LAYERS_LOADED) {
			return;
		}

		SkinLayerPasSupport.snapshotInjector(state, part);
	}

	public static void restoreInjector(Object state) {
		if (!SKIN_LAYERS_LOADED) {
			return;
		}

		SkinLayerPasSupport.restoreInjector(state);
	}
}
