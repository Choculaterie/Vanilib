package com.choculaterie.vanilib.network;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public interface AccountLinkingContext {
	String getScreenTitle();

	String getClientName();

	String getCompletionText();

	boolean hasApiKey();

	String extractKey(JsonObject completedStatusJson);

	void saveApiKey(String apiKey);

	void clearApiKey();

	void onLinked(Minecraft mc, Screen parent);

	void goBack(Minecraft mc, Screen parent);
}
