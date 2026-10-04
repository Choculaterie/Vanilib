package com.choculaterie.vanilib.gui.widget;

import com.mojang.blaze3d.platform.InputConstants;
import com.choculaterie.vanilib.util.MouseState;

public class KeyboardHelper {

    public KeyboardHelper(long windowHandle) {
    }

    public boolean isKeyPressed(int key) {
        return InputConstants.isKeyDown(key);
    }

    public boolean isCtrlHeld() {
        return isKeyPressed(InputConstants.KEY_LCONTROL) || isKeyPressed(InputConstants.KEY_RCONTROL);
    }

    public boolean isShiftHeld() {
        return isKeyPressed(InputConstants.KEY_LSHIFT) || isKeyPressed(InputConstants.KEY_RSHIFT);
    }

    public boolean isAltHeld() {
        return isKeyPressed(InputConstants.KEY_LALT) || isKeyPressed(InputConstants.KEY_RALT);
    }

    public boolean isMouseButtonPressed(int button) {
        return MouseState.isButtonDown(button);
    }

    public boolean isLeftMousePressed() {
        return isMouseButtonPressed(InputConstants.MOUSE_BUTTON_LEFT);
    }

    public boolean isRightMousePressed() {
        return isMouseButtonPressed(InputConstants.MOUSE_BUTTON_RIGHT);
    }
}
