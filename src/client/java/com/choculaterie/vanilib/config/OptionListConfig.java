package com.choculaterie.vanilib.config;

import java.util.List;
import java.util.function.Function;

public class OptionListConfig<E> extends ConfigOption<E> {
	private final List<E> entries;
	private final Function<E, String> displayNameFn;

	public OptionListConfig(String name, E defaultValue, List<E> entries, Function<E, String> displayNameFn, String comment) {
		super(name, defaultValue, comment);
		this.entries = entries;
		this.displayNameFn = displayNameFn;
	}

	public List<E> getEntries() {
		return entries;
	}

	public String getDisplayName(E entry) {
		return displayNameFn.apply(entry);
	}

	public void cycleNext() {
		setValue(entries.get((entries.indexOf(value) + 1) % entries.size()));
	}

	public void cyclePrevious() {
		int next = entries.indexOf(value) - 1;
		setValue(entries.get(next < 0 ? entries.size() - 1 : next));
	}

	@Override
	public ConfigType getType() {
		return ConfigType.OPTION_LIST;
	}

	@Override
	public String getStringValue() {
		return displayNameFn.apply(value);
	}

	@Override
	public void setValueFromString(String str) {
		for (E entry : entries) {
			if (displayNameFn.apply(entry).equals(str)) {
				setValue(entry);
				return;
			}
		}
	}
}
