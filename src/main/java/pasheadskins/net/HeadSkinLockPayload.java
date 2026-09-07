package pasheadskins.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.ResolvableProfile;
import pasheadskins.PasHeadSkins;

import java.util.Optional;

public record HeadSkinLockPayload(int entityId, boolean locked, Optional<ResolvableProfile> profile) implements CustomPacketPayload {
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
		HeadSkinLockPayload::new
	);

	public static HeadSkinLockPayload of(int entityId, boolean locked, ResolvableProfile profile) {
		if (locked && profile != null) {
			return new HeadSkinLockPayload(entityId, true, Optional.of(profile));
		}
		return new HeadSkinLockPayload(entityId, false, Optional.empty());
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
