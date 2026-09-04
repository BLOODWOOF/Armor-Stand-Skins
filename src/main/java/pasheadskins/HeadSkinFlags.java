package pasheadskins;

import net.minecraft.world.entity.decoration.ArmorStand;

public final class HeadSkinFlags {
	public static final String NBT_KEY = "HeadSkinDisabled";

	private HeadSkinFlags() {
	}

	public static boolean isDisabled(ArmorStand stand) {
		if (stand instanceof HeadSkinHolder holder) {
			return holder.pasheadskins$isDisabled();
		}
		return false;
	}

	public static void setDisabled(ArmorStand stand, boolean disabled) {
		if (stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setDisabled(disabled);
		}
	}
}
