package com.disupport.test.lwjgl.apptest;

import com.tosken.ngin.application.DesktopApplication;
import com.tosken.ngin.gl.*;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengles.GLES20.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengles.GLES20.GL_DEPTH_BUFFER_BIT;

/**
 * Created by Sebastian Greif on 25.07.2016.
 * Copyright di support 2016
 */
public class SampleDesktopApplication extends DesktopApplication {

    private ShaderProgram prog;
    private VertexArrayObject vao;
    private Matrix4f viewMat;
    private Matrix4f projMat;
    private VertexBufferObject vboIndices;
    private VertexBufferObject vbo;
    private Texture texture;

    @Override
    protected void onUpdateFrame(final double elapsedMillis) {

    }

    @Override
    protected void onRenderFrame(final double elapsedMillis) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        prog.bind();
        prog.setUniform("viewMat", viewMat);
        prog.setUniform("projectionMat", projMat);
        prog.setUniform("tex", 0);

        texture.bind();

        vao.bind();
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndices.getId());

        //GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
        GL11.glDrawElements(GL11.GL_TRIANGLES, vboIndices.size(), GL11.GL_UNSIGNED_INT, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        vao.unbind();
        prog.unbind();
    }

    @Override
    protected void onInitApplication() {
        log.info("onInitApplication");
    }

    @Override
    protected void onInitGL() {
        log.info("onInitGL");
        GL11.glClearColor(80f / 255f, 140f / 255f, 164f / 255f, 1f);
        GL11.glEnable(GL_DEPTH_TEST);

        try {
            // Load shader
            final Shader vert = Shader.createFromFile("/com/disupport/test/lwjgl/resources/simpleVert.glsl", Shader.Type.Vertex);
            final Shader frag = Shader.createFromFile("/com/disupport/test/lwjgl/resources/simpleFrag.glsl", Shader.Type.Fragment);
            prog = ShaderProgram.create(Arrays.asList(vert, frag));

            try {
                prog.createUniform("viewMat");
                prog.createUniform("projectionMat");
                prog.createUniform("tex");
            } catch (Exception e) {
                e.printStackTrace();
            }

            texture = Texture.loadTexture("texture.jpg", GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR ,GL11.GL_REPEAT, true);


            // Create vao from buffer
            float[] vertices = {
                    -0.5f, 0.5f, 0f,    // Left top         ID: 0
                    -0.5f, -0.5f, 0f,   // Left bottom      ID: 1
                    0.5f, -0.5f, 0f,    // Right bottom     ID: 2
                    0.5f, 0.5f, 0f  // Right top       ID: 3
            };

            float[] texCoords = {
                    0, 1,
                    0, 0,
                    1, 0,
                    1, 1,
            };

            int[] indices = {
                    // Left bottom triangle
                    0, 1, 2,
                    // Right top triangle
                    2, 3, 0
            };


            final int numVertices = vertices.length / 3;
            final int vertexFloatCount = 3;
            final int texCoordFloatCount = 2;
            final FloatBuffer bufferData = FloatBuffer.allocate(vertices.length + texCoords.length);
            for (int i = 0; i < numVertices; i++) {
                bufferData.put(vertices, i * 3, vertexFloatCount);
                bufferData.put(texCoords, i * 2, texCoordFloatCount);
            }
            bufferData.flip();
            vbo = VertexBufferObject.from(bufferData.array());
            vboIndices = VertexBufferObject.fromIndices(indices);
            final Map<VertexArrayObject.VertexAttribBinding, VertexBufferObject> binding = new HashMap<VertexArrayObject.VertexAttribBinding, VertexBufferObject>() {
                {
                    put(new VertexArrayObject.VertexAttribBinding(0, vertexFloatCount,      vertexFloatCount * 4 + texCoordFloatCount * 4,      0), vbo);
                    put(new VertexArrayObject.VertexAttribBinding(1, texCoordFloatCount,    vertexFloatCount * 4 + texCoordFloatCount * 4,    vertexFloatCount * 4), vbo);
                }
            };
            vao = VertexArrayObject.create(binding);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFrameBufferSizeChanged(final Vector2i frameBufferSize) {
        if (frameBufferSize.x > 0) {
            GL11.glViewport(0, 0, frameBufferSize.x, frameBufferSize.y);

            projMat = new Matrix4f().perspective(((float) Math.toRadians(50.0f)), (float)frameBufferSize.x / (float)frameBufferSize.y, 0.1f, 100f);
            viewMat = new Matrix4f().lookAt(0.0f, 0.0f, 2.0f,
                    0.0f, 0.0f, 0.0f,
                    0.0f, 1.0f, 0.0f);
        }
    }
}
