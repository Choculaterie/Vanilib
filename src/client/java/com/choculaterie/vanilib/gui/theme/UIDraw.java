package com.choculaterie.vanilib.gui.theme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class UIDraw {
	public static final int ICON_TEXT_GAP = 4;
	private static int shadowOverride = 0;
	private static final long FLASH_MS = 250;

	private UIDraw() {}

	public static void playClick() {
		AbstractWidget.playButtonClickSound(Minecraft.getInstance().getSoundManager());
	}

	public static Font font() {
		return Minecraft.getInstance().font;
	}

	public static int lighten(int argb, float amount) {
		int a = argb >>> 24;
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;
		r = (int) (r + (255 - r) * amount);
		g = (int) (g + (255 - g) * amount);
		b = (int) (b + (255 - b) * amount);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public static int darken(int argb, float amount) {
		int a = argb >>> 24;
		int r = (int) (((argb >> 16) & 0xFF) * (1 - amount));
		int g = (int) (((argb >> 8) & 0xFF) * (1 - amount));
		int b = (int) ((argb & 0xFF) * (1 - amount));
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	public static int withAlpha(int argb, int alpha) {
		return (Math.max(0, Math.min(255, alpha)) << 24) | (argb & 0xFFFFFF);
	}

	public static int flatContentY(int y, int height) {
		return y + (height - UITheme.Typography.CAP_HEIGHT) / 2;
	}

	public static int bevelContentY(int y, int height) {
		return flatContentY(y, height);
	}

	public static void sidePanel(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2) {
		ctx.fill(x1, y1, x2, y2, UITheme.Colors.PANEL_BG);
		outline(ctx, x1, y1, x2, y2, UITheme.Colors.PANEL_BORDER);
	}

	public static void buttonContent(GuiGraphicsExtractor ctx, int x, int y, int width, int height,
									 UIIcon icon, String text, int color, boolean bevel) {
		buttonContent(ctx, x, y, width, height, icon, text, color, bevel, false);
	}

	public static void buttonContent(GuiGraphicsExtractor ctx, int x, int y, int width, int height,
									 UIIcon icon, String text, int color, boolean bevel, boolean iconAfter) {
		int contentY = bevel ? bevelContentY(y, height) : flatContentY(y, height);
		boolean hasText = text != null && !text.isEmpty();
		if (icon != null && !hasText) {
			icon.drawCenteredIn(ctx, x, y, width, height, color);
			return;
		}
		int iconSpace = icon != null ? icon.inkWidth() + ICON_TEXT_GAP : 0;
		String shown = ellipsize(hasText ? text : "", Math.max(0, width - 8 - iconSpace));
		int textInk = Math.max(0, font().width(shown) - 1);
		int startX = x + (width - textInk - iconSpace) / 2;
		if (icon != null && iconAfter) {
			text(ctx, shown, startX, contentY, color);
			icon.drawOnText(ctx, startX + textInk + ICON_TEXT_GAP - icon.inkLeft(), contentY, color);
			return;
		}
		if (icon != null) {
			icon.drawOnText(ctx, startX - icon.inkLeft(), contentY, color);
			startX += iconSpace;
		}
		text(ctx, shown, startX, contentY, color);
	}

	public static void drawButton(GuiGraphicsExtractor ctx, int x, int y, int width, int height, ButtonStyle style,
								  boolean active, boolean hovered, UIIcon icon, String text) {
		button(ctx, x, y, x + width, y + height, style, active, hovered);
		applyButtonShadow(style, active, hovered);
		buttonContent(ctx, x, y, width, height, icon, text, buttonTextColor(active, hovered), style != ButtonStyle.GHOST);
		clearShadowOverride();
	}

	public static void applyButtonShadow(ButtonStyle style, boolean active, boolean hovered) {
		if (style == ButtonStyle.PRIMARY || style == ButtonStyle.DANGER || style == ButtonStyle.SUCCESS) {
			setShadowOverride(darken(style.fill(active, hovered), 0.45f));
		}
	}

	public static void successFlash(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2, long startMillis) {
		long elapsed = System.currentTimeMillis() - startMillis;
		if (elapsed >= 0 && elapsed < FLASH_MS) {
			int alpha = (int) (0x70 * (1 - elapsed / (float) FLASH_MS));
			ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, withAlpha(0xFFFFFFFF, alpha));
		}
	}

	public static void chip(GuiGraphicsExtractor ctx, int x, int y, int width, int height, String label, boolean selected, boolean hovered) {
		ButtonStyle style = selected ? ButtonStyle.PRIMARY : ButtonStyle.GRAY;
		button(ctx, x, y, x + width, y + height, style, true, hovered);
		int color = selected || hovered ? UITheme.Colors.TEXT_HIGHLIGHT : UITheme.Colors.TEXT_SUBTITLE;
		String shown = ellipsize(label, width - 6);
		centeredText(ctx, shown, x + width / 2, bevelContentY(y, height), color);
	}

	public static int iconText(GuiGraphicsExtractor ctx, UIIcon icon, String text, int x, int y, int color) {
		icon.drawOnText(ctx, x, y, color);
		int textX = x + UIIcon.SIZE + 4;
		text(ctx, text, textX, y, color);
		return textX + font().width(text);
	}

	public static boolean isInside(double mx, double my, int x, int y, int width, int height) {
		return mx >= x && my >= y && mx < x + width && my < y + height;
	}

	public static void sectionHeader(GuiGraphicsExtractor ctx, String title, int x1, int y, int x2) {
		text(ctx, title, x1, y, UITheme.Colors.ACCENT);
		hDivider(ctx, x1, x2, y + UITheme.Typography.TEXT_HEIGHT + 3, UITheme.Colors.DIVIDER_LIGHT);
	}

	public static void outline(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2, int color) {
		ctx.fill(x1, y1, x2, y1 + 1, color);
		ctx.fill(x1, y2 - 1, x2, y2, color);
		ctx.fill(x1, y1 + 1, x1 + 1, y2 - 1, color);
		ctx.fill(x2 - 1, y1 + 1, x2, y2 - 1, color);
	}

	public static void panel(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2, int fill, int border) {
		ctx.fill(x1, y1, x2, y2, fill);
		outline(ctx, x1, y1, x2, y2, border);
	}

	public static void hDivider(GuiGraphicsExtractor ctx, int x1, int x2, int y, int color) {
		ctx.fill(x1, y, x2, y + 1, color);
	}

	public static void vDivider(GuiGraphicsExtractor ctx, int x, int y1, int y2, int color) {
		ctx.fill(x, y1, x + 1, y2, color);
	}

	public static void bevel(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2, int base, int outline) {
		if ((base >>> 24) != 0) {
			ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, base);
			int highlight = lighten(base, 0.12f);
			int shadow = withAlpha(darken(base, 0.55f), 0xC0);
			ctx.fill(x1 + 1, y1 + 1, x2 - 1, y1 + 2, highlight);
			ctx.fill(x1 + 1, y1 + 2, x1 + 2, y2 - 1, highlight);
			ctx.fill(x2 - 2, y1 + 2, x2 - 1, y2 - 1, shadow);
			ctx.fill(x1 + 2, y2 - 2, x2 - 2, y2 - 1, shadow);
		}
		outline(ctx, x1, y1, x2, y2, outline);
	}

	public static void button(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2, ButtonStyle style, boolean active, boolean hovered) {
		int fill = style.fill(active, hovered);
		if (style == ButtonStyle.GHOST) {
			if (active && hovered) ctx.fill(x1, y1, x2, y2, fill);
			return;
		}
		int border = active && hovered ? UITheme.Colors.OUTLINE_HOVER : UITheme.Colors.OUTLINE;
		bevel(ctx, x1, y1, x2, y2, fill, border);
	}

	public static int buttonTextColor(boolean active, boolean hovered) {
		if (!active) return UITheme.Colors.TEXT_DISABLED;
		return hovered ? UITheme.Colors.TEXT_HIGHLIGHT : UITheme.Colors.TEXT_PRIMARY;
	}

	public static void text(GuiGraphicsExtractor ctx, String text, int x, int y, int color) {
		Font f = font();
		ctx.text(f, text, x + 1, y + 1, shadowFor(color), false);
		ctx.text(f, text, x, y, color, false);
	}

	public static void text(GuiGraphicsExtractor ctx, Component text, int x, int y, int color) {
		Font f = font();
		ctx.text(f, text, x + 1, y + 1, shadowFor(color), false);
		ctx.text(f, text, x, y, color, false);
	}

	public static void text(GuiGraphicsExtractor ctx, FormattedCharSequence text, int x, int y, int color) {
		Font f = font();
		ctx.text(f, text, x + 1, y + 1, shadowFor(color), false);
		ctx.text(f, text, x, y, color, false);
	}

	public static void centeredText(GuiGraphicsExtractor ctx, String text, int centerX, int y, int color) {
		text(ctx, text, centerX - font().width(text) / 2, y, color);
	}

	public static void centeredText(GuiGraphicsExtractor ctx, Component text, int centerX, int y, int color) {
		text(ctx, text, centerX - font().width(text) / 2, y, color);
	}

	public static void rightText(GuiGraphicsExtractor ctx, String text, int rightX, int y, int color) {
		text(ctx, text, rightX - font().width(text), y, color);
	}

	public static String ellipsize(String text, int maxWidth) {
		Font f = font();
		if (f.width(text) <= maxWidth) return text;
		String dots = "...";
		int room = Math.max(0, maxWidth - f.width(dots));
		return f.plainSubstrByWidth(text, room) + dots;
	}

	public static void overlay(GuiGraphicsExtractor ctx) {
		Minecraft mc = Minecraft.getInstance();
		ctx.fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), UITheme.Colors.OVERLAY_BG);
	}

	public static void modalFrame(GuiGraphicsExtractor ctx, int x, int y, int width, int height) {
		int x2 = x + width;
		int y2 = y + height;
		ctx.fill(x + 2, y + 2, x2 + 2, y2 + 2, withAlpha(UITheme.Colors.OUTLINE, 0x80));
		ctx.fill(x, y, x2, y2, UITheme.Colors.PANEL_BG);
		outline(ctx, x, y, x2, y2, UITheme.Colors.OUTLINE);
		outline(ctx, x + 1, y + 1, x2 - 1, y2 - 1, UITheme.Colors.PANEL_BORDER);
	}

	public static void modalTitle(GuiGraphicsExtractor ctx, String title, int x, int y, int width) {
		centeredText(ctx, ellipsize(title, width - 2 * UITheme.Dimensions.PADDING), x + width / 2, y, UITheme.Colors.ACCENT);
	}

	public static void fadeTop(GuiGraphicsExtractor ctx, int x1, int y, int x2, int height, int color) {
		ctx.fillGradient(x1, y, x2, y + height, color, withAlpha(color, 0));
	}

	public static void fadeBottom(GuiGraphicsExtractor ctx, int x1, int y, int x2, int height, int color) {
		ctx.fillGradient(x1, y - height, x2, y, withAlpha(color, 0), color);
	}

	private static int shadowFor(int color) {
		int alpha = color >>> 24;
		int base = shadowOverride != 0 ? shadowOverride : UITheme.Colors.TEXT_SHADOW;
		return withAlpha(base, alpha == 0 ? 255 : alpha);
	}

	public static int shadowColor(int color) {
		return shadowFor(color);
	}

	public static void setShadowOverride(int color) {
		shadowOverride = color;
	}

	public static void clearShadowOverride() {
		shadowOverride = 0;
	}
}
