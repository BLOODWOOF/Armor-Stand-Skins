package pasheadskins.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;
import pasheadskins.HeadSkinWorldData;
import pasheadskins.PasHeadSkins;

import java.util.Optional;

public record HeadSkinLockPayload(
	int entityId,
	boolean locked,
	Optional<ResolvableProfile> profile,
	Optional<Identifier> body,
	boolean slim
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<HeadSkinLockPayload> TYPE = new CustomPacketPayload.Type<>(
		Identifier.fromNamespaceAndPath(PasHeadSkins.MOD_ID, "head_skin_lock")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, HeadSkinLockPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		HeadSkinLockPayload::entityId,
		ByteBufCodecs.BOOL,
		HeadSkinLockPayload::locked,
		ResolvableProfile.STREAM_CODEC.apply(ByteBufCodecs::optional),
		HeadSkinLockPayload::profile,
		Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional),
		HeadSkinLockPayload::body,
		ByteBufCodecs.BOOL,
		HeadSkinLockPayload::slim,
		HeadSkinLockPayload::new
	);

	public static HeadSkinLockPayload of(int entityId, boolean locked, ResolvableProfile profile) {
		return of(entityId, locked, profile, null, false);
	}

	public static HeadSkinLockPayload of(int entityId, boolean locked, ResolvableProfile profile, Identifier body, boolean slim) {
		if (!locked) {
			return new HeadSkinLockPayload(entityId, false, Optional.empty(), Optional.empty(), false);
		}
		return new HeadSkinLockPayload(
			entityId,
			true,
			Optional.ofNullable(profile),
			Optional.ofNullable(HeadSkinWorldData.persistable(body)),
			slim
		);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
