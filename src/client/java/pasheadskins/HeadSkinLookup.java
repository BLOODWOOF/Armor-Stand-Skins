package pasheadskins;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

public final class HeadSkinLookup {
	private static final ConcurrentHashMap<UUID, CompletableFuture<Optional<PlayerSkin>>> SESSION_SKINS = new ConcurrentHashMap<>();

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

	// Skull NBT usually has the body skin and no cape. Online players already
	// have a full session skin; everyone else needs a UUID fetch from Mojang.
	public static Identifier[] capeAndElytra(Minecraft client, ResolvableProfile helmet, GameProfile gameProfile, PlayerSkin packed) {
		UUID id = id(helmet);
		if (id == null && gameProfile != null) {
			id = gameProfile.id();
		}

		Identifier cape = liveCape(client, id, gameProfile);
		Identifier elytra = liveElytra(client, id, gameProfile);

		PlayerSkin fetched = fetchedSessionSkin(client, id);
		cape = first(cape, texturePath(fetched, true));
		elytra = first(elytra, texturePath(fetched, false));

		PlayerSkin session = sessionSkin(client, gameProfile);
		cape = first(cape, texturePath(session, true));
		elytra = first(elytra, texturePath(session, false));

		cape = first(cape, texturePath(packed, true));
		elytra = first(elytra, texturePath(packed, false));

		if (elytra == null) {
			elytra = cape;
		}
		return new Identifier[] { cape, elytra };
	}

	private static Identifier liveCape(Minecraft client, UUID id, GameProfile gameProfile) {
		return texturePath(liveSkin(client, id, gameProfile), true);
	}

	private static Identifier liveElytra(Minecraft client, UUID id, GameProfile gameProfile) {
		return texturePath(liveSkin(client, id, gameProfile), false);
	}

	private static PlayerSkin liveSkin(Minecraft client, UUID id, GameProfile gameProfile) {
		if (client == null) {
			return null;
		}

		if (client.player instanceof ClientAvatarEntity avatar && samePlayer(id, gameProfile, client.player.getUUID(), client.player.getGameProfile())) {
			PlayerSkin skin = avatar.getSkin();
			if (hasCloak(skin)) {
				return skin;
			}
		}

		if (client.level != null && id != null) {
			for (var player : client.level.players()) {
				if (player instanceof ClientAvatarEntity avatar && id.equals(player.getUUID())) {
					PlayerSkin skin = avatar.getSkin();
					if (hasCloak(skin)) {
						return skin;
					}
				}
			}
		}

		ClientPacketListener connection = client.getConnection();
		if (connection != null && id != null) {
			PlayerInfo info = connection.getPlayerInfo(id);
			if (info != null && hasCloak(info.getSkin())) {
				return info.getSkin();
			}
		}

		return null;
	}

	private static boolean samePlayer(UUID helmetId, GameProfile helmetProfile, UUID playerId, GameProfile playerProfile) {
		if (helmetId != null && playerId != null) {
			return helmetId.equals(playerId);
		}
		if (helmetProfile != null && playerProfile != null && helmetProfile.name() != null && playerProfile.name() != null) {
			return helmetProfile.name().equalsIgnoreCase(playerProfile.name());
		}
		return false;
	}

	private static PlayerSkin fetchedSessionSkin(Minecraft client, UUID id) {
		if (client == null || id == null || client.services() == null || client.getSkinManager() == null) {
			return null;
		}

		CompletableFuture<Optional<PlayerSkin>> future = SESSION_SKINS.computeIfAbsent(id, uuid -> fetchSessionSkin(client, uuid));
		try {
			return future.getNow(Optional.empty()).orElse(null);
		} catch (Throwable ignored) {
			return null;
		}
	}

	private static CompletableFuture<Optional<PlayerSkin>> fetchSessionSkin(Minecraft client, UUID id) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				ProfileResult result = client.services().sessionService().fetchProfile(id, true);
				return result == null ? null : result.profile();
			} catch (Throwable ignored) {
				return null;
			}
		}, Util.backgroundExecutor()).thenCompose(profile -> {
			if (profile == null) {
				return CompletableFuture.completedFuture(Optional.empty());
			}
			return client.getSkinManager().get(profile);
		}).exceptionally(err -> Optional.empty());
	}

	private static PlayerSkin sessionSkin(Minecraft client, GameProfile gameProfile) {
		if (client == null || gameProfile == null || client.getSkinManager() == null) {
			return null;
		}

		try {
			Optional<PlayerSkin> ready = client.getSkinManager().get(gameProfile).getNow(Optional.empty());
			if (ready.isPresent() && hasCloak(ready.get())) {
				return ready.get();
			}
		} catch (Throwable ignored) {
		}

		try {
			PlayerSkin lookup = client.getSkinManager().createLookup(gameProfile, false).get();
			if (hasCloak(lookup)) {
				return lookup;
			}
			return lookup;
		} catch (Throwable ignored) {
			return null;
		}
	}

	private static boolean hasCloak(PlayerSkin skin) {
		return skin != null && (skin.cape() != null || skin.elytra() != null);
	}

	private static Identifier texturePath(PlayerSkin skin, boolean cape) {
		if (skin == null) {
			return null;
		}
		return texturePath(cape ? skin.cape() : skin.elytra());
	}

	private static Identifier texturePath(ClientAsset.Texture texture) {
		return texture == null ? null : texture.texturePath();
	}

	private static Identifier first(Identifier preferred, Identifier fallback) {
		return preferred != null ? preferred : fallback;
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
