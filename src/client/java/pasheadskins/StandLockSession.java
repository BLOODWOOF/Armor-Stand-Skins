package pasheadskins;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.decoration.ArmorStand;

public final class StandLockSession {
	private static final Set<UUID> open = new HashSet<>();

	private StandLockSession() {
	}

	public static boolean frozen(ArmorStand stand) {
		return HeadSkinFlags.hasPassword(stand) && !open.contains(stand.getUUID());
	}

	public static void unlock(UUID standId) {
		if (standId != null) {
			open.add(standId);
		}
	}

	public static void end(UUID standId) {
		open.remove(standId);
	}

	public static void clear() {
		open.clear();
	}

	public static void tick(Minecraft client) {
		if (open.isEmpty()) {
			return;
		}
		if (!isEditor(client.gui.screen())) {
			open.clear();
		}
	}

	private static boolean isEditor(Screen screen) {
		if (screen == null) {
			return false;
		}
		if (screen instanceof HeadSkinStandScreen) {
			return true;
		}
		return screen.getClass().getName().startsWith("com.mrbysco.armorposer.client.gui.");
	}
}
