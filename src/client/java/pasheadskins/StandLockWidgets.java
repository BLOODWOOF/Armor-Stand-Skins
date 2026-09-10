package pasheadskins;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

public final class StandLockWidgets {
	private StandLockWidgets() {
	}

	public static EditBox passwordBox(Font font, HeadSkinPanel.Slot slot) {
		EditBox box = new EditBox(font, slot.x(), slot.y(), slot.w(), HeadSkinPanel.BTN_H, Component.translatable("pasheadskins.gui.label.password"));
		box.setMaxLength(64);
		box.setHint(Component.translatable("pasheadskins.gui.hint.password"));
		box.setTooltip(Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.password")));
		box.addFormatter((text, first) -> FormattedCharSequence.forward("*".repeat(text.length()), Style.EMPTY));
		return box;
	}

	public static void snapshot(Screen screen, EditBox password, Map<AbstractWidget, Boolean> active, Map<EditBox, Boolean> editable) {
		active.clear();
		editable.clear();
		String cancel = Component.translatable("gui.cancel").getString();
		for (GuiEventListener child : screen.children()) {
			if (!(child instanceof AbstractWidget widget) || widget == password) {
				continue;
			}
			if (cancel.equals(widget.getMessage().getString())) {
				continue;
			}
			active.put(widget, widget.active);
			if (widget instanceof EditBox box) {
				editable.put(box, box.isFocused() || box.active);
			}
		}
	}

	public static void apply(Screen screen, EditBox password, boolean frozen, Map<AbstractWidget, Boolean> active, Map<EditBox, Boolean> editable) {
		if (active == null) {
			return;
		}
		String cancel = Component.translatable("gui.cancel").getString();
		for (GuiEventListener child : screen.children()) {
			if (!(child instanceof AbstractWidget widget)) {
				continue;
			}
			if (widget == password) {
				widget.active = true;
				if (widget instanceof EditBox box) {
					box.setEditable(true);
				}
				continue;
			}
			if (cancel.equals(widget.getMessage().getString())) {
				widget.active = true;
				continue;
			}
			if (frozen) {
				widget.active = false;
				if (widget instanceof EditBox box) {
					box.setEditable(false);
				}
			} else {
				Boolean was = active.get(widget);
				widget.active = was == null || was;
				if (widget instanceof EditBox box) {
					Boolean ed = editable.get(box);
					box.setEditable(ed == null || ed);
				}
			}
		}
	}

	public static IdentityHashMap<AbstractWidget, Boolean> newActiveMap() {
		return new IdentityHashMap<>();
	}

	public static IdentityHashMap<EditBox, Boolean> newEditMap() {
		return new IdentityHashMap<>();
	}
}
