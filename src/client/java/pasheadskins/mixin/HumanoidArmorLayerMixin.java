package pasheadskins.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pasheadskins.HeadStandArmor;
import pasheadskins.HeadStandRender;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
	@Inject(method = "getArmorModel", at = @At("HEAD"), cancellable = true)
	private void pasheadskins$playerArmor(HumanoidRenderState state, EquipmentSlot slot, CallbackInfoReturnable<HumanoidModel<?>> cir) {
		if (!(state instanceof HeadStandRender head) || !head.pasheadskins$usingHeadSkin()) {
			return;
		}

		ArmorStandArmorModel model = HeadStandArmor.get(head.pasheadskins$slim(), slot);
		if (model == null) {
			return;
		}

		Object parent = ((RenderLayer<?, ?>) (Object) this).getParentModel();
		if (parent instanceof HumanoidModel<?> humanoid) {
			HeadStandArmor.copyPose(humanoid, model);
		}
		cir.setReturnValue(model);
	}
}
