package com.choculaterie.vanilib.network;

import com.google.gson.JsonObject;

import java.util.concurrent.CompletableFuture;

public interface AccountLinkingApi {
	CompletableFuture<JsonObject> initiateOAuthFlow(String clientName);

	CompletableFuture<JsonObject> getOAuthFlowStatus(String flowId);

	String getOAuthAuthorizeUrl(String flowId);
}
