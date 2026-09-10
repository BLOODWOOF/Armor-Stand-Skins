package pasheadskins.mixin;

import com.mrbysco.armorposer.data.SyncData;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.PoserNameVisible;

@Mixin(SyncData.class)
public class SyncDataMixin {
	@Inject(method = "handleData", at = @At("RETURN"))
	private void pasheadskins$keepNameVisible(ArmorStand stand, Player player, CallbackInfo ci) {
		PoserNameVisible.apply(stand, ((SyncData) (Object) this).tag());
	}
}
