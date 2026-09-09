package pasheadskins.mixin;

import com.mrbysco.armorposer.util.ArmorStandData;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pasheadskins.StandSlotFlags;

@Mixin(ArmorStandData.class)
public class ArmorStandDataMixin {
	@Inject(method = "writeToNBT", at = @At("RETURN"))
	private void pasheadskins$keepSlotFlags(CallbackInfoReturnable<CompoundTag> cir) {
		StandSlotFlags.keepInTag(cir.getReturnValue());
	}

	@Inject(method = "getDifference", at = @At("RETURN"))
	private void pasheadskins$keepSlotFlagsOnDiff(ArmorStandData other, CallbackInfoReturnable<CompoundTag> cir) {
		StandSlotFlags.keepInTag(cir.getReturnValue());
	}
}
