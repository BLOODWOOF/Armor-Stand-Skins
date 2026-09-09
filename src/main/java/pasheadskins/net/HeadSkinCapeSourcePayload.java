package pasheadskins.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import pasheadskins.PasHeadSkins;

public record HeadSkinCapeSourcePayload(int entityId, int source) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<HeadSkinCapeSourcePayload> TYPE = new CustomPacketPayload.Type<>(
		Identifier.fromNamespaceAndPath(PasHeadSkins.MOD_ID, "head_skin_cape_source")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, HeadSkinCapeSourcePayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		HeadSkinCapeSourcePayload::entityId,
		ByteBufCodecs.VAR_INT,
		HeadSkinCapeSourcePayload::source,
		HeadSkinCapeSourcePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
