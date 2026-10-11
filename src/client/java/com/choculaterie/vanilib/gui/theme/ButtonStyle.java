package com.choculaterie.vanilib.gui.theme;

public enum ButtonStyle {
	GRAY(UITheme.Colors.BUTTON_BG, UITheme.Colors.BUTTON_BG_HOVER, UITheme.Colors.BUTTON_BG_DISABLED),
	PRIMARY(UITheme.Colors.PRIMARY_BG, UITheme.Colors.PRIMARY_BG_HOVER, UITheme.Colors.PRIMARY_BG_DISABLED),
	DANGER(UITheme.Colors.DANGER_BG, UITheme.Colors.DANGER_BG_HOVER, UITheme.Colors.DANGER_BG_DISABLED),
	SUCCESS(UITheme.Colors.SUCCESS_BG_REST, UITheme.Colors.SUCCESS_BG_HOVER, UITheme.Colors.SUCCESS_BG_DISABLED),
	GHOST(0, UITheme.Colors.SURFACE_HOVER, 0);

	public final int base;
	public final int hover;
	public final int disabled;

	ButtonStyle(int base, int hover, int disabled) {
		this.base = base;
		this.hover = hover;
		this.disabled = disabled;
	}

	public int fill(boolean active, boolean hovered) {
		if (!active) return disabled;
		return hovered ? hover : base;
	}
}
