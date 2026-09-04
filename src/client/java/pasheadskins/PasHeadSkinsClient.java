package pasheadskins;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import pasheadskins.net.HeadSkinDisabledPayload;

public class PasHeadSkinsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(HeadSkinDisabledPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EquippedHeadHider.applyPacket(payload.entityId(), payload.disabled()));
		});
	}
}
