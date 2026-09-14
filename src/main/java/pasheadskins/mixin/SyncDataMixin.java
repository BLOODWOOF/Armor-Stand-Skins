package pasheadskins.mixin;

import com.mrbysco.armorposer.data.SyncData;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.PasHeadSkins;
import pasheadskins.PoserNameVisible;

@Mixin(SyncData.class)
public class SyncDataMixin {
	@Inject(method = "handleData", at = @At("HEAD"), cancellable = true)
	private void pasheadskins$blockLocked(ArmorStand stand, Player player, CallbackInfo ci) {
		if (!PasHeadSkins.mayEdit(player, stand)) {
			ci.cancel();
		}
	}

	@Inject(method = "handleData", at = @At("RETURN"))
	private void pasheadskins$keepNameVisible(ArmorStand stand, Player player, CallbackInfo ci) {
		PoserNameVisible.apply(stand, ((SyncData) (Object) this).tag());
	}
}
