package com.tosken.photoviewer.rendering;

import com.tosken.ngin.geometry.object.Quad;
import com.tosken.ngin.gl.Shader;
import com.tosken.ngin.gl.ShaderProgram;
import com.tosken.ngin.gl.Texture;
import com.tosken.photoviewer.rendering.camera.MovingCamera;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Sebastian Greif on 01.08.2016.
 * Copyright di support 2016
 */
public class DebugRenderer implements PhotoRenderer {
    private MovingCamera camera;

    private Matrix4f projM;

    private Quad quadObject;

    private ShaderProgram shaderProgram;
    private Texture texture;

    public DebugRenderer() {
    }

    @Override
    public void initGl() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(96 / 255f, 125 / 255f, 139 / 255f, 1f);

        camera = new MovingCamera(new Vector3f(0, 0, -2), new Vector3f(0, 0, 0));

        quadObject = new Quad();
        quadObject.init();


        try {

            texture = Texture.loadTexture("texture.jpg", true);

            // Load shader
            final Shader vert = Shader.createFromFile("/com/tosken/photoviewer/resources/simpleVert.glsl", Shader.Type.Vertex);
            final Shader frag = Shader.createFromFile("/com/tosken/photoviewer/resources/simpleFrag.glsl", Shader.Type.Fragment);
            shaderProgram = ShaderProgram.create(Arrays.asList(vert, frag));
            shaderProgram.createUniform("viewMat");
            shaderProgram.createUniform("projectionMat");
            shaderProgram.createUniform("tex");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(final double elapsedMillis) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        camera.update((float)elapsedMillis);

        shaderProgram.bind();
        shaderProgram.setUniform("viewMat", camera.viewMatrix(new Matrix4f().identity()));
        shaderProgram.setUniform("projectionMat", projM);
        shaderProgram.setUniform("tex", 0);

        texture.bind();

        quadObject.getVao().bind();
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, quadObject.getVboIndices().getId());

        GL11.glDrawElements(GL11.GL_TRIANGLES, quadObject.getVboIndices().size(), GL11.GL_UNSIGNED_INT, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        texture.unbind();
        quadObject.getVao().unbind();
        shaderProgram.unbind();
    }

    @Override
    public void onFrameBufferSizeChanged(final Vector2i frameBufferSize) {
        GL11.glViewport(0, 0, frameBufferSize.x, frameBufferSize.y);
        projM = new Matrix4f().perspective(((float) Math.toRadians(50.0f)), (float)frameBufferSize.x / (float)frameBufferSize.y, 0.1f, 100f);
    }
}
