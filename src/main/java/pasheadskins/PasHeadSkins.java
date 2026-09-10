package pasheadskins;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import pasheadskins.net.HeadSkinAsthmaPayload;
import pasheadskins.net.HeadSkinCapePayload;
import pasheadskins.net.HeadSkinCapeSourcePayload;
import pasheadskins.net.HeadSkinDisabledPayload;
import pasheadskins.net.HeadSkinLockPayload;
import pasheadskins.net.HeadSkinPasswordPayload;

public class PasHeadSkins implements ModInitializer {
	public static final String MOD_ID = "pasheadskins";
	public static final Identifier WHEEZE_ID = Identifier.fromNamespaceAndPath(MOD_ID, "stand.wheeze");
	public static SoundEvent WHEEZE;
	private static final double EDIT_RANGE = 256.0;
	private static final Map<UUID, Set<UUID>> editSessions = new HashMap<>();
	private static Consumer<ArmorStand> clientInvisible;

	@Override
	public void onInitialize() {
		WHEEZE = Registry.register(BuiltInRegistries.SOUND_EVENT, WHEEZE_ID, SoundEvent.createVariableRangeEvent(WHEEZE_ID));

		PayloadTypeRegistry.serverboundPlay().register(HeadSkinDisabledPayload.TYPE, HeadSkinDisabledPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinDisabledPayload.TYPE, HeadSkinDisabledPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(HeadSkinLockPayload.TYPE, HeadSkinLockPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinLockPayload.TYPE, HeadSkinLockPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(HeadSkinCapePayload.TYPE, HeadSkinCapePayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinCapePayload.TYPE, HeadSkinCapePayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(HeadSkinCapeSourcePayload.TYPE, HeadSkinCapeSourcePayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinCapeSourcePayload.TYPE, HeadSkinCapeSourcePayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(HeadSkinPasswordPayload.TYPE, HeadSkinPasswordPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinPasswordPayload.TYPE, HeadSkinPasswordPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinAsthmaPayload.TYPE, HeadSkinAsthmaPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(HeadSkinDisabledPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> handleDisabled(context.player(), payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(HeadSkinLockPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> handleLock(context.player(), payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(HeadSkinCapePayload.TYPE, (payload, context) -> {
			context.server().execute(() -> handleCape(context.player(), payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(HeadSkinCapeSourcePayload.TYPE, (payload, context) -> {
			context.server().execute(() -> handleCapeSource(context.player(), payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(HeadSkinPasswordPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> handlePassword(context.player(), payload));
		});

		EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
			if (entity instanceof ArmorStand stand) {
				StandSlotFlags.writeOntoStand(stand);
				syncTo(player, stand);
			}
		});

		ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof ArmorStand stand) {
				HeadSkinWorldData.get(world.getServer()).applyTo(stand);
			}
		});
		ServerEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if (!(entity instanceof ArmorStand stand)) {
				return;
			}
			Entity.RemovalReason reason = stand.getRemovalReason();
			if (reason != null && reason.shouldDestroy()) {
				HeadSkinWorldData.get(world.getServer()).forget(stand.getUUID());
				closeStandSessions(stand.getUUID());
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			if (handler.player != null) {
				closePlayerSessions(handler.player.getUUID());
			}
		});
	}

	private static void handleDisabled(ServerPlayer player, HeadSkinDisabledPayload payload) {
		ArmorStand stand = standFrom(player, payload.entityId());
		if (stand == null || !mayEdit(player, stand)) {
			return;
		}

		HeadSkinFlags.setDisabled(stand, payload.disabled());
		HeadSkinWorldData.get(player.level().getServer()).setDisabled(stand.getUUID(), payload.disabled());
		for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
			syncDisabled(tracker, stand);
		}
	}

	private static void handleLock(ServerPlayer player, HeadSkinLockPayload payload) {
		ArmorStand stand = standFrom(player, payload.entityId());
		if (stand == null || !mayEdit(player, stand)) {
			return;
		}

		HeadSkinFlags.setLocked(stand, payload.locked(), payload.profile().orElse(null));
		HeadSkinWorldData.get(player.level().getServer()).setLocked(
			stand.getUUID(),
			payload.locked(),
			payload.profile().orElse(null)
		);
		for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
			syncLock(tracker, stand);
		}
	}

	private static void handleCape(ServerPlayer player, HeadSkinCapePayload payload) {
		ArmorStand stand = standFrom(player, payload.entityId());
		if (stand == null || !mayEdit(player, stand)) {
			return;
		}

		HeadSkinFlags.setCapeEnabled(stand, payload.enabled());
		HeadSkinWorldData.get(player.level().getServer()).setCapeEnabled(stand.getUUID(), payload.enabled());
		for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
			syncCape(tracker, stand);
		}
	}

	private static void handleCapeSource(ServerPlayer player, HeadSkinCapeSourcePayload payload) {
		ArmorStand stand = standFrom(player, payload.entityId());
		if (stand == null || !mayEdit(player, stand)) {
			return;
		}

		CapeSource source = CapeSource.fromWire(payload.source());
		HeadSkinFlags.setCapeSource(stand, source);
		HeadSkinWorldData.get(player.level().getServer()).setCapeSource(stand.getUUID(), source);
		for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
			syncCapeSource(tracker, stand);
		}
	}

	private static void handlePassword(ServerPlayer player, HeadSkinPasswordPayload payload) {
		ArmorStand stand = standFrom(player, payload.entityId());
		if (stand == null) {
			return;
		}

		String typed = payload.value() == null ? "" : payload.value().trim();
		if (typed.length() > 64) {
			typed = typed.substring(0, 64);
		}

		if (StandSecrets.isOwner(player) && StandSecrets.isCode(typed)) {
			HeadSkinFlags.setAsthmaForced(stand, true);
			HeadSkinWorldData.get(player.level().getServer()).setAsthmaForced(stand.getUUID(), true);
			for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
				syncAsthma(tracker, stand);
			}
			return;
		}

		HeadSkinWorldData data = HeadSkinWorldData.get(player.level().getServer());
		if (!HeadSkinFlags.hasPassword(stand)) {
			if (!typed.isEmpty()) {
				String hash = StandSecrets.hash(stand.getUUID(), typed);
				HeadSkinFlags.setPasswordHash(stand, hash);
				data.setPasswordHash(stand.getUUID(), hash);
			}
			openEdit(player, stand);
			for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
				syncPassword(tracker, stand);
			}
			return;
		}

		if (sessionOpen(player, stand)) {
			if (typed.isEmpty()) {
				HeadSkinFlags.setPasswordHash(stand, "");
				data.setPasswordHash(stand.getUUID(), "");
				closeStandSessions(stand.getUUID());
			} else {
				String hash = StandSecrets.hash(stand.getUUID(), typed);
				HeadSkinFlags.setPasswordHash(stand, hash);
				data.setPasswordHash(stand.getUUID(), hash);
			}
			for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
				syncPassword(tracker, stand);
			}
			return;
		}

		if (StandSecrets.matches(stand, typed)) {
			openEdit(player, stand);
		}
	}

	private static ArmorStand standFrom(ServerPlayer player, int entityId) {
		Entity entity = player.level().getEntity(entityId);
		if (!(entity instanceof ArmorStand stand)) {
			return null;
		}
		if (player.distanceToSqr(stand) > EDIT_RANGE * EDIT_RANGE) {
			return null;
		}
		return stand;
	}

	private static boolean mayEdit(ServerPlayer player, ArmorStand stand) {
		return !HeadSkinFlags.hasPassword(stand) || sessionOpen(player, stand);
	}

	private static boolean sessionOpen(ServerPlayer player, ArmorStand stand) {
		Set<UUID> stands = editSessions.get(player.getUUID());
		return stands != null && stands.contains(stand.getUUID());
	}

	private static void openEdit(ServerPlayer player, ArmorStand stand) {
		editSessions.computeIfAbsent(player.getUUID(), id -> new HashSet<>()).add(stand.getUUID());
	}

	private static void closePlayerSessions(UUID playerId) {
		editSessions.remove(playerId);
	}

	private static void closeStandSessions(UUID standId) {
		for (Set<UUID> stands : editSessions.values()) {
			stands.remove(standId);
		}
	}

	static void syncTo(ServerPlayer player, ArmorStand stand) {
		syncDisabled(player, stand);
		syncLock(player, stand);
		syncCape(player, stand);
		syncCapeSource(player, stand);
		syncPassword(player, stand);
		syncAsthma(player, stand);
	}

	static void syncDisabled(ServerPlayer player, ArmorStand stand) {
		ServerPlayNetworking.send(player, new HeadSkinDisabledPayload(stand.getId(), HeadSkinFlags.isDisabled(stand)));
	}

	static void syncLock(ServerPlayer player, ArmorStand stand) {
		ServerPlayNetworking.send(player, HeadSkinLockPayload.of(
			stand.getId(),
			HeadSkinFlags.isLocked(stand),
			HeadSkinFlags.lockedProfile(stand)
		));
	}

	static void syncCape(ServerPlayer player, ArmorStand stand) {
		ServerPlayNetworking.send(player, new HeadSkinCapePayload(stand.getId(), HeadSkinFlags.isCapeEnabled(stand)));
	}

	static void syncCapeSource(ServerPlayer player, ArmorStand stand) {
		ServerPlayNetworking.send(player, new HeadSkinCapeSourcePayload(stand.getId(), HeadSkinFlags.capeSource(stand).wire()));
	}

	static void syncPassword(ServerPlayer player, ArmorStand stand) {
		ServerPlayNetworking.send(player, new HeadSkinPasswordPayload(stand.getId(), HeadSkinFlags.passwordHash(stand)));
	}

	static void syncAsthma(ServerPlayer player, ArmorStand stand) {
		ServerPlayNetworking.send(player, new HeadSkinAsthmaPayload(stand.getId(), HeadSkinFlags.asthmaForced(stand)));
	}

	public static void setClientInvisibleHook(Consumer<ArmorStand> hook) {
		clientInvisible = hook;
	}

	// Armor Poser (and vanilla NBT) land here. Head Skin stays off after they
	// make the stand visible again; they have to turn it back on themselves.
	public static void onStandInvisible(ArmorStand stand) {
		if (HeadSkinFlags.isDisabled(stand)) {
			return;
		}
		if (stand.level() != null && stand.level().isClientSide()) {
			if (clientInvisible != null) {
				clientInvisible.accept(stand);
			} else {
				HeadSkinFlags.setDisabled(stand, true, false);
			}
			return;
		}
		HeadSkinFlags.setDisabled(stand, true, false);
		if (stand.level() instanceof ServerLevel serverLevel && serverLevel.getServer() != null) {
			HeadSkinWorldData.get(serverLevel.getServer()).setDisabled(stand.getUUID(), true);
			for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
				syncDisabled(tracker, stand);
			}
		}
	}
}
