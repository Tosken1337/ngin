package com.tosken.ngin.gl;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.Map;
import java.util.Objects;

/**
 * Created by Sebastian Greif on 22.06.2016.
 * Copyright di support 2016
 */
public class VertexArrayObject {
    private int vaoId;

    private VertexArrayObject(final int vaoId) {
        this.vaoId = vaoId;
    }

    public static VertexArrayObject create(final Map<VertexAttribBinding, VertexBufferObject> vertexAttribdata) {
        final int vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // set buffers to bin at the specified binding slots
        vertexAttribdata.forEach((vertexAttribBinding, vertexBufferObject) -> {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBufferObject.getId());
            GL20.glVertexAttribPointer(vertexAttribBinding.index, vertexAttribBinding.size, vertexBufferObject.getGlDataType(), false, vertexAttribBinding.stride, vertexAttribBinding.pointer);
            GL20.glEnableVertexAttribArray(vertexAttribBinding.index);
        });

        GL30.glBindVertexArray(0);

        return new VertexArrayObject(vaoId);
    }

    public void bind() {
        GL30.glBindVertexArray(vaoId);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    public static class VertexAttribBinding {
        /**
         * Specifies the index of the generic vertex attribute
         */
        public int index = 0;

        /**
         * Specifies the number of components per generic vertex attribute. Must be 1, 2, 3, 4
         */
        public int size = 3;

        /**
         * Specifies the byte offset between consecutive generic vertex attributes.
         * If stride is 0, the generic vertex attributes are understood to be tightly packed in the array.
         */
        public int stride = 0;

        /**
         * Specifies a offset of the first component of the first generic vertex attribute in the array in the data store of the buffer
         * currently bound to the GL_ARRAY_BUFFER target.
         * The initial value is 0.
         */
        public int pointer = 0;

        public VertexAttribBinding(final int index, final int size, final int stride) {
            this.index = index;
            this.size = size;
            this.stride = stride;
        }

        public VertexAttribBinding(final int index, final int size, final int stride, final int pointer) {
            this.index = index;
            this.size = size;
            this.stride = stride;
            this.pointer = pointer;
        }

        @Override
        public int hashCode() {
            return Objects.hash(index);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final VertexAttribBinding other = (VertexAttribBinding) obj;
            return Objects.equals(this.index, other.index);
        }
    }
}
