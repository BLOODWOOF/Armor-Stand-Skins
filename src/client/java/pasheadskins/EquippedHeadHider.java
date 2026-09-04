package pasheadskins;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

public final class EquippedHeadHider {
	private EquippedHeadHider() {
	}

	public static boolean shouldHide(Object state) {
		return state instanceof HeadStandRender head && head.pasheadskins$usingHeadSkin();
	}

	public static void hideOnState(net.minecraft.client.renderer.entity.state.ArmorStandRenderState state) {
		state.headEquipment = ItemStack.EMPTY;
		state.headItem.clear();
		state.wornHeadProfile = null;
		state.wornHeadType = null;
	}

	public static void applyPacket(int entityId, boolean disabled) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			return;
		}

		Entity entity = client.level.getEntity(entityId);
		if (entity instanceof ArmorStand stand) {
			HeadSkinFlags.setDisabled(stand, disabled);
		}
	}
}
