package pasheadskins;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;

// Live player skins often reuse one texture id. Copy the pixels for drawing
// so a later skin change cant overwrite what we already locked. The stand
// still stores the original id so chunk reload can find it again.
public final class FrozenSkins {
	private static final ConcurrentHashMap<String, Identifier> PINS = new ConcurrentHashMap<>();

	private FrozenSkins() {
	}

	public static Identifier pin(ArmorStand stand, String kind, Identifier source) {
		if (source == null || stand == null || kind == null || kind.isEmpty()) {
			return source;
		}
		if ("pasheadskins".equals(source.getNamespace()) && source.getPath().startsWith("locked/")) {
			PINS.put(key(stand, kind), source);
			return source;
		}

		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getTextureManager() == null) {
			return source;
		}

		TextureManager textures = client.getTextureManager();
		AbstractTexture texture = textures.getTexture(source);
		if (!(texture instanceof DynamicTexture dynamic)) {
			return source;
		}

		NativeImage pixels = dynamic.getPixels();
		if (pixels == null || pixels.isClosed()) {
			return source;
		}

		try {
			NativeImage copy = new NativeImage(pixels.format(), pixels.getWidth(), pixels.getHeight(), true);
			copy.copyFrom(pixels);
			Identifier pinned = Identifier.fromNamespaceAndPath(
				"pasheadskins",
				"locked/" + stand.getUUID() + "/" + kind
			);
			textures.register(pinned, new DynamicTexture(() -> "locked-" + kind, copy));
			PINS.put(key(stand, kind), pinned);
		} catch (Throwable ignored) {
		}
		return source;
	}

	public static Identifier drawn(ArmorStand stand, String kind, Identifier source) {
		if (stand == null || kind == null) {
			return source;
		}
		Identifier pinned = PINS.get(key(stand, kind));
		return pinned != null ? pinned : source;
	}

	public static void release(ArmorStand stand) {
		if (stand == null) {
			return;
		}
		PINS.remove(key(stand, "body"));
		PINS.remove(key(stand, "cape"));
		PINS.remove(key(stand, "elytra"));
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getTextureManager() == null) {
			return;
		}
		TextureManager textures = client.getTextureManager();
		String prefix = "locked/" + stand.getUUID() + "/";
		tryRelease(textures, prefix + "body");
		tryRelease(textures, prefix + "cape");
		tryRelease(textures, prefix + "elytra");
	}

	private static String key(ArmorStand stand, String kind) {
		return stand.getUUID() + "/" + kind;
	}

	private static void tryRelease(TextureManager textures, String path) {
		try {
			textures.release(Identifier.fromNamespaceAndPath("pasheadskins", path));
		} catch (Throwable ignored) {
		}
	}
}
