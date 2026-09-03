package pasheadskins;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

public final class HeadSkinLookup {
	private HeadSkinLookup() {
	}

	public static HeadSkin fromHelmet(ArmorStand stand) {
		ItemStack helmet = stand.getItemBySlot(EquipmentSlot.HEAD);
		if (helmet.isEmpty()) {
			return null;
		}

		ResolvableProfile profile = helmet.get(DataComponents.PROFILE);
		if (profile == null) {
			return null;
		}

		String name = nameFromProfile(profile);
		if (name == null) {
			return null;
		}

		return new HeadSkin(name, slimArms(profile));
	}

	public static String playerNameFromHelmet(ArmorStand stand) {
		HeadSkin skin = fromHelmet(stand);
		return skin == null ? null : skin.name();
	}

	private static String nameFromProfile(ResolvableProfile profile) {
		Optional<String> name = profile.name();
		if (name.isPresent()) {
			String trimmed = name.get().trim();
			if (!trimmed.isEmpty()) {
				return trimmed;
			}
		}

		GameProfile partial = profile.partialProfile();
		if (partial == null) {
			return null;
		}

		String partialName = partial.name();
		if (partialName != null && !partialName.isBlank()) {
			return partialName.trim();
		}

		UUID id = partial.id();
		if (id == null || id.getMostSignificantBits() == 0L && id.getLeastSignificantBits() == 0L) {
			return null;
		}

		return id.toString();
	}

	private static boolean slimArms(ResolvableProfile profile) {
		Optional<PlayerModelType> patched = profile.skinPatch().model();
		if (patched.isPresent()) {
			return patched.get() == PlayerModelType.SLIM;
		}

		Boolean fromTextures = slimFromTextureProperty(profile.partialProfile());
		if (fromTextures != null) {
			return fromTextures;
		}

		Minecraft client = Minecraft.getInstance();
		if (client == null) {
			return false;
		}

		PlayerSkin skin = client.playerSkinRenderCache().getOrDefault(profile).playerSkin();
		return skin.model() == PlayerModelType.SLIM;
	}

	private static Boolean slimFromTextureProperty(GameProfile profile) {
		if (profile == null) {
			return null;
		}

		Collection<Property> textures = profile.properties().get("textures");
		if (textures == null || textures.isEmpty()) {
			return null;
		}

		Property property = textures.iterator().next();
		try {
			String json = new String(Base64.getDecoder().decode(property.value()), StandardCharsets.UTF_8);
			JsonObject root = JsonParser.parseString(json).getAsJsonObject();
			JsonObject skin = root.getAsJsonObject("textures").getAsJsonObject("SKIN");
			if (skin == null) {
				return null;
			}
			if (!skin.has("metadata")) {
				return false;
			}

			JsonObject metadata = skin.getAsJsonObject("metadata");
			if (metadata == null || !metadata.has("model")) {
				return false;
			}

			return "slim".equalsIgnoreCase(metadata.get("model").getAsString());
		} catch (RuntimeException ignored) {
			return null;
		}
	}

	public record HeadSkin(String name, boolean slim) {
	}
}
