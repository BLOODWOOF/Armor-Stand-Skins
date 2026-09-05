package pasheadskins;

import net.minecraft.world.entity.decoration.ArmorStand;

public final class HeadSkinFlags {
	public static final String NBT_KEY = "HeadSkinDisabled";

	private static Lookup lookup;

	private HeadSkinFlags() {
	}

	public static void bind(Lookup next) {
		lookup = next;
	}

	public static boolean isDisabled(ArmorStand stand) {
		if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$isDisabled()) {
			return true;
		}
		return lookup != null && lookup.isDisabled(stand);
	}

	public static void setDisabled(ArmorStand stand, boolean disabled) {
		if (stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setDisabled(disabled);
		}
		if (lookup != null) {
			lookup.setDisabled(stand, disabled);
		}
	}

	public interface Lookup {
		boolean isDisabled(ArmorStand stand);

		void setDisabled(ArmorStand stand, boolean disabled);
	}
}
