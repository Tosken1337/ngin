package com.tosken.photoviewer.model;

import com.tosken.ngin.gl.Texture;
import com.tosken.photoviewer.io.image.PhotoLibraryResourceManager;
import org.junit.Test;
import org.lwjgl.BufferUtils;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

/**
 * ngin
 * User: Sebastian
 * Date: 30.07.2016
 * Time: 18:27
 */
public class PhotoLibraryTest {
    @Test
    public void scan() throws Exception {
        SimplePhotoLibrary library = new SimplePhotoLibrary(Paths.get("photos"));
        library.scan();

        PhotoLibraryResourceManager resourceManager = new PhotoLibraryResourceManager(library, this::loadTexture);
        resourceManager.loadExifData()
                .subscribe(photoTextureResource -> {
                    System.out.println(photoTextureResource.photo);
                });
    }

    public Texture loadTexture(byte[] imageData, Dimension size) {
        final ByteBuffer buffer = BufferUtils.createByteBuffer(imageData.length);
        buffer.put(imageData);
        buffer.flip();
        return new Texture(size.width, size.height, buffer);
    }

}