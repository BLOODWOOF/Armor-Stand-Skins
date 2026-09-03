package pasheadskins.mixin;

import com.danrus.pas.api.info.NameInfo;
import com.danrus.pas.render.armorstand.PlayerArmorStandModel;
import com.danrus.pas.render.common.PasModelPoseSettings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pasheadskins.OverlayHierarchy;
import pasheadskins.SkinLayerCompat;

@Mixin(PlayerArmorStandModel.class)
public class PlayerArmorStandModelMixin {
	@Inject(method = "setupModel", at = @At("TAIL"))
	private void pasheadskins$wireOverlays(PasModelPoseSettings settings, NameInfo info, CallbackInfo ci) {
		PlayerArmorStandModel model = (PlayerArmorStandModel) (Object) this;
		OverlayHierarchy.wire(model);
		OverlayHierarchy.flattenOverlays(model);
		SkinLayerCompat.applyToStand(model, info);
	}

	@Inject(method = "getPlayerParts", at = @At("RETURN"), cancellable = true)
	private void pasheadskins$skipRootOverlays(CallbackInfoReturnable<Collection<ModelPart>> cir) {
		PlayerArmorStandModel model = (PlayerArmorStandModel) (Object) this;
		Collection<ModelPart> parts = cir.getReturnValue();
		List<ModelPart> filtered = new ArrayList<>(parts.size());
		for (ModelPart part : parts) {
			if (!OverlayHierarchy.isRootOverlay(model, part)) {
				filtered.add(part);
			}
		}
		cir.setReturnValue(filtered);
	}
}
