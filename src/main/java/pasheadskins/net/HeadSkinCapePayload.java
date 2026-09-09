package pasheadskins.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import pasheadskins.PasHeadSkins;

public record HeadSkinCapePayload(int entityId, boolean enabled) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<HeadSkinCapePayload> TYPE = new CustomPacketPayload.Type<>(
		Identifier.fromNamespaceAndPath(PasHeadSkins.MOD_ID, "head_skin_cape")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, HeadSkinCapePayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		HeadSkinCapePayload::entityId,
		ByteBufCodecs.BOOL,
		HeadSkinCapePayload::enabled,
		HeadSkinCapePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
