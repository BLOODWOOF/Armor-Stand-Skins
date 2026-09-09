package pasheadskins;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HeadSkinWorldData extends SavedData {
	public static final Codec<HeadSkinWorldData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		UUIDUtil.CODEC_SET.optionalFieldOf("disabled", Set.of()).forGetter(data -> data.disabled),
		Codec.unboundedMap(UUIDUtil.STRING_CODEC, ResolvableProfile.CODEC)
			.optionalFieldOf("locked", Map.of())
			.forGetter(data -> data.locked),
		UUIDUtil.CODEC_SET.optionalFieldOf("capes", Set.of()).forGetter(data -> data.capes)
	).apply(instance, HeadSkinWorldData::new));

	public static final SavedDataType<HeadSkinWorldData> TYPE = new SavedDataType<>(
		Identifier.fromNamespaceAndPath(PasHeadSkins.MOD_ID, "stands"),
		HeadSkinWorldData::new,
		CODEC,
		DataFixTypes.SAVED_DATA_COMMAND_STORAGE
	);

	private final Set<UUID> disabled;
	private final Map<UUID, ResolvableProfile> locked;
	private final Set<UUID> capes;

	public HeadSkinWorldData() {
		this(Set.of(), Map.of(), Set.of());
	}

	public HeadSkinWorldData(Set<UUID> disabled, Map<UUID, ResolvableProfile> locked, Set<UUID> capes) {
		this.disabled = new HashSet<>(disabled);
		this.locked = new HashMap<>(locked);
		this.capes = new HashSet<>(capes);
	}

	public static HeadSkinWorldData get(MinecraftServer server) {
		return server.getDataStorage().computeIfAbsent(TYPE);
	}

	public void applyTo(ArmorStand stand) {
		if (!(stand instanceof HeadSkinHolder holder)) {
			return;
		}

		UUID id = stand.getUUID();
		if (this.disabled.contains(id)) {
			holder.pasheadskins$setDisabled(true);
		} else if (holder.pasheadskins$isDisabled()) {
			this.disabled.add(id);
			this.setDirty();
		}

		ResolvableProfile stored = this.locked.get(id);
		if (stored != null) {
			holder.pasheadskins$setLocked(true, stored);
		} else if (holder.pasheadskins$isLocked()) {
			this.locked.put(id, holder.pasheadskins$lockedProfile());
			this.setDirty();
		}

		if (this.capes.contains(id)) {
			holder.pasheadskins$setCapeEnabled(true);
		} else if (holder.pasheadskins$isCapeEnabled()) {
			this.capes.add(id);
			this.setDirty();
		}
	}

	public void setDisabled(UUID id, boolean value) {
		if (value) {
			if (this.disabled.add(id)) {
				this.setDirty();
			}
		} else if (this.disabled.remove(id)) {
			this.setDirty();
		}
	}

	public void setLocked(UUID id, boolean locked, ResolvableProfile profile) {
		if (locked && profile != null) {
			this.locked.put(id, profile);
			this.setDirty();
		} else if (this.locked.remove(id) != null) {
			this.setDirty();
		}
	}

	public void setCapeEnabled(UUID id, boolean enabled) {
		if (enabled) {
			if (this.capes.add(id)) {
				this.setDirty();
			}
		} else if (this.capes.remove(id)) {
			this.setDirty();
		}
	}

	public void forget(UUID id) {
		boolean changed = this.disabled.remove(id);
		changed |= this.locked.remove(id) != null;
		changed |= this.capes.remove(id);
		if (changed) {
			this.setDirty();
		}
	}
}
