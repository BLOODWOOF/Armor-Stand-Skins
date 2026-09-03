package pasheadskins.mixin;

import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pasheadskins.SkinLayerCompat;

@Mixin(targets = "com.danrus.pas.render.common.PasRenderer$ModelPartState")
public class PasModelPartStateMixin {
	@Inject(method = "capture", at = @At("RETURN"))
	private static void pasheadskins$snapshotLayers(ModelPart part, CallbackInfoReturnable<Object> cir) {
		SkinLayerCompat.snapshotInjector(cir.getReturnValue(), part);
	}

	@Inject(method = "apply", at = @At("TAIL"))
	private void pasheadskins$restoreLayers(CallbackInfo ci) {
		SkinLayerCompat.restoreInjector(this);
	}
}
