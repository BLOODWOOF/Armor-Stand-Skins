package pasheadskins;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;

public final class HeadSkinFlags {
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

	public static boolean isLocked(ArmorStand stand) {
		if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$isLocked()) {
			return true;
		}
		return lookup != null && lookup.isLocked(stand);
	}

	public static ResolvableProfile lockedProfile(ArmorStand stand) {
		if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$isLocked()) {
			return holder.pasheadskins$lockedProfile();
		}
		if (lookup != null) {
			return lookup.lockedProfile(stand);
		}
		return null;
	}

	public static void setLocked(ArmorStand stand, boolean locked, ResolvableProfile profile) {
		if (stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setLocked(locked, profile);
		}
		if (lookup != null) {
			lookup.setLocked(stand, locked, profile);
		}
	}

	public interface Lookup {
		boolean isDisabled(ArmorStand stand);

		void setDisabled(ArmorStand stand, boolean disabled);

		boolean isLocked(ArmorStand stand);

		ResolvableProfile lockedProfile(ArmorStand stand);

		void setLocked(ArmorStand stand, boolean locked, ResolvableProfile profile);
	}
}
