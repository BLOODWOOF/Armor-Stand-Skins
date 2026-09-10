package pasheadskins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.decoration.ArmorStand;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class StandPasswords {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, Map<String, String>>>() {
	}.getType();

	private static final Map<String, Map<UUID, String>> worlds = new HashMap<>();

	private StandPasswords() {
	}

	static String get(ArmorStand stand) {
		String hash = standsInCurrentWorld().get(stand.getUUID());
		return hash == null ? "" : hash;
	}

	static void set(ArmorStand stand, String hash) {
		Map<UUID, String> stands = standsInCurrentWorld();
		if (hash == null || hash.isEmpty()) {
			stands.remove(stand.getUUID());
		} else {
			stands.put(stand.getUUID(), hash);
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
			Map<String, Map<String, String>> raw = GSON.fromJson(reader, FILE_TYPE);
			if (raw == null) {
				return;
			}
			for (Map.Entry<String, Map<String, String>> entry : raw.entrySet()) {
				Map<UUID, String> stands = new HashMap<>();
				if (entry.getValue() != null) {
					for (Map.Entry<String, String> stand : entry.getValue().entrySet()) {
						try {
							if (stand.getValue() != null && !stand.getValue().isEmpty()) {
								stands.put(UUID.fromString(stand.getKey()), stand.getValue());
							}
						} catch (IllegalArgumentException ignored) {
						}
					}
				}
				worlds.put(entry.getKey(), stands);
			}
		} catch (IOException ignored) {
		}
	}

	static void save() {
		Map<String, Map<String, String>> raw = new HashMap<>();
		for (Map.Entry<String, Map<UUID, String>> entry : worlds.entrySet()) {
			Map<String, String> stands = new HashMap<>();
			for (Map.Entry<UUID, String> stand : entry.getValue().entrySet()) {
				if (stand.getValue() != null && !stand.getValue().isEmpty()) {
					stands.put(stand.getKey().toString(), stand.getValue());
				}
			}
			if (!stands.isEmpty()) {
				raw.put(entry.getKey(), stands);
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

	private static Map<UUID, String> standsInCurrentWorld() {
		return worlds.computeIfAbsent(ClientWorldKey.current(), key -> new HashMap<>());
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("pasheadskins").resolve("stand-passwords.json");
	}
}
