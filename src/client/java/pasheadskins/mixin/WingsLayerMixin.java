package pasheadskins.mixin;

import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pasheadskins.HeadStandRender;

@Mixin(WingsLayer.class)
public class WingsLayerMixin {
	@Inject(method = "getPlayerElytraTexture", at = @At("HEAD"), cancellable = true)
	private static void pasheadskins$standCapeElytra(HumanoidRenderState state, CallbackInfoReturnable<Identifier> cir) {
		if (state instanceof HeadStandRender head) {
			Identifier texture = head.pasheadskins$elytraTexture();
			if (texture != null) {
				cir.setReturnValue(texture);
			}
		}
	}
}
