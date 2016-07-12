package com.disupport.test.lwjgl;

import com.tosken.ngin.geometry.Geometry;
import com.tosken.ngin.geometry.IndexedGeometry;
import com.tosken.ngin.geometry.loader.GeometryLoaderFactory;
import com.tosken.ngin.gl.Shader;
import com.tosken.ngin.gl.ShaderProgram;
import com.tosken.ngin.gl.VertexArrayObject;
import com.tosken.ngin.gl.VertexBufferObject;
import org.joml.Matrix4f;
import org.joml.camera.ArcBallCamera;
import org.lwjgl.BufferUtils;
import org.lwjgl.Version;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengles.GLES20.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengles.GLES20.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // The window handle
    private long window;
    private ShaderProgram prog;
    private VertexArrayObject vao;
    private Matrix4f viewMat;
    private Matrix4f projMat;
    private GLFWFramebufferSizeCallback fbCallback;
    private int width;
    private int height;
    private GLFWCursorPosCallback cpCallback;
    private int x;
    private int y;
    private GLFWMouseButtonCallback mbCallback;
    private boolean down;
    private int mouseX;
    private int mouseY;
    private GLFWScrollCallback sCallback;
    private float zoom = 10;
    private ArcBallCamera cam;
    private VertexBufferObject vboIndices;
    private VertexBufferObject vbo;

    public void run() {

        try {
            init();
            loop();

            // Free the window callbacks and destroy the window
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        } finally {
            // Terminate GLFW and free the error callback
            glfwTerminate();
            glfwSetErrorCallback(null).free();
        }
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_SAMPLES, 4);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, 1);

        int WIDTH = 1024;
        int HEIGHT = 768;

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in our rendering loop
        });

        glfwSetFramebufferSizeCallback(window, fbCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                if (w > 0 && h > 0) {
                    width = w;
                    height = h;
                }
            }
        });
        glfwSetCursorPosCallback(window, cpCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                x = (int) xpos - width / 2;
                y = height / 2 - (int) ypos;
            }
        });
        glfwSetMouseButtonCallback(window, mbCallback = new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if (action == GLFW_PRESS) {
                    down = true;
                    mouseX = x;
                    mouseY = y;
                } else if (action == GLFW_RELEASE) {
                    down = false;
                }
            }
        });
        glfwSetScrollCallback(window, sCallback = new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                if (yoffset > 0) {
                    zoom /= 1.1f;
                } else {
                    zoom *= 1.1f;
                }
            }
        });

        // Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (vidmode.width() - WIDTH) / 2,
                (vidmode.height() - HEIGHT) / 2
        );

        //glfwSetWindowUserPointer(window, this);

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();

        System.out.println("OpenGL: " + GL11.glGetString(GL11.GL_VERSION));
        System.out.println("Vendor: " + GL11.glGetString(GL11.GL_VENDOR));

        initResources();

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        projMat = new Matrix4f().perspective(((float) Math.toRadians(50.0f)), 1024f / 768f, 0.1f, 100f);
        viewMat = new Matrix4f().lookAt(0.0f, 0.0f, 2.0f,
                        0.0f, 0.0f, 0.0f,
                        0.0f, 1.0f, 0.0f);

        try {
            prog.createUniform("viewMat");
            prog.createUniform("projectionMat");
        } catch (Exception e) {
            e.printStackTrace();
        }

        GL11.glViewport(0, 0, 1024, 768);
        
        long lastTime = System.nanoTime();

        cam = new ArcBallCamera();
        cam.setAlpha((float) Math.toRadians(0));
        cam.setBeta((float) Math.toRadians(0));
        while ( !glfwWindowShouldClose(window) ) {
            if (down) {
                cam.setAlpha(cam.getAlpha() + Math.toRadians((x - mouseX) * 0.1f));
                cam.setBeta(cam.getBeta() + Math.toRadians((mouseY - y) * 0.1f));
                mouseX = x;
                mouseY = y;
            }
            cam.zoom(zoom);

            /* Compute delta time */
            long thisTime = System.nanoTime();
            float diff = (float) ((thisTime - lastTime) / 1E9);
            lastTime = thisTime;
            /* And let the camera make its update */
            cam.update(diff);

            final Matrix4f matrix4f = cam.viewMatrix(viewMat.identity());

            GL11.glEnable(GL_DEPTH_TEST);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            prog.bind();
            prog.setUniform("viewMat", matrix4f);
            prog.setUniform("projectionMat", projMat);

            vao.bind();
            GL20.glEnableVertexAttribArray(0);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndices.getId());

            //GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
            GL11.glDrawElements(GL11.GL_TRIANGLES, vboIndices.size(), GL11.GL_UNSIGNED_INT, 0);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

            vao.unbind();
            prog.unbind();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void initResources() {
        try {
            final Shader vert = Shader.createFromFile("/com/disupport/test/lwjgl/resources/simpleVert.glsl", Shader.Type.Vertex);
            final Shader frag = Shader.createFromFile("/com/disupport/test/lwjgl/resources/simpleFrag.glsl", Shader.Type.Fragment);
            prog = ShaderProgram.create(Arrays.asList(vert, frag));

            float[] vertices = {
                    -0.5f, 0.5f, 0f,    // Left top         ID: 0
                    -0.5f, -0.5f, 0f,   // Left bottom      ID: 1
                    0.5f, -0.5f, 0f,    // Right bottom     ID: 2
                    0.5f, 0.5f, 0f  // Right left       ID: 3
            };

            int[] indices = {
                    // Left bottom triangle
                    0, 1, 2,
                    // Right top triangle
                    2, 3, 0
            };

            final IndexedGeometry model = ((IndexedGeometry) GeometryLoaderFactory.create(GeometryLoaderFactory.Format.OBJ).load(Paths.get("cube.obj")));


            vbo = VertexBufferObject.from(model.vertexData());
            vboIndices = VertexBufferObject.fromIndices(model.indexData());
            //vbo = VertexBufferObject.from(vertices);
            //vboIndices = VertexBufferObject.fromIndices(indices);
            final Map<VertexArrayObject.VertexAttribBinding, VertexBufferObject> binding = new HashMap() {
                {
                    put(new VertexArrayObject.VertexAttribBinding(0, vbo.size(), 0), vbo);
                }
            };

            vao = VertexArrayObject.create(binding);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
