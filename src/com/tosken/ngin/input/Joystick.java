package com.tosken.ngin.input;

import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;

/**
 * Created by Sebastian Greif on 26.07.2016.
 * Copyright di support 2016
 */
public class Joystick {
    public void poll() {
        final ByteBuffer buttonState = GLFW.glfwGetJoystickButtons(GLFW.GLFW_JOYSTICK_1);
        //buttonState.asIntBuffer().
    }
}
