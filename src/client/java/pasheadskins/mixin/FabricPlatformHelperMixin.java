package pasheadskins.mixin;

import com.mrbysco.armorposer.platform.FabricPlatformHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.PoserNameVisible;

@Mixin(FabricPlatformHelper.class)
public class FabricPlatformHelperMixin {
	@Inject(method = "updateEntity", at = @At("HEAD"))
	private void pasheadskins$noteNameVisible(ArmorStand stand, CompoundTag compound, CallbackInfo ci) {
		PoserNameVisible.note(compound);
		PoserNameVisible.apply(stand, compound);
	}

	@ModifyArg(
		method = "updateEntity",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mrbysco/armorposer/data/SyncData;<init>(Ljava/util/UUID;Lnet/minecraft/nbt/CompoundTag;)V"
		),
		index = 1
	)
	private CompoundTag pasheadskins$keepNameInSync(CompoundTag tag) {
		PoserNameVisible.copyPending(tag);
		return tag;
	}

	@Inject(method = "updateEntity", at = @At("RETURN"))
	private void pasheadskins$clearNameVisible(ArmorStand stand, CompoundTag compound, CallbackInfo ci) {
		PoserNameVisible.clear();
	}
}
