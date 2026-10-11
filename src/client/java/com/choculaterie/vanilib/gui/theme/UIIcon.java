package com.choculaterie.vanilib.gui.theme;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public enum UIIcon {
	CLOSE(
		".........",
		".#....#..",
		".##..##..",
		"..####...",
		"...##....",
		"..####...",
		".##..##..",
		".#....#..",
		"........."),
	SEARCH(
		"..###....",
		".#...#...",
		"#..#..#..",
		"#.#...#..",
		"#.....#..",
		".#...#...",
		"..###.#..",
		".......#.",
		"........#"),
	FOLDER(
		".........",
		".###.....",
		".#..####.",
		".#.....#.",
		".#.....#.",
		".#.....#.",
		".#######.",
		".........",
		"........."),
	GEAR(
		".........",
		".#..#..#.",
		"..#####..",
		"..#####..",
		".###.###.",
		"..#####..",
		"..#####..",
		".#..#..#.",
		"........."),
	REFRESH(
		".#.###...",
		".##...##.",
		".###...#.",
		"........#",
		"#.......#",
		"#........",
		".#...###.",
		".##...##.",
		"...###.#."),
	DOWNLOAD(
		".........",
		"....#....",
		"....#....",
		"....#....",
		"....#....",
		"..#####..",
		"...###...",
		"....#....",
		"........."),
	UPLOAD(
		".........",
		"....#....",
		"...###...",
		"..#####..",
		"....#....",
		"....#....",
		"....#....",
		"....#....",
		"........."),
	PLUS(
		".........",
		"....#....",
		"....#....",
		"....#....",
		".#######.",
		"....#....",
		"....#....",
		"....#....",
		"........."),
	MINUS(
		".........",
		"...#.....",
		"....#....",
		".....#...",
		"....#....",
		"...#.....",
		".........",
		"..#####..",
		"........."),
	CHECK(
		".........",
		"......#..",
		".....##..",
		".....#...",
		".#..##...",
		".##.#....",
		"..###....",
		"...#.....",
		"........."),
	ARROW_LEFT(
		".........",
		".........",
		"...#.....",
		"..##.....",
		".#######.",
		"..##.....",
		"...#.....",
		".........",
		"........."),
	ARROW_RIGHT(
		".........",
		".........",
		".....#...",
		".....##..",
		".#######.",
		".....##..",
		".....#...",
		".........",
		"........."),
	CHEVRON_DOWN(
		".........",
		".........",
		".........",
		"..#####..",
		"...###...",
		"....#....",
		".........",
		".........",
		"........."),
	CHEVRON_UP(
		".........",
		".........",
		".........",
		"....#....",
		"...###...",
		"..#####..",
		".........",
		".........",
		"........."),
	TRASH(
		"..#####..",
		"#########",
		"##.....##",
		".#.#.#.#.",
		".#.#.#.#.",
		".#.#.#.#.",
		".#.#.#.#.",
		".#.....#.",
		"..#####.."),
	FILTER(
		"#########",
		"#.......#",
		"#.......#",
		".#.....#.",
		"..#...#..",
		"...#.#...",
		"...#.#...",
		"...#.#...",
		"...##...."),
	PUZZLE(
		"...##....",
		"###..###.",
		"#......#.",
		"#......#.",
		".#......#",
		".#......#",
		"#......#.",
		"#..##..#.",
		"###..###."),
	HOME(
		".........",
		"....#....",
		"...#.#...",
		"..#...#..",
		".#.....#.",
		".#.....#.",
		".#.....#.",
		".#######.",
		"........."),
	GLOBE(
		"...###...",
		".##.#.##.",
		".#.###.#.",
		"#...#...#",
		"#########",
		"#...#...#",
		".#.###.#.",
		".##.#.##.",
		"...###..."),
	ALERT(
		"....#....",
		"...#.#...",
		"...#.#...",
		"..#.#.#..",
		"..#.#.#..",
		".#..#..#.",
		".#.....#.",
		"#...#...#",
		"#########"),
	INFO(
		"...###...",
		".##...##.",
		".#..#..#.",
		"#.......#",
		"#..##...#",
		"#...#...#",
		".#.###.#.",
		".##...##.",
		"...###..."),
	SHARE(
		".........",
		".........",
		".###.###.",
		"#.......#",
		"#..###..#",
		"#.......#",
		".###.###.",
		".........",
		"........."),
	PENCIL(
		".........",
		"...#.....",
		"..#.#....",
		".#.#.#...",
		"..#...#..",
		"...#...#.",
		"....#.##.",
		".....###.",
		"........."),
	FILE(
		".........",
		".####....",
		".#..##...",
		".#...##..",
		".#....#..",
		".#....#..",
		".#....#..",
		".######..",
		"........."),
	PIN(
		".........",
		"...###...",
		"..#...#..",
		"..#.#.#..",
		"..#...#..",
		"...#.#...",
		"...#.#...",
		"....#....",
		"........."),
	CAMERA(
		"..##.....",
		".#######.",
		"#.......#",
		"##.###..#",
		"#..#.#..#",
		"#..###..#",
		"#.......#",
		".#######.",
		"........."),
	CUBE(
		"..#######",
		".#.....##",
		"#.....#.#",
		"######..#",
		"#....#..#",
		"#....#..#",
		"#....#..#",
		"#....#.#.",
		"#######.."),
	LIST(
		".........",
		".........",
		".######..",
		".........",
		".######..",
		".........",
		".######..",
		".........",
		"........."),
	LINK(
		".........",
		"....###..",
		"...#...#.",
		"....#..#.",
		"...#.#...",
		".#..#....",
		".#...#...",
		"..###....",
		"........."),
	CLIPBOARD(
		"..####...",
		"..#...#..",
		"#.#....#.",
		"#.#....#.",
		"#.#....#.",
		"#.#....#.",
		"#.######.",
		"#........",
		"######..."),
	EXTERNAL(
		"...###...",
		".##.#.##.",
		".#.###.#.",
		"#...#...#",
		"#########",
		"#...#...#",
		".#.###.#.",
		".##.#.##.",
		"...###...");

	public static final int SIZE = 9;
	private static final int SIZE_HINT = 9;
	public static final int TEXT_OFFSET = -1;
	private static final int BASELINE_ROW = 7;

	private final String[] rows;
	private final int inkLeft;
	private final int inkWidth;
	private final int inkTop;
	private final int inkHeight;

	UIIcon(String... rows) {
		this.rows = rows;
		int left = SIZE_HINT;
		int right = -1;
		int top = SIZE_HINT;
		int bottom = -1;
		for (int r = 0; r < rows.length; r++) {
			String row = rows[r];
			for (int col = 0; col < row.length(); col++) {
				if (row.charAt(col) == '#') {
					left = Math.min(left, col);
					right = Math.max(right, col);
					top = Math.min(top, r);
					bottom = Math.max(bottom, r);
				}
			}
		}
		this.inkLeft = right < 0 ? 0 : left;
		this.inkWidth = right < 0 ? 0 : right - left + 1;
		this.inkTop = bottom < 0 ? 0 : top;
		this.inkHeight = bottom < 0 ? 0 : bottom - top + 1;
	}

	public int inkLeft() {
		return inkLeft;
	}

	public int inkWidth() {
		return inkWidth;
	}

	public void draw(GuiGraphicsExtractor ctx, int x, int y, int color) {
		draw(ctx, x, y, color, true);
	}

	public void draw(GuiGraphicsExtractor ctx, int x, int y, int color, boolean shadow) {
		if (shadow) paint(ctx, x + 1, y + 1, UIDraw.shadowColor(color));
		paint(ctx, x, y, color);
	}

	public void drawOnText(GuiGraphicsExtractor ctx, int x, int textY, int color) {
		int centeredTop = (SIZE - inkHeight + 1) / 2;
		if (inkHeight % 2 == 0 && centeredTop + inkHeight - 1 > BASELINE_ROW) centeredTop--;
		draw(ctx, x, textY + TEXT_OFFSET + centeredTop - inkTop, color);
	}

	public void drawCenteredIn(GuiGraphicsExtractor ctx, int x, int y, int width, int height, int color) {
		draw(ctx, x + (width - inkWidth) / 2 - inkLeft, y + (height - inkHeight) / 2 - inkTop, color);
	}

	public void drawCentered(GuiGraphicsExtractor ctx, int centerX, int centerY, int color) {
		draw(ctx, centerX - inkWidth / 2 - inkLeft, centerY - inkHeight / 2 - inkTop, color);
	}

	private void paint(GuiGraphicsExtractor ctx, int x, int y, int color) {
		for (int row = 0; row < rows.length; row++) {
			String line = rows[row];
			int start = -1;
			for (int col = 0; col <= line.length(); col++) {
				boolean on = col < line.length() && line.charAt(col) == '#';
				if (on && start < 0) start = col;
				if (!on && start >= 0) {
					ctx.fill(x + start, y + row, x + col, y + row + 1, color);
					start = -1;
				}
			}
		}
	}
}
