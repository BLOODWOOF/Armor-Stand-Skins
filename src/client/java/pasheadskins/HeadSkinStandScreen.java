package pasheadskins;

import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;

public class HeadSkinStandScreen extends Screen {
	private static final int WHITE = 16777215;

	private final ArmorStand stand;
	private HeadSkinPanel.Placement place;
	private EditBox password;
	private Map<AbstractWidget, Boolean> activeSnap;
	private Map<EditBox, Boolean> editSnap;

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

		HeadSkinPanel.Placement next = HeadSkinPanel.forStandalone(this.width, this.height, this.font);
		this.place = next;

		CycleButton<Boolean> skin = HeadSkinPanel.yesNo(
			next.skin(),
			!HeadSkinFlags.isDisabled(this.stand),
			Component.translatable("gui.yes"),
			Component.translatable("gui.no"),
			"pasheadskins.gui.tooltip.head_skin",
			(button, value) -> HeadSkinControls.setHeadSkinVisible(this.stand, value)
		);
		CycleButton<Boolean> lock = HeadSkinPanel.yesNo(
			next.lock(),
			HeadSkinFlags.isLocked(this.stand),
			Component.translatable("gui.yes"),
			Component.translatable("gui.no"),
			"pasheadskins.gui.tooltip.lock_skin",
			(button, value) -> {
				if (!HeadSkinControls.setLocked(this.stand, value) && value) {
					button.setValue(false);
				}
			}
		);
		CycleButton<Boolean> cape = HeadSkinPanel.yesNo(
			next.cape(),
			HeadSkinFlags.isCapeEnabled(this.stand),
			Component.translatable("gui.yes"),
			Component.translatable("gui.no"),
			"pasheadskins.gui.tooltip.cape",
			(button, value) -> HeadSkinControls.setCapeEnabled(this.stand, value)
		);

		this.addRenderableWidget(skin);
		this.addRenderableWidget(lock);
		this.addRenderableWidget(cape);
		this.addRenderableWidget(HeadSkinPanel.source(next.source(), HeadSkinFlags.capeSource(this.stand), (button, value) -> {
			HeadSkinControls.setCapeSource(this.stand, value);
		}));
		this.password = this.addRenderableWidget(StandLockWidgets.passwordBox(this.font, next.password()));

		int doneY = HeadSkinPanel.standaloneDoneY(next, this.height);
		this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> this.onClose())
			.bounds(this.width / 2 - 50, doneY, 100, 20)
			.build());

		this.activeSnap = StandLockWidgets.newActiveMap();
		this.editSnap = StandLockWidgets.newEditMap();
		StandLockWidgets.snapshot(this, this.password, this.activeSnap, this.editSnap);
		StandLockWidgets.apply(this, this.password, StandLockSession.frozen(this.stand), this.activeSnap, this.editSnap);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.password != null && this.password.isFocused() && event.isConfirmation()) {
			this.submitPassword();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		HeadSkinPanel.Placement next = this.place;
		if (next == null) {
			return;
		}
		this.drawLabel(graphics, next.skin(), "pasheadskins.gui.label.head_skin", "pasheadskins.gui.label.head_skin.short");
		this.drawLabel(graphics, next.lock(), "pasheadskins.gui.label.lock_skin", "pasheadskins.gui.label.lock_skin.short");
		this.drawLabel(graphics, next.cape(), "pasheadskins.gui.label.cape", null);
		this.drawLabel(graphics, next.source(), "pasheadskins.gui.label.cape_source", "pasheadskins.gui.label.cape_source.short");
		this.drawLabel(graphics, next.password(), "pasheadskins.gui.label.password", "pasheadskins.gui.label.password.short");
	}

	private void submitPassword() {
		if (this.password == null) {
			return;
		}
		HeadSkinControls.submitPassword(this.stand, this.password.getValue());
		this.password.setValue("");
		StandLockWidgets.apply(this, this.password, StandLockSession.frozen(this.stand), this.activeSnap, this.editSnap);
	}

	private void drawLabel(GuiGraphicsExtractor graphics, HeadSkinPanel.Slot slot, String key, String shortKey) {
		int max = slot.x() - slot.labelX() - 4;
		String text = HeadSkinPanel.label(this.font, key, shortKey, max);
		int y = slot.y() + 10 - 9 / 2;
		graphics.text(this.font, text, slot.labelX(), y, WHITE, true);
	}
}
