package pasheadskins;

import com.mrbysco.armorposer.Reference;
import com.mrbysco.armorposer.data.SyncData;
import com.mrbysco.armorposer.packets.ArmorStandSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ArmorStand;

final class PoserStandSync {
	private PoserStandSync() {
	}

	static boolean available() {
		return ClientPlayNetworking.canSend(ArmorStandSyncPayload.ID);
	}

	static boolean trySend(ArmorStand stand) {
		if (!available()) {
			return false;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.level == null) {
			return false;
		}

		int max = Reference.getMaxDistance();
		if (client.player.distanceToSqr(stand) > (double) max * max) {
			return false;
		}

		CompoundTag tag = StandSlotFlags.syncTag(stand);
		if (tag.isEmpty()) {
			return false;
		}

		ClientPlayNetworking.send(new ArmorStandSyncPayload(new SyncData(stand.getUUID(), tag)));
		return true;
	}
}
