package pasheadskins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

final class ClientWorldKey {
	private ClientWorldKey() {
	}

	static String current() {
		Minecraft client = Minecraft.getInstance();
		if (client == null) {
			return "unknown";
		}

		ServerData server = client.getCurrentServer();
		if (server != null && server.ip != null && !server.ip.isEmpty()) {
			return "mp:" + server.ip;
		}

		if (client.hasSingleplayerServer() && client.getSingleplayerServer() != null) {
			return "sp:" + client.getSingleplayerServer().getServerDirectory().getFileName();
		}

		return "unknown";
	}
}
