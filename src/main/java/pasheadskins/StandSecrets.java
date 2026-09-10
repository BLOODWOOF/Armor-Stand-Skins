package pasheadskins;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;

public final class StandSecrets {
	private static final String CODE = "wheeze";
	private static final String OWNER = "BLOODWOLF";
	private static final long LUCKY_MOD = 800L;
	private static final long LUCKY_HIT = 13L;

	private StandSecrets() {
	}

	public static boolean lucky(UUID id) {
		return id != null && Math.floorMod(id.getLeastSignificantBits(), LUCKY_MOD) == LUCKY_HIT;
	}

	public static boolean isCode(String text) {
		return CODE.equalsIgnoreCase(text == null ? "" : text.trim());
	}

	public static boolean isOwner(Player player) {
		if (player == null) {
			return false;
		}
		String name = player.getGameProfile().name();
		return name != null && name.equalsIgnoreCase(OWNER);
	}

	public static String hash(UUID standId, String password) {
		if (standId == null) {
			return "";
		}
		String raw = standId + ":" + (password == null ? "" : password);
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (NoSuchAlgorithmException ignored) {
			return "";
		}
	}

	public static boolean matches(ArmorStand stand, String typed) {
		String stored = HeadSkinFlags.passwordHash(stand);
		if (stored == null || stored.isEmpty()) {
			return false;
		}
		return stored.equals(hash(stand.getUUID(), typed == null ? "" : typed.trim()));
	}
}
