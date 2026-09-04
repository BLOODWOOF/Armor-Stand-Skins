package pasheadskins.mixin;

import com.mrbysco.armorposer.client.gui.ArmorStandScreen;
import com.mrbysco.armorposer.client.gui.widgets.ToggleButton;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pasheadskins.HeadSkinFlags;
import pasheadskins.net.HeadSkinDisabledPayload;

@Mixin(ArmorStandScreen.class)
public abstract class ArmorStandScreenMixin {
	@Shadow
	protected abstract ArmorStand getArmorStandEntity();

	@Shadow
	protected abstract <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T widget);

	@Inject(method = "init", at = @At("RETURN"))
	private void pasheadskins$addHeadSkinToggle(CallbackInfo ci) {
		ArmorStand stand = this.getArmorStandEntity();
		boolean disabled = HeadSkinFlags.isDisabled(stand);
		ToggleButton button = new ToggleButton.Builder(disabled, clicked -> {
			if (!(clicked instanceof ToggleButton toggle)) {
				return;
			}

			boolean next = !toggle.getValue();
			toggle.setValue(next);
			HeadSkinFlags.setDisabled(stand, next);
			if (ClientPlayNetworking.canSend(HeadSkinDisabledPayload.TYPE)) {
				ClientPlayNetworking.send(new HeadSkinDisabledPayload(stand.getId(), next));
			}
		}).bounds(110, 20 + 6 * 22, 40, 20)
			.tooltip(Tooltip.create(Component.translatable("pasheadskins.toggle.head_skin")))
			.build();

		this.addRenderableWidget(button);
	}
}
