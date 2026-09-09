package pasheadskins.mixin;

import com.mrbysco.armorposer.client.gui.ArmorStandScreen;
import com.mrbysco.armorposer.client.gui.widgets.ToggleButton;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pasheadskins.CapeSource;
import pasheadskins.HeadSkinControls;
import pasheadskins.HeadSkinFlags;
import pasheadskins.StandSlotFlags;

@Mixin(ArmorStandScreen.class)
public abstract class ArmorStandScreenMixin extends Screen {
	@Shadow
	@Final
	private ToggleButton[] toggleButtons;

	@Shadow
	@Final
	private int whiteColor;

	protected ArmorStandScreenMixin(Component title) {
		super(title);
	}

	@Shadow
	protected abstract ArmorStand getArmorStandEntity();

	@Inject(method = "init()V", at = @At("RETURN"))
	private void pasheadskins$addHeadSkinToggle(CallbackInfo ci) {
		int posesY = this.height / 4 + 134;
		for (GuiEventListener child : this.children()) {
			if (child instanceof AbstractWidget widget && widget.getY() >= posesY) {
				widget.setY(widget.getY() + 88);
			}
		}

		ArmorStand stand = this.getArmorStandEntity();
		int skinRow = this.toggleButtons.length + 2;
		int lockRow = this.toggleButtons.length + 3;
		int capeRow = this.toggleButtons.length + 4;
		int sourceRow = this.toggleButtons.length + 5;

		ToggleButton skin = new ToggleButton.Builder(!HeadSkinFlags.isDisabled(stand), clicked -> {
			if (!(clicked instanceof ToggleButton toggle)) {
				return;
			}

			boolean next = !toggle.getValue();
			toggle.setValue(next);
			HeadSkinControls.setHeadSkinVisible(stand, next);
		}).bounds(110, 20 + skinRow * 22, 40, 20)
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
		}).bounds(110, 20 + lockRow * 22, 40, 20)
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
		}).bounds(110, 20 + capeRow * 22, 40, 20)
			.tooltip(Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.cape")))
			.build();

		if (HeadSkinFlags.isCapeEnabled(stand)) {
			cape.setValue(true);
		}

		CycleButton<CapeSource> source = CycleButton.builder(CapeSource::label, HeadSkinFlags.capeSource(stand))
			.withValues(CapeSource.MOJANG, CapeSource.ESSENTIAL, CapeSource.BOTH)
			.displayOnlyValue()
			.withTooltip(value -> Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.cape_source")))
			.create(110, 20 + sourceRow * 22, 72, 20, Component.translatable("pasheadskins.gui.label.cape_source"), (button, value) -> {
				HeadSkinControls.setCapeSource(stand, value);
			});

		this.addRenderableWidget(skin);
		this.addRenderableWidget(lock);
		this.addRenderableWidget(cape);
		this.addRenderableWidget(source);
	}

	@Inject(method = "writeFieldsToNBT", at = @At("RETURN"))
	private void pasheadskins$keepSlotFlags(CallbackInfoReturnable<CompoundTag> cir) {
		ArmorStand stand = this.getArmorStandEntity();
		StandSlotFlags.remember(stand);
		StandSlotFlags.keepInTag(cir.getReturnValue());
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void pasheadskins$drawHeadSkinLabel(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		int skinRow = this.toggleButtons.length + 2;
		int lockRow = this.toggleButtons.length + 3;
		int capeRow = this.toggleButtons.length + 4;
		int sourceRow = this.toggleButtons.length + 5;
		int x = 20;
		int skinY = 20 + skinRow * 22 + 10 - 9 / 2;
		int lockY = 20 + lockRow * 22 + 10 - 9 / 2;
		int capeY = 20 + capeRow * 22 + 10 - 9 / 2;
		int sourceY = 20 + sourceRow * 22 + 10 - 9 / 2;
		graphics.text(this.font, I18n.get("pasheadskins.gui.label.head_skin"), x, skinY, this.whiteColor, true);
		graphics.text(this.font, I18n.get("pasheadskins.gui.label.lock_skin"), x, lockY, this.whiteColor, true);
		graphics.text(this.font, I18n.get("pasheadskins.gui.label.cape"), x, capeY, this.whiteColor, true);
		graphics.text(this.font, I18n.get("pasheadskins.gui.label.cape_source"), x, sourceY, this.whiteColor, true);
	}
}
