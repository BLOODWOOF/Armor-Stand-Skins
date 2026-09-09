package pasheadskins.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.PasHeadSkins;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "setInvisible", at = @At("TAIL"))
	private void pasheadskins$offHeadSkinWhenInvisible(boolean invisible, CallbackInfo ci) {
		if (!invisible || !((Object) this instanceof ArmorStand stand)) {
			return;
		}
		PasHeadSkins.onStandInvisible(stand);
	}
}
