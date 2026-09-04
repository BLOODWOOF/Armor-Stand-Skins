package pasheadskins.mixin;

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
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.PlayerModelType;
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
import pasheadskins.HeadSkinFlags;
import pasheadskins.HeadSkinLookup;
import pasheadskins.HeadStandModel;
import pasheadskins.HeadStandRender;

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
			if (HeadSkinFlags.isDisabled(stand) || state.isMarker) {
				return;
			}

			ResolvableProfile profile = HeadSkinLookup.profileFromHelmet(stand);
			if (profile == null) {
				return;
			}

			Minecraft client = Minecraft.getInstance();
			if (client == null || client.playerSkinRenderCache() == null) {
				return;
			}

			var info = client.playerSkinRenderCache().getOrDefault(profile);
			if (info == null || info.playerSkin() == null || info.playerSkin().body() == null) {
				return;
			}

			Identifier texture = info.playerSkin().body().texturePath();
			boolean slim = info.playerSkin().model() == PlayerModelType.SLIM;
			head.pasheadskins$setHeadSkin(texture, slim);
			EquippedHeadHider.hideOnState(state);
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
