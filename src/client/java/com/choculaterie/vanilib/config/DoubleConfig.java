package com.choculaterie.vanilib.config;

public class DoubleConfig extends ConfigOption<Double> {
	private final double min;
	private final double max;

	public DoubleConfig(String name, double defaultValue, double min, double max, String comment) {
		super(name, clamp(defaultValue, min, max), comment);
		this.min = min;
		this.max = max;
	}

	private static double clamp(double v, double min, double max) {
		return Math.max(min, Math.min(max, v));
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	@Override
	public void setValue(Double value) {
		super.setValue(clamp(value, min, max));
	}

	@Override
	public ConfigType getType() {
		return ConfigType.DOUBLE;
	}

	@Override
	public String getStringValue() {
		return String.valueOf(value);
	}

	@Override
	public void setValueFromString(String str) {
		try {
			setValue(Double.parseDouble(str.trim()));
		} catch (NumberFormatException ignored) {
		}
	}
}
