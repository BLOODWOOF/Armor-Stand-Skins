package pasheadskins;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;

public final class HeadSkinLookup {
	private HeadSkinLookup() {
	}

	public static ResolvableProfile profileFromHelmet(ArmorStand stand) {
		ItemStack helmet = stand.getItemBySlot(EquipmentSlot.HEAD);
		if (helmet.isEmpty()) {
			return null;
		}
		return helmet.get(DataComponents.PROFILE);
	}
}
