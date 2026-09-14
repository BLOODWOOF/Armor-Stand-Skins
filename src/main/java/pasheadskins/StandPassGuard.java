package pasheadskins;

import net.minecraft.world.entity.decoration.ArmorStand;

// Same DisabledSlots mask Armor Poser uses for "lock slots". Vanilla clients
// honor it even if they never installed us, so they cant swap the gear.
public final class StandPassGuard {
	public static final int VANILLA_LOCK = 4144959;

	private StandPassGuard() {
	}

	public static void sync(ArmorStand stand) {
		if (!(stand instanceof HeadSkinHolder holder)) {
			return;
		}
		int slots = holder.pasheadskins$disabledSlots();
		if (HeadSkinFlags.hasPassword(stand)) {
			slots |= VANILLA_LOCK;
		} else {
			slots &= ~VANILLA_LOCK;
		}
		holder.pasheadskins$setDisabledSlots(slots);
		StandSlotFlags.writeOntoStand(stand);
	}
}
