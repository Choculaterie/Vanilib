package com.choculaterie.vanilib.gui.theme;

public final class UITheme {
	private UITheme() {}

	public static final class Palette {
		public static final int PLUM       = 0xFF4B373D;
		public static final int MAUVE      = 0xFF7E5B63;
		public static final int DUSTY_ROSE = 0xFFBFA1A6;
		public static final int PALE_PINK  = 0xFFD9C6C5;
		public static final int CREAM      = 0xFFF1E2CF;
		public static final int CREAM_LIGHT = 0xFFFFF3D9;
		public static final int GOLD       = 0xFFFFCF7C;
		public static final int CORAL      = 0xFFFF9090;

		private Palette() {}
	}

	public static final class Colors {
		public static final int BACKGROUND        = 0xFF171416;
		public static final int SURFACE           = 0xFF241F22;
		public static final int SURFACE_HOVER     = 0xFF322A2E;
		public static final int SURFACE_SELECTED  = 0xFF3A2C31;
		public static final int OUTLINE           = 0xFF0D0A0C;
		public static final int OUTLINE_HOVER     = Palette.CREAM_LIGHT;
		public static final int DIVIDER_LIGHT     = 0xFF2E272A;

		public static final int ACCENT       = Palette.DUSTY_ROSE;
		public static final int ACCENT_HOVER = Palette.PALE_PINK;
		public static final int ACCENT_DARK  = Palette.PLUM;

		public static final int BUTTON_BG          = 0xFF352E31;
		public static final int BUTTON_BG_HOVER    = 0xFF4A4044;
		public static final int BUTTON_BG_DISABLED = 0xFF262124;
		public static final int BUTTON_BORDER      = Palette.PLUM;

		public static final int PRIMARY_BG          = Palette.MAUVE;
		public static final int PRIMARY_BG_HOVER    = 0xFF977079;
		public static final int PRIMARY_BG_DISABLED = 0xFF3F3236;

		public static final int DANGER_BG          = 0xFF6E2D33;
		public static final int DANGER_BG_HOVER    = 0xFF934049;
		public static final int DANGER_BG_DISABLED = 0xFF3A1F22;

		public static final int SUCCESS_BG_REST     = 0xFF2E5636;
		public static final int SUCCESS_BG_HOVER    = 0xFF3E7449;
		public static final int SUCCESS_BG_DISABLED = 0xFF1E3022;

		public static final int TEXT_PRIMARY   = 0xFFE6DCDA;
		public static final int TEXT_HIGHLIGHT = Palette.CREAM_LIGHT;
		public static final int TEXT_DISABLED  = 0xFF6E6366;
		public static final int TEXT_MUTED     = 0xFF9A8E90;
		public static final int TEXT_SUBTITLE  = 0xFFBDB1B2;
		public static final int TEXT_TAG       = Palette.PALE_PINK;
		public static final int TEXT_SHADOW    = 0xFF120E10;
		public static final int TEXT_LINK      = Palette.DUSTY_ROSE;
		public static final int TEXT_LINK_HOVER = Palette.CREAM_LIGHT;

		public static final int FIELD_BG             = 0xFF1A1618;
		public static final int FIELD_BORDER         = 0xFF3A3135;
		public static final int FIELD_BORDER_FOCUSED = Palette.DUSTY_ROSE;
		public static final int SELECTION_HIGHLIGHT  = 0x66BFA1A6;

		public static final int PANEL_BG           = 0xFF1C181A;
		public static final int PANEL_BG_SECONDARY = SURFACE;
		public static final int PANEL_BORDER       = 0xFF3A3135;

		public static final int TOAST_BG_SUCCESS     = 0xFF1E2A21;
		public static final int TOAST_BG_ERROR       = 0xFF2E1A1D;
		public static final int TOAST_BG_INFO        = 0xFF261F22;
		public static final int TOAST_BG_WARNING     = 0xFF2E2619;
		public static final int TOAST_BORDER         = 0xFF3A3135;
		public static final int TOAST_BORDER_HOVER   = Palette.DUSTY_ROSE;
		public static final int TOAST_ACCENT_SUCCESS = 0xFF7FCB8A;
		public static final int TOAST_ACCENT_ERROR   = Palette.CORAL;
		public static final int TOAST_ACCENT_INFO    = Palette.DUSTY_ROSE;
		public static final int TOAST_ACCENT_WARNING = Palette.GOLD;

		public static final int OVERLAY_BG   = 0xB0000000;
		public static final int CONTAINER_BG = SURFACE;
		public static final int DIVIDER      = 0xFF3A3135;

		public static final int SCROLLBAR_BG          = 0xFF2A2326;
		public static final int SCROLLBAR_THUMB       = 0xFF5E4F55;
		public static final int SCROLLBAR_THUMB_HOVER = 0xFF85707A;

		public static final int TOGGLE_ON        = Palette.MAUVE;
		public static final int TOGGLE_ON_HOVER  = 0xFF977079;
		public static final int TOGGLE_OFF       = 0xFF5E4F55;
		public static final int TOGGLE_OFF_HOVER = 0xFF85707A;
		public static final int TOGGLE_KNOB      = Palette.CREAM_LIGHT;

		public static final int ERROR_TEXT   = Palette.CORAL;
		public static final int WARNING_TEXT = Palette.GOLD;
		public static final int SUCCESS_TEXT = 0xFF7FCB8A;
		public static final int SUCCESS_BG   = SUCCESS_BG_REST;

		private Colors() {}
	}

	public static final class Dimensions {
		public static final int PADDING       = 10;
		public static final int PADDING_SMALL = 5;
		public static final int BORDER_WIDTH  = 1;

		public static final int BUTTON_HEIGHT       = 21;
		public static final int BUTTON_HEIGHT_LARGE = 31;
		public static final int SEARCH_BAR_HEIGHT   = 21;

		public static final int TITLE_BAR_HEIGHT = 31;
		public static final int SIDEBAR_WIDTH    = 120;
		public static final int FADE_HEIGHT      = 12;

		public static final int TOAST_HEIGHT        = 70;
		public static final int TOAST_MIN_WIDTH     = 200;
		public static final int TOAST_MAX_WIDTH     = 300;

		public static final int ICON_SMALL  = 13;
		public static final int ICON_MEDIUM = 17;
		public static final int ICON_LARGE  = 25;

		public static final int SCROLLBAR_WIDTH = 6;

		private Dimensions() {}
	}

	public static final class Typography {
		public static final int TEXT_HEIGHT = 8;
		public static final int CAP_HEIGHT = 7;
		public static final int LINE_HEIGHT = 12;

		private Typography() {}
	}

	public static final class Animation {
		public static final int TOAST_DISPLAY_DURATION = 5000;
		public static final int TOAST_FADE_DURATION    = 500;

		private Animation() {}
	}

	public static final class Layout {
		public static final int ITEMS_PER_PAGE    = 20;
		public static final int SPLIT_PANEL_RATIO = 2;

		private Layout() {}
	}
}
