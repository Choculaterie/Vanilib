package com.choculaterie.vanilib.config;

public class StringConfig extends ConfigOption<String> {
	public StringConfig(String name, String defaultValue, String comment) {
		super(name, defaultValue, comment);
	}

	@Override
	public ConfigType getType() {
		return ConfigType.STRING;
	}

	@Override
	public String getStringValue() {
		return value;
	}

	@Override
	public void setValueFromString(String str) {
		setValue(str);
	}
}
