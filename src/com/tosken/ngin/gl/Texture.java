package com.tosken.ngin.gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_CLAMP_TO_BORDER;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * This class represents a texture.
 */
public class Texture implements GLResource {

    /**
     * Stores the handle of the texture.
     */
    private int id;

    /**
     * Width of the texture.
     */
    private int width;
    /**
     * Height of the texture.
     */
    private int height;

    private Texture() {
        id = 0;
        width = -1;
        height = -1;
    }

    /**
     * Creates a texture with specified width, height and data.
     *
     * @param width Width of the texture
     * @param height Height of the texture
     * @param data Picture Data in RGBA format
     */
    public Texture(int width, int height, ByteBuffer data) {
        this(width, height, GL_RGBA8, data, GL_LINEAR, GL_LINEAR, GL_CLAMP, false);
    }

    public Texture(int width, int height, ByteBuffer data, int minFilterMode, int magFilterMode, int wrapMode, boolean mipmap) {
        this(width, height, GL_RGBA8, data, minFilterMode, magFilterMode, wrapMode, mipmap);
    }

    public Texture(int width, int height, int format) {
        id = glGenTextures();
        this.width = width;
        this.height = height;

        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format == GL_RGBA8 ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, 0);
        glBindTexture(GL_TEXTURE_2D, 0);

        GLHelper.checkAndThrow();
    }

    /**
     *
     * @param width
     * @param height
     * @param format    Either GL_RGBA8 or GL_RGB
     * @param data
     */
    public Texture(int width, int height, int format, ByteBuffer data, int minFilterMode, int magFilterMode, int wrapMode, boolean mipmap) {
        id = glGenTextures();
        this.width = width;
        this.height = height;

        glBindTexture(GL_TEXTURE_2D, id);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilterMode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilterMode);

        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format == GL_RGBA8 ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, data);

        if (mipmap) {
            GL30.glGenerateMipmap(GL_TEXTURE_2D);
        }

        glBindTexture(GL_TEXTURE_2D, 0);

        GLHelper.checkAndThrow();
    }


    public void setParameters(final int filterMode, final int wrapMode) {
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapMode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapMode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, filterMode);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, filterMode);
        glBindTexture(GL_TEXTURE_2D, 0);
    }



    /**
     * Binds the texture.
     */
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    /**
     * Delete the texture.
     */
    public void delete() {
        glDeleteTextures(id);
    }

    /**
     * Gets the texture width.
     *
     * @return Texture width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the texture height.
     *
     * @return Texture height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Wraps an existing texture generated with gl functions
     * @param textureId
     * @param width
     * @param height
     * @return
     */
    public static Texture wrap(final int textureId, final int width, final int height) {
        final Texture tex = new Texture();
        tex.id = textureId;
        tex.width = width;
        tex.height = height;

        return tex;
    }

    public static Texture loadTexture(final String path, final boolean mipmap) {
        return loadTexture(path, GL_LINEAR, GL_LINEAR, GL_REPEAT, mipmap);
    }

    public static Texture loadTexture(final String path, final TextureAttributes attributes, final boolean mipmap) {
        return loadTexture(path, attributes.getFilterMode(), attributes.getFilterMode(), attributes.getWrapMode(), mipmap);
    }

    /**
     * Load texture from file.
     *
     * @param path File path of the texture
     * @return Texture from specified file
     */
    public static Texture loadTexture(String path, int minFilterMode, int magFilterMode, int wrapMode, final boolean mipmap) {
        BufferedImage image = null;
        try {
            InputStream in = new FileInputStream(path);
            image = ImageIO.read(in);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to load a texture file!"
                    + System.lineSeparator() + ex.getMessage());
        }
        if (image != null) {
            /* Flip image Horizontal to get the origin to bottom left */
            AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
            transform.translate(0, -image.getHeight());
            AffineTransformOp operation = new AffineTransformOp(transform,
                    AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = operation.filter(image, null);

            /* Get width and height of image */
            int width = image.getWidth();
            int height = image.getHeight();

            /* Get pixel data of image */
            int[] pixels = new int[width * height];
            image.getRGB(0, 0, width, height, pixels, 0, width);

            /* Put pixel data into a ByteBuffer */
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    /* Pixel as RGBA: 0xAARRGGBB */
                    int pixel = pixels[y * width + x];
                    /* Red component 0xAARRGGBB >> 16 = 0x0000AARR */
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    /* Green component 0xAARRGGBB >> 8 = 0x00AARRGG */
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    /* Blue component 0xAARRGGBB >> 0 = 0xAARRGGBB */
                    buffer.put((byte) (pixel & 0xFF));
                    /* Alpha component 0xAARRGGBB >> 24 = 0x000000AA */
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }
            /* Do not forget to flip the buffer! */
            buffer.flip();

            return new Texture(width, height, buffer, minFilterMode, magFilterMode, wrapMode, mipmap);
        } else {
            throw new RuntimeException("File extension not supported!"
                    + System.lineSeparator() + "The following file extensions "
                    + "are supported: "
                    + Arrays.toString(ImageIO.getReaderFileSuffixes()));
        }
    }

    public int getId() {
        return id;
    }

    /**
     * Binds the texture to the given texture unit.
     * @param textureUnit
     */
    public void bind(int textureUnit) {
        glActiveTexture(GL13.GL_TEXTURE0 + textureUnit);
        bind();
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public void free() {
        glDeleteTextures(id);
    }


}
