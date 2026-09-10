package pasheadskins.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import pasheadskins.PasHeadSkins;

public record HeadSkinAsthmaPayload(int entityId, boolean forced) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<HeadSkinAsthmaPayload> TYPE = new CustomPacketPayload.Type<>(
		Identifier.fromNamespaceAndPath(PasHeadSkins.MOD_ID, "stand_asthma")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, HeadSkinAsthmaPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		HeadSkinAsthmaPayload::entityId,
		ByteBufCodecs.BOOL,
		HeadSkinAsthmaPayload::forced,
		HeadSkinAsthmaPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
