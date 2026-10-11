package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.gui.theme.UIDraw;
import com.choculaterie.vanilib.gui.theme.UIIcon;
import com.choculaterie.vanilib.gui.theme.UITheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import com.choculaterie.vanilib.util.MouseState;

public class CustomTextField extends EditBox {
	private static final long KEY_INITIAL_DELAY = 400;

	private static final long KEY_REPEAT_DELAY = 50;

	private static final int TEXT_PADDING = 5;
	private static final int ICON_GAP = 4;

	private UIIcon leadingIcon;

	private static final long CURSOR_BLINK_MS = 500;

	private static final int CLEAR_BUTTON_SIZE = UITheme.Dimensions.ICON_SMALL;

	private final Minecraft client;
	private Runnable onEnterPressed;
	private Runnable onChanged;
	private Runnable onClearPressed;
	private Component placeholderText;

	private boolean wasEnterDown = false;
	private boolean wasClearButtonMouseDown = false;

	private static CustomTextField activeField = null;

	private final KeyRepeatState backspaceState = new KeyRepeatState();
	private final KeyRepeatState deleteState    = new KeyRepeatState();
	private final KeyRepeatState leftState      = new KeyRepeatState();
	private final KeyRepeatState rightState     = new KeyRepeatState();
	private boolean wasHomePressed = false;
	private boolean wasEndPressed  = false;

	private boolean wasCtrlVDown = false;
	private boolean wasCtrlADown = false;

	private static class KeyRepeatState {
		boolean wasPressed = false;
		long holdStart  = 0;
		long lastRepeat = 0;

		boolean shouldTrigger(long currentTime, boolean isKeyDown) {
			if (!isKeyDown) {
				wasPressed = false;
				return false;
			}

			if (!wasPressed) {
				wasPressed  = true;
				holdStart   = currentTime;
				lastRepeat  = currentTime;
				return true;
			}

			if (currentTime - holdStart > KEY_INITIAL_DELAY && currentTime - lastRepeat > KEY_REPEAT_DELAY) {
				lastRepeat = currentTime;
				return true;
			}

			return false;
		}
	}

	public CustomTextField(Minecraft client, int x, int y, int width, int height, Component text) {
		super(client.font, x, y, width, height, text);
		this.client = client;
		this.setMaxLength(256);
		this.setBordered(false);
	}

	public void setOnEnterPressed(Runnable callback) {
		this.onEnterPressed = callback;
	}

	public void setOnChanged(Runnable callback) {
		this.onChanged = callback;
	}

	public void setOnClearPressed(Runnable callback) {
		this.onClearPressed = callback;
	}

	@Override
	public void setFocused(boolean focused) {
		super.setFocused(focused);
		if (focused) {
			activeField = this;
		} else if (activeField == this) {
			activeField = null;
		}
	}

	public CustomTextField setIcon(UIIcon icon) {
		this.leadingIcon = icon;
		return this;
	}

	private int leftPadding() {
		return TEXT_PADDING + (leadingIcon != null ? UIIcon.SIZE + ICON_GAP : 0);
	}

	public void setPlaceholder(Component placeholder) {
		this.placeholderText = placeholder;
	}

	public static void restoreMinecraftCharCallback() {
		activeField = null;
	}

	public static boolean routeCharTyped(int codepoint) {
		if (activeField == null || !activeField.isFocused()) {
			return false;
		}
		activeField.onCharTyped(codepoint);
		return true;
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		return this.isFocused();
	}

	@Override
	public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
		return this.isFocused();
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		handleMouseInput(mouseX, mouseY);
		handleKeyboardInput();

		drawBackground(context);
		drawBorder(context, mouseX, mouseY);
		drawTextContent(context, mouseX, mouseY);
		drawClearButton(context, mouseX, mouseY);
	}

	private void onCharTyped(int codepoint) {
		if (codepoint < 32) {
			return;
		}

		this.insertText(Character.toString(codepoint));
		if (onChanged != null) {
			onChanged.run();
		}
	}

	private void handleMouseInput(int mouseX, int mouseY) {
		long windowHandle = Minecraft.getInstance().getWindow().handle();
		if (windowHandle == 0) {
			wasClearButtonMouseDown = false;
			return;
		}

		boolean isMouseDown = MouseState.isLeftDown();

		if (!this.getValue().isEmpty() && isMouseDown && !wasClearButtonMouseDown
				&& isOverClearButton(mouseX, mouseY)) {
			this.setValue("");
			if (onChanged != null) {
				onChanged.run();
			}
			if (onClearPressed != null) {
				onClearPressed.run();
			}
		} else if (isMouseDown && !wasClearButtonMouseDown && this.isFocused() && isOverTextArea(mouseX, mouseY)) {
			int relativeX = mouseX - (this.getX() + leftPadding());
			int index = client.font.plainSubstrByWidth(this.getValue(), Math.max(0, relativeX)).length();
			this.moveCursorTo(index, false);
		}

		wasClearButtonMouseDown = isMouseDown;
	}

	private boolean isOverTextArea(int mouseX, int mouseY) {
		return mouseX >= this.getX() && mouseX < this.getX() + this.getWidth()
				&& mouseY >= this.getY() && mouseY < this.getY() + this.getHeight()
				&& !isOverClearButton(mouseX, mouseY);
	}

	private void handleKeyboardInput() {
		long windowHandle = Minecraft.getInstance().getWindow().handle();
		if (windowHandle == 0)
			return;

		handleEnterKey(windowHandle);

		if (this.isFocused()) {
			handleSpecialKeys(windowHandle);
		}
	}

	private void handleEnterKey(long windowHandle) {
		boolean isEnterDown = InputConstants.isKeyDown(InputConstants.KEY_RETURN) ||
				InputConstants.isKeyDown(InputConstants.KEY_NUMPADENTER);

		if (this.isFocused() && onEnterPressed != null && isEnterDown && !wasEnterDown) {
			onEnterPressed.run();
		}

		wasEnterDown = isEnterDown;
	}

	private void handleSpecialKeys(long windowHandle) {
		long currentTime = System.currentTimeMillis();
		String currentText = this.getValue();
		int cursorPos = this.getCursorPosition();

		boolean isCtrlDown = InputConstants.isKeyDown(InputConstants.KEY_LCONTROL) ||
				InputConstants.isKeyDown(InputConstants.KEY_RCONTROL);

		boolean isVDown = InputConstants.isKeyDown(InputConstants.KEY_V);
		if (isCtrlDown && isVDown && !wasCtrlVDown) {
			String clipboard = client.keyboardHandler.getClipboard();
			if (clipboard != null && !clipboard.isEmpty()) {
				this.insertText(clipboard);
				if (onChanged != null) {
					onChanged.run();
				}
			}
		}
		wasCtrlVDown = isCtrlDown && isVDown;

		boolean isADown = InputConstants.isKeyDown(InputConstants.KEY_A);
		if (isCtrlDown && isADown && !wasCtrlADown) {
			this.moveCursorToEnd(false);
			this.setHighlightPos(0);
		}
		wasCtrlADown = isCtrlDown && isADown;

		boolean isBackspaceDown = InputConstants.isKeyDown(InputConstants.KEY_BACKSPACE);
		boolean hasSelection = !this.getHighlighted().isEmpty();
		if (backspaceState.shouldTrigger(currentTime, isBackspaceDown) && (cursorPos > 0 || hasSelection)) {
			this.deleteChars(-1);
			if (onChanged != null) {
				onChanged.run();
			}
		}

		boolean isDeleteDown = InputConstants.isKeyDown(InputConstants.KEY_DELETE);
		if (deleteState.shouldTrigger(currentTime, isDeleteDown) && (cursorPos < currentText.length() || hasSelection)) {
			this.deleteChars(1);
			if (onChanged != null) {
				onChanged.run();
			}
		}

		boolean isLeftDown = InputConstants.isKeyDown(InputConstants.KEY_LEFT);
		if (leftState.shouldTrigger(currentTime, isLeftDown) && cursorPos > 0) {
			this.moveCursorTo(cursorPos - 1, false);
		}

		boolean isRightDown = InputConstants.isKeyDown(InputConstants.KEY_RIGHT);
		if (rightState.shouldTrigger(currentTime, isRightDown) && cursorPos < currentText.length()) {
			this.moveCursorTo(cursorPos + 1, false);
		}

		boolean isHomeDown = InputConstants.isKeyDown(InputConstants.KEY_HOME);
		if (isHomeDown && !wasHomePressed) {
			this.moveCursorToStart(false);
		}
		wasHomePressed = isHomeDown;

		boolean isEndDown = InputConstants.isKeyDown(InputConstants.KEY_END);
		if (isEndDown && !wasEndPressed) {
			this.moveCursorToEnd(false);
		}
		wasEndPressed = isEndDown;

		boolean isEscapeDown = InputConstants.isKeyDown(InputConstants.KEY_ESCAPE);
		if (isEscapeDown) {
			this.setFocused(false);
		}
	}

	private void drawBackground(GuiGraphicsExtractor context) {
		int x1 = this.getX();
		int y1 = this.getY();
		int x2 = x1 + this.getWidth();
		int y2 = y1 + this.getHeight();
		context.fill(x1, y1, x2, y2, UITheme.Colors.FIELD_BG);
		context.fill(x1 + 1, y1 + 1, x2 - 1, y1 + 2, UIDraw.withAlpha(UITheme.Colors.OUTLINE, 0x90));
	}

	private void drawBorder(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		boolean hovered = mouseX >= this.getX() && mouseY >= this.getY()
				&& mouseX < this.getX() + this.getWidth() && mouseY < this.getY() + this.getHeight();
		int borderColor;
		if (this.isFocused()) {
			borderColor = UITheme.Colors.FIELD_BORDER_FOCUSED;
		} else if (hovered) {
			borderColor = UITheme.Colors.SCROLLBAR_THUMB_HOVER;
		} else {
			borderColor = UITheme.Colors.FIELD_BORDER;
		}
		UIDraw.outline(context, this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight(), borderColor);

		if (leadingIcon != null) {
			int iconColor = this.isFocused() ? UITheme.Colors.ACCENT : UITheme.Colors.TEXT_MUTED;
			leadingIcon.drawOnText(context, this.getX() + TEXT_PADDING, UIDraw.flatContentY(this.getY(), this.getHeight()), iconColor);
		}
	}

	private void drawTextContent(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		int textY = this.getY() + (this.getHeight() - UITheme.Typography.CAP_HEIGHT) / 2;
		int textX = this.getX() + leftPadding();

		int maxTextWidth = this.getWidth() - leftPadding() - TEXT_PADDING - (this.getValue().isEmpty() ? 0 : CLEAR_BUTTON_SIZE + 4);

		String text = this.getValue();
		if (text.isEmpty() && !this.isFocused()) {
			drawPlaceholder(context, textX, textY);
		} else {
			drawActiveText(context, text, textX, textY, maxTextWidth);
		}
	}

	private void drawPlaceholder(GuiGraphicsExtractor context, int x, int y) {
		if (placeholderText != null) {
			context.text(client.font, placeholderText, x, y, UITheme.Colors.TEXT_MUTED, false);
		}
	}

	private void drawActiveText(GuiGraphicsExtractor context, String text, int textX, int textY, int maxTextWidth) {
		int color = this.isFocused() ? UITheme.Colors.TEXT_PRIMARY : UITheme.Colors.TEXT_SUBTITLE;

		context.enableScissor(textX, this.getY(), textX + maxTextWidth, this.getY() + this.getHeight());
		if (this.isFocused() && !this.getHighlighted().isEmpty()) {
			drawSelectionHighlight(context, text, textX, textY);
		}
		UIDraw.text(context, text, textX, textY, color);
		context.disableScissor();

		if (this.isFocused() && this.isActive()) {
			drawCursor(context, text, textX, textY);
		}
	}

	private void drawSelectionHighlight(GuiGraphicsExtractor context, String text, int textX, int textY) {
		int highlightWidth = client.font.width(text);
		context.fill(textX, textY - 1, textX + highlightWidth, textY + 9, UITheme.Colors.SELECTION_HIGHLIGHT);
	}

	private void drawCursor(GuiGraphicsExtractor context, String text, int textX, int textY) {
		if ((System.currentTimeMillis() / CURSOR_BLINK_MS) % 2 == 0) {
			int cursorPos = this.getCursorPosition();
			String beforeCursor = text.substring(0, Math.min(cursorPos, text.length()));
			int cursorX = textX + client.font.width(beforeCursor);
			context.fill(cursorX, textY - 1, cursorX + UITheme.Dimensions.BORDER_WIDTH, textY + 9,
					UITheme.Colors.ACCENT_HOVER);
		}
	}

	private void drawClearButton(GuiGraphicsExtractor context, int mouseX, int mouseY) {
		if (this.getValue().isEmpty())
			return;

		int clearX = this.getX() + this.getWidth() - CLEAR_BUTTON_SIZE - 4;
		int clearY = this.getY() + (this.getHeight() - CLEAR_BUTTON_SIZE) / 2;
		boolean isHovered = isOverClearButton(mouseX, mouseY);
		int clearColor = isHovered ? UITheme.Colors.TEXT_HIGHLIGHT : UITheme.Colors.TEXT_MUTED;
		UIIcon.CLOSE.drawOnText(context, clearX + (CLEAR_BUTTON_SIZE - UIIcon.SIZE) / 2, UIDraw.flatContentY(this.getY(), this.getHeight()), clearColor);
	}

	private boolean isOverClearButton(int mouseX, int mouseY) {
		if (this.getValue().isEmpty())
			return false;
		int clearX = this.getX() + this.getWidth() - CLEAR_BUTTON_SIZE - 4;
		int clearY = this.getY() + (this.getHeight() - CLEAR_BUTTON_SIZE) / 2;
		return mouseX >= clearX && mouseX < clearX + CLEAR_BUTTON_SIZE &&
				mouseY >= clearY && mouseY < clearY + CLEAR_BUTTON_SIZE;
	}
}
