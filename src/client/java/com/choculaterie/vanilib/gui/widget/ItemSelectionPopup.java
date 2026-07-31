package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.gui.theme.UITheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemSelectionPopup<T> implements Renderable {
	private static final int ITEM_HEIGHT = 24;
	private static final int HEADER_HEIGHT = 90;
	private static final int BUTTON_WIDTH = 80;

	private final Minecraft client;
	private final int x;
	private final int y;
	private final int width;
	private final int height;
	private final String title;
	private final Function<T, String> displayNameFn;
	private final List<T> allItems = new ArrayList<>();
	private final List<T> filteredItems = new ArrayList<>();

	private final CustomTextField searchField;
	private final CustomButton cancelButton;
	private final CustomButton selectButton;
	private ScrollBar scrollBar;
	private int selectedIndex = -1;

	private Consumer<T> onSelected;
	private Runnable onCancel;

	public ItemSelectionPopup(int screenWidth, int screenHeight, String title, List<T> items, Function<T, String> displayNameFn) {
		this.client = Minecraft.getInstance();
		this.title = title;
		this.displayNameFn = displayNameFn;
		this.allItems.addAll(items);
		this.filteredItems.addAll(allItems);

		this.width = Math.min(400, screenWidth - 40);
		this.height = Math.min(500, screenHeight - 40);
		this.x = (screenWidth - width) / 2;
		this.y = (screenHeight - height) / 2;

		int searchFieldWidth = width - UITheme.Dimensions.PADDING * 2;
		searchField = new CustomTextField(client,
			x + UITheme.Dimensions.PADDING,
			y + UITheme.Dimensions.PADDING + UITheme.Typography.LINE_HEIGHT + UITheme.Dimensions.PADDING,
			searchFieldWidth, UITheme.Dimensions.BUTTON_HEIGHT, Component.literal("Search"));
		searchField.setPlaceholder(Component.literal("Search..."));
		searchField.setOnChanged(() -> onSearchChanged(searchField.getValue()));
		searchField.setFocused(true);

		cancelButton = new CustomButton(
			x + UITheme.Dimensions.PADDING,
			y + height - UITheme.Dimensions.PADDING - UITheme.Dimensions.BUTTON_HEIGHT,
			BUTTON_WIDTH, UITheme.Dimensions.BUTTON_HEIGHT, Component.literal("Cancel"),
			b -> { if (onCancel != null) onCancel.run(); });

		selectButton = new CustomButton(
			x + width - UITheme.Dimensions.PADDING - BUTTON_WIDTH,
			y + height - UITheme.Dimensions.PADDING - UITheme.Dimensions.BUTTON_HEIGHT,
			BUTTON_WIDTH, UITheme.Dimensions.BUTTON_HEIGHT, Component.literal("Select"),
			b -> trySelect());

		updateScrollBar();
	}

	public void setOnSelected(Consumer<T> callback) {
		this.onSelected = callback;
	}

	public void setOnCancel(Runnable callback) {
		this.onCancel = callback;
	}

	private void trySelect() {
		if (selectedIndex >= 0 && selectedIndex < filteredItems.size() && onSelected != null) {
			onSelected.accept(filteredItems.get(selectedIndex));
		}
	}

	private void onSearchChanged(String search) {
		filteredItems.clear();
		String lower = search.toLowerCase();
		for (T item : allItems) {
			if (lower.isEmpty() || displayNameFn.apply(item).toLowerCase().contains(lower)) {
				filteredItems.add(item);
			}
		}
		selectedIndex = -1;
		updateScrollBar();
	}

	private int getListY() {
		return y + HEADER_HEIGHT;
	}

	private int getListHeight() {
		return height - HEADER_HEIGHT - UITheme.Dimensions.PADDING - UITheme.Dimensions.BUTTON_HEIGHT - UITheme.Dimensions.PADDING;
	}

	private int getListRightEdge() {
		return x + width - UITheme.Dimensions.PADDING - UITheme.Dimensions.SCROLLBAR_WIDTH - 2;
	}

	private void updateScrollBar() {
		scrollBar = new ScrollBar(getListRightEdge() + 2, getListY(), getListHeight());
		scrollBar.setScrollData(filteredItems.size() * ITEM_HEIGHT, getListHeight());
	}

	private int getMaxScroll() {
		int listHeight = getListHeight();
		int contentHeight = filteredItems.size() * ITEM_HEIGHT;
		return contentHeight <= listHeight ? 0 : filteredItems.size() - listHeight / ITEM_HEIGHT;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight(), UITheme.Colors.OVERLAY_BG);
		context.fill(x, y, x + width, y + height, UITheme.Colors.BUTTON_BG_DISABLED);

		int border = UITheme.Dimensions.BORDER_WIDTH;
		context.fill(x, y, x + width, y + border, UITheme.Colors.BUTTON_BORDER);
		context.fill(x, y + height - border, x + width, y + height, UITheme.Colors.BUTTON_BORDER);
		context.fill(x, y, x + border, y + height, UITheme.Colors.BUTTON_BORDER);
		context.fill(x + width - border, y, x + width, y + height, UITheme.Colors.BUTTON_BORDER);

		context.centeredText(client.font, title, x + width / 2, y + UITheme.Dimensions.PADDING, UITheme.Colors.TEXT_PRIMARY);
		searchField.extractRenderState(context, mouseX, mouseY, delta);

		int listY = getListY();
		int listHeight = getListHeight();
		int listRightEdge = getListRightEdge();

		context.fill(x + UITheme.Dimensions.PADDING, listY, listRightEdge, listY + listHeight, UITheme.Colors.PANEL_BG);
		context.enableScissor(x + UITheme.Dimensions.PADDING, listY, listRightEdge, listY + listHeight);

		int scrollOffset = (int) (scrollBar.getScrollPercentage() * getMaxScroll());
		int maxVisible = (listHeight / ITEM_HEIGHT) + 1;
		for (int i = scrollOffset; i < Math.min(filteredItems.size(), scrollOffset + maxVisible); i++) {
			T item = filteredItems.get(i);
			int itemY = listY + (i - scrollOffset) * ITEM_HEIGHT;
			boolean isSelected = i == selectedIndex;
			boolean isHovered = mouseX >= x + UITheme.Dimensions.PADDING && mouseX < listRightEdge
				&& mouseY >= itemY && mouseY < itemY + ITEM_HEIGHT;

			int bgColor = isSelected ? 0xFF505050 : isHovered ? 0xFF404040 : UITheme.Colors.PANEL_BG;
			context.fill(x + UITheme.Dimensions.PADDING + 2, itemY, listRightEdge - 2, itemY + ITEM_HEIGHT, bgColor);
			context.text(client.font, displayNameFn.apply(item), x + UITheme.Dimensions.PADDING + 4, itemY + 8, UITheme.Colors.TEXT_PRIMARY);
		}

		context.disableScissor();

		if (scrollBar.isVisible()) {
			scrollBar.updateAndRender(context, mouseX, mouseY, delta, GLFW.glfwGetCurrentContext());
		}

		selectButton.active = selectedIndex >= 0;
		cancelButton.extractRenderState(context, mouseX, mouseY, delta);
		selectButton.extractRenderState(context, mouseX, mouseY, delta);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) {
			return true;
		}
		if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
			if (onCancel != null) onCancel.run();
			return true;
		}

		boolean clickedInField = mouseX >= searchField.getX() && mouseX < searchField.getX() + searchField.getWidth()
			&& mouseY >= searchField.getY() && mouseY < searchField.getY() + searchField.getHeight();
		searchField.setFocused(clickedInField);
		if (clickedInField) {
			return true;
		}

		if (isInside(cancelButton, mouseX, mouseY)) {
			if (onCancel != null) onCancel.run();
			return true;
		}
		if (isInside(selectButton, mouseX, mouseY)) {
			trySelect();
			return true;
		}

		int listY = getListY();
		int listHeight = getListHeight();
		int listRightEdge = getListRightEdge();
		if (mouseX >= x + UITheme.Dimensions.PADDING && mouseX < listRightEdge && mouseY >= listY && mouseY < listY + listHeight) {
			int scrollOffset = (int) (scrollBar.getScrollPercentage() * getMaxScroll());
			int clickedIndex = scrollOffset + (int) ((mouseY - listY) / ITEM_HEIGHT);
			if (clickedIndex >= 0 && clickedIndex < filteredItems.size()) {
				selectedIndex = clickedIndex;
			}
		}
		return true;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int listY = getListY();
		int listHeight = getListHeight();
		if (mouseX < x || mouseX >= x + width || mouseY < listY || mouseY >= listY + listHeight) {
			return false;
		}
		int maxScroll = getMaxScroll();
		if (maxScroll > 0) {
			int scrollOffset = (int) (scrollBar.getScrollPercentage() * maxScroll);
			scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) verticalAmount));
			scrollBar.setScrollPercentage((double) scrollOffset / maxScroll);
		}
		return true;
	}

	private static boolean isInside(CustomButton button, double mouseX, double mouseY) {
		return mouseX >= button.getX() && mouseX < button.getX() + button.getWidth()
			&& mouseY >= button.getY() && mouseY < button.getY() + button.getHeight();
	}
}
