package com.tosken.ngin.gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;

/**
 * Created by Sebastian Greif on 22.06.2016.
 * Copyright di support 2016
 */
public class VertexBufferObject {
    private int bufferId;

    private int glDataType;

    VertexBufferObject(final int bufferId, final int dataType) {
        this.bufferId = bufferId;
        this.glDataType = dataType;
    }

    /**
     * Create and initializes a vbo data store on gpu memory
     *
     * @param buffer
     * @return
     */
    public static VertexBufferObject from(final FloatBuffer buffer) {
        final int bufferId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        return new VertexBufferObject(bufferId, GL11.GL_FLOAT);
    }

    public static VertexBufferObject from(final float[] buffer) {
        final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(buffer.length);
        floatBuffer.put(buffer).flip();

        return from(floatBuffer);
    }

    public int getId() {
        return bufferId;
    }

    public int getGlDataType() {
        return glDataType;
    }
}
