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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class ForcedAsthma {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Type FILE_TYPE = new TypeToken<Map<String, List<String>>>() {
	}.getType();

	private static final Map<String, Set<UUID>> worlds = new HashMap<>();

	private ForcedAsthma() {
	}

	static boolean isForced(ArmorStand stand) {
		return standsInCurrentWorld().contains(stand.getUUID());
	}

	static void set(ArmorStand stand, boolean forced) {
		Set<UUID> stands = standsInCurrentWorld();
		if (forced) {
			stands.add(stand.getUUID());
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
			Map<String, List<String>> raw = GSON.fromJson(reader, FILE_TYPE);
			if (raw == null) {
				return;
			}
			for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
				Set<UUID> stands = new LinkedHashSet<>();
				if (entry.getValue() != null) {
					for (String id : entry.getValue()) {
						try {
							stands.add(UUID.fromString(id));
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
		Map<String, List<String>> raw = new HashMap<>();
		for (Map.Entry<String, Set<UUID>> entry : worlds.entrySet()) {
			List<String> ids = new ArrayList<>();
			for (UUID id : entry.getValue()) {
				ids.add(id.toString());
			}
			if (!ids.isEmpty()) {
				raw.put(entry.getKey(), ids);
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

	private static Set<UUID> standsInCurrentWorld() {
		return worlds.computeIfAbsent(ClientWorldKey.current(), key -> new LinkedHashSet<>());
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("pasheadskins").resolve("forced-asthma.json");
	}
}
