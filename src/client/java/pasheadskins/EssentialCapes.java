package pasheadskins;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

// Optional hook into Essential's wardrobe. Vanilla SkinManager.unpackTextures
// wants a signed property, so a homemade profile never loads. We pull the
// 64-char hash and download it the same way cape textures normally do.
final class EssentialCapes {
	private static final Identifier[] NONE = new Identifier[] { null, null };
	private static final String DISABLED = "CAPE_DISABLED";
	private static final ConcurrentHashMap<String, CompletableFuture<Identifier>> BY_HASH = new ConcurrentHashMap<>();

	private EssentialCapes() {
	}

	static boolean present() {
		try {
			if (FabricLoader.getInstance().isModLoaded("essential")) {
				return true;
			}
		} catch (Throwable ignored) {
		}
		try {
			return Holder.available();
		} catch (Throwable ignored) {
			return false;
		}
	}

	static Identifier[] cloak(UUID uuid) {
		if (uuid == null) {
			return NONE;
		}

		try {
			return Holder.cloak(uuid);
		} catch (Throwable ignored) {
			return NONE;
		}
	}

	static void invalidate(UUID uuid) {
		if (uuid == null) {
			return;
		}
		try {
			Holder.invalidate(uuid);
		} catch (Throwable ignored) {
		}
	}

	private static Identifier[] fromHash(Minecraft client, String hash) {
		if (client == null || !usableHash(hash)) {
			return NONE;
		}

		String key = hash.toLowerCase(Locale.ROOT);
		CompletableFuture<Identifier> future = BY_HASH.computeIfAbsent(key, h -> loadCape(client, h));
		try {
			Identifier cape = future.getNow(null);
			return cape == null ? NONE : new Identifier[] { cape, cape };
		} catch (Throwable ignored) {
			return NONE;
		}
	}

	private static CompletableFuture<Identifier> loadCape(Minecraft client, String hash) {
		try {
			SkinTextureDownloader downloader = Holder.downloader(client);
			if (downloader == null || client.gameDirectory == null) {
				return CompletableFuture.completedFuture(null);
			}

			Identifier id = Identifier.fromNamespaceAndPath("pasheadskins", "cape/" + hash);
			Path cache = client.gameDirectory.toPath()
				.resolve("assets")
				.resolve("skins")
				.resolve("pasheadskins")
				.resolve(hash.substring(0, 2))
				.resolve(hash);
			String url = "http://textures.minecraft.net/texture/" + hash;
			return downloader.downloadAndRegisterSkin(id, cache, url, false)
				.thenApply(EssentialCapes::texturePath)
				.exceptionally(err -> null);
		} catch (Throwable ignored) {
			return CompletableFuture.completedFuture(null);
		}
	}

	private static Identifier texturePath(ClientAsset.Texture texture) {
		return texture == null ? null : texture.texturePath();
	}

	private static boolean usableHash(String hash) {
		if (hash == null || hash.length() != 64) {
			return false;
		}
		if (DISABLED.equalsIgnoreCase(hash)) {
			return false;
		}
		for (int i = 0; i < hash.length(); i++) {
			char c = hash.charAt(i);
			boolean hex = c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
			if (!hex) {
				return false;
			}
		}
		return true;
	}

	private static final class Holder {
		private static SkinTextureDownloader downloader;

		private static SkinTextureDownloader downloader(Minecraft client) {
			if (downloader != null) {
				return downloader;
			}
			if (client == null || client.getTextureManager() == null) {
				return null;
			}
			try {
				downloader = new SkinTextureDownloader(client.getProxy(), client.getTextureManager(), client);
				return downloader;
			} catch (Throwable ignored) {
				return null;
			}
		}

		private static Object essential() {
			try {
				Class<?> cls = Class.forName("gg.essential.Essential");
				try {
					return cls.getMethod("getInstance").invoke(null);
				} catch (NoSuchMethodException ignored) {
				}
				try {
					Field instance = cls.getField("INSTANCE");
					return instance.get(null);
				} catch (NoSuchFieldException ignored) {
				}
			} catch (Throwable ignored) {
			}
			return null;
		}

		private static boolean available() {
			return essential() != null;
		}

		private static Identifier[] cloak(UUID uuid) {
			String hash = capeHash(uuid);
			if (!usableHash(hash)) {
				return NONE;
			}
			return fromHash(Minecraft.getInstance(), hash.toLowerCase(Locale.ROOT));
		}

		private static void invalidate(UUID uuid) {
			String hash = capeHash(uuid);
			if (hash != null) {
				BY_HASH.remove(hash.toLowerCase(Locale.ROOT));
			}
		}

		private static String capeHash(UUID uuid) {
			String ingame = hashFrom(ingameManager(), uuid);
			if (usableHash(ingame)) {
				return ingame;
			}
			Object essential = essential();
			Object connection = call(essential, "getConnectionManager", "connectionManager");
			Object cosmetics = call(connection, "getCosmeticsManager", "cosmeticsManager");
			Object infra = call(cosmetics, "getInfraEquippedOutfitsManager", "infraEquippedOutfitsManager");
			String fromInfra = hashFrom(infra, uuid);
			if (usableHash(fromInfra)) {
				return fromInfra;
			}
			return hashFrom(connection, uuid);
		}

		private static Object ingameManager() {
			Minecraft client = Minecraft.getInstance();
			if (client == null) {
				return null;
			}
			Object connection = client.getConnection();
			if (connection == null) {
				return null;
			}
			if (findCapeHash(connection.getClass()) != null) {
				return connection;
			}
			Object named = call(
				connection,
				"getEssential$ingameEquippedOutfitsManager",
				"getEssential$equippedOutfitsManager",
				"essential$getIngameEquippedOutfitsManager",
				"getEquippedOutfitsManager",
				"equippedOutfitsManager",
				"essential$getEquippedOutfitsManager",
				"essential$equippedOutfitsManager",
				"ingameEquippedOutfitsManager"
			);
			if (named != null && findCapeHash(named.getClass()) != null) {
				return named;
			}
			return ownerOn(connection);
		}

		private static Object ownerOn(Object root) {
			if (root == null) {
				return null;
			}
			Class<?> type = root.getClass();
			for (Method method : type.getMethods()) {
				if (method.getParameterCount() != 0 || method.getReturnType() == void.class) {
					continue;
				}
				String name = method.getName().toLowerCase(Locale.ROOT);
				if (!(name.contains("outfit") || name.contains("cape") || name.contains("essential"))) {
					continue;
				}
				try {
					method.setAccessible(true);
					Object value = method.invoke(root);
					if (value != null && findCapeHash(value.getClass()) != null) {
						return value;
					}
				} catch (Throwable ignored) {
				}
			}
			for (Class<?> cursor = type; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
				for (Field field : cursor.getDeclaredFields()) {
					if (Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive()) {
						continue;
					}
					String name = field.getName().toLowerCase(Locale.ROOT);
					if (!(name.contains("outfit") || name.contains("cape") || name.contains("essential"))) {
						continue;
					}
					try {
						field.setAccessible(true);
						Object value = field.get(root);
						if (value != null && findCapeHash(value.getClass()) != null) {
							return value;
						}
					} catch (Throwable ignored) {
					}
				}
			}
			return null;
		}

		private static String hashFrom(Object manager, UUID uuid) {
			if (manager == null || uuid == null) {
				return null;
			}
			Method method = findCapeHash(manager.getClass());
			if (method == null) {
				return null;
			}
			try {
				Object value = method.invoke(manager, uuid);
				return value == null ? null : String.valueOf(value);
			} catch (Throwable ignored) {
				return null;
			}
		}

		private static Method findCapeHash(Class<?> type) {
			if (type == null) {
				return null;
			}
			for (Class<?> cursor = type; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
				Method method = methodNamed(cursor, "getCapeHash");
				if (method != null) {
					return method;
				}
			}
			for (Class<?> iface : type.getInterfaces()) {
				Method method = methodNamed(iface, "getCapeHash");
				if (method != null) {
					return method;
				}
			}
			return null;
		}

		private static Method methodNamed(Class<?> type, String name) {
			try {
				Method method = type.getMethod(name, UUID.class);
				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException ignored) {
			}
			try {
				Method method = type.getDeclaredMethod(name, UUID.class);
				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException ignored) {
			}
			return null;
		}

		private static Object call(Object target, String... names) {
			if (target == null) {
				return null;
			}
			Class<?> type = target.getClass();
			for (String name : names) {
				try {
					Method method = type.getMethod(name);
					method.setAccessible(true);
					return method.invoke(target);
				} catch (Throwable ignored) {
				}
				try {
					Method method = type.getDeclaredMethod(name);
					method.setAccessible(true);
					return method.invoke(target);
				} catch (Throwable ignored) {
				}
				try {
					Field field = type.getField(name);
					field.setAccessible(true);
					return field.get(target);
				} catch (Throwable ignored) {
				}
				try {
					Field field = type.getDeclaredField(name);
					field.setAccessible(true);
					return field.get(target);
				} catch (Throwable ignored) {
				}
			}
			return null;
		}
	}
}
