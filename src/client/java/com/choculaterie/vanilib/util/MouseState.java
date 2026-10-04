package com.choculaterie.vanilib.util;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.sdl.SDLMouse;

public final class MouseState {

	public static boolean isButtonDown(int button) {
		if (button < InputConstants.MOUSE_BUTTON_LEFT || button > 32) {
			return false;
		}
		int flags = SDLMouse.SDL_GetMouseState(null, null);
		return (flags & (1 << (button - 1))) != 0;
	}

	public static boolean isLeftDown() {
		return isButtonDown(InputConstants.MOUSE_BUTTON_LEFT);
	}

	private MouseState() {}
}
