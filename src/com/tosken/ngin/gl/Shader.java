package com.tosken.ngin.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Sebastian Greif on 20.06.2016.
 * Copyright di support 2016
 */
public class Shader {
    public enum Type {
        Vertex(GL20.GL_VERTEX_SHADER),
        Fragment(GL20.GL_FRAGMENT_SHADER);

        Type(int glType) {
            this.glType = glType;
        }

        int glType;
    }

    Type type;

    int shaderId;

    private Shader(final int shaderId, final Type type) {
        this.shaderId = shaderId;
        this.type = type;
    }

    public static Shader createFromFile(final String resourceFile, final Type type) throws Exception {
        final URL url = Shader.class.getResource(resourceFile);
        String source = new String(Files.readAllBytes(Paths.get(url.toURI())));
        return create(source, type);
    }

    public static Shader create(final String source, final Type type) throws Exception {
        final int shaderId = GL20.glCreateShader(type.glType);
        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);

        final int status = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
        if (status == GL11.GL_FALSE) {
            final String errorLog = GL20.glGetShaderInfoLog(shaderId);
            throw new Exception(errorLog);
        }

        return new Shader(shaderId, type);
    }
}
