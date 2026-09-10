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
		UUIDUtil.CODEC_SET.optionalFieldOf("capes", Set.of()).forGetter(data -> data.capes),
		Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT)
			.optionalFieldOf("capeSources", Map.of())
			.forGetter(data -> data.capeSources),
		Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING)
			.optionalFieldOf("passwords", Map.of())
			.forGetter(data -> data.passwords),
		UUIDUtil.CODEC_SET.optionalFieldOf("asthmatic", Set.of()).forGetter(data -> data.asthmatic)
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
	private final Map<UUID, Integer> capeSources;
	private final Map<UUID, String> passwords;
	private final Set<UUID> asthmatic;

	public HeadSkinWorldData() {
		this(Set.of(), Map.of(), Set.of(), Map.of(), Map.of(), Set.of());
	}

	public HeadSkinWorldData(
		Set<UUID> disabled,
		Map<UUID, ResolvableProfile> locked,
		Set<UUID> capes,
		Map<UUID, Integer> capeSources,
		Map<UUID, String> passwords,
		Set<UUID> asthmatic
	) {
		this.disabled = new HashSet<>(disabled);
		this.locked = new HashMap<>(locked);
		this.capes = new HashSet<>(capes);
		this.capeSources = new HashMap<>(capeSources);
		this.passwords = new HashMap<>(passwords);
		this.asthmatic = new HashSet<>(asthmatic);
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

		Integer storedSource = this.capeSources.get(id);
		if (storedSource != null) {
			holder.pasheadskins$setCapeSource(CapeSource.fromWire(storedSource));
		} else if (holder.pasheadskins$capeSource() != CapeSource.BOTH) {
			this.capeSources.put(id, holder.pasheadskins$capeSource().wire());
			this.setDirty();
		}

		String storedPass = this.passwords.get(id);
		if (storedPass != null && !storedPass.isEmpty()) {
			holder.pasheadskins$setPasswordHash(storedPass);
		} else if (!holder.pasheadskins$passwordHash().isEmpty()) {
			this.passwords.put(id, holder.pasheadskins$passwordHash());
			this.setDirty();
		}

		if (this.asthmatic.contains(id)) {
			holder.pasheadskins$setAsthmaForced(true);
		} else if (holder.pasheadskins$asthmaForced()) {
			this.asthmatic.add(id);
			this.setDirty();
		}

		StandSlotFlags.writeOntoStand(stand);
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

	public void setCapeSource(UUID id, CapeSource source) {
		if (source == null || source == CapeSource.BOTH) {
			if (this.capeSources.remove(id) != null) {
				this.setDirty();
			}
			return;
		}
		Integer previous = this.capeSources.put(id, source.wire());
		if (previous == null || previous.intValue() != source.wire()) {
			this.setDirty();
		}
	}

	public void setPasswordHash(UUID id, String hash) {
		if (hash == null || hash.isEmpty()) {
			if (this.passwords.remove(id) != null) {
				this.setDirty();
			}
			return;
		}
		String previous = this.passwords.put(id, hash);
		if (!hash.equals(previous)) {
			this.setDirty();
		}
	}

	public void setAsthmaForced(UUID id, boolean forced) {
		if (forced) {
			if (this.asthmatic.add(id)) {
				this.setDirty();
			}
		} else if (this.asthmatic.remove(id)) {
			this.setDirty();
		}
	}

	public void forget(UUID id) {
		boolean changed = this.disabled.remove(id);
		changed |= this.locked.remove(id) != null;
		changed |= this.capes.remove(id);
		changed |= this.capeSources.remove(id) != null;
		changed |= this.passwords.remove(id) != null;
		changed |= this.asthmatic.remove(id);
		if (changed) {
			this.setDirty();
		}
	}
}
