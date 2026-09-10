package pasheadskins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;

// Vanilla only writes CustomNameVisible when its true, so Poser's
// save-merge-load can drop a false toggle and snap the name back on.
public final class PoserNameVisible {
	public static final String KEY = "CustomNameVisible";

	private static final ThreadLocal<Boolean> pending = new ThreadLocal<>();

	private PoserNameVisible() {
	}

	public static void note(CompoundTag tag) {
		if (tag != null && tag.contains(KEY)) {
			pending.set(tag.getBooleanOr(KEY, false));
		} else {
			pending.remove();
		}
	}

	public static void apply(ArmorStand stand, CompoundTag tag) {
		if (stand == null || tag == null || !tag.contains(KEY)) {
			return;
		}
		stand.setCustomNameVisible(tag.getBooleanOr(KEY, false));
	}

	public static void copyPending(CompoundTag tag) {
		Boolean show = pending.get();
		if (show == null || tag == null) {
			return;
		}
		tag.putBoolean(KEY, show);
	}

	public static void writeCurrent(ArmorStand stand, CompoundTag tag) {
		if (stand == null || tag == null) {
			return;
		}
		tag.putBoolean(KEY, stand.isCustomNameVisible());
	}

	public static void clear() {
		pending.remove();
	}
}
