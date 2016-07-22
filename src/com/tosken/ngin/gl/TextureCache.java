package com.tosken.ngin.gl;

import com.tosken.ngin.util.ResourceUtil;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Lwjgl
 * User: Sebastian
 * Date: 25.07.2015
 * Time: 08:30
 */
public class TextureCache {

    private static final TextureAttributes DEFAULT_TEXTURE_ATTRIBUTES = new TextureAttributes(GL11.GL_LINEAR, GL11.GL_CLAMP);

    private Map<String, Texture> cache;

    public TextureCache() {
        cache = new HashMap<>();
    }

    public Texture get(final String textureResource) throws Exception {
        return cache.computeIfAbsent(textureResource, s -> loadTexture(textureResource, DEFAULT_TEXTURE_ATTRIBUTES));
    }

    public Texture get(final String textureResource, final TextureAttributes attributes) throws Exception {
        return cache.computeIfAbsent(textureResource, s -> loadTexture(textureResource, attributes));
    }

    public void free() {
        cache.values().forEach(Texture::free);
        cache.clear();

    }

    private Texture loadTexture(final String resource, final TextureAttributes attributes) {
        File textureFile;
        try {
            // First we try to load it from a file in the jar
            textureFile = ResourceUtil.getResourceFile(resource, this.getClass());
        } catch (Exception e) {
            // Check resources directory in working directory
            textureFile = Paths.get("resources", resource).toFile();
        }

        return Texture.loadTexture(textureFile.toString(), attributes, false);
    }
}
