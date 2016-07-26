package com.tosken.ngin.application;

import com.tosken.ngin.gl.FrameBufferObject;
import com.tosken.ngin.oculus.OculusHmd;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
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

        GL11.glEnable(GL30.GL_FRAMEBUFFER_SRGB);

        Matrix4f viewM = new Matrix4f();
        Matrix4f projM = new Matrix4f();

        glfwSetTime(0);
        double lastTime = 0;
        while (!glfwWindowShouldClose(window)) {
            if (!hmd.update()) {
                return;
            }

            final double currentTime = glfwGetTime();
            final double elapsedMillis = (currentTime - lastTime) * 1000;
            lastTime = currentTime;

            // Let the application perform application updates per frame (physics, input, ...) (once per eye)
            onUpdateFrame(elapsedMillis);

            //@TODO call those for each eye with different matrices (hmd.getView(lefteye), ...)
            for(int eye = 0; eye < 2; eye++) {
                // Let the application perform frame rendering for each eye
                onRenderFrame(elapsedMillis, viewM, projM, hmd.getCurrentFrameBuffer());
            }

            hmd.endFrame();

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
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, 1);


        // Create the window
        window = glfwCreateWindow(hmd.getResolutionW() / 2, hmd.getResolutionH(), "Rift", NULL, NULL);
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
    }

    protected abstract void onInitApplication();

    protected abstract void onInitGL();

    protected abstract void onUpdateFrame(double elapsedMillis);

    protected abstract void onRenderFrame(double elapsedMillis, Matrix4f eyeViewM, Matrix4f projM, FrameBufferObject currentFrameBuffer);

    protected abstract void onCloseApplication();

    protected abstract void onKeyEvent(final int action, final int key);


    public static class Configuration {
    }
}
