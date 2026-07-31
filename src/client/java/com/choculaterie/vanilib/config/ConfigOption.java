package com.choculaterie.vanilib.config;

public abstract class ConfigOption<T> {
	private final String name;
	private final T defaultValue;
	private final String comment;
	protected T value;

	protected ConfigOption(String name, T defaultValue, String comment) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.comment = comment;
		this.value = defaultValue;
	}

	public abstract ConfigType getType();

	public abstract String getStringValue();

	public abstract void setValueFromString(String str);

	public String getName() {
		return name;
	}

	public String getComment() {
		return comment;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public boolean isModified() {
		return !value.equals(defaultValue);
	}

	public void resetToDefault() {
		setValue(defaultValue);
	}
}
