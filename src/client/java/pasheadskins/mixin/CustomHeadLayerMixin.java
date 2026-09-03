package pasheadskins.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.EquippedHeadHider;

@Mixin(CustomHeadLayer.class)
public class CustomHeadLayerMixin<S extends LivingEntityRenderState, M extends EntityModel<S>> {
	@Inject(
		method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;FF)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void pasheadskins$skipHeadItem(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot, CallbackInfo ci) {
		if (EquippedHeadHider.shouldHide(state)) {
			ci.cancel();
		}
	}
}
