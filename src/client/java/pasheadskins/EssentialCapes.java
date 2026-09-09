package pasheadskins;

import com.google.common.collect.HashMultimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;

// Optional hook into Essential's wardrobe. We only keep 64-char texture hashes;
// their 3d cape cosmetics dont fit the vanilla cloak mesh.
final class EssentialCapes {
	private static final Identifier[] NONE = new Identifier[] { null, null };
	private static final String DISABLED = "CAPE_DISABLED";
	private static final ConcurrentHashMap<String, CompletableFuture<Optional<PlayerSkin>>> BY_HASH = new ConcurrentHashMap<>();

	private EssentialCapes() {
	}

	static boolean present() {
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
		if (client == null || client.getSkinManager() == null || !usableHash(hash)) {
			return NONE;
		}

		CompletableFuture<Optional<PlayerSkin>> future = BY_HASH.computeIfAbsent(hash, key -> loadCape(client, key));
		try {
			PlayerSkin skin = future.getNow(Optional.empty()).orElse(null);
			Identifier cape = texturePath(skin);
			return new Identifier[] { cape, cape };
		} catch (Throwable ignored) {
			return NONE;
		}
	}

	private static CompletableFuture<Optional<PlayerSkin>> loadCape(Minecraft client, String hash) {
		try {
			String json = "{\"textures\":{\"CAPE\":{\"url\":\"http://textures.minecraft.net/texture/" + hash + "\"}}}";
			String encoded = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
			HashMultimap<String, Property> values = HashMultimap.create();
			values.put("textures", new Property("textures", encoded));
			UUID dummy = UUID.nameUUIDFromBytes(("pasheadskins-cape-" + hash).getBytes(StandardCharsets.UTF_8));
			GameProfile profile = new GameProfile(dummy, "cape", new PropertyMap(values));
			return client.getSkinManager().get(profile);
		} catch (Throwable ignored) {
			return CompletableFuture.completedFuture(Optional.empty());
		}
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

	private static Identifier texturePath(PlayerSkin skin) {
		if (skin == null) {
			return null;
		}
		ClientAsset.Texture cape = skin.cape();
		if (cape == null) {
			cape = skin.elytra();
		}
		return cape == null ? null : cape.texturePath();
	}

	private static final class Holder {
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
			return hashFrom(infra, uuid);
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
			Object hash = hashMethodOwner(connection);
			if (hash != null) {
				return hash;
			}
			return call(
				connection,
				"getEquippedOutfitsManager",
				"equippedOutfitsManager",
				"essential$getEquippedOutfitsManager",
				"essential$equippedOutfitsManager"
			);
		}

		private static Object hashMethodOwner(Object root) {
			if (root == null) {
				return null;
			}
			if (findCapeHash(root.getClass()) != null) {
				return root;
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
			for (Class<?> cursor = type; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
				try {
					Method method = cursor.getMethod("getCapeHash", UUID.class);
					method.setAccessible(true);
					return method;
				} catch (NoSuchMethodException ignored) {
				}
				try {
					Method method = cursor.getDeclaredMethod("getCapeHash", UUID.class);
					method.setAccessible(true);
					return method;
				} catch (NoSuchMethodException ignored) {
				}
			}
			for (Class<?> iface : type.getInterfaces()) {
				try {
					Method method = iface.getMethod("getCapeHash", UUID.class);
					method.setAccessible(true);
					return method;
				} catch (NoSuchMethodException ignored) {
				}
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
