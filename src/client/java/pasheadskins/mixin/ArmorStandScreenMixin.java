package pasheadskins.mixin;

import com.mrbysco.armorposer.client.gui.ArmorStandScreen;
import com.mrbysco.armorposer.client.gui.widgets.ToggleButton;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pasheadskins.CapeSource;
import pasheadskins.HeadSkinControls;
import pasheadskins.HeadSkinFlags;
import pasheadskins.HeadSkinPanel;
import pasheadskins.PoserNameVisible;
import pasheadskins.StandLockSession;
import pasheadskins.StandLockWidgets;
import pasheadskins.StandSlotFlags;

@Mixin(ArmorStandScreen.class)
public abstract class ArmorStandScreenMixin extends Screen {
	@Shadow
	@Final
	private ToggleButton[] toggleButtons;

	@Shadow
	@Final
	private int whiteColor;

	@Shadow
	private EditBox nameField;

	@Shadow
	private Button renameButton;

	@Unique
	private AbstractWidget pasheadskins$skinToggle;

	@Unique
	private HeadSkinPanel.Placement pasheadskins$place;

	@Unique
	private EditBox pasheadskins$password;

	@Unique
	private Map<AbstractWidget, Boolean> pasheadskins$activeSnap;

	@Unique
	private Map<EditBox, Boolean> pasheadskins$editSnap;

	protected ArmorStandScreenMixin(Component title) {
		super(title);
	}

	@Shadow
	protected abstract ArmorStand getArmorStandEntity();

	@Inject(method = "init()V", at = @At("RETURN"))
	private void pasheadskins$addHeadSkinToggle(CallbackInfo ci) {
		HeadSkinPanel.Placement place = HeadSkinPanel.forPoser(this.width, this.height, this.font, this.toggleButtons.length);
		this.pasheadskins$place = place;

		if (place.bottomShift() > 0) {
			int posesY = this.height / 4 + 134;
			for (GuiEventListener child : this.children()) {
				if (child instanceof AbstractWidget widget && widget.getY() >= posesY) {
					widget.setY(widget.getY() + place.bottomShift());
				}
			}
		}

		ArmorStand stand = this.getArmorStandEntity();
		if (place.stacked()) {
			this.pasheadskins$addStacked(stand, place);
		} else {
			this.pasheadskins$addCompact(stand, place);
		}

		this.pasheadskins$password = this.addRenderableWidget(StandLockWidgets.passwordBox(this.font, place.password()));
		this.pasheadskins$moveNameToBottom(place.bottomShift());

		this.pasheadskins$activeSnap = StandLockWidgets.newActiveMap();
		this.pasheadskins$editSnap = StandLockWidgets.newEditMap();
		StandLockWidgets.snapshot(this, this.pasheadskins$password, this.pasheadskins$activeSnap, this.pasheadskins$editSnap);
		StandLockWidgets.apply(this, this.pasheadskins$password, StandLockSession.frozen(stand), this.pasheadskins$activeSnap, this.pasheadskins$editSnap);
	}

	@Unique
	private void pasheadskins$moveNameToBottom(int barShift) {
		if (this.nameField == null) {
			return;
		}
		int nameY = this.height - 22;
		int posesY = this.height / 4 + 134;
		int secondY = posesY + barShift + 22;
		int lift = 0;
		if (secondY + 20 > nameY - 2) {
			lift = (secondY + 20) - (nameY - 2);
		}
		if (lift > 0) {
			for (GuiEventListener child : this.children()) {
				if (child instanceof AbstractWidget widget
					&& widget != this.nameField
					&& widget != this.renameButton
					&& widget != this.pasheadskins$password
					&& widget.getY() >= posesY) {
					widget.setY(widget.getY() - lift);
				}
			}
		}
		int nameW = 100;
		int renameW = 40;
		int x = this.width / 2 - (nameW + 4 + renameW) / 2;
		this.nameField.setX(x);
		this.nameField.setY(nameY);
		if (this.renameButton != null) {
			this.renameButton.setX(x + nameW + 4);
			this.renameButton.setY(nameY);
		}
	}

	@Unique
	private void pasheadskins$addStacked(ArmorStand stand, HeadSkinPanel.Placement place) {
		ToggleButton skin = new ToggleButton.Builder(!HeadSkinFlags.isDisabled(stand), clicked -> {
			if (!(clicked instanceof ToggleButton toggle)) {
				return;
			}
			boolean next = !toggle.getValue();
			toggle.setValue(next);
			HeadSkinControls.setHeadSkinVisible(stand, next);
		}).bounds(place.skin().x(), place.skin().y(), place.skin().w(), HeadSkinPanel.BTN_H)
			.tooltip(Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.head_skin")))
			.build();

		ToggleButton lock = new ToggleButton.Builder(false, clicked -> {
			if (!(clicked instanceof ToggleButton toggle)) {
				return;
			}
			boolean next = !toggle.getValue();
			if (HeadSkinControls.setLocked(stand, next)) {
				toggle.setValue(next);
			} else {
				toggle.setValue(false);
			}
		}).bounds(place.lock().x(), place.lock().y(), place.lock().w(), HeadSkinPanel.BTN_H)
			.tooltip(Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.lock_skin")))
			.build();
		if (HeadSkinFlags.isLocked(stand)) {
			lock.setValue(true);
		}

		ToggleButton cape = new ToggleButton.Builder(false, clicked -> {
			if (!(clicked instanceof ToggleButton toggle)) {
				return;
			}
			boolean next = !toggle.getValue();
			toggle.setValue(next);
			HeadSkinControls.setCapeEnabled(stand, next);
		}).bounds(place.cape().x(), place.cape().y(), place.cape().w(), HeadSkinPanel.BTN_H)
			.tooltip(Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.cape")))
			.build();
		if (HeadSkinFlags.isCapeEnabled(stand)) {
			cape.setValue(true);
		}

		this.pasheadskins$skinToggle = skin;
		this.addRenderableWidget(skin);
		this.addRenderableWidget(lock);
		this.addRenderableWidget(cape);
		this.addRenderableWidget(HeadSkinPanel.source(place.source(), HeadSkinFlags.capeSource(stand), (button, value) -> {
			HeadSkinControls.setCapeSource(stand, value);
		}));
	}

	@Unique
	private void pasheadskins$addCompact(ArmorStand stand, HeadSkinPanel.Placement place) {
		boolean tight = place.skin().w() < 72;
		CycleButton<Boolean> skin = HeadSkinPanel.yesNo(
			place.skin(),
			!HeadSkinFlags.isDisabled(stand),
			Component.translatable(tight ? "pasheadskins.gui.compact.skin.on.short" : "pasheadskins.gui.compact.skin.on"),
			Component.translatable(tight ? "pasheadskins.gui.compact.skin.off.short" : "pasheadskins.gui.compact.skin.off"),
			"pasheadskins.gui.tooltip.head_skin",
			(button, value) -> HeadSkinControls.setHeadSkinVisible(stand, value)
		);
		CycleButton<Boolean> lock = HeadSkinPanel.yesNo(
			place.lock(),
			HeadSkinFlags.isLocked(stand),
			Component.translatable(tight ? "pasheadskins.gui.compact.lock.on.short" : "pasheadskins.gui.compact.lock.on"),
			Component.translatable(tight ? "pasheadskins.gui.compact.lock.off.short" : "pasheadskins.gui.compact.lock.off"),
			"pasheadskins.gui.tooltip.lock_skin",
			(button, value) -> {
				if (!HeadSkinControls.setLocked(stand, value) && value) {
					button.setValue(false);
				}
			}
		);
		CycleButton<Boolean> cape = HeadSkinPanel.yesNo(
			place.cape(),
			HeadSkinFlags.isCapeEnabled(stand),
			Component.translatable(tight ? "pasheadskins.gui.compact.cape.on.short" : "pasheadskins.gui.compact.cape.on"),
			Component.translatable(tight ? "pasheadskins.gui.compact.cape.off.short" : "pasheadskins.gui.compact.cape.off"),
			"pasheadskins.gui.tooltip.cape",
			(button, value) -> HeadSkinControls.setCapeEnabled(stand, value)
		);

		this.pasheadskins$skinToggle = skin;
		this.addRenderableWidget(skin);
		this.addRenderableWidget(lock);
		this.addRenderableWidget(cape);
		this.addRenderableWidget(HeadSkinPanel.source(place.source(), HeadSkinFlags.capeSource(stand), (button, value) -> {
			HeadSkinControls.setCapeSource(stand, value);
		}));
	}

	@Unique
	private void pasheadskins$submitPassword() {
		ArmorStand stand = this.getArmorStandEntity();
		if (stand == null || this.pasheadskins$password == null) {
			return;
		}
		HeadSkinControls.submitPassword(stand, this.pasheadskins$password.getValue());
		this.pasheadskins$password.setValue("");
		StandLockWidgets.apply(this, this.pasheadskins$password, StandLockSession.frozen(stand), this.pasheadskins$activeSnap, this.pasheadskins$editSnap);
	}

	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void pasheadskins$onPasswordEnter(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
		if (this.pasheadskins$password == null || !this.pasheadskins$password.isFocused() || !event.isConfirmation()) {
			return;
		}
		this.pasheadskins$submitPassword();
		cir.setReturnValue(true);
	}

	@Inject(method = "updateEntity", at = @At("HEAD"), cancellable = true)
	private void pasheadskins$blockLockedUpdates(CompoundTag tag, CallbackInfo ci) {
		ArmorStand stand = this.getArmorStandEntity();
		if (stand != null && StandLockSession.frozen(stand)) {
			ci.cancel();
		}
	}

	@Inject(method = "writeFieldsToNBT", at = @At("RETURN"))
	private void pasheadskins$keepSlotFlags(CallbackInfoReturnable<CompoundTag> cir) {
		ArmorStand stand = this.getArmorStandEntity();
		CompoundTag tag = cir.getReturnValue();
		if (tag != null && tag.getBooleanOr("Invisible", false)) {
			this.pasheadskins$setSkinToggle(false);
			if (!HeadSkinFlags.isDisabled(stand)) {
				HeadSkinControls.setHeadSkinVisible(stand, false);
			}
		}
		PoserNameVisible.apply(stand, tag);
		StandSlotFlags.remember(stand);
		StandSlotFlags.keepInTag(tag);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private void pasheadskins$hideSkinIfInvisible(CallbackInfo ci) {
		ArmorStand stand = this.getArmorStandEntity();
		if (stand == null) {
			return;
		}
		if (this.pasheadskins$skinToggle != null && stand.isInvisible()) {
			this.pasheadskins$setSkinToggle(false);
		}
		if (this.pasheadskins$password != null && this.pasheadskins$activeSnap != null) {
			StandLockWidgets.apply(this, this.pasheadskins$password, StandLockSession.frozen(stand), this.pasheadskins$activeSnap, this.pasheadskins$editSnap);
		}
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void pasheadskins$drawHeadSkinLabel(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		HeadSkinPanel.Placement place = this.pasheadskins$place;
		if (place == null || !place.drawLabels()) {
			return;
		}
		this.pasheadskins$drawLabel(graphics, place.skin(), "pasheadskins.gui.label.head_skin", "pasheadskins.gui.label.head_skin.short");
		this.pasheadskins$drawLabel(graphics, place.lock(), "pasheadskins.gui.label.lock_skin", "pasheadskins.gui.label.lock_skin.short");
		this.pasheadskins$drawLabel(graphics, place.cape(), "pasheadskins.gui.label.cape", null);
		this.pasheadskins$drawLabel(graphics, place.source(), "pasheadskins.gui.label.cape_source", "pasheadskins.gui.label.cape_source.short");
		this.pasheadskins$drawLabel(graphics, place.password(), "pasheadskins.gui.label.password", "pasheadskins.gui.label.password.short");
	}

	@Unique
	private void pasheadskins$drawLabel(GuiGraphicsExtractor graphics, HeadSkinPanel.Slot slot, String key, String shortKey) {
		int max = slot.x() - slot.labelX() - 4;
		String text = HeadSkinPanel.label(this.font, key, shortKey, max);
		int y = slot.y() + 10 - 9 / 2;
		graphics.text(this.font, text, slot.labelX(), y, this.whiteColor, true);
	}

	@Unique
	@SuppressWarnings("unchecked")
	private void pasheadskins$setSkinToggle(boolean on) {
		if (this.pasheadskins$skinToggle instanceof ToggleButton toggle) {
			toggle.setValue(on);
		} else if (this.pasheadskins$skinToggle instanceof CycleButton<?> cycle) {
			((CycleButton<Boolean>) cycle).setValue(on);
		}
	}
}
