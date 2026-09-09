package pasheadskins;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;
import pasheadskins.net.HeadSkinCapePayload;
import pasheadskins.net.HeadSkinCapeSourcePayload;
import pasheadskins.net.HeadSkinDisabledPayload;
import pasheadskins.net.HeadSkinLockPayload;

public final class HeadSkinControls {
	private HeadSkinControls() {
	}

	public static void setHeadSkinVisible(ArmorStand stand, boolean visible) {
		boolean persist = persistLocalJson();
		HeadSkinFlags.setDisabled(stand, !visible, persist);
		push(stand, () -> ClientPlayNetworking.send(new HeadSkinDisabledPayload(stand.getId(), !visible)));
	}

	public static boolean setLocked(ArmorStand stand, boolean locked) {
		ResolvableProfile snapshot = null;
		HeadSkinLookup.FrozenLook look = null;
		if (locked) {
			look = HeadSkinLookup.freezeIfReady(HeadSkinLookup.profileFromHelmet(stand), HeadSkinFlags.capeSource(stand));
			if (look == null) {
				return false;
			}
			snapshot = look.profile();
		} else {
			HeadSkinLookup.invalidateLive(HeadSkinLookup.profileFromHelmet(stand));
		}
		boolean persist = persistLocalJson();
		HeadSkinFlags.setLocked(stand, locked, snapshot, persist);
		if (locked && look != null && stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setLockedCloak(look.cape(), look.elytra());
		}
		ResolvableProfile toSend = snapshot;
		push(stand, () -> ClientPlayNetworking.send(HeadSkinLockPayload.of(stand.getId(), locked, toSend)));
		return true;
	}

	public static void setCapeEnabled(ArmorStand stand, boolean enabled) {
		boolean persist = persistLocalJson();
		HeadSkinFlags.setCapeEnabled(stand, enabled, persist);
		push(stand, () -> ClientPlayNetworking.send(new HeadSkinCapePayload(stand.getId(), enabled)));
	}

	public static void setCapeSource(ArmorStand stand, CapeSource source) {
		CapeSource next = source == null ? CapeSource.BOTH : source;
		boolean persist = persistCapeSource();
		HeadSkinFlags.setCapeSource(stand, next, persist);
		pushCapeSource(stand, () -> ClientPlayNetworking.send(new HeadSkinCapeSourcePayload(stand.getId(), next.wire())));
	}

	private static boolean persistLocalJson() {
		return !ClientPlayNetworking.canSend(HeadSkinDisabledPayload.TYPE) && !poserAvailable();
	}

	private static boolean persistCapeSource() {
		return !ClientPlayNetworking.canSend(HeadSkinCapeSourcePayload.TYPE) && !poserAvailable();
	}

	private static boolean poserAvailable() {
		return FabricLoader.getInstance().isModLoaded("armorposer") && PoserStandSync.available();
	}

	private static void push(ArmorStand stand, Runnable sendOurPacket) {
		if (ClientPlayNetworking.canSend(HeadSkinDisabledPayload.TYPE)) {
			sendOurPacket.run();
			return;
		}
		if (FabricLoader.getInstance().isModLoaded("armorposer")) {
			PoserStandSync.trySend(stand);
		}
	}

	private static void pushCapeSource(ArmorStand stand, Runnable sendOurPacket) {
		if (ClientPlayNetworking.canSend(HeadSkinCapeSourcePayload.TYPE)) {
			sendOurPacket.run();
			return;
		}
		if (FabricLoader.getInstance().isModLoaded("armorposer")) {
			PoserStandSync.trySend(stand);
		}
	}
}
