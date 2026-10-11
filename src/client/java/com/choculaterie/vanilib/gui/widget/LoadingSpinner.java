package com.choculaterie.vanilib.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public class LoadingSpinner implements Renderable {
	private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("vanilib", "textures/gui/loading.png");
	private static final int FRAME_WIDTH = 19;
	private static final int FRAME_HEIGHT = 18;
	private static final int[] FRAME_MS = {600, 100, 80, 60, 40, 600, 100, 80, 60, 40};
	private static final int CYCLE_MS = totalDuration();
	private static final int DEFAULT_WIDTH = 32;

	private int x;
	private int y;
	private final int width;
	private final int height;

	public LoadingSpinner(int x, int y) {
		this(x, y, DEFAULT_WIDTH);
	}

	public LoadingSpinner(int x, int y, int width) {
		this.x = x;
		this.y = y;
		this.width = Math.max(FRAME_WIDTH, width);
		this.height = Math.round(this.width * FRAME_HEIGHT / (float) FRAME_WIDTH);
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		int frame = currentFrame();
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, frame * FRAME_HEIGHT,
				getWidth(), getHeight(), FRAME_WIDTH, FRAME_HEIGHT, FRAME_WIDTH, FRAME_HEIGHT * FRAME_MS.length);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private static int currentFrame() {
		long t = System.currentTimeMillis() % CYCLE_MS;
		for (int i = 0; i < FRAME_MS.length; i++) {
			t -= FRAME_MS[i];
			if (t < 0) return i;
		}
		return 0;
	}

	private static int totalDuration() {
		int sum = 0;
		for (int ms : FRAME_MS) sum += ms;
		return sum;
	}
}
