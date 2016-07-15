package com.tosken.ngin.gl;

import org.lwjgl.opengl.GL11;

/**
 * Lwjgl
 * User: Sebastian
 * Date: 16.07.2015
 * Time: 21:21
 */
public final class GLHelper {
    private GLHelper() {
    }

    public static void checkAndThrow() {
        int error = GL11.glGetError();
        if (error != 0) {
            //String errorMessage = GLContext.translateGLErrorString(error);
            throw new RuntimeException("" + error);
        }
    }
}
