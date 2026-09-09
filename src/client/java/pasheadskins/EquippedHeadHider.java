package pasheadskins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.HashMap;
import java.util.Map;

public final class EquippedHeadHider {
	private static final Map<Integer, Boolean> pendingDisabled = new HashMap<>();
	private static final Map<Integer, HeadSkinLockPayloadState> pendingLock = new HashMap<>();
	private static final Map<Integer, Boolean> pendingCape = new HashMap<>();
	private static final Map<Integer, CapeSource> pendingCapeSource = new HashMap<>();

	private EquippedHeadHider() {
	}

	public static void hideOnState(ArmorStandRenderState state) {
		state.headEquipment = ItemStack.EMPTY;
		if (state.headItem != null) {
			state.headItem.clear();
		}
		state.wornHeadProfile = null;
		state.wornHeadType = null;
	}

	public static void applyDisabledPacket(int entityId, boolean disabled) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			pendingDisabled.put(entityId, disabled);
			return;
		}

		Entity entity = client.level.getEntity(entityId);
		if (entity instanceof ArmorStand stand) {
			HeadSkinFlags.setDisabled(stand, disabled);
			pendingDisabled.remove(entityId);
			return;
		}

		pendingDisabled.put(entityId, disabled);
	}

	public static void applyLockPacket(int entityId, boolean locked, ResolvableProfile profile) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			pendingLock.put(entityId, new HeadSkinLockPayloadState(locked, profile));
			return;
		}

		Entity entity = client.level.getEntity(entityId);
		if (entity instanceof ArmorStand stand) {
			HeadSkinFlags.setLocked(stand, locked, profile);
			pendingLock.remove(entityId);
			return;
		}

		pendingLock.put(entityId, new HeadSkinLockPayloadState(locked, profile));
	}

	public static void applyCapePacket(int entityId, boolean enabled) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			pendingCape.put(entityId, enabled);
			return;
		}

		Entity entity = client.level.getEntity(entityId);
		if (entity instanceof ArmorStand stand) {
			HeadSkinFlags.setCapeEnabled(stand, enabled);
			pendingCape.remove(entityId);
			return;
		}

		pendingCape.put(entityId, enabled);
	}

	public static void applyCapeSourcePacket(int entityId, CapeSource source) {
		Minecraft client = Minecraft.getInstance();
		CapeSource next = source == null ? CapeSource.BOTH : source;
		if (client.level == null) {
			pendingCapeSource.put(entityId, next);
			return;
		}

		Entity entity = client.level.getEntity(entityId);
		if (entity instanceof ArmorStand stand) {
			HeadSkinFlags.setCapeSource(stand, next);
			pendingCapeSource.remove(entityId);
			return;
		}

		pendingCapeSource.put(entityId, next);
	}

	public static void applyLoadedStand(ArmorStand stand) {
		Boolean disabled = pendingDisabled.remove(stand.getId());
		HeadSkinLockPayloadState lock = pendingLock.remove(stand.getId());
		Boolean cape = pendingCape.remove(stand.getId());
		CapeSource capeSource = pendingCapeSource.remove(stand.getId());
		boolean fromPacket = disabled != null || lock != null || cape != null || capeSource != null;

		if (fromPacket) {
			if (disabled != null) {
				HeadSkinFlags.setDisabled(stand, disabled);
			}
			if (lock != null) {
				HeadSkinFlags.setLocked(stand, lock.locked(), lock.profile());
			}
			if (cape != null) {
				HeadSkinFlags.setCapeEnabled(stand, cape);
			}
			if (capeSource != null) {
				HeadSkinFlags.setCapeSource(stand, capeSource);
			} else {
				applySavedCapeSource(stand);
			}
			return;
		}

		if (!HeadSkinFlags.packetChannelOpen() && StandSlotFlags.hasRecord(stand)) {
			StandSlotFlags.applyToHolder(stand);
			return;
		}

		if (DisabledHeadSkins.isDisabled(stand) && stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setDisabled(true);
		}
		if (LockedHeadSkins.isLocked(stand) && stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setLocked(true, LockedHeadSkins.lockedProfile(stand));
		}
		if (EnabledCapes.isEnabled(stand) && stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setCapeEnabled(true);
		}
		applySavedCapeSource(stand);
	}

	private static void applySavedCapeSource(ArmorStand stand) {
		if (!(stand instanceof HeadSkinHolder holder)) {
			return;
		}
		CapeSource stored = CapeSources.get(stand);
		if (stored != CapeSource.BOTH) {
			holder.pasheadskins$setCapeSource(stored);
		}
	}

	private record HeadSkinLockPayloadState(boolean locked, ResolvableProfile profile) {
	}
}
