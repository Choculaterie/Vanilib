package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.gui.theme.ButtonStyle;
import com.choculaterie.vanilib.gui.theme.UIDraw;
import com.choculaterie.vanilib.gui.theme.UIIcon;
import com.choculaterie.vanilib.gui.theme.UITheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;

public class CustomButton extends Button {
    private static final int ICON_TEXT_GAP = 4;
    private static final long SUCCESS_MS = 1500;
    private static final long ERROR_MS = 2500;

    private boolean renderAsXIcon = false;
    private boolean renderAsDownloadIcon = false;
    private ButtonStyle style = ButtonStyle.GRAY;
    private UIIcon icon;
    private boolean selected = false;
    private boolean iconAfterText = false;
    private long flashStart = 0;
    private long flashDuration = 0;
    private String flashLabel = "";
    private ButtonStyle flashStyle = ButtonStyle.SUCCESS;
    private UIIcon flashIcon = UIIcon.CHECK;

    public CustomButton(int x, int y, int width, int height, net.minecraft.network.chat.Component message, Button.OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public void setRenderAsXIcon(boolean renderAsXIcon) {
        this.renderAsXIcon = renderAsXIcon;
    }

    public void setRenderAsDownloadIcon(boolean renderAsDownloadIcon) {
        this.renderAsDownloadIcon = renderAsDownloadIcon;
    }

    public CustomButton setStyle(ButtonStyle style) {
        this.style = style;
        return this;
    }

    public ButtonStyle getStyle() {
        return style;
    }

    public CustomButton setIcon(UIIcon icon) {
        this.icon = icon;
        return this;
    }

    public CustomButton setIconAfterText(boolean iconAfterText) {
        this.iconAfterText = iconAfterText;
        return this;
    }

    public CustomButton setSelected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public CustomButton showSuccess(String label) {
        return flash(ButtonStyle.SUCCESS, UIIcon.CHECK, label, SUCCESS_MS);
    }

    public CustomButton showError(String label) {
        return flash(ButtonStyle.DANGER, UIIcon.ALERT, label, ERROR_MS);
    }

    public CustomButton clearFlash() {
        this.flashStart = 0;
        return this;
    }

    public boolean isShowingSuccess() {
        return flashStart > 0 && System.currentTimeMillis() - flashStart < flashDuration;
    }

    private CustomButton flash(ButtonStyle style, UIIcon icon, String label, long duration) {
        this.flashStart = System.currentTimeMillis();
        this.flashDuration = duration;
        this.flashStyle = style;
        this.flashIcon = icon;
        this.flashLabel = label == null ? "" : label;
        return this;
    }

    public void press() {
        if (!this.active) return;
        UIDraw.playClick();
        this.onPress.onPress(this);
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        boolean hovered = isPointerOver(mouseX, mouseY);
        int x1 = getX();
        int y1 = getY();
        int x2 = x1 + getWidth();
        int y2 = y1 + getHeight();

        if (isShowingSuccess()) {
            UIDraw.button(context, x1, y1, x2, y2, flashStyle, true, false);
            String label = getWidth() < 40 ? "" : flashLabel;
            UIDraw.setShadowOverride(UIDraw.darken(flashStyle.fill(true, false), 0.45f));
            UIDraw.buttonContent(context, x1, y1, getWidth(), getHeight(), flashIcon, label, UITheme.Colors.TEXT_HIGHLIGHT, true);
            UIDraw.clearShadowOverride();
            UIDraw.successFlash(context, x1, y1, x2, y2, flashStart);
            return;
        }
        if (selected && style == ButtonStyle.GHOST) {
            context.fill(x1, y1, x2, y2, UITheme.Colors.SURFACE_SELECTED);
        }
        UIDraw.button(context, x1, y1, x2, y2, style, this.active, hovered);
        drawContent(context, hovered);
    }

    @Override
    protected void handleCursor(GuiGraphicsExtractor context) {
        if (ToastManager.isMouseOverAnyToast()) {
            return;
        }
        super.handleCursor(context);
    }

    private boolean isPointerOver(int mouseX, int mouseY) {
        if (ToastManager.isMouseOverAnyToast()) return false;
        return mouseX >= getX() && mouseY >= getY()
                && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
    }

    private int contentColor(boolean hovered) {
        if (!this.active) return UITheme.Colors.TEXT_DISABLED;
        if (hovered) return UITheme.Colors.TEXT_HIGHLIGHT;
        return selected ? UITheme.Colors.ACCENT : UITheme.Colors.TEXT_PRIMARY;
    }

    private void drawContent(GuiGraphicsExtractor context, boolean hovered) {
        int color = contentColor(hovered);
        String label = getMessage().getString();

        UIIcon shownIcon;
        String text;
        if (renderAsXIcon || renderAsDownloadIcon) {
            shownIcon = renderAsXIcon ? UIIcon.CLOSE : UIIcon.DOWNLOAD;
            text = "";
        } else if (icon != null) {
            shownIcon = icon;
            text = label.isBlank() ? "" : label;
        } else {
            shownIcon = iconForLabel(label);
            text = shownIcon != null ? "" : label;
        }

        UIDraw.applyButtonShadow(style, this.active, hovered);
        UIDraw.buttonContent(context, getX(), getY(), getWidth(), getHeight(), shownIcon, text, color, style != ButtonStyle.GHOST, iconAfterText);
        UIDraw.clearShadowOverride();
    }

    private static UIIcon iconForLabel(String label) {
        return switch (label) {
            case "X", "x", "✕", "✖", "×" -> UIIcon.CLOSE;
            case "⚙" -> UIIcon.GEAR;
            case "📁", "📂" -> UIIcon.FOLDER;
            case "🔄", "↻", "⟳", "↺" -> UIIcon.REFRESH;
            case "💾", "⬇", "⬇️" -> UIIcon.DOWNLOAD;
            case "✏", "✏️" -> UIIcon.PENCIL;
            case "📤" -> UIIcon.SHARE;
            case "📷" -> UIIcon.CAMERA;
            case "☰" -> UIIcon.LIST;
            case "🔗" -> UIIcon.LINK;
            case "📋" -> UIIcon.CLIPBOARD;
            case "+" -> UIIcon.PLUS;
            case "-", "−" -> UIIcon.MINUS;
            case "🔍" -> UIIcon.SEARCH;
            case "🗑" -> UIIcon.TRASH;
            case "←", "<", "◀" -> UIIcon.ARROW_LEFT;
            case "→", ">", "▶" -> UIIcon.ARROW_RIGHT;
            case "✓", "✔" -> UIIcon.CHECK;
            default -> null;
        };
    }
}
