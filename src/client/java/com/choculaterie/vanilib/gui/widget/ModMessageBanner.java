package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.gui.theme.UIDraw;
import com.choculaterie.vanilib.gui.theme.UIIcon;
import com.choculaterie.vanilib.gui.theme.UITheme;
import com.choculaterie.vanilib.models.ModMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.function.Consumer;
import com.mojang.blaze3d.platform.InputConstants;

public class ModMessageBanner implements Renderable, GuiEventListener {
    private static final int BANNER_HEIGHT = 31;
    private static final int CLOSE_BUTTON_SIZE = UITheme.Dimensions.ICON_SMALL;

    private static final int INFO_BG_COLOR = UITheme.Colors.TOAST_BG_INFO;
    private static final int WARNING_BG_COLOR = UITheme.Colors.TOAST_BG_WARNING;
    private static final int ERROR_BG_COLOR = UITheme.Colors.TOAST_BG_ERROR;
    private static final int DEFAULT_BG_COLOR = UITheme.Colors.SURFACE;
    private static final int CLOSE_HOVER_COLOR = UITheme.Colors.SURFACE_HOVER;

    private final Minecraft client;
    private int x;
    private int y;
    private int width;
    private ModMessage message;
    private Consumer<ModMessage> onDismiss;
    private boolean visible = false;

    public ModMessageBanner(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.client = Minecraft.getInstance();
    }

    public void setMessage(ModMessage message) {
        this.message = message;
        this.visible = message != null && message.hasMessage();
    }

    public void setOnDismiss(Consumer<ModMessage> onDismiss) {
        this.onDismiss = onDismiss;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getHeight() {
        return visible ? BANNER_HEIGHT : 0;
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!visible) {
            return false;
        }
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + BANNER_HEIGHT;
    }

    private int getCloseButtonX() {
        return x + width - CLOSE_BUTTON_SIZE - UITheme.Dimensions.PADDING;
    }

    private int getCloseButtonY() {
        return y + (BANNER_HEIGHT - CLOSE_BUTTON_SIZE) / 2;
    }

    private boolean isOverCloseButton(double mouseX, double mouseY) {
        int closeX = getCloseButtonX();
        int closeY = getCloseButtonY();
        return mouseX >= closeX && mouseX < closeX + CLOSE_BUTTON_SIZE &&
               mouseY >= closeY && mouseY < closeY + CLOSE_BUTTON_SIZE;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        if (!visible || message == null || !message.hasMessage()) {
            return;
        }

        int bgColor = getBackgroundColor(message.type());

        drawBackground(context, bgColor);
        drawBorder(context);
        drawMessageText(context);
        drawCloseButton(context, mouseX, mouseY);
    }

    private static int accentFor(String type) {
        if (type == null) return UITheme.Colors.ACCENT;
        return switch (type.toLowerCase()) {
            case "warning" -> UITheme.Colors.TOAST_ACCENT_WARNING;
            case "error" -> UITheme.Colors.TOAST_ACCENT_ERROR;
            default -> UITheme.Colors.TOAST_ACCENT_INFO;
        };
    }

    private void drawBackground(GuiGraphicsExtractor context, int bgColor) {
        context.fill(x, y, x + width, y + BANNER_HEIGHT, bgColor);
    }

    private void drawBorder(GuiGraphicsExtractor context) {
        context.fill(x, y + BANNER_HEIGHT - UITheme.Dimensions.BORDER_WIDTH,
                    x + width, y + BANNER_HEIGHT, UITheme.Colors.DIVIDER);
        context.fill(x, y, x + 3, y + BANNER_HEIGHT - UITheme.Dimensions.BORDER_WIDTH, accentFor(message.type()));
    }

    private void drawMessageText(GuiGraphicsExtractor context) {
        String text = message.message();
        if (text == null) return;

        int maxTextWidth = width - UITheme.Dimensions.PADDING * 2 - CLOSE_BUTTON_SIZE - UITheme.Dimensions.PADDING;
        text = truncateText(text, maxTextWidth);

        int textY = y + (BANNER_HEIGHT - UITheme.Typography.CAP_HEIGHT) / 2;
        UIDraw.text(context, text, x + UITheme.Dimensions.PADDING, textY, UITheme.Colors.TEXT_PRIMARY);
    }

    private String truncateText(String text, int maxWidth) {
        if (client.font.width(text) <= maxWidth) {
            return text;
        }

        while (client.font.width(text + "...") > maxWidth && text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    private void drawCloseButton(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        int closeX = getCloseButtonX();
        int closeY = getCloseButtonY();

        if (isOverCloseButton(mouseX, mouseY)) {
            context.fill(closeX, closeY, closeX + CLOSE_BUTTON_SIZE, closeY + CLOSE_BUTTON_SIZE, CLOSE_HOVER_COLOR);
        }

        int closeColor = isOverCloseButton(mouseX, mouseY) ? UITheme.Colors.TEXT_HIGHLIGHT : UITheme.Colors.TEXT_MUTED;
        UIIcon.CLOSE.drawCentered(context, closeX + CLOSE_BUTTON_SIZE / 2, closeY + CLOSE_BUTTON_SIZE / 2, closeColor);
    }

    private int getBackgroundColor(String type) {
        if (type == null) {
            return DEFAULT_BG_COLOR;
        }
        return switch (type.toLowerCase()) {
            case "info" -> INFO_BG_COLOR;
            case "warning" -> WARNING_BG_COLOR;
            case "error" -> ERROR_BG_COLOR;
            default -> DEFAULT_BG_COLOR;
        };
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || button != InputConstants.MOUSE_BUTTON_LEFT) {
            return false;
        }

        if (isOverCloseButton(mouseX, mouseY)) {
            UIDraw.playClick();
            visible = false;
            if (onDismiss != null && message != null) {
                onDismiss.accept(message);
            }
            return true;
        }

        return false;
    }

    @Override
    public void setFocused(boolean focused) {}

    @Override
    public boolean isFocused() {
        return false;
    }
}
