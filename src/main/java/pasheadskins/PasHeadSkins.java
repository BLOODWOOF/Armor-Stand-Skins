package pasheadskins;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import pasheadskins.net.HeadSkinCapePayload;
import pasheadskins.net.HeadSkinCapeSourcePayload;
import pasheadskins.net.HeadSkinDisabledPayload;
import pasheadskins.net.HeadSkinLockPayload;

public class PasHeadSkins implements ModInitializer {
	public static final String MOD_ID = "pasheadskins";

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(HeadSkinDisabledPayload.TYPE, HeadSkinDisabledPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinDisabledPayload.TYPE, HeadSkinDisabledPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(HeadSkinLockPayload.TYPE, HeadSkinLockPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinLockPayload.TYPE, HeadSkinLockPayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(HeadSkinCapePayload.TYPE, HeadSkinCapePayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinCapePayload.TYPE, HeadSkinCapePayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(HeadSkinCapeSourcePayload.TYPE, HeadSkinCapeSourcePayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinCapeSourcePayload.TYPE, HeadSkinCapeSourcePayload.STREAM_CODEC);

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

		EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
			if (entity instanceof ArmorStand stand) {
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
			}
		});
	}

	private static void handleDisabled(ServerPlayer player, HeadSkinDisabledPayload payload) {
		ArmorStand stand = standFrom(player, payload.entityId());
		if (stand == null) {
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
		if (stand == null) {
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
		if (stand == null) {
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
		if (stand == null) {
			return;
		}

		CapeSource source = CapeSource.fromWire(payload.source());
		HeadSkinFlags.setCapeSource(stand, source);
		HeadSkinWorldData.get(player.level().getServer()).setCapeSource(stand.getUUID(), source);
		for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
			syncCapeSource(tracker, stand);
		}
	}

	private static ArmorStand standFrom(ServerPlayer player, int entityId) {
		Entity entity = player.level().getEntity(entityId);
		if (!(entity instanceof ArmorStand stand)) {
			return null;
		}
		if (player.distanceToSqr(stand) > 64.0 * 64.0) {
			return null;
		}
		return stand;
	}

	static void syncTo(ServerPlayer player, ArmorStand stand) {
		syncDisabled(player, stand);
		syncLock(player, stand);
		syncCape(player, stand);
		syncCapeSource(player, stand);
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
}
