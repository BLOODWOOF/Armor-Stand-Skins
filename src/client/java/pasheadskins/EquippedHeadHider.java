package pasheadskins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class EquippedHeadHider {
	private static final Map<Integer, Boolean> pending = new HashMap<>();

	private EquippedHeadHider() {
	}

	public static void hideOnState(ArmorStandRenderState state) {
		state.headEquipment = ItemStack.EMPTY;
		if (state.headItem != null) {
			state.headItem.clear();
		}
		state.wornHeadProfile = null;
		state.wornHeadType = null;
	}

	public static void applyPacket(int entityId, boolean disabled) {
		Minecraft client = Minecraft.getInstance();
		if (client.level == null) {
			pending.put(entityId, disabled);
			return;
		}

		Entity entity = client.level.getEntity(entityId);
		if (entity instanceof ArmorStand stand) {
			HeadSkinFlags.setDisabled(stand, disabled);
			pending.remove(entityId);
			return;
		}

		pending.put(entityId, disabled);
	}

	public static void applyLoadedStand(ArmorStand stand) {
		Boolean fromServer = pending.remove(stand.getId());
		if (fromServer != null) {
			HeadSkinFlags.setDisabled(stand, fromServer);
			return;
		}

		if (DisabledHeadSkins.isDisabled(stand) && stand instanceof HeadSkinHolder holder) {
			holder.pasheadskins$setDisabled(true);
		}
	}
}
