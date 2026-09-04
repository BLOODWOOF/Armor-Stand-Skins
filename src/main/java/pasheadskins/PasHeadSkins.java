package pasheadskins;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import pasheadskins.net.HeadSkinDisabledPayload;

public class PasHeadSkins implements ModInitializer {
	public static final String MOD_ID = "pasheadskins";

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.serverboundPlay().register(HeadSkinDisabledPayload.TYPE, HeadSkinDisabledPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(HeadSkinDisabledPayload.TYPE, HeadSkinDisabledPayload.STREAM_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(HeadSkinDisabledPayload.TYPE, (payload, context) -> {
			context.server().execute(() -> handleToggle(context.player(), payload));
		});

		EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
			if (entity instanceof ArmorStand stand) {
				syncTo(player, stand);
			}
		});
	}

	private static void handleToggle(ServerPlayer player, HeadSkinDisabledPayload payload) {
		Entity entity = player.level().getEntity(payload.entityId());
		if (!(entity instanceof ArmorStand stand)) {
			return;
		}
		if (player.distanceToSqr(stand) > 64.0 * 64.0) {
			return;
		}

		HeadSkinFlags.setDisabled(stand, payload.disabled());
		for (ServerPlayer tracker : PlayerLookup.tracking(stand)) {
			syncTo(tracker, stand);
		}
	}

	static void syncTo(ServerPlayer player, ArmorStand stand) {
		ServerPlayNetworking.send(player, new HeadSkinDisabledPayload(stand.getId(), HeadSkinFlags.isDisabled(stand)));
	}
}
