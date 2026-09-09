package pasheadskins;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

// Figures out where the four skin controls fit for the current scaled
// width/height. Armor Poser's own bottom bar is only nudged when the
// stacked column still lands on screen. High GUI scale gets a tighter
// 2x2 so nothing slides off the alligned row.
public final class HeadSkinPanel {
	public static final int ROW = 22;
	public static final int BTN_H = 20;
	static final int LABEL_X = 20;
	static final int BTN_X = 110;
	static final int TOGGLE_W = 40;
	static final int SOURCE_W = 72;
	static final int LEFT_END = BTN_X + TOGGLE_W;

	private HeadSkinPanel() {
	}

	public record Slot(int labelX, int x, int y, int w) {
	}

	public record Placement(boolean stacked, int bottomShift, boolean drawLabels, Slot skin, Slot lock, Slot cape, Slot source) {
	}

	public static Placement forPoser(int width, int height, Font font, int toggleCount) {
		int firstExtra = toggleCount + 2;
		int stackBottom = 20 + (firstExtra + 3) * ROW + BTN_H;
		int bottomY = height / 4 + 134;
		int poseLeft = width - 120;
		int copyBottom = bottomY + ROW + BTN_H;
		int shift = 4 * ROW;

		boolean stackFits = bottomY >= stackBottom + 2;
		boolean stackAfterShift = copyBottom + shift + 2 <= height;
		if (stackFits || stackAfterShift) {
			return stacked(firstExtra, stackFits ? 0 : shift);
		}
		return compact(width, height, bottomY, poseLeft, font);
	}

	public static Placement forStandalone(int width, int height, Font font) {
		int block = 4 * ROW + 28;
		int top = Math.max(12, (height - block) / 2);
		if (top + block > height - 8) {
			top = 8;
		}
		int labelX = Math.max(12, width / 2 - 110);
		int btnX = labelX + 90;
		if (btnX + SOURCE_W > width - 8) {
			btnX = width - 8 - SOURCE_W;
			labelX = Math.max(8, btnX - 90);
		}
		return new Placement(
			true,
			0,
			true,
			new Slot(labelX, btnX, top + 0 * ROW, TOGGLE_W),
			new Slot(labelX, btnX, top + 1 * ROW, TOGGLE_W),
			new Slot(labelX, btnX, top + 2 * ROW, TOGGLE_W),
			new Slot(labelX, btnX, top + 3 * ROW, SOURCE_W)
		);
	}

	public static int standaloneDoneY(Placement place, int height) {
		int under = place.source.y + BTN_H + 8;
		int max = height - 24;
		return Math.min(under, max);
	}

	public static String label(Font font, String key, String shortKey, int maxWidth) {
		String full = Component.translatable(key).getString();
		if (font.width(full) <= maxWidth) {
			return full;
		}
		if (shortKey != null) {
			return Component.translatable(shortKey).getString();
		}
		return full;
	}

	public static CycleButton<Boolean> yesNo(Slot slot, boolean on, Component onText, Component offText, String tooltipKey, CycleButton.OnValueChange<Boolean> change) {
		return CycleButton.booleanBuilder(onText, offText, on)
			.displayOnlyValue()
			.withTooltip(value -> Tooltip.create(Component.translatable(tooltipKey)))
			.create(slot.x, slot.y, slot.w, BTN_H, Component.empty(), change);
	}

	public static CycleButton<CapeSource> source(Slot slot, CapeSource current, CycleButton.OnValueChange<CapeSource> change) {
		return CycleButton.builder(CapeSource::label, current)
			.withValues(CapeSource.MOJANG, CapeSource.ESSENTIAL, CapeSource.BOTH)
			.displayOnlyValue()
			.withTooltip(value -> Tooltip.create(Component.translatable("pasheadskins.gui.tooltip.cape_source")))
			.create(slot.x, slot.y, slot.w, BTN_H, Component.empty(), change);
	}

	private static Placement stacked(int firstExtra, int shift) {
		return new Placement(
			true,
			shift,
			true,
			row(firstExtra + 0, TOGGLE_W),
			row(firstExtra + 1, TOGGLE_W),
			row(firstExtra + 2, TOGGLE_W),
			row(firstExtra + 3, SOURCE_W)
		);
	}

	private static Slot row(int index, int width) {
		return new Slot(LABEL_X, BTN_X, 20 + index * ROW, width);
	}

	private static Placement compact(int width, int height, int bottomY, int poseLeft, Font font) {
		int gap = 4;
		int minCol = 54;
		int alley = poseLeft - 12 - (LEFT_END + 8);
		int x;
		int y;
		int col;
		if (alley >= minCol * 2 + gap) {
			x = LEFT_END + 8;
			col = Math.min(96, (alley - gap) / 2);
			y = 20;
		} else {
			col = Math.min(96, Math.max(minCol, (width - 24 - gap) / 2));
			int grid = col * 2 + gap;
			x = Math.max(8, (width - grid) / 2);
			y = Math.max(8, Math.min(20, bottomY - 2 * ROW - 4));
			if (y + 2 * ROW + BTN_H > height - 2) {
				y = Math.max(2, height - 2 * ROW - 4);
			}
		}
		int sourceW = Math.max(col, Math.min(col + 8, font.width(Component.translatable("pasheadskins.gui.cape_source.essential").getString()) + 14));
		if (x + col + gap + sourceW > width - 4) {
			sourceW = col;
		}
		return new Placement(
			false,
			0,
			false,
			new Slot(x, x, y, col),
			new Slot(x + col + gap, x + col + gap, y, col),
			new Slot(x, x, y + ROW, col),
			new Slot(x + col + gap, x + col + gap, y + ROW, sourceW)
		);
	}

}
