package com.tosken.photoviewer.rendering;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Sebastian Greif on 01.08.2016.
 * Copyright di support 2016
 */
public class DebugRenderer implements PhotoRenderer {
    private Matrix4f viewM;

    private Matrix4f projM;

    public DebugRenderer() {
    }

    @Override
    public void initGl() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(96 / 255f, 125 / 255f, 139 / 255f, 1f);
    }

    @Override
    public void render(final double elapsedMillis) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void onFrameBufferSizeChanged(final Vector2i frameBufferSize) {
        GL11.glViewport(0, 0, frameBufferSize.x, frameBufferSize.y);
        projM = new Matrix4f().perspective(((float) Math.toRadians(50.0f)), (float)frameBufferSize.x / (float)frameBufferSize.y, 0.1f, 100f);
        /*viewM = new Matrix4f().lookAt(0.0f, 0.0f, 2.0f,
                    0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f);*/
    }
}
