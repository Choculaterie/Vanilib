package com.choculaterie.vanilib.gui.widget;

import com.choculaterie.vanilib.config.BooleanConfig;
import com.choculaterie.vanilib.config.ConfigOption;
import com.choculaterie.vanilib.config.ConfigType;
import com.choculaterie.vanilib.config.OptionListConfig;
import com.choculaterie.vanilib.gui.theme.UITheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ConfigOptionWidget implements Renderable, GuiEventListener {
	private static final double LABEL_WIDTH_RATIO = 0.5;
	private static final int LABEL_RIGHT_MARGIN = 8;
	private static final int RESET_BUTTON_WIDTH = 36;

	private final Minecraft client;
	private final ConfigOption<?> config;
	private final int x;
	private int y;
	private final int width;
	private final int height;
	private final int controlX;
	private final int controlWidth;

	private ToggleButton toggleButton;
	private CustomTextField textField;
	private CustomButton dropdownHeaderButton;
	private DropdownWidget dropdownWidget;
	private final CustomButton resetButton;

	public ConfigOptionWidget(int x, int y, int width, int height, ConfigOption<?> config) {
		this.client = Minecraft.getInstance();
		this.config = config;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.controlX = x + (int) (width * LABEL_WIDTH_RATIO) + LABEL_RIGHT_MARGIN;
		this.controlWidth = Math.max(0, x + width - controlX - RESET_BUTTON_WIDTH - LABEL_RIGHT_MARGIN);

		buildControl();

		this.resetButton = new CustomButton(x + width - RESET_BUTTON_WIDTH, y, RESET_BUTTON_WIDTH, height,
			Component.literal("↺"), b -> {
				config.resetToDefault();
				syncControlsFromValue();
			});
	}

	private void buildControl() {
		switch (config.getType()) {
			case BOOLEAN -> {
				BooleanConfig boolConfig = (BooleanConfig) config;
				toggleButton = new ToggleButton(controlX, y, boolConfig.getValue(), boolConfig::setValue);
			}
			case OPTION_LIST -> buildOptionListControl((OptionListConfig<?>) config);
			case INTEGER, DOUBLE, STRING -> {
				textField = new CustomTextField(client, controlX, y, controlWidth, height, Component.literal(config.getName()));
				textField.setValue(config.getStringValue());
				textField.setOnChanged(() -> config.setValueFromString(textField.getValue()));
			}
		}
	}

	private <E> void buildOptionListControl(OptionListConfig<E> optionList) {
		List<DropdownWidget.DropdownItem> items = new ArrayList<>();
		for (E entry : optionList.getEntries()) {
			items.add(new DropdownWidget.DropdownItem(optionList.getDisplayName(entry), entry));
		}

		dropdownWidget = new DropdownWidget(controlX, y + height, controlWidth, item -> {
			@SuppressWarnings("unchecked")
			E selected = (E) item.getData();
			optionList.setValue(selected);
			dropdownWidget.onClose();
			updateOptionListHeader(optionList);
		});
		dropdownWidget.setItems(items);

		dropdownHeaderButton = new CustomButton(controlX, y, controlWidth, height, Component.empty(),
			b -> {
				if (dropdownWidget.isOpen()) {
					dropdownWidget.onClose();
				} else {
					dropdownWidget.open();
				}
			});
		updateOptionListHeader(optionList);
	}

	private <E> void updateOptionListHeader(OptionListConfig<E> optionList) {
		dropdownHeaderButton.setMessage(Component.literal(optionList.getDisplayName(optionList.getValue()) + " ▼"));
	}

	private void syncControlsFromValue() {
		switch (config.getType()) {
			case BOOLEAN -> toggleButton.setToggled(((BooleanConfig) config).getValue());
			case OPTION_LIST -> updateOptionListHeader((OptionListConfig<?>) config);
			case INTEGER, DOUBLE, STRING -> textField.setValue(config.getStringValue());
		}
	}

	private boolean isTextFieldType() {
		ConfigType type = config.getType();
		return type == ConfigType.STRING || type == ConfigType.INTEGER || type == ConfigType.DOUBLE;
	}

	public ConfigOption<?> getConfig() {
		return config;
	}

	public int getHeight() {
		return height;
	}

	public int getY() {
		return y;
	}

	public void setY(int newY) {
		int dy = newY - this.y;
		this.y = newY;
		if (toggleButton != null) toggleButton.setY(toggleButton.getY() + dy);
		if (textField != null) textField.setY(textField.getY() + dy);
		if (dropdownHeaderButton != null) dropdownHeaderButton.setY(dropdownHeaderButton.getY() + dy);
		if (dropdownWidget != null) dropdownWidget.setPosition(controlX, newY + height);
		resetButton.setY(resetButton.getY() + dy);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		int textY = y + (height - UITheme.Typography.TEXT_HEIGHT) / 2;
		context.text(client.font, config.getName(), x, textY, UITheme.Colors.TEXT_PRIMARY);

		switch (config.getType()) {
			case BOOLEAN -> toggleButton.extractRenderState(context, mouseX, mouseY, delta);
			case OPTION_LIST -> {
				dropdownHeaderButton.extractRenderState(context, mouseX, mouseY, delta);
				dropdownWidget.extractRenderState(context, mouseX, mouseY, delta);
			}
			case INTEGER, DOUBLE, STRING -> textField.extractRenderState(context, mouseX, mouseY, delta);
		}

		resetButton.visible = config.isModified();
		if (resetButton.visible) {
			resetButton.extractRenderState(context, mouseX, mouseY, delta);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0) {
			return false;
		}

		if (config.getType() == ConfigType.OPTION_LIST) {
			if (dropdownWidget.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
			if (isInside(dropdownHeaderButton, mouseX, mouseY)) {
				if (dropdownWidget.isOpen()) {
					dropdownWidget.onClose();
				} else {
					dropdownWidget.open();
				}
				return true;
			}
		} else if (config.getType() == ConfigType.BOOLEAN) {
			if (isInside(toggleButton, mouseX, mouseY)) {
				((BooleanConfig) config).toggle();
				toggleButton.setToggled(((BooleanConfig) config).getValue());
				return true;
			}
		} else if (isTextFieldType()) {
			boolean clickedInField = isInside(textField, mouseX, mouseY);
			textField.setFocused(clickedInField);
			if (clickedInField) {
				return true;
			}
		}

		if (resetButton.visible && isInside(resetButton, mouseX, mouseY)) {
			config.resetToDefault();
			syncControlsFromValue();
			return true;
		}

		return false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (config.getType() == ConfigType.OPTION_LIST) {
			return dropdownWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
		}
		return false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		return isTextFieldType() && textField.isFocused() && textField.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return isTextFieldType() && textField.isFocused() && textField.charTyped(event);
	}

	@Override
	public void setFocused(boolean focused) {
	}

	@Override
	public boolean isFocused() {
		return isTextFieldType() && textField.isFocused();
	}

	private static boolean isInside(AbstractWidget widget, double mouseX, double mouseY) {
		return mouseX >= widget.getX() && mouseX < widget.getX() + widget.getWidth()
			&& mouseY >= widget.getY() && mouseY < widget.getY() + widget.getHeight();
	}
}
