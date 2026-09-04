package pasheadskins.mixin;

import com.mrbysco.armorposer.client.gui.ArmorStandScreen;
import com.mrbysco.armorposer.client.gui.widgets.ToggleButton;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.HeadSkinFlags;
import pasheadskins.net.HeadSkinDisabledPayload;

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
		ArmorStand stand = this.getArmorStandEntity();
		boolean enabled = !HeadSkinFlags.isDisabled(stand);
		int row = this.toggleButtons.length + 2;
		ToggleButton button = new ToggleButton.Builder(enabled, clicked -> {
			if (!(clicked instanceof ToggleButton toggle)) {
				return;
			}

			boolean next = !toggle.getValue();
			toggle.setValue(next);
			HeadSkinFlags.setDisabled(stand, !next);
			if (ClientPlayNetworking.canSend(HeadSkinDisabledPayload.TYPE)) {
				ClientPlayNetworking.send(new HeadSkinDisabledPayload(stand.getId(), !next));
			}
		}).bounds(110, 20 + row * 22, 40, 20)
			.tooltip(Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.head_skin")))
			.build();

		this.addRenderableWidget(button);
	}

	@Inject(method = "extractRenderState", at = @At("RETURN"))
	private void pasheadskins$drawHeadSkinLabel(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		int row = this.toggleButtons.length + 2;
		int x = 20;
		int y = 20 + row * 22 + 10 - 9 / 2;
		graphics.text(this.font, I18n.get("pasheadskins.gui.label.head_skin"), x, y, this.whiteColor, true);
	}
}
