package com.tosken.photoviewer.rendering;

import com.tosken.ngin.application.Application;
import com.tosken.ngin.geometry.object.Quad;
import com.tosken.ngin.gl.Shader;
import com.tosken.ngin.gl.ShaderProgram;
import com.tosken.ngin.gl.Texture;
import com.tosken.photoviewer.io.image.PhotoLibraryResourceManager;
import com.tosken.photoviewer.model.Photo;
import com.tosken.photoviewer.model.PhotoLibrary;
import com.tosken.photoviewer.rendering.camera.MovingCamera;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;

/**
 * Created by Sebastian Greif on 01.08.2016.
 * Copyright di support 2016
 */
public class DebugRenderer implements PhotoRenderer {
    private static final Logger log = LoggerFactory.getLogger(DebugRenderer.class);

    private PhotoLibrary library;

    private PhotoLibraryResourceManager libraryResourceManager;

    private Map<Photo, Texture> textureMap = new HashMap<>();

    private MovingCamera camera;

    private Matrix4f projM;

    private Quad quadObject;
    private ShaderProgram shaderProgram;

    private Texture texture;

    public DebugRenderer() {
    }

    @Override
    public void setLibrary(final PhotoLibrary library) {
        this.library = library;
        libraryResourceManager = new PhotoLibraryResourceManager(library);
    }

    @Override
    public void initGl() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(96 / 255f, 125 / 255f, 139 / 255f, 1f);

        camera = new MovingCamera(new Vector3f(0, 0, -10), new Vector3f(0, 0, 0));

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

        libraryResourceManager.loadExifData()
                .subscribeOn(Schedulers.io())
                .observeOn(Application.getApplication().getGlScheduler())
                .subscribe(new Action1<PhotoLibraryResourceManager.PhotoExifResource>() {
                    @Override
                    public void call(final PhotoLibraryResourceManager.PhotoExifResource photoExifResource) {
                        if (photoExifResource.exifThumb.isPresent()) {
                            Texture texture = Texture.loadTexture(photoExifResource.exifThumb.get().toAbsolutePath().toString(), true);
                            textureMap.putIfAbsent(photoExifResource.photo, texture);
                        }
                    }
                });

    }

    @Override
    public void render(final double elapsedMillis) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        camera.update((float)elapsedMillis);

        shaderProgram.bind();
        //shaderProgram.setUniform("viewMat", camera.viewMatrix(new Matrix4f().identity()));
        shaderProgram.setUniform("projectionMat", projM);
        shaderProgram.setUniform("tex", 0);

        final List<Photo> photos = library.photoList();
        final int numPhotos = photos.size();
        final int visiblePhotos = textureMap.size();

        int currentVisiblePhoto = 0;
        int row = 0;
        int coloumn = 0;
        for (final Map.Entry<Photo, Texture> photoEntry : textureMap.entrySet()) {
            float xOffset = (float)coloumn++ * 1.1f;
            if (coloumn > 2) {
                coloumn = 0;
                row++;
            }
            float yOffset = row * 1.1f;
            shaderProgram.setUniform("viewMat", camera.viewMatrix(new Matrix4f().identity().translate(-xOffset, yOffset, 0)));

            final Texture texture = photoEntry.getValue();
            texture.bind();

            quadObject.getVao().bind();
            GL20.glEnableVertexAttribArray(0);
            GL20.glEnableVertexAttribArray(1);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, quadObject.getVboIndices().getId());

            GL11.glDrawElements(GL11.GL_TRIANGLES, quadObject.getVboIndices().size(), GL11.GL_UNSIGNED_INT, 0);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

            texture.unbind();
            quadObject.getVao().unbind();

            currentVisiblePhoto++;
        }


        shaderProgram.unbind();

        /*texture.bind();

        quadObject.getVao().bind();
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, quadObject.getVboIndices().getId());

        GL11.glDrawElements(GL11.GL_TRIANGLES, quadObject.getVboIndices().size(), GL11.GL_UNSIGNED_INT, 0);

        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        texture.unbind();
        quadObject.getVao().unbind();
        shaderProgram.unbind();*/
    }

    @Override
    public void onFrameBufferSizeChanged(final Vector2i frameBufferSize) {
        GL11.glViewport(0, 0, frameBufferSize.x, frameBufferSize.y);
        projM = new Matrix4f().perspective(((float) Math.toRadians(50.0f)), (float)frameBufferSize.x / (float)frameBufferSize.y, 0.1f, 100f);
    }
}
