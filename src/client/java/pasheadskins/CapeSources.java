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
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

final class CapeSources {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, Map<String, String>>>() {
	}.getType();

	private static final Map<String, Map<UUID, CapeSource>> worlds = new HashMap<>();

	private CapeSources() {
	}

	static CapeSource get(ArmorStand stand) {
		CapeSource source = standsInCurrentWorld().get(stand.getUUID());
		return source == null ? CapeSource.BOTH : source;
	}

	static void set(ArmorStand stand, CapeSource source) {
		Map<UUID, CapeSource> stands = standsInCurrentWorld();
		if (source == null || source == CapeSource.BOTH) {
			stands.remove(stand.getUUID());
		} else {
			stands.put(stand.getUUID(), source);
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
				Map<UUID, CapeSource> stands = new HashMap<>();
				if (entry.getValue() != null) {
					for (Map.Entry<String, String> stand : entry.getValue().entrySet()) {
						try {
							CapeSource source = parse(stand.getValue());
							if (source != CapeSource.BOTH) {
								stands.put(UUID.fromString(stand.getKey()), source);
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
		for (Map.Entry<String, Map<UUID, CapeSource>> entry : worlds.entrySet()) {
			Map<String, String> stands = new HashMap<>();
			for (Map.Entry<UUID, CapeSource> stand : entry.getValue().entrySet()) {
				if (stand.getValue() != null && stand.getValue() != CapeSource.BOTH) {
					stands.put(stand.getKey().toString(), stand.getValue().name().toLowerCase(Locale.ROOT));
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

	private static CapeSource parse(String raw) {
		if (raw == null) {
			return CapeSource.BOTH;
		}
		return switch (raw.trim().toLowerCase(Locale.ROOT)) {
			case "mojang", "1" -> CapeSource.MOJANG;
			case "essential", "2" -> CapeSource.ESSENTIAL;
			default -> CapeSource.BOTH;
		};
	}

	private static Map<UUID, CapeSource> standsInCurrentWorld() {
		return worlds.computeIfAbsent(ClientWorldKey.current(), key -> new HashMap<>());
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("pasheadskins").resolve("cape-sources.json");
	}
}
