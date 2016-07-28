package com.tosken.ngin.input;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

/**
 * Created by Sebastian Greif on 26.07.2016.
 * Copyright di support 2016
 */
public class Keyboard {
    private boolean keyStates[] = new boolean[GLFW_KEY_LAST];


    public void onKeyEvent(final int action, final int key) {
        keyStates[key] = action == GLFW_PRESS ? true : false;
    }

    public boolean isPressed(final int key) {
        return keyStates[key];
    }
}
