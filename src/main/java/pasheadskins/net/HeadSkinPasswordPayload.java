package pasheadskins.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import pasheadskins.PasHeadSkins;

public record HeadSkinPasswordPayload(int entityId, String value) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<HeadSkinPasswordPayload> TYPE = new CustomPacketPayload.Type<>(
		Identifier.fromNamespaceAndPath(PasHeadSkins.MOD_ID, "stand_password")
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, HeadSkinPasswordPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.VAR_INT,
		HeadSkinPasswordPayload::entityId,
		ByteBufCodecs.stringUtf8(128),
		HeadSkinPasswordPayload::value,
		HeadSkinPasswordPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
