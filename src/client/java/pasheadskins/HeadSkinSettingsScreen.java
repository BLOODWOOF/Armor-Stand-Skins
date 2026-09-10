package pasheadskins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HeadSkinSettingsScreen extends Screen {
	private final Screen parent;

	public HeadSkinSettingsScreen(Screen parent) {
		super(Component.translatable("pasheadskins.config.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		int x = this.width / 2 - 100;
		int y = this.height / 6 + 24;
		this.addRenderableWidget(CycleButton.booleanBuilder(
				Component.translatable("gui.yes"),
				Component.translatable("gui.no"),
				HeadSkinClientConfig.personality()
			)
			.withTooltip(value -> Tooltip.create(Component.translatable("pasheadskins.config.personality.tooltip")))
			.create(x, y, 200, 20, Component.translatable("pasheadskins.config.personality"), (button, value) -> {
				HeadSkinClientConfig.setPersonality(value);
			}));
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
			.bounds(this.width / 2 - 100, this.height - 28, 200, 20)
			.build());
	}

	@Override
	public void onClose() {
		this.minecraft.setScreenAndShow(this.parent);
	}
}
