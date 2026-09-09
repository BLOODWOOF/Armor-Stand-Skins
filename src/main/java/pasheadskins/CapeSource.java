package pasheadskins;

import net.minecraft.network.chat.Component;

public enum CapeSource {
	MOJANG,
	ESSENTIAL,
	BOTH;

	public static CapeSource fromWire(int id) {
		return switch (id) {
			case 1 -> MOJANG;
			case 2 -> ESSENTIAL;
			default -> BOTH;
		};
	}

	public int wire() {
		return switch (this) {
			case BOTH -> 0;
			case MOJANG -> 1;
			case ESSENTIAL -> 2;
		};
	}

	public Component label() {
		return Component.translatable("pasheadskins.gui.cape_source." + this.name().toLowerCase());
	}
}
