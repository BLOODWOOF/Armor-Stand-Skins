package pasheadskins.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.component.ResolvableProfile;
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
public abstract class ArmorStandRendererMixin {
	@Shadow
	protected ArmorStandArmorModel model;

	@Unique
	private HeadStandModel pasheadskins$wide;

	@Unique
	private HeadStandModel pasheadskins$slim;

	@Unique
	private HeadStandModel pasheadskins$wideSmall;

	@Unique
	private HeadStandModel pasheadskins$slimSmall;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void pasheadskins$bakePlayerModels(EntityRendererProvider.Context context, CallbackInfo ci) {
		this.pasheadskins$wide = new HeadStandModel(context.bakeLayer(ModelLayers.PLAYER), false);
		this.pasheadskins$slim = new HeadStandModel(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
		this.pasheadskins$wideSmall = HeadStandModel.bakeSmall(false);
		this.pasheadskins$slimSmall = HeadStandModel.bakeSmall(true);
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
		if (HeadSkinFlags.isDisabled(stand) || state.isMarker) {
			return;
		}

		ResolvableProfile profile = HeadSkinLookup.profileFromHelmet(stand);
		if (profile == null) {
			return;
		}

		var info = Minecraft.getInstance().playerSkinRenderCache().getOrDefault(profile);
		Identifier texture = info.playerSkin().body().texturePath();
		boolean slim = info.playerSkin().model() == PlayerModelType.SLIM;
		head.pasheadskins$setHeadSkin(texture, slim);
		EquippedHeadHider.hideOnState(state);
	}

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
		)
	)
	private void pasheadskins$usePlayerModel(ArmorStandRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
		if (!(state instanceof HeadStandRender head) || !head.pasheadskins$usingHeadSkin()) {
			return;
		}

		EquippedHeadHider.hideOnState(state);
		this.model = pasheadskins$pickModel(state.isSmall, head.pasheadskins$slim());
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
		if (texture == null) {
			return;
		}

		if (translucent) {
			cir.setReturnValue(RenderTypes.entityTranslucent(texture));
		} else if (visible) {
			cir.setReturnValue(RenderTypes.entityTranslucent(texture));
		}
	}

	@Unique
	private HeadStandModel pasheadskins$pickModel(boolean small, boolean slim) {
		if (small) {
			return slim ? this.pasheadskins$slimSmall : this.pasheadskins$wideSmall;
		}
		return slim ? this.pasheadskins$slim : this.pasheadskins$wide;
	}
}
