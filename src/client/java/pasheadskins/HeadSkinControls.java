package pasheadskins;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;
import pasheadskins.net.HeadSkinCapePayload;
import pasheadskins.net.HeadSkinCapeSourcePayload;
import pasheadskins.net.HeadSkinDisabledPayload;
import pasheadskins.net.HeadSkinLockPayload;
import pasheadskins.net.HeadSkinPasswordPayload;

public final class HeadSkinControls {
	private HeadSkinControls() {
	}

	public static void setHeadSkinVisible(ArmorStand stand, boolean visible) {
		if (StandLockSession.frozen(stand)) {
			return;
		}
		boolean persist = persistLocalJson();
		HeadSkinFlags.setDisabled(stand, !visible, persist);
		push(stand, () -> ClientPlayNetworking.send(new HeadSkinDisabledPayload(stand.getId(), !visible)));
	}

	public static boolean setLocked(ArmorStand stand, boolean locked) {
		if (StandLockSession.frozen(stand)) {
			return false;
		}
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
		if (StandLockSession.frozen(stand)) {
			return;
		}
		boolean persist = persistLocalJson();
		HeadSkinFlags.setCapeEnabled(stand, enabled, persist);
		push(stand, () -> ClientPlayNetworking.send(new HeadSkinCapePayload(stand.getId(), enabled)));
	}

	public static void setCapeSource(ArmorStand stand, CapeSource source) {
		if (StandLockSession.frozen(stand)) {
			return;
		}
		CapeSource next = source == null ? CapeSource.BOTH : source;
		boolean persist = persistCapeSource();
		HeadSkinFlags.setCapeSource(stand, next, persist);
		pushCapeSource(stand, () -> ClientPlayNetworking.send(new HeadSkinCapeSourcePayload(stand.getId(), next.wire())));
	}

	public static boolean submitPassword(ArmorStand stand, String typed) {
		String text = typed == null ? "" : typed.trim();
		if (text.length() > 64) {
			text = text.substring(0, 64);
		}

		if (StandSecrets.isOwner(Minecraft.getInstance().player) && StandSecrets.isCode(text)) {
			HeadSkinFlags.setAsthmaForced(stand, true);
			if (persistSecrets()) {
				ForcedAsthma.set(stand, true);
			}
			sendPassword(stand, text);
			if (Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.sendSystemMessage(Component.literal("that stand's gonna sound a little rough now"));
			}
			return true;
		}

		if (StandLockSession.frozen(stand)) {
			if (!StandSecrets.matches(stand, text)) {
				return false;
			}
			StandLockSession.unlock(stand.getUUID());
			sendPassword(stand, text);
			return true;
		}

		String hash = text.isEmpty() ? "" : StandSecrets.hash(stand.getUUID(), text);
		HeadSkinFlags.setPasswordHash(stand, hash);
		if (persistSecrets()) {
			StandPasswords.set(stand, hash);
		}
		sendPassword(stand, text);
		if (text.isEmpty()) {
			StandLockSession.end(stand.getUUID());
		} else {
			StandLockSession.unlock(stand.getUUID());
		}
		return true;
	}

	private static void sendPassword(ArmorStand stand, String typed) {
		if (ClientPlayNetworking.canSend(HeadSkinPasswordPayload.TYPE)) {
			ClientPlayNetworking.send(new HeadSkinPasswordPayload(stand.getId(), typed));
		}
	}

	private static boolean persistSecrets() {
		return !ClientPlayNetworking.canSend(HeadSkinPasswordPayload.TYPE);
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
		pushAll(stand, sendOurPacket);
	}

	private static void pushCapeSource(ArmorStand stand, Runnable sendOurPacket) {
		pushAll(stand, sendOurPacket);
	}

	// Send every channel we have. Missed packets used to stick forever if we
	// treated our custom packet as the only copy.
	private static void pushAll(ArmorStand stand, Runnable sendOurPacket) {
		StandSlotFlags.writeOntoStand(stand);
		if (ClientPlayNetworking.canSend(HeadSkinDisabledPayload.TYPE)) {
			sendOurPacket.run();
		}
		if (FabricLoader.getInstance().isModLoaded("armorposer")) {
			PoserStandSync.trySend(stand);
		}
	}
}
