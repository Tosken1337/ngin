package com.tosken.ngin.application;

import com.tosken.ngin.gl.FrameBufferObject;
import com.tosken.ngin.oculus.OculusHmd;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.ovr.OVRRecti;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengles.GLES20.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengles.GLES20.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public abstract class RiftApplication extends Application {

    protected OculusHmd hmd;
    private long window;

    protected RiftApplication() {
    }

    public final void run(final Configuration configuration) {
        try {
            // Basic window and application callback setup
            initApplication(configuration);

            // Let the implementing class do it's initialization tasks
            onInitApplication();

            // Start gl loop
            glLoop();

            onCloseApplication();

            hmd.destroy();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void glLoop() {
        GL.createCapabilities();

        log.info("OpenGL version: {}", GL11.glGetString(GL11.GL_VERSION));
        log.info("Vendor: {}", GL11.glGetString(GL11.GL_VENDOR));

        hmd.initGL();

        onInitGL();

        final FrameBufferObject mirrorTextureFbo = hmd.getMirrorTexture(hmd.getResolutionW(), hmd.getResolutionH());

        GL11.glEnable(GL30.GL_FRAMEBUFFER_SRGB);

        glfwSetTime(0);
        double lastTime = 0;
        boolean isVisible = true;
        while (!glfwWindowShouldClose(window)) {
            if (!hmd.update()) {
                //continue;
            }

            if (isVisible) {
                final double currentTime = glfwGetTime();
                final double elapsedMillis = (currentTime - lastTime) * 1000;
                lastTime = currentTime;

                executeGlActions();

                final FrameBufferObject currentFrameBuffer = hmd.getCurrentFrameBuffer();
                currentFrameBuffer.bind();
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                currentFrameBuffer.unbind();

                // Let the application perform application updates per frame (physics, input, ...) (once per eye)
                onUpdateFrame(elapsedMillis);

                for(int eye = 0; eye < 2; eye++) {
                    // Let the application perform frame rendering for each eye
                    final OVRRecti viewport = hmd.getViewport(eye);
                    final Matrix4f projM = hmd.getProjectionMatrix(eye);
                    final Matrix4f viewM = hmd.getViewMatrix(eye);

                    GL11.glViewport(viewport.Pos().x(), viewport.Pos().y(), viewport.Size().w(), viewport.Size().h());
                    onRenderFrame(elapsedMillis, viewM, projM, currentFrameBuffer);
                }
                hmd.commitFrame();
            }

            isVisible = hmd.endFrame();


            // Blit mirror texture
            mirrorTextureFbo.bind();

            GL30.glBlitFramebuffer(0, hmd.getResolutionH(), hmd.getResolutionW(), 0, 0, 0, hmd.getResolutionW(), hmd.getResolutionH(), GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST);
            mirrorTextureFbo.unbind();

            // Swap the back buffer
            glfwSwapBuffers(window);

            // Poll for input events which will be handled in the next update
            glfwPollEvents();
        }
    }

    private void initApplication(Configuration configuration) {
        hmd = new OculusHmd();
        try {
            hmd.init();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize hmd", e);
        }

        // Init glfw and a window for displaying the rift texture also on the monitor
        GLFWErrorCallback errorCallback = new GLFWErrorCallback() {
            @Override
            public void invoke(final int error, final long description) {
                log.error("glfw error occured. Code: {} - Description: {}", error, description);
            }
        };
        glfwSetErrorCallback(errorCallback);
        org.lwjgl.system.Configuration.DEBUG.set(false);

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Setup window related stuff (non visible dummy window for gl context creation)
        glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, 1);


        // Create the window
        window = glfwCreateWindow(hmd.getResolutionW(), hmd.getResolutionH(), "Rift", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true);
            } else if (key != GLFW_KEY_ESCAPE) {
                onKeyEvent(action, key);
            }
        });

        // Get the resolution of the primary monitor
        GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(
                window,
                (videoMode.width() - hmd.getResolutionW() / 2) / 2,
                (videoMode.height() - hmd.getResolutionH()) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Turn off vsync on oculus devices
        glfwSwapInterval(0);
    }

    protected abstract void onRenderFrame(double elapsedMillis, Matrix4f eyeViewM, Matrix4f projM, FrameBufferObject currentFrameBuffer);


    public static class Configuration {
    }
}
