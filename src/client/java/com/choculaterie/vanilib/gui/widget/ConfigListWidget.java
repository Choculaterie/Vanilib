package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.config.ConfigOption;
import com.choculaterie.vanilib.gui.theme.UITheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigListWidget implements Renderable, GuiEventListener {
	private static final int ROW_SPACING = 2;
	private static final int SCROLL_SPEED = 20;

	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final CustomTextField searchField;
	private final ScrollBar scrollBar;
	private final List<ConfigOptionWidget> allRows = new ArrayList<>();
	private final List<Integer> rowBaseY = new ArrayList<>();
	private List<ConfigOptionWidget> visibleRows;
	private double scrollAmount = 0;
	private int totalContentHeight = 0;

	public ConfigListWidget(int x, int y, int width, int height, List<ConfigOption<?>> configs) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		Minecraft client = Minecraft.getInstance();
		int rowWidth = width - UITheme.Dimensions.SCROLLBAR_WIDTH - UITheme.Dimensions.PADDING_SMALL;

		searchField = new CustomTextField(client, x, y, rowWidth, UITheme.Dimensions.SEARCH_BAR_HEIGHT, Component.literal("Search"));
		searchField.setPlaceholder(Component.literal("Search settings..."));
		searchField.setOnChanged(() -> applyFilter(searchField.getValue()));

		scrollBar = new ScrollBar(x + width - UITheme.Dimensions.SCROLLBAR_WIDTH, getListY(), getListHeight());

		List<ConfigOption<?>> sorted = new ArrayList<>(configs);
		sorted.sort(Comparator.comparing(ConfigOption::getName, String.CASE_INSENSITIVE_ORDER));
		for (ConfigOption<?> config : sorted) {
			allRows.add(new ConfigOptionWidget(x, 0, rowWidth, UITheme.Dimensions.BUTTON_HEIGHT, config));
		}
		visibleRows = new ArrayList<>(allRows);
		layoutVisibleRows();
	}

	private int getListY() {
		return y + UITheme.Dimensions.SEARCH_BAR_HEIGHT + UITheme.Dimensions.PADDING_SMALL;
	}

	private int getListHeight() {
		return height - UITheme.Dimensions.SEARCH_BAR_HEIGHT - UITheme.Dimensions.PADDING_SMALL;
	}

	private void applyFilter(String query) {
		String q = query.toLowerCase();
		visibleRows = allRows.stream()
			.filter(row -> row.getConfig().getName().toLowerCase().contains(q)
				|| row.getConfig().getComment().toLowerCase().contains(q))
			.collect(Collectors.toList());
		scrollAmount = 0;
		layoutVisibleRows();
	}

	private void layoutVisibleRows() {
		int rowY = getListY();
		rowBaseY.clear();
		for (ConfigOptionWidget row : visibleRows) {
			rowBaseY.add(rowY);
			rowY += row.getHeight() + ROW_SPACING;
		}
		totalContentHeight = rowY - getListY();
		applyScroll();
	}

	private void applyScroll() {
		for (int i = 0; i < visibleRows.size(); i++) {
			visibleRows.get(i).setY(rowBaseY.get(i) - (int) scrollAmount);
		}
		double maxScroll = getMaxScroll();
		scrollBar.setScrollData(totalContentHeight, getListHeight());
		scrollBar.setScrollPercentage(maxScroll > 0 ? scrollAmount / maxScroll : 0);
	}

	private double getMaxScroll() {
		return Math.max(0, totalContentHeight - getListHeight());
	}

	private boolean isInsideList(double mouseX, double mouseY) {
		return mouseX >= x && mouseX < x + width && mouseY >= getListY() && mouseY < getListY() + getListHeight();
	}

	private boolean isRowVisible(ConfigOptionWidget row) {
		return row.getY() + row.getHeight() >= getListY() && row.getY() < getListY() + getListHeight();
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		searchField.render(context, mouseX, mouseY, delta);

		context.enableScissor(x, getListY(), x + width, getListY() + getListHeight());
		for (ConfigOptionWidget row : visibleRows) {
			if (isRowVisible(row)) {
				row.render(context, mouseX, mouseY, delta);
			}
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

		boolean clickedInSearch = mouseX >= searchField.getX() && mouseX < searchField.getX() + searchField.getWidth()
			&& mouseY >= searchField.getY() && mouseY < searchField.getY() + searchField.getHeight();
		searchField.setFocused(clickedInSearch);
		if (clickedInSearch) {
			return true;
		}

		if (isInsideList(mouseX, mouseY)) {
			for (ConfigOptionWidget row : visibleRows) {
				if (isRowVisible(row) && row.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!isInsideList(mouseX, mouseY)) {
			return false;
		}
		double maxScroll = getMaxScroll();
		scrollAmount = Math.max(0, Math.min(maxScroll, scrollAmount - verticalAmount * SCROLL_SPEED));
		applyScroll();
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (searchField.isFocused()) {
			return searchField.keyPressed(keyCode, scanCode, modifiers);
		}
		for (ConfigOptionWidget row : visibleRows) {
			if (row.isFocused()) {
				return row.keyPressed(keyCode, scanCode, modifiers);
			}
		}
		return false;
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (searchField.isFocused()) {
			return searchField.charTyped(codePoint, modifiers);
		}
		for (ConfigOptionWidget row : visibleRows) {
			if (row.isFocused()) {
				return row.charTyped(codePoint, modifiers);
			}
		}
		return false;
	}

	@Override
	public void setFocused(boolean focused) {
	}

	@Override
	public boolean isFocused() {
		return searchField.isFocused();
	}
}
