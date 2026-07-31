package com.choculaterie.vanilib.config;

public class BooleanConfig extends ConfigOption<Boolean> {
	public BooleanConfig(String name, boolean defaultValue, String comment) {
		super(name, defaultValue, comment);
	}

	@Override
	public ConfigType getType() {
		return ConfigType.BOOLEAN;
	}

	@Override
	public String getStringValue() {
		return String.valueOf(value);
	}

	@Override
	public void setValueFromString(String str) {
		setValue(Boolean.parseBoolean(str));
	}

	public void toggle() {
		setValue(!value);
	}
}
