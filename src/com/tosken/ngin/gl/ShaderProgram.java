package com.tosken.ngin.gl;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Sebastian Greif on 20.06.2016.
 * Copyright di support 2016
 */
public class ShaderProgram {
    //private HashMap<Shader.Type, Shader> pipeline = new HashMap<>();

    private int programId;

    private Map<String, Integer> uniforms = new HashMap<>();

    private ShaderProgram() {
    }

    public ShaderProgram(final Collection<Shader> pipeline, final int programId) {
        this.programId = programId;
    }

    public static ShaderProgram create(final Collection<Shader> shaders) throws Exception {
        final int programId = GL20.glCreateProgram();
        shaders.forEach(shader -> GL20.glAttachShader(programId, shader.shaderId));

        GL20.glLinkProgram(programId);

        int status = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE) {
            final String errorLog = GL20.glGetProgramInfoLog(programId);
            throw new Exception("Linker failure: " + errorLog);
        }

        GL20.glValidateProgram(programId);
        status = GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS);
        if (status == GL11.GL_FALSE) {
            final String errorLog = GL20.glGetProgramInfoLog(programId);
            throw new Exception("Validation failure: " + errorLog);
        }


        return new ShaderProgram(shaders, programId);
    }

    public ShaderProgram createUniform(String uniformName) throws Exception {
        int uniformLocation = GL20.glGetUniformLocation(programId, uniformName);
        if (uniformLocation < 0) {
            throw new Exception("Could not find uniform:" +
                    uniformName);
        }
        uniforms.put(uniformName, uniformLocation);

        return this;
    }

    public ShaderProgram setUniform(String uniformName, Matrix4f value) {
        // Dump the matrix into a float buffer
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        value.get(fb);
        GL20.glUniformMatrix4fv(uniforms.get(uniformName), false, fb);

        return this;
    }

    public void bind() {
        GL20.glUseProgram(programId);
    }

    public void unbind() {
        GL20.glUseProgram(0);
    }
}
