package pasheadskins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;

public class HeadSkinStandScreen extends Screen {
	private static final int WHITE = 16777215;

	private final ArmorStand stand;

	public HeadSkinStandScreen(ArmorStand stand) {
		super(Component.translatable("pasheadskins.gui.title"));
		this.stand = stand;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	protected void init() {
		super.init();

		int buttonX = 110;
		int width = 40;
		int height = 20;

		CycleButton<Boolean> skin = CycleButton.booleanBuilder(
			Component.translatable("gui.yes"),
			Component.translatable("gui.no"),
			!HeadSkinFlags.isDisabled(this.stand)
		).displayOnlyValue()
			.withTooltip(value -> Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.head_skin")))
			.create(buttonX, 20 + 0 * 22, width, height, Component.translatable("pasheadskins.gui.label.head_skin"), (button, value) -> {
				HeadSkinControls.setHeadSkinVisible(this.stand, value);
			});

		CycleButton<Boolean> lock = CycleButton.booleanBuilder(
			Component.translatable("gui.yes"),
			Component.translatable("gui.no"),
			HeadSkinFlags.isLocked(this.stand)
		).displayOnlyValue()
			.withTooltip(value -> Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.lock_skin")))
			.create(buttonX, 20 + 1 * 22, width, height, Component.translatable("pasheadskins.gui.label.lock_skin"), (button, value) -> {
				if (!HeadSkinControls.setLocked(this.stand, value) && value) {
					button.setValue(false);
				}
			});

		CycleButton<Boolean> cape = CycleButton.booleanBuilder(
			Component.translatable("gui.yes"),
			Component.translatable("gui.no"),
			HeadSkinFlags.isCapeEnabled(this.stand)
		).displayOnlyValue()
			.withTooltip(value -> Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.cape")))
			.create(buttonX, 20 + 2 * 22, width, height, Component.translatable("pasheadskins.gui.label.cape"), (button, value) -> {
				HeadSkinControls.setCapeEnabled(this.stand, value);
			});

		CycleButton<CapeSource> source = CycleButton.builder(CapeSource::label, HeadSkinFlags.capeSource(this.stand))
			.withValues(CapeSource.MOJANG, CapeSource.ESSENTIAL, CapeSource.BOTH)
			.displayOnlyValue()
			.withTooltip(value -> Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.cape_source")))
			.create(buttonX, 20 + 3 * 22, 72, height, Component.translatable("pasheadskins.gui.label.cape_source"), (button, value) -> {
				HeadSkinControls.setCapeSource(this.stand, value);
			});

		this.addRenderableWidget(skin);
		this.addRenderableWidget(lock);
		this.addRenderableWidget(cape);
		this.addRenderableWidget(source);
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
			.bounds(this.width / 2 - 50, this.height / 4 + 120, 100, 20)
			.build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		int x = 20;
		graphics.text(this.font, I18n.get("pasheadskins.gui.label.head_skin"), x, 20 + 0 * 22 + 10 - 9 / 2, WHITE, true);
		graphics.text(this.font, I18n.get("pasheadskins.gui.label.lock_skin"), x, 20 + 1 * 22 + 10 - 9 / 2, WHITE, true);
		graphics.text(this.font, I18n.get("pasheadskins.gui.label.cape"), x, 20 + 2 * 22 + 10 - 9 / 2, WHITE, true);
		graphics.text(this.font, I18n.get("pasheadskins.gui.label.cape_source"), x, 20 + 3 * 22 + 10 - 9 / 2, WHITE, true);
	}
}
