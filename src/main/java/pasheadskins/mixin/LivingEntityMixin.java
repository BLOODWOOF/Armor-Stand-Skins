package pasheadskins.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.HeadSkinFlags;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Unique
	private static final ThreadLocal<Boolean> pasheadskins$hadHeadProfile = new ThreadLocal<>();

	@Inject(method = "setItemSlot", at = @At("HEAD"))
	private void pasheadskins$rememberHead(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
		if (slot != EquipmentSlot.HEAD || !((Object) this instanceof ArmorStand stand)) {
			return;
		}
		pasheadskins$hadHeadProfile.set(stand.getItemBySlot(EquipmentSlot.HEAD).has(DataComponents.PROFILE));
	}

	@Inject(method = "setItemSlot", at = @At("TAIL"))
	private void pasheadskins$enableArmsOnHead(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
		if (slot != EquipmentSlot.HEAD || !((Object) this instanceof ArmorStand stand)) {
			return;
		}

		try {
			Boolean hadProfile = pasheadskins$hadHeadProfile.get();
			if (hadProfile != null && !hadProfile && stack != null && stack.has(DataComponents.PROFILE) && !HeadSkinFlags.isDisabled(stand)) {
				stand.setShowArms(true);
			}
		} finally {
			pasheadskins$hadHeadProfile.remove();
		}
	}
}
