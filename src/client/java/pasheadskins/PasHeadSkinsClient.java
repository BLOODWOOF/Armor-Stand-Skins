package pasheadskins;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;
import pasheadskins.net.HeadSkinCapePayload;
import pasheadskins.net.HeadSkinCapeSourcePayload;
import pasheadskins.net.HeadSkinDisabledPayload;
import pasheadskins.net.HeadSkinLockPayload;

public class PasHeadSkinsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		DisabledHeadSkins.load();
		LockedHeadSkins.load();
		EnabledCapes.load();
		CapeSources.load();
		HeadSkinFlags.setPacketChannel(() -> ClientPlayNetworking.canSend(HeadSkinDisabledPayload.TYPE));
		PasHeadSkins.setClientInvisibleHook(stand -> HeadSkinControls.setHeadSkinVisible(stand, false));
		HeadSkinFlags.bind(new HeadSkinFlags.Lookup() {
			@Override
			public boolean isDisabled(ArmorStand stand) {
				return DisabledHeadSkins.isDisabled(stand);
			}

			@Override
			public void setDisabled(ArmorStand stand, boolean disabled) {
				DisabledHeadSkins.setDisabled(stand, disabled);
			}

			@Override
			public boolean isLocked(ArmorStand stand) {
				return LockedHeadSkins.isLocked(stand);
			}

			@Override
			public ResolvableProfile lockedProfile(ArmorStand stand) {
				return LockedHeadSkins.lockedProfile(stand);
			}

			@Override
			public void setLocked(ArmorStand stand, boolean locked, ResolvableProfile profile) {
				LockedHeadSkins.setLocked(stand, locked, profile);
			}

			@Override
			public boolean isCapeEnabled(ArmorStand stand) {
				return EnabledCapes.isEnabled(stand);
			}

			@Override
			public void setCapeEnabled(ArmorStand stand, boolean enabled) {
				EnabledCapes.setEnabled(stand, enabled);
			}

			@Override
			public CapeSource capeSource(ArmorStand stand) {
				return CapeSources.get(stand);
			}

			@Override
			public void setCapeSource(ArmorStand stand, CapeSource source) {
				CapeSources.set(stand, source);
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(HeadSkinDisabledPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EquippedHeadHider.applyDisabledPacket(payload.entityId(), payload.disabled()));
		});
		ClientPlayNetworking.registerGlobalReceiver(HeadSkinLockPayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EquippedHeadHider.applyLockPacket(
				payload.entityId(),
				payload.locked(),
				payload.profile().orElse(null)
			));
		});
		ClientPlayNetworking.registerGlobalReceiver(HeadSkinCapePayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EquippedHeadHider.applyCapePacket(payload.entityId(), payload.enabled()));
		});
		ClientPlayNetworking.registerGlobalReceiver(HeadSkinCapeSourcePayload.TYPE, (payload, context) -> {
			context.client().execute(() -> EquippedHeadHider.applyCapeSourcePacket(
				payload.entityId(),
				CapeSource.fromWire(payload.source())
			));
		});

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if (entity instanceof ArmorStand stand) {
				EquippedHeadHider.applyLoadedStand(stand);
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			DisabledHeadSkins.save();
			LockedHeadSkins.save();
			EnabledCapes.save();
			CapeSources.save();
			EquippedHeadHider.clearPending();
		});

		if (!FabricLoader.getInstance().isModLoaded("armorposer")) {
			UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
				if (!(entity instanceof ArmorStand stand) || !player.isShiftKeyDown() || hand != InteractionHand.MAIN_HAND) {
					return InteractionResult.PASS;
				}
				if (world.isClientSide()) {
					Minecraft.getInstance().setScreenAndShow(new HeadSkinStandScreen(stand));
				}
				return InteractionResult.SUCCESS;
			});
		}
	}
}
