package pasheadskins;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;
import pasheadskins.net.HeadSkinCapePayload;
import pasheadskins.net.HeadSkinDisabledPayload;
import pasheadskins.net.HeadSkinLockPayload;

public final class HeadSkinControls {
	private HeadSkinControls() {
	}

	public static void setHeadSkinVisible(ArmorStand stand, boolean visible) {
		HeadSkinFlags.setDisabled(stand, !visible);
		if (ClientPlayNetworking.canSend(HeadSkinDisabledPayload.TYPE)) {
			ClientPlayNetworking.send(new HeadSkinDisabledPayload(stand.getId(), !visible));
		}
	}

	public static boolean setLocked(ArmorStand stand, boolean locked) {
		ResolvableProfile snapshot = null;
		if (locked) {
			snapshot = HeadSkinLookup.snapshotIfReady(HeadSkinLookup.profileFromHelmet(stand));
			if (snapshot == null) {
				return false;
			}
		}
		HeadSkinFlags.setLocked(stand, locked, snapshot);
		if (ClientPlayNetworking.canSend(HeadSkinLockPayload.TYPE)) {
			ClientPlayNetworking.send(HeadSkinLockPayload.of(stand.getId(), locked, snapshot));
		}
		return true;
	}

	public static void setCapeEnabled(ArmorStand stand, boolean enabled) {
		HeadSkinFlags.setCapeEnabled(stand, enabled);
		if (ClientPlayNetworking.canSend(HeadSkinCapePayload.TYPE)) {
			ClientPlayNetworking.send(new HeadSkinCapePayload(stand.getId(), enabled));
		}
	}
}
