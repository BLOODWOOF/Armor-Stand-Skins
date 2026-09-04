package pasheadskins.mixin;

import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.HeadSkinFlags;
import pasheadskins.HeadSkinHolder;

@Mixin(ArmorStand.class)
public class ArmorStandMixin implements HeadSkinHolder {
	@Unique
	private boolean pasheadskins$disabled;

	@Override
	public boolean pasheadskins$isDisabled() {
		return this.pasheadskins$disabled;
	}

	@Override
	public void pasheadskins$setDisabled(boolean disabled) {
		this.pasheadskins$disabled = disabled;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void pasheadskins$saveFlag(ValueOutput output, CallbackInfo ci) {
		output.putBoolean(HeadSkinFlags.NBT_KEY, this.pasheadskins$disabled);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void pasheadskins$loadFlag(ValueInput input, CallbackInfo ci) {
		this.pasheadskins$disabled = input.getBooleanOr(HeadSkinFlags.NBT_KEY, false);
	}
}
