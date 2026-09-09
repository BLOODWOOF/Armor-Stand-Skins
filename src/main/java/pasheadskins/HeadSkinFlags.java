package pasheadskins;

import java.util.function.BooleanSupplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;

public final class HeadSkinFlags {
	private static Lookup lookup;
	private static BooleanSupplier packetChannel = () -> true;

	private HeadSkinFlags() {
	}

	public static void bind(Lookup next) {
		lookup = next;
	}

	public static void setPacketChannel(BooleanSupplier supplier) {
		packetChannel = supplier != null ? supplier : () -> true;
	}

	public static boolean packetChannelOpen() {
		try {
			return packetChannel.getAsBoolean();
		} catch (Throwable ignored) {
			return false;
		}
	}

	public static boolean isDisabled(ArmorStand stand) {
		if (StandSlotFlags.hasRecord(stand)) {
			return StandSlotFlags.headOff(stand);
		}
		if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$isDisabled()) {
			return true;
		}
		return lookup != null && lookup.isDisabled(stand);
	}

	public static void setDisabled(ArmorStand stand, boolean disabled) {
		setDisabled(stand, disabled, true);
	}

	public static void setDisabled(ArmorStand stand, boolean disabled, boolean persist) {
		if (stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setDisabled(disabled);
		}
		if (persist && lookup != null) {
			lookup.setDisabled(stand, disabled);
		}
		StandSlotFlags.writeOntoStand(stand);
	}

	public static boolean isLocked(ArmorStand stand) {
		if (StandSlotFlags.hasRecord(stand)) {
			return StandSlotFlags.lockOn(stand);
		}
		if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$isLocked()) {
			return true;
		}
		return lookup != null && lookup.isLocked(stand);
	}

	public static ResolvableProfile lockedProfile(ArmorStand stand) {
		if (StandSlotFlags.hasRecord(stand) && StandSlotFlags.lockOn(stand)) {
			if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$isLocked()) {
				return holder.pasheadskins$lockedProfile();
			}
			return helmetProfile(stand);
		}
		if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$isLocked()) {
			return holder.pasheadskins$lockedProfile();
		}
		return lookup == null ? null : lookup.lockedProfile(stand);
	}

	public static void setLocked(ArmorStand stand, boolean locked, ResolvableProfile profile) {
		setLocked(stand, locked, profile, true);
	}

	public static void setLocked(ArmorStand stand, boolean locked, ResolvableProfile profile, boolean persist) {
		if (stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setLocked(locked, profile);
		}
		if (persist && lookup != null) {
			lookup.setLocked(stand, locked, profile);
		}
		StandSlotFlags.writeOntoStand(stand);
	}

	public static boolean isCapeEnabled(ArmorStand stand) {
		if (StandSlotFlags.hasRecord(stand)) {
			return StandSlotFlags.capeOn(stand);
		}
		if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$isCapeEnabled()) {
			return true;
		}
		return lookup != null && lookup.isCapeEnabled(stand);
	}

	public static void setCapeEnabled(ArmorStand stand, boolean enabled) {
		setCapeEnabled(stand, enabled, true);
	}

	public static void setCapeEnabled(ArmorStand stand, boolean enabled, boolean persist) {
		if (stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setCapeEnabled(enabled);
		}
		if (persist && lookup != null) {
			lookup.setCapeEnabled(stand, enabled);
		}
		StandSlotFlags.writeOntoStand(stand);
	}

	public static CapeSource capeSource(ArmorStand stand) {
		if (StandSlotFlags.hasRecord(stand)) {
			return StandSlotFlags.capeSource(stand);
		}
		if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$capeSource() != CapeSource.BOTH) {
			return holder.pasheadskins$capeSource();
		}
		return lookup == null ? CapeSource.BOTH : lookup.capeSource(stand);
	}

	public static void setCapeSource(ArmorStand stand, CapeSource source) {
		setCapeSource(stand, source, true);
	}

	public static void setCapeSource(ArmorStand stand, CapeSource source, boolean persist) {
		CapeSource next = source == null ? CapeSource.BOTH : source;
		if (stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setCapeSource(next);
		}
		if (persist && lookup != null) {
			lookup.setCapeSource(stand, next);
		}
		StandSlotFlags.writeOntoStand(stand);
	}

	public static ResolvableProfile helmetProfile(ArmorStand stand) {
		return stand.getItemBySlot(EquipmentSlot.HEAD).get(DataComponents.PROFILE);
	}

	public interface Lookup {
		boolean isDisabled(ArmorStand stand);

		void setDisabled(ArmorStand stand, boolean disabled);

		boolean isLocked(ArmorStand stand);

		ResolvableProfile lockedProfile(ArmorStand stand);

		void setLocked(ArmorStand stand, boolean locked, ResolvableProfile profile);

		boolean isCapeEnabled(ArmorStand stand);

		void setCapeEnabled(ArmorStand stand, boolean enabled);

		CapeSource capeSource(ArmorStand stand);

		void setCapeSource(ArmorStand stand, CapeSource source);
	}
}
