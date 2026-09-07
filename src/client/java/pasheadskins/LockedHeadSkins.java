package pasheadskins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class LockedHeadSkins {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, Map<String, JsonElement>>>() {
	}.getType();

	private static final Map<String, Map<UUID, ResolvableProfile>> worlds = new HashMap<>();

	private LockedHeadSkins() {
	}

	static boolean isLocked(ArmorStand stand) {
		return lockedProfile(stand) != null;
	}

	static ResolvableProfile lockedProfile(ArmorStand stand) {
		return standsInCurrentWorld().get(stand.getUUID());
	}

	static void setLocked(ArmorStand stand, boolean locked, ResolvableProfile profile) {
		Map<UUID, ResolvableProfile> stands = standsInCurrentWorld();
		if (locked && profile != null) {
			stands.put(stand.getUUID(), profile);
		} else {
			stands.remove(stand.getUUID());
		}
		save();
	}

	static void load() {
		worlds.clear();
		Path file = file();
		if (!Files.isRegularFile(file)) {
			return;
		}

		try (Reader reader = Files.newBufferedReader(file)) {
			Map<String, Map<String, JsonElement>> raw = GSON.fromJson(reader, FILE_TYPE);
			if (raw == null) {
				return;
			}
			for (Map.Entry<String, Map<String, JsonElement>> world : raw.entrySet()) {
				Map<UUID, ResolvableProfile> stands = new HashMap<>();
				if (world.getValue() != null) {
					for (Map.Entry<String, JsonElement> entry : world.getValue().entrySet()) {
						try {
							UUID id = UUID.fromString(entry.getKey());
							ResolvableProfile profile = ResolvableProfile.CODEC.parse(JsonOps.INSTANCE, entry.getValue()).result().orElse(null);
							if (profile != null) {
								stands.put(id, profile);
							}
						} catch (IllegalArgumentException ignored) {
						}
					}
				}
				worlds.put(world.getKey(), stands);
			}
		} catch (IOException ignored) {
		}
	}

	static void save() {
		Map<String, Map<String, JsonElement>> raw = new HashMap<>();
		for (Map.Entry<String, Map<UUID, ResolvableProfile>> world : worlds.entrySet()) {
			Map<String, JsonElement> stands = new HashMap<>();
			for (Map.Entry<UUID, ResolvableProfile> entry : world.getValue().entrySet()) {
				ResolvableProfile.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).result().ifPresent(json -> {
					stands.put(entry.getKey().toString(), json);
				});
			}
			if (!stands.isEmpty()) {
				raw.put(world.getKey(), stands);
			}
		}

		Path file = file();
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				GSON.toJson(raw, FILE_TYPE, writer);
			}
		} catch (IOException ignored) {
		}
	}

	private static Map<UUID, ResolvableProfile> standsInCurrentWorld() {
		return worlds.computeIfAbsent(ClientWorldKey.current(), key -> new HashMap<>());
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("pasheadskins").resolve("locked-skins.json");
	}
}
