package pasheadskins.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pasheadskins.EquippedHeadHider;
import pasheadskins.HeadSkinControls;
import pasheadskins.HeadSkinFlags;
import pasheadskins.HeadSkinHolder;
import pasheadskins.HeadSkinLookup;
import pasheadskins.HeadStandArmor;
import pasheadskins.HeadStandCapeLayer;
import pasheadskins.HeadStandModel;
import pasheadskins.HeadStandRender;
import pasheadskins.StandSlotFlags;

@Mixin(ArmorStandRenderer.class)
public abstract class ArmorStandRendererMixin extends LivingEntityRenderer<ArmorStand, ArmorStandRenderState, ArmorStandArmorModel> {
	@Shadow
	@Final
	private ArmorStandArmorModel bigModel;

	@Shadow
	@Final
	private ArmorStandArmorModel smallModel;

	@Unique
	private ArmorStandArmorModel pasheadskins$wide;

	@Unique
	private ArmorStandArmorModel pasheadskins$slim;

	@Unique
	private boolean pasheadskins$pushedPose;

	public ArmorStandRendererMixin(EntityRendererProvider.Context context, ArmorStandArmorModel model, float shadowRadius) {
		super(context, model, shadowRadius);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void pasheadskins$addCapeLayer(EntityRendererProvider.Context context, CallbackInfo ci) {
		HeadStandArmor.bake(context.getModelSet());
		this.addLayer(new HeadStandCapeLayer((ArmorStandRenderer) (Object) this));
	}

	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ArmorStand;Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;F)V",
		at = @At("TAIL")
	)
	private void pasheadskins$extractHeadSkin(ArmorStand stand, ArmorStandRenderState state, float partialTick, CallbackInfo ci) {
		if (!(state instanceof HeadStandRender head)) {
			return;
		}

		head.pasheadskins$clearHeadSkin();
		try {
			StandSlotFlags.applyToHolder(stand);

			if (HeadSkinFlags.isDisabled(stand) || state.isMarker || state.isInvisible) {
				return;
			}

			ResolvableProfile helmet = HeadSkinLookup.profileFromHelmet(stand);
			if (helmet == null) {
				return;
			}

			boolean locked = HeadSkinFlags.isLocked(stand);
			ResolvableProfile profile = helmet;
			if (locked) {
				ResolvableProfile frozen = HeadSkinFlags.lockedProfile(stand);
				if (frozen != null && HeadSkinLookup.sameIdentity(helmet, frozen)) {
					profile = frozen;
				} else if (StandSlotFlags.lockOn(stand)) {
					profile = helmet;
				} else {
					HeadSkinControls.setLocked(stand, false);
					locked = false;
					profile = HeadSkinLookup.liveQuery(helmet);
				}
			} else {
				profile = HeadSkinLookup.liveQuery(helmet);
			}

			Minecraft client = Minecraft.getInstance();
			if (client == null || client.playerSkinRenderCache() == null) {
				return;
			}

			Identifier texture = null;
			boolean slim = false;
			PlayerSkin packed = null;
			GameProfile gameProfile = null;

			if (!locked) {
				PlayerSkin live = HeadSkinLookup.liveBody(client, helmet);
				if (live != null && live.body() != null) {
					texture = live.body().texturePath();
					slim = live.model() == PlayerModelType.SLIM;
					packed = live;
					gameProfile = helmet.partialProfile();
				}
			}

			if (texture == null) {
				var info = client.playerSkinRenderCache().getOrDefault(profile);
				if (info == null || info.playerSkin() == null || info.playerSkin().body() == null) {
					return;
				}
				texture = info.playerSkin().body().texturePath();
				slim = info.playerSkin().model() == PlayerModelType.SLIM;
				packed = info.playerSkin();
				gameProfile = info.gameProfile();
			}

			head.pasheadskins$setHeadSkin(texture, slim);
			if (HeadSkinFlags.isCapeEnabled(stand)) {
				Identifier[] cloak;
				if (locked) {
					HeadSkinHolder holder = stand instanceof HeadSkinHolder h ? h : null;
					cloak = HeadSkinLookup.lockedCloak(holder, packed);
				} else {
					cloak = HeadSkinLookup.capeAndElytra(client, helmet, gameProfile, packed, HeadSkinFlags.capeSource(stand));
				}
				head.pasheadskins$setCapeTextures(cloak[0], cloak[1]);
			}
			EquippedHeadHider.hideOnState(state);
			state.isBaby = false;
			if (!state.showArms) {
				state.rightHandItemStack = ItemStack.EMPTY;
				state.leftHandItemStack = ItemStack.EMPTY;
				if (state.rightHandItemState != null) {
					state.rightHandItemState.clear();
				}
				if (state.leftHandItemState != null) {
					state.leftHandItemState.clear();
				}
			}
		} catch (Throwable ignored) {
			head.pasheadskins$clearHeadSkin();
		}
	}

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
		)
	)
	private void pasheadskins$usePlayerModel(ArmorStandRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
		this.pasheadskins$pushedPose = false;
		if (!(state instanceof HeadStandRender head) || !head.pasheadskins$usingHeadSkin()) {
			return;
		}

		try {
			EquippedHeadHider.hideOnState(state);
			this.model = this.pasheadskins$model(head.pasheadskins$slim());
			poseStack.pushPose();
			this.pasheadskins$pushedPose = true;
			float scale = 0.9375F;
			if (state.isSmall) {
				scale *= 0.5F;
			}
			poseStack.scale(scale, scale, scale);
		} catch (Throwable ignored) {
			this.pasheadskins$restoreVanillaModel(state, poseStack);
		}
	}

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
			shift = At.Shift.AFTER
		)
	)
	private void pasheadskins$restorePose(ArmorStandRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
		this.pasheadskins$restoreVanillaModel(state, poseStack);
	}

	@Inject(
		method = "getTextureLocation(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;)Lnet/minecraft/resources/Identifier;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void pasheadskins$playerTexture(ArmorStandRenderState state, CallbackInfoReturnable<Identifier> cir) {
		if (state instanceof HeadStandRender head && head.pasheadskins$usingHeadSkin() && head.pasheadskins$texture() != null) {
			cir.setReturnValue(head.pasheadskins$texture());
		}
	}

	@Inject(
		method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void pasheadskins$playerRenderType(ArmorStandRenderState state, boolean visible, boolean translucent, boolean glowing, CallbackInfoReturnable<RenderType> cir) {
		if (!(state instanceof HeadStandRender head) || !head.pasheadskins$usingHeadSkin()) {
			return;
		}

		Identifier texture = head.pasheadskins$texture();
		if (texture == null || !(translucent || visible)) {
			return;
		}

		cir.setReturnValue(RenderTypes.entityTranslucent(texture, false));
	}

	@Unique
	private static Identifier texturePath(ClientAsset.Texture texture) {
		return texture == null ? null : texture.texturePath();
	}

	@Unique
	private void pasheadskins$restoreVanillaModel(ArmorStandRenderState state, PoseStack poseStack) {
		if (this.pasheadskins$pushedPose) {
			poseStack.popPose();
			this.pasheadskins$pushedPose = false;
		}

		if (this.smallModel != null && this.bigModel != null) {
			this.model = state.isSmall ? this.smallModel : this.bigModel;
		}
	}

	@Unique
	private ArmorStandArmorModel pasheadskins$model(boolean slim) {
		if (slim) {
			if (this.pasheadskins$slim == null) {
				this.pasheadskins$slim = HeadStandModel.bake(true);
			}
			return this.pasheadskins$slim;
		}
		if (this.pasheadskins$wide == null) {
			this.pasheadskins$wide = HeadStandModel.bake(false);
		}
		return this.pasheadskins$wide;
	}
}
