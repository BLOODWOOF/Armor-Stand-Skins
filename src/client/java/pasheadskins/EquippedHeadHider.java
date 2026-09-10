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
	private static final Map<Integer, String> pendingPassword = new HashMap<>();
	private static final Map<Integer, Boolean> pendingAsthma = new HashMap<>();

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
			HeadSkinFlags.setDisabled(stand, disabled, false);
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
			HeadSkinFlags.setLocked(stand, locked, profile, false);
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
			HeadSkinFlags.setCapeEnabled(stand, enabled, false);
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
			HeadSkinFlags.setCapeSource(stand, next, false);
			pendingCapeSource.remove(entityId);
			return;
		}

		pendingCapeSource.put(entityId, next);
	}

	public static void applyPasswordPacket(int entityId, String hash) {
		Minecraft client = Minecraft.getInstance();
		String next = hash == null ? "" : hash;
		if (client.level == null) {
			pendingPassword.put(entityId, next);
			return;
		}

		Entity entity = client.level.getEntity(entityId);
		if (entity instanceof ArmorStand stand) {
			HeadSkinFlags.setPasswordHash(stand, next);
			pendingPassword.remove(entityId);
			return;
		}

		pendingPassword.put(entityId, next);
	}

	public static void applyAsthmaPacket(int entityId, boolean forced) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			pendingAsthma.put(entityId, forced);
			return;
		}

		Entity entity = client.level.getEntity(entityId);
		if (entity instanceof ArmorStand stand) {
			HeadSkinFlags.setAsthmaForced(stand, forced);
			pendingAsthma.remove(entityId);
			return;
		}

		pendingAsthma.put(entityId, forced);
	}

	public static void clearPending() {
		pendingDisabled.clear();
		pendingLock.clear();
		pendingCape.clear();
		pendingCapeSource.clear();
		pendingPassword.clear();
		pendingAsthma.clear();
	}

	public static void applyLoadedStand(ArmorStand stand) {
		if (StandSlotFlags.hasRecord(stand)) {
			StandSlotFlags.applyToHolder(stand);
		}

		Boolean disabled = pendingDisabled.remove(stand.getId());
		HeadSkinLockPayloadState lock = pendingLock.remove(stand.getId());
		Boolean cape = pendingCape.remove(stand.getId());
		CapeSource capeSource = pendingCapeSource.remove(stand.getId());
		String password = pendingPassword.remove(stand.getId());
		Boolean asthma = pendingAsthma.remove(stand.getId());
		boolean fromPacket = disabled != null || lock != null || cape != null || capeSource != null;

		if (fromPacket) {
			if (disabled != null) {
				HeadSkinFlags.setDisabled(stand, disabled, false);
			}
			if (lock != null) {
				HeadSkinFlags.setLocked(stand, lock.locked(), lock.profile(), false);
			}
			if (cape != null) {
				HeadSkinFlags.setCapeEnabled(stand, cape, false);
			}
			if (capeSource != null) {
				HeadSkinFlags.setCapeSource(stand, capeSource, false);
			} else {
				applySavedCapeSource(stand);
			}
		} else if (!StandSlotFlags.hasRecord(stand)) {
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

		if (password != null) {
			HeadSkinFlags.setPasswordHash(stand, password);
		} else if (stand instanceof HeadSkinHolder holder && holder.pasheadskins$passwordHash().isEmpty()) {
			String stored = StandPasswords.get(stand);
			if (!stored.isEmpty()) {
				holder.pasheadskins$setPasswordHash(stored);
			}
		}

		if (asthma != null) {
			HeadSkinFlags.setAsthmaForced(stand, asthma);
		} else if (stand instanceof HeadSkinHolder holder && !holder.pasheadskins$asthmaForced() && ForcedAsthma.isForced(stand)) {
			holder.pasheadskins$setAsthmaForced(true);
		}
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
