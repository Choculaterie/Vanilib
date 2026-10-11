package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.gui.theme.UIDraw;
import com.choculaterie.vanilib.gui.theme.UITheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;

import java.util.function.Consumer;

public class ToggleButton extends Button {
	private static final int TOGGLE_WIDTH = 40;
	private static final int TOGGLE_HEIGHT = 21;
	private static final int TRACK_WIDTH = 26;
	private static final int TRACK_HEIGHT = 14;
	private static final int KNOB_SIZE = 8;

	private boolean toggled;
	private final Consumer<Boolean> onToggle;

	public ToggleButton(int x, int y, boolean initialState, Consumer<Boolean> onToggle) {
		super(x, y, TOGGLE_WIDTH, TOGGLE_HEIGHT, net.minecraft.network.chat.Component.literal(" "),
			button -> ((ToggleButton) button).toggle(),
			DEFAULT_NARRATION);
		this.toggled = initialState;
		this.onToggle = onToggle;
	}

	private void toggle() {
		this.toggled = !this.toggled;
		if (this.onToggle != null) {
			this.onToggle.accept(this.toggled);
		}
	}

	public boolean isToggled() {
		return toggled;
	}

	public void setToggled(boolean toggled) {
		this.toggled = toggled;
	}

	@Override
	protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		boolean hovered = this.active && mouseX >= getX() && mouseY >= getY()
			&& mouseX < getX() + getWidth() && mouseY < getY() + getHeight();

		int x1 = getX() + (getWidth() - TRACK_WIDTH) / 2;
		int y1 = getY() + (getHeight() - TRACK_HEIGHT) / 2;
		int x2 = x1 + TRACK_WIDTH;
		int y2 = y1 + TRACK_HEIGHT;

		int track;
		if (!this.active) {
			track = UITheme.Colors.BUTTON_BG_DISABLED;
		} else if (toggled) {
			track = hovered ? UITheme.Colors.TOGGLE_ON_HOVER : UITheme.Colors.TOGGLE_ON;
		} else {
			track = hovered ? UITheme.Colors.TOGGLE_OFF_HOVER : UITheme.Colors.TOGGLE_OFF;
		}
		int border = hovered ? UITheme.Colors.OUTLINE_HOVER : UITheme.Colors.OUTLINE;

		context.fill(x1 + 1, y1, x2 - 1, y2, border);
		context.fill(x1, y1 + 1, x2, y2 - 1, border);
		context.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, track);

		int knobY = y1 + (TRACK_HEIGHT - KNOB_SIZE) / 2;
		int knobX = toggled ? x2 - 3 - KNOB_SIZE : x1 + 3;
		int knob = this.active ? UITheme.Colors.TOGGLE_KNOB : UITheme.Colors.TEXT_DISABLED;
		context.fill(knobX + 1, knobY + 1, knobX + KNOB_SIZE + 1, knobY + KNOB_SIZE + 1,
			UIDraw.withAlpha(UITheme.Colors.TEXT_SHADOW, 0xA0));
		context.fill(knobX, knobY, knobX + KNOB_SIZE, knobY + KNOB_SIZE, knob);
	}
}
