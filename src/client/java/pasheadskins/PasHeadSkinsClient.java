package pasheadskins;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.decoration.ArmorStand;
import pasheadskins.net.HeadSkinDisabledPayload;

public class PasHeadSkinsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		DisabledHeadSkins.load();
		HeadSkinFlags.bind(new HeadSkinFlags.Lookup() {
			@Override
			public boolean isDisabled(ArmorStand stand) {
				return DisabledHeadSkins.isDisabled(stand);
			}

			@Override
			public void setDisabled(ArmorStand stand, boolean disabled) {
				DisabledHeadSkins.setDisabled(stand, disabled);
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(HeadSkinDisabledPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EquippedHeadHider.applyPacket(payload.entityId(), payload.disabled()));
		});

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof ArmorStand stand) {
				EquippedHeadHider.applyLoadedStand(stand);
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> DisabledHeadSkins.save());
	}
}
