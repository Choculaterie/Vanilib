package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.gui.theme.ButtonStyle;
import com.choculaterie.vanilib.gui.theme.UIDraw;
import com.choculaterie.vanilib.gui.theme.UIIcon;
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
    private static final long SLIDE_DURATION    = 300;
    private static final long ERROR_DISPLAY_DURATION = 8000;
    private static final int ACCENT_BORDER_WIDTH = 4;
    private static final int BUTTON_SPACING     = 4;
    private static final int CLOSE_BUTTON_SIZE  = 17;
    private static final int TEXT_LEFT          = 28;
    private static final int TEXT_RIGHT_GAP     = 6;
    private static final int COPY_BUTTON_WIDTH  = 56;
    private static final long COPIED_MS         = 1500;
    private static final int COPY_BUTTON_HEIGHT = 19;
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
    private long copiedAt;

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
        long elapsed  = getEffectiveElapsedTime(now);
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

    public void markCopied() {
        this.copiedAt = System.currentTimeMillis();
    }

    public void dismiss() {
        this.dismissed = true;
    }

    public void setHovered(boolean hovered) {
        if (hovered && !this.hovered) {
            this.hoverStartTime = System.currentTimeMillis();
        } else if (!hovered && this.hovered) {
            this.pausedTime += pauseBetween(this.hoverStartTime, System.currentTimeMillis());
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
            totalPausedTime += pauseBetween(hoverStartTime, now);
        }
        return now - createdTime - totalPausedTime;
    }

    private long pauseBetween(long from, long to) {
        return Math.max(0L, to - Math.max(from, createdTime + SLIDE_DURATION));
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
        int lines = wrappedLines != null ? Math.max(1, wrappedLines.size()) : 1;
        int textBlock = UITheme.Typography.TEXT_HEIGHT + lines * UITheme.Typography.LINE_HEIGHT;
        int h = textBlock + UITheme.Dimensions.PADDING_SMALL;
        if (hasCopyButton)
            h += COPY_BUTTON_HEIGHT + UITheme.Dimensions.PADDING_SMALL;
        int minimum = CLOSE_BUTTON_SIZE + BUTTON_SPACING * 2
                + (hasCopyButton ? COPY_BUTTON_HEIGHT + UITheme.Dimensions.PADDING_SMALL : 0);
        return Math.max(minimum, h);
    }

    private List<String> getWrappedLines(net.minecraft.client.gui.Font font) {
        if (wrappedLines == null) {
            wrappedLines = wrapText(font, message,
                    TOAST_WIDTH - TEXT_LEFT - CLOSE_BUTTON_SIZE - BUTTON_SPACING - TEXT_RIGHT_GAP);
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
        int x2 = currentX + TOAST_WIDTH;
        int y2 = yPosition + toastHeight;
        boolean hovered = isWithinBounds(mouseX, mouseY, currentX, yPosition, TOAST_WIDTH, toastHeight);

        context.fill(currentX + 2, yPosition + 2, x2 + 2, y2 + 2, UIDraw.withAlpha(UITheme.Colors.OUTLINE, alpha / 2));
        context.fill(currentX, yPosition, x2, y2, UIDraw.withAlpha(getBackgroundColor(), alpha));
        UIDraw.outline(context, currentX, yPosition, x2, y2, UIDraw.withAlpha(UITheme.Colors.OUTLINE, alpha));
        int border = hovered ? UITheme.Colors.TOAST_BORDER_HOVER : UITheme.Colors.TOAST_BORDER;
        UIDraw.outline(context, currentX + 1, yPosition + 1, x2 - 1, y2 - 1, UIDraw.withAlpha(border, alpha));
        context.fill(currentX + 1, yPosition + 1, currentX + 1 + ACCENT_BORDER_WIDTH, y2 - 1, UIDraw.withAlpha(getAccentColor(), alpha));
    }

    private void renderIcon(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer, int currentX, int alpha) {
        getTypeIcon().drawOnText(context, currentX + UITheme.Typography.LINE_HEIGHT, yPosition + UITheme.Typography.TEXT_HEIGHT,
                UIDraw.withAlpha(getAccentColor(), alpha));
    }

    private void renderMessage(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer, int currentX, int alpha) {
        int textX = currentX + TEXT_LEFT;
        int textY = yPosition + UITheme.Typography.TEXT_HEIGHT;
        int color = UIDraw.withAlpha(UITheme.Colors.TEXT_PRIMARY, alpha);

        for (String line : getWrappedLines(textRenderer)) {
            UIDraw.text(context, line, textX, textY, color);
            textY += UITheme.Typography.LINE_HEIGHT;
        }
    }

    private void renderCloseButton(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer, int currentX, int alpha) {
        int closeX = currentX + TOAST_WIDTH - CLOSE_BUTTON_SIZE - BUTTON_SPACING;
        int closeY = yPosition + BUTTON_SPACING;
        boolean isHovered = isWithinBounds(mouseX, mouseY, closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE);
        int color = isHovered ? UITheme.Colors.TEXT_HIGHLIGHT : UITheme.Colors.TEXT_MUTED;
        UIIcon.CLOSE.drawCentered(context, closeX + CLOSE_BUTTON_SIZE / 2, closeY + CLOSE_BUTTON_SIZE / 2, UIDraw.withAlpha(color, alpha));
    }

    private void renderCopyButton(GuiGraphicsExtractor context, net.minecraft.client.gui.Font textRenderer, int currentX, int toastHeight, int alpha) {
        int copyX = currentX + TOAST_WIDTH - COPY_BUTTON_WIDTH - UITheme.Dimensions.PADDING_SMALL - 3;
        int copyY = yPosition + toastHeight - COPY_BUTTON_HEIGHT - 6;
        if (copiedAt > 0 && System.currentTimeMillis() - copiedAt < COPIED_MS) {
            int x2 = copyX + COPY_BUTTON_WIDTH, y2 = copyY + COPY_BUTTON_HEIGHT;
            UIDraw.button(context, copyX, copyY, x2, y2, ButtonStyle.SUCCESS, true, false);
            UIDraw.setShadowOverride(UIDraw.darken(ButtonStyle.SUCCESS.fill(true, false), 0.45f));
            UIDraw.buttonContent(context, copyX, copyY, COPY_BUTTON_WIDTH, COPY_BUTTON_HEIGHT, UIIcon.CHECK, "Copied", UITheme.Colors.TEXT_HIGHLIGHT, true);
            UIDraw.clearShadowOverride();
            UIDraw.successFlash(context, copyX, copyY, x2, y2, copiedAt);
            return;
        }
        boolean isHovered = isWithinBounds(mouseX, mouseY, copyX, copyY, COPY_BUTTON_WIDTH, COPY_BUTTON_HEIGHT);
        int fill = UIDraw.withAlpha(isHovered ? UITheme.Colors.BUTTON_BG_HOVER : UITheme.Colors.BUTTON_BG, alpha);
        int border = UIDraw.withAlpha(isHovered ? UITheme.Colors.OUTLINE_HOVER : UITheme.Colors.OUTLINE, alpha);
        UIDraw.bevel(context, copyX, copyY, copyX + COPY_BUTTON_WIDTH, copyY + COPY_BUTTON_HEIGHT, fill, border);
        int color = UIDraw.withAlpha(isHovered ? UITheme.Colors.TEXT_HIGHLIGHT : UITheme.Colors.TEXT_PRIMARY, alpha);
        UIDraw.centeredText(context, "Copy", copyX + COPY_BUTTON_WIDTH / 2,
                copyY + (COPY_BUTTON_HEIGHT - UITheme.Typography.CAP_HEIGHT) / 2, color);
    }

    private int getBackgroundColor() {
        return switch (type) {
            case SUCCESS -> UITheme.Colors.TOAST_BG_SUCCESS;
            case ERROR   -> UITheme.Colors.TOAST_BG_ERROR;
            case INFO    -> UITheme.Colors.TOAST_BG_INFO;
            case WARNING -> UITheme.Colors.TOAST_BG_WARNING;
        };
    }

    private int getAccentColor() {
        return switch (type) {
            case SUCCESS -> UITheme.Colors.TOAST_ACCENT_SUCCESS;
            case ERROR   -> UITheme.Colors.TOAST_ACCENT_ERROR;
            case INFO    -> UITheme.Colors.TOAST_ACCENT_INFO;
            case WARNING -> UITheme.Colors.TOAST_ACCENT_WARNING;
        };
    }

    private UIIcon getTypeIcon() {
        return switch (type) {
            case SUCCESS -> UIIcon.CHECK;
            case ERROR   -> UIIcon.CLOSE;
            case WARNING -> UIIcon.ALERT;
            case INFO    -> UIIcon.INFO;
        };
    }

    private boolean isWithinBounds(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
