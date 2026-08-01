package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.gui.theme.UITheme;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class CardListWidget<T> implements Renderable, GuiEventListener {
	private static final int ENTRY_SPACING = 2;
	private static final int SCROLL_SPEED = 20;

	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final Function<T, List<CardEntryWidget.Line>> lineProvider;
	private final Consumer<T> onItemClick;
	private final List<CardEntryWidget<T>> entries = new ArrayList<>();
	private final ScrollBar scrollBar;
	private double scrollAmount = 0;

	public CardListWidget(int x, int y, int width, int height,
						   Function<T, List<CardEntryWidget.Line>> lineProvider, Consumer<T> onItemClick) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.lineProvider = lineProvider;
		this.onItemClick = onItemClick;
		this.scrollBar = new ScrollBar(x + width - UITheme.Dimensions.SCROLLBAR_WIDTH, y, height);
	}

	public void setItems(List<T> items) {
		entries.clear();
		scrollAmount = 0;
		int currentY = 0;
		int entryWidth = getEntryWidth();
		for (T item : items) {
			CardEntryWidget<T> entry = new CardEntryWidget<>(item, x, y + currentY, entryWidth, lineProvider,
				onItemClick == null ? null : () -> onItemClick.accept(item));
			entries.add(entry);
			currentY += entry.getHeight() + ENTRY_SPACING;
		}
		updateScrollBar();
	}

	public void clear() {
		entries.clear();
		scrollAmount = 0;
		updateScrollBar();
	}

	private int getEntryWidth() {
		return Math.max(0, width - UITheme.Dimensions.PADDING);
	}

	private double getTotalContentHeight() {
		int total = 0;
		for (CardEntryWidget<T> entry : entries) {
			total += entry.getHeight() + ENTRY_SPACING;
		}
		return total;
	}

	private double getMaxScroll() {
		return Math.max(0, getTotalContentHeight() - height);
	}

	private void updateScrollBar() {
		double contentHeight = getTotalContentHeight();
		scrollBar.setScrollData(contentHeight, height);
		scrollBar.setScrollPercentage(contentHeight > height ? scrollAmount / (contentHeight - height) : 0);
	}

	private boolean isEntryVisible(int entryY, int entryHeight) {
		return entryY + entryHeight >= y && entryY < y + height;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		context.enableScissor(x, y, x + width, y + height);

		int offsetY = (int) scrollAmount;
		int currentY = 0;
		int entryWidth = getEntryWidth();
		for (CardEntryWidget<T> entry : entries) {
			int entryY = y + currentY - offsetY;
			if (isEntryVisible(entryY, entry.getHeight())) {
				entry.setX(x);
				entry.setY(entryY);
				entry.setWidth(entryWidth);
				entry.render(context, mouseX, mouseY, delta);
			}
			currentY += entry.getHeight() + ENTRY_SPACING;
		}

		context.disableScissor();
		if (scrollBar.isVisible()) {
			scrollBar.render(context, mouseX, mouseY, delta);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (scrollBar.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		if (button != 0 || !isMouseOver(mouseX, mouseY)) {
			return false;
		}

		int offsetY = (int) scrollAmount;
		int currentY = 0;
		int entryWidth = getEntryWidth();
		for (CardEntryWidget<T> entry : entries) {
			int entryY = y + currentY - offsetY;
			if (isEntryVisible(entryY, entry.getHeight())) {
				entry.setX(x);
				entry.setY(entryY);
				entry.setWidth(entryWidth);
				if (entry.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
			currentY += entry.getHeight() + ENTRY_SPACING;
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!isMouseOver(mouseX, mouseY)) {
			return false;
		}
		double maxScroll = getMaxScroll();
		scrollAmount = Math.max(0, Math.min(maxScroll, scrollAmount - verticalAmount * SCROLL_SPEED));
		updateScrollBar();
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
