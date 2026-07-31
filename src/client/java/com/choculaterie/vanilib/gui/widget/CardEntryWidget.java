package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.gui.theme.UITheme;
import com.choculaterie.vanilib.util.FormatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.List;
import java.util.function.Function;

public class CardEntryWidget<T> implements Renderable, GuiEventListener {
	public record Line(String text, int color) {}

	private static final int MIN_HEIGHT = 70;
	private static final int LINE_HEIGHT = UITheme.Typography.LINE_HEIGHT;
	private static final int CONTENT_SPACING = 5;

	private final T item;
	private final Minecraft client;
	private final Function<T, List<Line>> lineProvider;
	private final Runnable onClick;
	private int x;
	private int y;
	private int width;
	private boolean hovered;
	private int calculatedHeight = MIN_HEIGHT;

	public CardEntryWidget(T item, int x, int y, int width, Function<T, List<Line>> lineProvider, Runnable onClick) {
		this.item = item;
		this.client = Minecraft.getInstance();
		this.lineProvider = lineProvider;
		this.onClick = onClick;
		this.x = x;
		this.y = y;
		this.width = width;
		calculateHeight();
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setWidth(int width) {
		this.width = width;
		calculateHeight();
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return calculatedHeight;
	}

	public T getItem() {
		return item;
	}

	private void calculateHeight() {
		int contentWidth = width - UITheme.Dimensions.PADDING * 2;
		int currentY = UITheme.Dimensions.PADDING;
		for (Line line : lineProvider.apply(item)) {
			currentY += wrappedLines(line.text(), contentWidth).size() * LINE_HEIGHT + CONTENT_SPACING;
		}
		calculatedHeight = Math.max(MIN_HEIGHT, currentY + UITheme.Dimensions.PADDING);
	}

	private List<String> wrappedLines(String text, int maxWidth) {
		return (text == null || text.isEmpty()) ? List.of("") : FormatUtils.wrapText(client.font, text, maxWidth);
	}

	private void drawWrappedText(GuiGraphicsExtractor context, String text, int textX, int textY, int maxWidth, int color) {
		int lineY = textY;
		for (String line : wrappedLines(text, maxWidth)) {
			context.text(client.font, line, textX, lineY, color);
			lineY += LINE_HEIGHT;
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + calculatedHeight;

		int bgColor = hovered ? UITheme.Colors.BUTTON_BG_HOVER : UITheme.Colors.BUTTON_BG;
		context.fill(x, y, x + width, y + calculatedHeight, bgColor);

		int borderWidth = UITheme.Dimensions.BORDER_WIDTH;
		int borderColor = UITheme.Colors.BUTTON_BORDER;
		context.fill(x, y, x + width, y + borderWidth, borderColor);
		context.fill(x, y + calculatedHeight - borderWidth, x + width, y + calculatedHeight, borderColor);
		context.fill(x, y, x + borderWidth, y + calculatedHeight, borderColor);
		context.fill(x + width - borderWidth, y, x + width, y + calculatedHeight, borderColor);

		int contentWidth = width - UITheme.Dimensions.PADDING * 2;
		int currentY = y + UITheme.Dimensions.PADDING;
		for (Line line : lineProvider.apply(item)) {
			drawWrappedText(context, line.text(), x + UITheme.Dimensions.PADDING, currentY, contentWidth, line.color());
			currentY += wrappedLines(line.text(), contentWidth).size() * LINE_HEIGHT + CONTENT_SPACING;
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || mouseX < x || mouseX >= x + width || mouseY < y || mouseY >= y + calculatedHeight) {
			return false;
		}
		if (onClick != null) {
			onClick.run();
		}
		return true;
	}

	@Override
	public void setFocused(boolean focused) {
	}

	@Override
	public boolean isFocused() {
		return false;
	}
}
