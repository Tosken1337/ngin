package com.tosken.ngin.geometry.object;

import com.tosken.ngin.gl.VertexArrayObject;
import com.tosken.ngin.gl.VertexBufferObject;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sebastian Greif on 01.08.2016.
 * Copyright di support 2016
 */
public class Quad {
    private VertexArrayObject vao;
    private VertexBufferObject vboIndices;

    public void init() {
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
                0, 1, 2,
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
        VertexBufferObject vbo = VertexBufferObject.from(bufferData.array());
        vboIndices = VertexBufferObject.fromIndices(indices);
        final Map<VertexArrayObject.VertexAttribBinding, VertexBufferObject> binding = new HashMap<VertexArrayObject.VertexAttribBinding, VertexBufferObject>() {
            {
                put(new VertexArrayObject.VertexAttribBinding(0, vertexFloatCount,      vertexFloatCount * 4 + texCoordFloatCount * 4,      0), vbo);
                put(new VertexArrayObject.VertexAttribBinding(1, texCoordFloatCount,    vertexFloatCount * 4 + texCoordFloatCount * 4,    vertexFloatCount * 4), vbo);
            }
        };
        vao = VertexArrayObject.create(binding);
    }

    public VertexArrayObject getVao() {
        return vao;
    }

    public VertexBufferObject getVboIndices() {
        return vboIndices;
    }
}
