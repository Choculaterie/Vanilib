package com.choculaterie.vanilib.config;

public class IntConfig extends ConfigOption<Integer> {
	private final int min;
	private final int max;

	public IntConfig(String name, int defaultValue, int min, int max, String comment) {
		super(name, clamp(defaultValue, min, max), comment);
		this.min = min;
		this.max = max;
	}

	private static int clamp(int v, int min, int max) {
		return Math.max(min, Math.min(max, v));
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	@Override
	public void setValue(Integer value) {
		super.setValue(clamp(value, min, max));
	}

	@Override
	public ConfigType getType() {
		return ConfigType.INTEGER;
	}

	@Override
	public String getStringValue() {
		return String.valueOf(value);
	}

	@Override
	public void setValueFromString(String str) {
		try {
			setValue(Integer.parseInt(str.trim()));
		} catch (NumberFormatException ignored) {
		}
	}
}
