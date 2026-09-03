package pasheadskins.mixin;

import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.render.armorstand.PasEntityRenderState;
import com.danrus.pas.render.armorstand.PasEntityRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.EquippedHeadHider;
import pasheadskins.HeadDrivenSkin;
import pasheadskins.HeadSkinLookup;

@Mixin(PasEntityRenderer.class)
public class PasEntityRendererMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/decoration/ArmorStand;Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;F)V", at = @At("TAIL"))
	private void pasheadskins$useHelmetWhenUnnamed(ArmorStand stand, ArmorStandRenderState vanillaState, float partialTick, CallbackInfo ci) {
		if (!(vanillaState instanceof PasEntityRenderState state)) {
			return;
		}

		if (state instanceof HeadDrivenSkin headDriven) {
			headDriven.pasheadskins$setFromHead(false);
		}

		if (stand.hasCustomName() || (state.info != null && !state.info.isEmpty())) {
			return;
		}

		HeadSkinLookup.HeadSkin fromHead = HeadSkinLookup.fromHelmet(stand);
		if (fromHead == null) {
			return;
		}

		NameInfo info = NameInfo.parse(fromHead.name()).toBuilder().setSlim(fromHead.slim()).build();
		state.info = info;
		state.customName = Component.literal(info.compile());
		if (state instanceof HeadDrivenSkin headDriven) {
			headDriven.pasheadskins$setFromHead(true);
		}

		EquippedHeadHider.hideOnState(state);
	}

	@Inject(
		method = "submit(Lnet/minecraft/client/renderer/entity/state/ArmorStandRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
		at = @At("HEAD")
	)
	private void pasheadskins$hideCopiedHead(ArmorStandRenderState vanillaState, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
		if (EquippedHeadHider.shouldHide(vanillaState)) {
			EquippedHeadHider.hideOnState(vanillaState);
		}
	}
}
