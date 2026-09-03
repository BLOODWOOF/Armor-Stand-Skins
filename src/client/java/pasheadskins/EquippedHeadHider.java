package pasheadskins;

import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.world.item.ItemStack;

public final class EquippedHeadHider {
	private EquippedHeadHider() {
	}

	public static boolean shouldHide(Object state) {
		return state instanceof HeadDrivenSkin headDriven && headDriven.pasheadskins$isFromHead();
	}

	public static void hideOnState(ArmorStandRenderState state) {
		state.headEquipment = ItemStack.EMPTY;
		state.headItem.clear();
		state.wornHeadProfile = null;
		state.wornHeadType = null;
	}
}
