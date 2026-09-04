package pasheadskins;

import net.minecraft.world.entity.decoration.ArmorStand;

public final class HeadSkinFlags {
	public static final String NBT_KEY = "HeadSkinDisabled";

	private HeadSkinFlags() {
	}

	public static boolean isDisabled(ArmorStand stand) {
		return ((HeadSkinHolder) stand).pasheadskins$isDisabled();
	}

	public static void setDisabled(ArmorStand stand, boolean disabled) {
		((HeadSkinHolder) stand).pasheadskins$setDisabled(disabled);
	}
}
