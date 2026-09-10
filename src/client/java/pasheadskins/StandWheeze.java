package pasheadskins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;

final class StandWheeze {
	private static final int MIN_DELAY = 18 * 20;
	private static final int MAX_DELAY = 48 * 20;
	private static final Map<UUID, Integer> nextPlay = new HashMap<>();

	private StandWheeze() {
	}

	static void tick(Minecraft client) {
		if (!HeadSkinClientConfig.personality() || client.level == null || client.player == null || client.isPaused()) {
			return;
		}
		if (PasHeadSkins.WHEEZE == null) {
			return;
		}

		AABB box = client.player.getBoundingBox().inflate(24.0);
		Map<UUID, Boolean> seen = new HashMap<>();
		for (ArmorStand stand : client.level.getEntitiesOfClass(ArmorStand.class, box)) {
			seen.put(stand.getUUID(), Boolean.TRUE);
			if (!shouldWheeze(stand)) {
				nextPlay.remove(stand.getUUID());
				continue;
			}
			int now = stand.tickCount;
			Integer due = nextPlay.get(stand.getUUID());
			if (due == null) {
				nextPlay.put(stand.getUUID(), now + delay(stand));
				continue;
			}
			if (now < due) {
				continue;
			}
			float pitch = 0.88F + stand.getRandom().nextFloat() * 0.18F;
			client.level.playLocalSound(stand.getX(), stand.getY() + 1.4, stand.getZ(), PasHeadSkins.WHEEZE, SoundSource.NEUTRAL, 0.85F, pitch, false);
			nextPlay.put(stand.getUUID(), now + delay(stand));
		}

		Iterator<UUID> it = nextPlay.keySet().iterator();
		while (it.hasNext()) {
			if (!seen.containsKey(it.next())) {
				it.remove();
			}
		}
	}

	static void clear() {
		nextPlay.clear();
	}

	private static boolean shouldWheeze(ArmorStand stand) {
		if (HeadSkinFlags.isDisabled(stand) || !HeadSkinFlags.isAsthmatic(stand)) {
			return false;
		}
		return HeadSkinFlags.isLocked(stand) || HeadSkinLookup.profileFromHelmet(stand) != null;
	}

	private static int delay(ArmorStand stand) {
		return stand.getRandom().nextInt(MIN_DELAY, MAX_DELAY + 1);
	}
}
