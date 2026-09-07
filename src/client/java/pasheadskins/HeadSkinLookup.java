package pasheadskins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.UUID;

public final class HeadSkinLookup {
	private HeadSkinLookup() {
	}

	public static ResolvableProfile profileFromHelmet(ArmorStand stand) {
		ItemStack helmet = stand.getItemBySlot(EquipmentSlot.HEAD);
		if (helmet.isEmpty()) {
			return null;
		}
		return helmet.get(DataComponents.PROFILE);
	}

	public static boolean sameIdentity(ResolvableProfile a, ResolvableProfile b) {
		if (a == null || b == null) {
			return false;
		}

		UUID idA = id(a);
		UUID idB = id(b);
		if (idA != null && idB != null) {
			return idA.equals(idB);
		}

		String nameA = name(a);
		String nameB = name(b);
		return !nameA.isEmpty() && nameA.equalsIgnoreCase(nameB);
	}

	public static ResolvableProfile snapshotIfReady(ResolvableProfile helmet) {
		if (helmet == null) {
			return null;
		}

		Minecraft client = Minecraft.getInstance();
		if (client == null || client.playerSkinRenderCache() == null) {
			return null;
		}

		var info = client.playerSkinRenderCache().getOrDefault(helmet);
		if (info == null || info.playerSkin() == null || info.playerSkin().body() == null) {
			return null;
		}

		GameProfile resolved = info.gameProfile();
		if (resolved == null || resolved.properties() == null || !resolved.properties().containsKey("textures")) {
			return null;
		}

		return ResolvableProfile.createResolved(resolved);
	}

	private static UUID id(ResolvableProfile profile) {
		GameProfile partial = profile.partialProfile();
		if (partial == null || partial.id() == null) {
			return null;
		}
		UUID id = partial.id();
		if (id.getMostSignificantBits() == 0L && id.getLeastSignificantBits() == 0L) {
			return null;
		}
		return id;
	}

	private static String name(ResolvableProfile profile) {
		GameProfile partial = profile.partialProfile();
		if (partial != null && partial.name() != null && !partial.name().isBlank()) {
			return partial.name();
		}
		return profile.name().orElse("");
	}
}
