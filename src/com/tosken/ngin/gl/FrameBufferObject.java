package com.tosken.ngin.gl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.*;

/**
 * Lwjgl
 * User: Sebastian
 * Date: 18.07.2015
 * Time: 10:05
 */
public class FrameBufferObject implements GLResource {
    private int id;
    private Map<Integer, Texture> colorAttachements;
    private List<Integer> renderBufferObjectIds;

    private FrameBufferObject() {}

    public static FrameBufferObject create() {
        int bufferId = GL30.glGenFramebuffers();
        if (bufferId <= 0) {
            throw new RuntimeException("");
        }
        GLHelper.checkAndThrow();

        FrameBufferObject fbo = new FrameBufferObject();
        fbo.colorAttachements = new HashMap<>();
        fbo.renderBufferObjectIds = new ArrayList<>();
        fbo.id = bufferId;
        return fbo;
    }

    public FrameBufferObject addColorAttachment(int width, int height, int format, int attachmentIndex) {
        final Texture texture = new Texture(width, height, format);
        return addColorAttachment(texture, attachmentIndex);
    }

    public FrameBufferObject addColorAttachment(final Texture attachment, int attachmentIndex) {
        bind();
        GL30.glFramebufferTexture2D(
                GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + attachmentIndex, GL11.GL_TEXTURE_2D, attachment.getId(), 0);
        GLHelper.checkAndThrow();
        unbind();
        this.colorAttachements.put(attachmentIndex, attachment);
        return this;
    }

    public FrameBufferObject addDefaultDepthStencil(int width, int height) {
        int rboDepthStencilId;
        rboDepthStencilId = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rboDepthStencilId);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, width, height);

        bind();
        GL30.glFramebufferRenderbuffer(
                GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, rboDepthStencilId);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        GLHelper.checkAndThrow();
        unbind();
        renderBufferObjectIds.add(rboDepthStencilId);

        return this;
    }

    public Collection<Texture> getColorAttachments() {
        return colorAttachements.values();
    }

    public Optional<Texture> getColorAttachment(int attachmentIndex) {
        return Optional.ofNullable(colorAttachements.get(attachmentIndex));
    }

    /**
     * Checks if the framebuffer is complete
     * @return
     */
    public boolean isComplete() {
        return GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) == GL30.GL_FRAMEBUFFER_COMPLETE;
    }

    /**
     * Binds the framebuffer to enable it for rendering.
     * @return  The framebuffer object for fluent api use
     */
    public FrameBufferObject bind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
        return this;
    }

    /**
     * Binds the default back buffer.
     * @return  The framebuffer object for fluent api use
     */
    public FrameBufferObject unbind() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        return this;
    }

    /**
     * Deletes the associated gl frame buffer object and all attached textures and render buffer objects.
     */
    @Override
    public void free() {
        // Also release all attached
        colorAttachements.values().forEach(Texture::free);
        renderBufferObjectIds.forEach(id -> GL30.glDeleteRenderbuffers(id));
        colorAttachements.clear();
        renderBufferObjectIds.clear();

        GL30.glDeleteFramebuffers(id);
    }
}
