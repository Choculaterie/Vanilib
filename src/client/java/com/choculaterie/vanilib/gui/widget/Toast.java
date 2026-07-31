package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.gui.theme.UITheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public class Toast {
    public enum Type {
        SUCCESS,
        ERROR,
        INFO,
        WARNING
    }

    private static final int TOAST_WIDTH        = UITheme.Dimensions.TOAST_MAX_WIDTH;
    private static final int TOAST_HEIGHT       = UITheme.Dimensions.TOAST_HEIGHT;
    private static final long SLIDE_DURATION    = 300;
    private static final long ERROR_DISPLAY_DURATION = 8000;
    private static final int ACCENT_BORDER_WIDTH = 4;
    private static final int BUTTON_SPACING     = 4;
    private static final int CLOSE_BUTTON_SIZE  = 16;
    private static final int COPY_BUTTON_WIDTH  = 50;
    private static final int COPY_BUTTON_HEIGHT = 18;
    private static final int TOAST_EXTRA_HEIGHT = 20;
    private static final int Y_TRANSITION_DURATION = 400;
    private static final int TOAST_SPACING      = 5;

    private static double mouseX;
    private static double mouseY;

    private final String message;
    private final Type type;
    private List<String> wrappedLines = null;
    private final long createdTime;
    private final int screenWidth;
    private final boolean hasCopyButton;
    private final String copyText;

    private int yPosition;
    private int targetYPosition;
    private long lastYPositionChange;
    private boolean dismissed;
    private boolean hovered;
    private long pausedTime;
    private long hoverStartTime;

    public Toast(String message, Type type, int screenWidth, int yPosition) {
        this(message, type, screenWidth, yPosition, false, null);
    }

    public Toast(String message, Type type, int screenWidth, int yPosition, boolean hasCopyButton, String copyText) {
        this.message      = message;
        this.type         = type;
        this.screenWidth  = screenWidth;
        this.yPosition    = yPosition;
        this.targetYPosition  = yPosition;
        this.createdTime  = System.currentTimeMillis();
        this.lastYPositionChange = createdTime;
        this.hasCopyButton = hasCopyButton;
        this.copyText     = copyText != null ? copyText : message;
    }

    public boolean render(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer) {
        long now             = System.currentTimeMillis();
        long elapsed         = getEffectiveElapsedTime(now);
        long displayDuration = getDisplayDuration();

        if (shouldRemove(elapsed, displayDuration)) {
            return true;
        }

        updateYPosition(now);

        float slideProgress = calculateSlideProgress(elapsed);
        float fadeProgress  = calculateFadeProgress(elapsed, displayDuration);
        int alpha = (int) (255 * fadeProgress);

        if (alpha <= 0) {
            return true;
        }

        int currentX  = calculateXPosition(slideProgress);
        getWrappedLines(textRenderer);
        int toastHeight = getToastHeight();

        renderToastBackground(context, currentX, toastHeight, alpha);
        renderIcon(context, textRenderer, currentX, alpha);
        renderMessage(context, textRenderer, currentX, alpha);
        renderCloseButton(context, textRenderer, currentX, alpha);

        if (hasCopyButton) {
            renderCopyButton(context, textRenderer, currentX, toastHeight, alpha);
        }

        return false;
    }

    public void setTargetYPosition(int targetY) {
        if (this.targetYPosition != targetY) {
            this.targetYPosition = targetY;
            this.lastYPositionChange = System.currentTimeMillis();
        }
    }

    public boolean isHovering(double mouseX, double mouseY) {
        long now      = System.currentTimeMillis();
        long elapsed  = now - createdTime;
        long displayDuration = getDisplayDuration();

        if (dismissed || elapsed > SLIDE_DURATION + displayDuration) {
            return false;
        }

        float slideProgress = calculateSlideProgress(elapsed);
        int currentX  = calculateXPosition(slideProgress);
        int toastHeight = getToastHeight();

        return mouseX >= currentX && mouseX < currentX + TOAST_WIDTH &&
               mouseY >= yPosition && mouseY < yPosition + toastHeight;
    }

    public boolean isCloseButtonClicked(double mouseX, double mouseY) {
        if (dismissed) return false;
        long elapsed = getEffectiveElapsedTime(System.currentTimeMillis());
        if (elapsed > SLIDE_DURATION + getDisplayDuration()) return false;
        int currentX = calculateXPosition(calculateSlideProgress(elapsed));
        int closeX = currentX + TOAST_WIDTH - CLOSE_BUTTON_SIZE - BUTTON_SPACING;
        int closeY = yPosition + BUTTON_SPACING;
        return isWithinBounds(mouseX, mouseY, closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
    }

    public boolean isCopyButtonClicked(double mouseX, double mouseY) {
        if (!hasCopyButton || dismissed) return false;
        long elapsed = getEffectiveElapsedTime(System.currentTimeMillis());
        if (elapsed > SLIDE_DURATION + getDisplayDuration()) return false;
        int currentX = calculateXPosition(calculateSlideProgress(elapsed));
        int toastHeight = getToastHeight();
        int copyX = currentX + TOAST_WIDTH - COPY_BUTTON_WIDTH - UITheme.Dimensions.PADDING_SMALL - 3;
        int copyY = yPosition + toastHeight - COPY_BUTTON_HEIGHT - 6;
        return isWithinBounds(mouseX, mouseY, copyX, copyY, COPY_BUTTON_WIDTH, COPY_BUTTON_HEIGHT);
    }

    public static void updateMousePosition(double x, double y) {
        mouseX = x;
        mouseY = y;
    }

    public void dismiss() {
        this.dismissed = true;
    }

    public void setHovered(boolean hovered) {
        if (hovered && !this.hovered) {
            this.hoverStartTime = System.currentTimeMillis();
        } else if (!hovered && this.hovered) {
            this.pausedTime += System.currentTimeMillis() - this.hoverStartTime;
        }
        this.hovered = hovered;
    }

    public boolean isHovered() {
        return hovered;
    }

    public int getHeight() {
        return getToastHeight() + TOAST_SPACING;
    }

    public String getCopyText() {
        return copyText;
    }

    private long getEffectiveElapsedTime(long now) {
        long totalPausedTime = pausedTime;
        if (hovered && hoverStartTime > 0) {
            totalPausedTime += now - hoverStartTime;
        }
        return now - createdTime - totalPausedTime;
    }

    private long getDisplayDuration() {
        return hasCopyButton ? ERROR_DISPLAY_DURATION : UITheme.Animation.TOAST_DISPLAY_DURATION;
    }

    private boolean shouldRemove(long elapsed, long displayDuration) {
        return dismissed || elapsed > SLIDE_DURATION + displayDuration + UITheme.Animation.TOAST_FADE_DURATION;
    }

    private void updateYPosition(long now) {
        if (yPosition != targetYPosition) {
            long yTransitionElapsed   = now - lastYPositionChange;
            float yTransitionProgress = Math.min(1.0f, yTransitionElapsed / (float) Y_TRANSITION_DURATION);

            yTransitionProgress = (float)(1 - Math.pow(1 - yTransitionProgress, 3));
            yPosition = (int)(yPosition + (targetYPosition - yPosition) * yTransitionProgress);

            if (Math.abs(yPosition - targetYPosition) < 1) {
                yPosition = targetYPosition;
            }
        }
    }

    private float calculateSlideProgress(long elapsed) {
        float progress = Math.min(1.0f, elapsed / (float) SLIDE_DURATION);
        return 1 - (float) Math.pow(1 - progress, 3);
    }

    private float calculateFadeProgress(long elapsed, long displayDuration) {
        if (elapsed > SLIDE_DURATION + displayDuration) {
            long fadeElapsed = elapsed - SLIDE_DURATION - displayDuration;
            return 1.0f - (fadeElapsed / (float) UITheme.Animation.TOAST_FADE_DURATION);
        }
        return 1.0f;
    }

    private int calculateXPosition(float slideProgress) {
        int targetX = screenWidth - TOAST_WIDTH - UITheme.Dimensions.PADDING;
        int startX  = screenWidth + TOAST_WIDTH;
        return (int) (startX + (targetX - startX) * slideProgress);
    }

    private int getToastHeight() {
        int extraLines = wrappedLines != null ? Math.max(0, wrappedLines.size() - 1) : 0;
        int h = TOAST_HEIGHT + extraLines * UITheme.Typography.LINE_HEIGHT;
        return hasCopyButton ? h + TOAST_EXTRA_HEIGHT : h;
    }

    private List<String> getWrappedLines(net.minecraft.client.gui.Font font) {
        if (wrappedLines == null) {
            wrappedLines = wrapText(font, message, TOAST_WIDTH - 44);
        }
        return wrappedLines;
    }

    private static List<String> wrapText(net.minecraft.client.gui.Font font, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) { lines.add(""); return lines; }
        String[] words = text.split(" ", -1);
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (font.width(candidate) > maxWidth && current.length() > 0) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    private void renderToastBackground(GuiGraphicsExtractor context, int currentX, int toastHeight, int alpha) {
        int bgColor = UITheme.Colors.BUTTON_BG_DISABLED;
        int bgColorWithAlpha = (alpha << 24) | (bgColor & 0x00FFFFFF);
        context.fill(currentX, yPosition, currentX + TOAST_WIDTH, yPosition + toastHeight, bgColorWithAlpha);

        int borderColor = getAccentColor();
        int borderColorWithAlpha = (alpha << 24) | (borderColor & 0x00FFFFFF);
        context.fill(currentX, yPosition, currentX + ACCENT_BORDER_WIDTH, yPosition + toastHeight, borderColorWithAlpha);

        int topBorderColor = UITheme.Colors.PANEL_BORDER;
        int topBorderColorWithAlpha = (alpha << 24) | (topBorderColor & 0x00FFFFFF);
        context.fill(currentX, yPosition, currentX + TOAST_WIDTH, yPosition + UITheme.Dimensions.BORDER_WIDTH, topBorderColorWithAlpha);
        context.fill(currentX, yPosition + toastHeight - UITheme.Dimensions.BORDER_WIDTH, currentX + TOAST_WIDTH, yPosition + toastHeight, topBorderColorWithAlpha);
        context.fill(currentX + TOAST_WIDTH - UITheme.Dimensions.BORDER_WIDTH, yPosition, currentX + TOAST_WIDTH, yPosition + toastHeight, topBorderColorWithAlpha);
    }

    private void renderIcon(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer, int currentX, int alpha) {
        String icon = getTypeIcon();
        int iconColor = getAccentColor();
        int iconColorWithAlpha = (alpha << 24) | (iconColor & 0x00FFFFFF);
        context.text(
                textRenderer,
                icon,
                currentX + UITheme.Typography.LINE_HEIGHT,
                yPosition + UITheme.Typography.TEXT_HEIGHT,
                iconColorWithAlpha,
                false
        );
    }

    private void renderMessage(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer, int currentX, int alpha) {
        int textX = currentX + 28;
        int textY = yPosition + UITheme.Typography.TEXT_HEIGHT;
        int textColor = UITheme.Colors.TEXT_PRIMARY;
        int textColorWithAlpha = (alpha << 24) | (textColor & 0x00FFFFFF);

        for (String line : getWrappedLines(textRenderer)) {
            context.text(textRenderer, line, textX, textY, textColorWithAlpha, false);
            textY += UITheme.Typography.LINE_HEIGHT;
        }
    }

    private void renderCloseButton(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer, int currentX, int alpha) {
        int closeX = currentX + TOAST_WIDTH - CLOSE_BUTTON_SIZE - BUTTON_SPACING;
        int closeY = yPosition + BUTTON_SPACING;
        boolean isHovered = isWithinBounds(mouseX, mouseY, closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
        drawFadedButton(context, textRenderer, closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE, "✕", isHovered, alpha);
    }

    private void renderCopyButton(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer, int currentX, int toastHeight, int alpha) {
        int copyX = currentX + TOAST_WIDTH - COPY_BUTTON_WIDTH - UITheme.Dimensions.PADDING_SMALL - 3;
        int copyY = yPosition + toastHeight - COPY_BUTTON_HEIGHT - 6;
        boolean isHovered = isWithinBounds(mouseX, mouseY, copyX, copyY, COPY_BUTTON_WIDTH, COPY_BUTTON_HEIGHT);
        drawFadedButton(context, textRenderer, copyX, copyY, COPY_BUTTON_WIDTH, COPY_BUTTON_HEIGHT, "Copy", isHovered, alpha);
    }

    private void drawFadedButton(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer,
                                  int x, int y, int width, int height, String label, boolean isHovered, int alpha) {
        int bgColor = isHovered ? UITheme.Colors.BUTTON_BG_HOVER : UITheme.Colors.BUTTON_BG;
        context.fill(x, y, x + width, y + height, (alpha << 24) | (bgColor & 0x00FFFFFF));

        int borderColor = (alpha << 24) | (UITheme.Colors.BUTTON_BORDER & 0x00FFFFFF);
        int bw = UITheme.Dimensions.BORDER_WIDTH;
        context.fill(x, y, x + width, y + bw, borderColor);
        context.fill(x, y + height - bw, x + width, y + height, borderColor);
        context.fill(x, y, x + bw, y + height, borderColor);
        context.fill(x + width - bw, y, x + width, y + height, borderColor);

        int textColor = (alpha << 24) | (UITheme.Colors.TEXT_PRIMARY & 0x00FFFFFF);
        context.centeredText(textRenderer, label, x + width / 2, y + (height - UITheme.Typography.TEXT_HEIGHT) / 2 + 1, textColor);
    }

    private int getAccentColor() {
        return switch (type) {
            case SUCCESS -> UITheme.Colors.TOAST_ACCENT_SUCCESS;
            case ERROR   -> UITheme.Colors.TOAST_ACCENT_ERROR;
            case INFO    -> UITheme.Colors.TOAST_ACCENT_INFO;
            case WARNING -> UITheme.Colors.TOAST_ACCENT_WARNING;
        };
    }

    private String getTypeIcon() {
        return switch (type) {
            case SUCCESS -> "✓";
            case ERROR   -> "✗";
            case WARNING -> "⚠";
            case INFO    -> "ℹ";
        };
    }

    private boolean isWithinBounds(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
