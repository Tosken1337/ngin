package com.tosken.ngin.gl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Created by Sebastian Greif on 22.06.2016.
 * Copyright di support 2016
 */
public class VertexBufferObject {
    private int bufferId;

    private int glDataType;

    private int elementCount;

    VertexBufferObject(final int bufferId, final int dataType, final int elementCount) {
        this.bufferId = bufferId;
        this.glDataType = dataType;
        this.elementCount = elementCount;
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

        return new VertexBufferObject(bufferId, GL11.GL_FLOAT, buffer.limit());
    }

    public static VertexBufferObject from(final float[] buffer) {
        final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(buffer.length);
        floatBuffer.put(buffer).flip();

        return from(floatBuffer);
    }

    public static VertexBufferObject fromIndices(final int[] buffer) {
        final IntBuffer intBuffer = BufferUtils.createIntBuffer(buffer.length);
        intBuffer.put(buffer).flip();

        return fromIndices(intBuffer);
    }

    public static VertexBufferObject fromIndices(final IntBuffer buffer) {
        final int bufferId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, bufferId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

        return new VertexBufferObject(bufferId, GL11.GL_UNSIGNED_INT, buffer.limit());
    }

    public int getId() {
        return bufferId;
    }

    public int getGlDataType() {
        return glDataType;
    }

    public int size() {
        return elementCount;
    }
}
