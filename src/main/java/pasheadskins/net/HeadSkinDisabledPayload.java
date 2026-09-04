package pasheadskins.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import pasheadskins.PasHeadSkins;

public record HeadSkinDisabledPayload(int entityId, boolean disabled) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<HeadSkinDisabledPayload> TYPE = new CustomPacketPayload.Type<>(
		Identifier.fromNamespaceAndPath(PasHeadSkins.MOD_ID, "head_skin_disabled")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, HeadSkinDisabledPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		HeadSkinDisabledPayload::entityId,
		ByteBufCodecs.BOOL,
		HeadSkinDisabledPayload::disabled,
		HeadSkinDisabledPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
