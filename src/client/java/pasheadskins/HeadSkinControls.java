package pasheadskins;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;
import pasheadskins.net.HeadSkinAsthmaPayload;
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
		if (StandLockSession.frozen(stand) && locked) {
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
		if (locked && look != null && stand instanceof HeadSkinHolder holder) {
			FrozenSkins.pin(stand, "body", look.body());
			FrozenSkins.pin(stand, "cape", look.cape());
			FrozenSkins.pin(stand, "elytra", look.elytra());
			holder.pasheadskins$setLockedBody(look.body(), look.slim());
			holder.pasheadskins$setLockedCloak(look.cape(), look.elytra());
		} else if (!locked) {
			FrozenSkins.release(stand);
		}
		HeadSkinFlags.setLocked(stand, locked, snapshot, persist);
		ResolvableProfile toSend = snapshot;
		Identifier body = look == null ? null : look.body();
		boolean slim = look != null && look.slim();
		push(stand, () -> ClientPlayNetworking.send(HeadSkinLockPayload.of(stand.getId(), locked, toSend, body, slim)));
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
			boolean next = !HeadSkinFlags.asthmaForced(stand);
			HeadSkinFlags.setAsthmaForced(stand, next);
			ForcedAsthma.set(stand, next);
			pushAsthma(stand, next);
			if (Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.sendSystemMessage(Component.literal(
					next ? "that stand's gonna sound a little rough now" : "ok, it's breathing normal again"
				));
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

	private static void pushAsthma(ArmorStand stand, boolean forced) {
		StandSlotFlags.writeOntoStand(stand);
		if (ClientPlayNetworking.canSend(HeadSkinAsthmaPayload.TYPE)) {
			ClientPlayNetworking.send(new HeadSkinAsthmaPayload(stand.getId(), forced));
		} else if (forced && ClientPlayNetworking.canSend(HeadSkinPasswordPayload.TYPE)) {
			sendPassword(stand, "wheeze");
		}
		if (FabricLoader.getInstance().isModLoaded("armorposer")) {
			PoserStandSync.trySend(stand);
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
