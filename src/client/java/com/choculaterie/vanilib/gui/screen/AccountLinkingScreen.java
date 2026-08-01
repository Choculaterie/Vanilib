package com.choculaterie.vanilib.gui.screen;

import com.choculaterie.vanilib.gui.widget.CustomButton;
import com.choculaterie.vanilib.gui.widget.ToastManager;
import com.choculaterie.vanilib.network.AccountLinkingApi;
import com.choculaterie.vanilib.network.AccountLinkingContext;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AccountLinkingScreen extends Screen {
	private static final int PADDING = 6;
	private static final int BTN_SIZE = 20;
	private static final String VERIFY_SERVER_ADDRESS = "mc.choculaterie.com";
	private static final String VERIFY_SERVER_NAME = "Choculaterie";

	private final Screen parent;
	private final AccountLinkingContext context;
	private final AccountLinkingApi api;
	private ToastManager toastManager;

	private String currentFlowId = null;
	private String pendingLinkCode = null;
	private boolean isLinking = false;
	private String linkingStatus = "";
	private String pendingAuthUrl = null;
	private ScheduledExecutorService pollExecutor = null;
	private CustomButton linkBtn = null;
	private CustomButton copyUrlBtn = null;

	public AccountLinkingScreen(Screen parent, AccountLinkingContext context, AccountLinkingApi api) {
		super(Component.literal(context.getScreenTitle()));
		this.parent = parent;
		this.context = context;
		this.api = api;
	}

	@Override
	protected void init() {
		if (toastManager == null) {
			toastManager = new ToastManager(this.minecraft);
		}

		addRenderableWidget(new CustomButton(PADDING, PADDING, BTN_SIZE, BTN_SIZE, Component.literal("←"), b -> goBack()));

		boolean hasKey = context.hasApiKey();
		int cx = this.width / 2, btnW = 100;
		int btnY = this.height / 2 - 10;

		linkBtn = new CustomButton(cx - btnW / 2, btnY, btnW, BTN_SIZE,
			Component.literal(hasKey ? "Reset" : "Link Account"), b -> handleLinkOrReset(hasKey));
		addRenderableWidget(linkBtn);

		copyUrlBtn = new CustomButton(cx - btnW / 2, btnY, btnW, BTN_SIZE, Component.literal("Copy URL"), b -> copyAuthUrl());
		copyUrlBtn.visible = false;
		addRenderableWidget(copyUrlBtn);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor renderContext, int mouseX, int mouseY, float delta) {
		super.extractRenderState(renderContext, mouseX, mouseY, delta);
		int cx = this.width / 2;
		int btnY = this.height / 2 - 10;

		renderContext.centeredText(font, title, cx, 10, 0xFFFFFFFF);

		boolean hasKey = context.hasApiKey();
		if (hasKey && !isLinking) {
			renderContext.centeredText(font, Component.literal("§aAccount linked ✓"), cx, btnY - 20, 0xFFFFFFFF);
			renderContext.centeredText(font, Component.literal("Reset to unlink and connect a different account."),
				cx, btnY + 30, 0xFF888888);
		} else if (!isLinking) {
			int stepY = btnY + 32, lineH = 12;
			renderContext.centeredText(font, Component.literal("How it works:"), cx, stepY, 0xFF999999);
			stepY += lineH + 4;
			renderContext.centeredText(font, Component.literal("1. A browser window will open. Sign in and click Approve."),
				cx, stepY, 0xFFCCCCCC);
			stepY += lineH;
			renderContext.centeredText(font, Component.literal("2. The game will briefly join a server to verify your Minecraft account."),
				cx, stepY, 0xFFCCCCCC);
			stepY += lineH;
			renderContext.centeredText(font, Component.literal("3. " + context.getCompletionText()), cx, stepY, 0xFFCCCCCC);
		} else {
			if (!linkingStatus.isEmpty()) {
				renderContext.centeredText(font, Component.literal(linkingStatus), cx, btnY - 20, 0xFF88FF88);
			}
			if (pendingAuthUrl != null) {
				renderContext.centeredText(font, Component.literal("Browser didn't open? Copy the URL and paste it manually."),
					cx, btnY + 30, 0xFF888888);
			}
		}

		if (toastManager != null) toastManager.render(renderContext, delta, mouseX, mouseY);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return true;
	}

	@Override
	public void onClose() {
		goBack();
	}

	private void handleLinkOrReset(boolean hadKey) {
		if (hadKey) {
			context.clearApiKey();
			minecraft.setScreen(new AccountLinkingScreen(parent, context, api));
		} else {
			startOAuthFlow();
		}
	}

	private void goBack() {
		if (minecraft == null) return;
		stopPolling();
		context.goBack(minecraft, parent);
	}

	private void copyAuthUrl() {
		if (pendingAuthUrl != null && minecraft.keyboardHandler != null) {
			minecraft.keyboardHandler.setClipboard(pendingAuthUrl);
			toastManager.showSuccess("URL copied! Paste it in your browser.");
		}
	}

	private void startOAuthFlow() {
		if (isLinking) return;
		isLinking = true;
		linkingStatus = "Initiating...";

		api.initiateOAuthFlow(context.getClientName()).whenComplete((json, err) -> {
			if (err != null) {
				runOnClient(() -> { isLinking = false; linkingStatus = ""; });
				return;
			}
			try {
				currentFlowId = json.has("flowId") ? json.get("flowId").getAsString() : null;
				int expiresIn = json.has("expiresInSeconds") ? json.get("expiresInSeconds").getAsInt() : 300;
				if (currentFlowId == null) {
					runOnClient(() -> { isLinking = false; linkingStatus = ""; });
					return;
				}
				String authUrl = api.getOAuthAuthorizeUrl(currentFlowId);
				runOnClient(() -> {
					pendingAuthUrl = authUrl;
					if (linkBtn != null) linkBtn.visible = false;
					if (copyUrlBtn != null) copyUrlBtn.visible = true;
					linkingStatus = "Waiting for approval...";
					try {
						net.minecraft.util.Util.getPlatform().openUri(new java.net.URI(authUrl));
					} catch (Exception ignored) {}
				});
				startPolling(currentFlowId, expiresIn);
			} catch (Exception e) {
				runOnClient(() -> { isLinking = false; linkingStatus = ""; });
			}
		});
	}

	private void startPolling(String flowId, int timeoutSeconds) {
		stopPolling();
		pollExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "Vanilib-OAuth-Poll");
			t.setDaemon(true);
			return t;
		});

		final int[] attempts = {0};
		final int maxAttempts = timeoutSeconds / 2;
		final Minecraft mc = Minecraft.getInstance();

		pollExecutor.scheduleAtFixedRate(() -> {
			if (++attempts[0] >= maxAttempts) {
				mc.execute(() -> { stopPolling(); isLinking = false; linkingStatus = ""; });
				return;
			}
			api.getOAuthFlowStatus(flowId).whenComplete((json, err) -> {
				if (err != null) return;
				try { handlePollResponse(json, mc); } catch (Exception ignored) {}
			});
		}, 0, 2, TimeUnit.SECONDS);
	}

	private void handlePollResponse(JsonObject json, Minecraft mc) {
		String status = json.has("status") ? json.get("status").getAsString() : "pending";
		switch (status) {
			case "expired" -> mc.execute(() -> { stopPolling(); isLinking = false; linkingStatus = ""; resetFlowUi(); });
			case "cancelled" -> {
				mc.execute(() -> { stopPolling(); isLinking = false; linkingStatus = "§cCancelled"; resetFlowUi(); });
				CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() -> mc.execute(() -> linkingStatus = ""));
			}
			case "pending" -> mc.execute(() -> linkingStatus = "Waiting for approval...");
			case "completed" -> handleCompleted(json, mc);
		}
	}

	private void handleCompleted(JsonObject json, Minecraft mc) {
		String apiKey = context.extractKey(json);
		if (apiKey == null) return;

		boolean isMinecraftLinked = json.has("isMinecraftLinked") && json.get("isMinecraftLinked").getAsBoolean();
		boolean linkingComplete = json.has("minecraftLinkingComplete") && json.get("minecraftLinkingComplete").getAsBoolean();
		String linkCode = json.has("linkCode") && !json.get("linkCode").isJsonNull() ? json.get("linkCode").getAsString() : null;

		if (isMinecraftLinked) {
			stopPolling();
			mc.execute(() -> completeLinking(apiKey));
			return;
		}
		if (linkingComplete) {
			stopPolling();
			mc.execute(() -> {
				if (mc.getConnection() != null) {
					mc.getConnection().getConnection().disconnect(Component.literal("Linking complete"));
				}
				CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute(() -> mc.execute(() -> completeLinking(apiKey)));
			});
			return;
		}
		if (linkCode != null && !linkCode.equals(pendingLinkCode)) {
			pendingLinkCode = linkCode;
			mc.execute(() -> {
				linkingStatus = "Linking MC account...";
				autoJoinServerAndLink(linkCode);
			});
		}
	}

	private void stopPolling() {
		if (pollExecutor != null && !pollExecutor.isShutdown()) {
			pollExecutor.shutdownNow();
			pollExecutor = null;
		}
	}

	private void resetFlowUi() {
		pendingAuthUrl = null;
		if (linkBtn != null) linkBtn.visible = true;
		if (copyUrlBtn != null) copyUrlBtn.visible = false;
	}

	private void autoJoinServerAndLink(String linkCode) {
		linkingStatus = "Joining server...";
		final Minecraft mc = Minecraft.getInstance();
		try {
			var serverAddress = net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(VERIFY_SERVER_ADDRESS);
			var serverInfo = new net.minecraft.client.multiplayer.ServerData(
				VERIFY_SERVER_NAME, VERIFY_SERVER_ADDRESS, net.minecraft.client.multiplayer.ServerData.Type.OTHER);
			net.minecraft.client.gui.screens.ConnectScreen.startConnecting(this, mc, serverAddress, serverInfo, false, null);
			scheduleLinkCommand(mc, linkCode, 6);
		} catch (Exception e) {
			isLinking = false;
			linkingStatus = "";
		}
	}

	private void scheduleLinkCommand(Minecraft mc, String linkCode, int delaySeconds) {
		CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS).execute(() -> mc.execute(() -> {
			if (mc.player != null && mc.player.connection != null) {
				linkingStatus = "Sending link command...";
				mc.player.connection.sendCommand("link " + linkCode);
			} else if (delaySeconds == 6) {
				scheduleLinkCommand(mc, linkCode, 3);
			}
		}));
	}

	private void completeLinking(String apiKey) {
		stopPolling();
		isLinking = false;
		linkingStatus = "";
		pendingLinkCode = null;
		currentFlowId = null;

		context.saveApiKey(apiKey);

		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> context.onLinked(mc, parent));
	}

	private void runOnClient(Runnable r) {
		if (minecraft != null) minecraft.execute(r);
	}
}
