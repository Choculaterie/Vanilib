package com.choculaterie.vanilib.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ConfigStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private ConfigStore() {}

	public static void save(Path file, List<ConfigOption<?>> configs) {
		JsonObject root = new JsonObject();
		for (ConfigOption<?> config : configs) {
			root.addProperty(config.getName(), config.getStringValue());
		}
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(file, GSON.toJson(root), StandardCharsets.UTF_8);
		} catch (IOException ignored) {
		}
	}

	public static void load(Path file, List<ConfigOption<?>> configs) {
		if (!Files.exists(file)) {
			return;
		}
		JsonObject root;
		try {
			root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
		} catch (IOException | RuntimeException ignored) {
			return;
		}
		for (ConfigOption<?> config : configs) {
			if (root.has(config.getName())) {
				config.setValueFromString(root.get(config.getName()).getAsString());
			}
		}
	}
}
