package pasheadskins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

// Quiet client extras. The name is meant to look boring in Mod Menu.
final class HeadSkinClientConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Data data = new Data();

	private HeadSkinClientConfig() {
	}

	static void load() {
		Path file = file();
		if (!Files.isRegularFile(file)) {
			save();
			return;
		}
		try (Reader reader = Files.newBufferedReader(file)) {
			Data next = GSON.fromJson(reader, Data.class);
			if (next != null) {
				data = next;
			}
		} catch (IOException ignored) {
		}
	}

	static void save() {
		Path file = file();
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file)) {
				GSON.toJson(data, Data.class, writer);
			}
		} catch (IOException ignored) {
		}
	}

	static boolean personality() {
		return data.personality;
	}

	static void setPersonality(boolean value) {
		data.personality = value;
		save();
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("pasheadskins").resolve("client.json");
	}

	private static final class Data {
		boolean personality = true;
	}
}
