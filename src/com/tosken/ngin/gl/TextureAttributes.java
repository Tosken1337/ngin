package com.tosken.ngin.gl;

/**
 * Lwjgl
 * User: Sebastian
 * Date: 25.07.2015
 * Time: 08:47
 */
public class TextureAttributes {
    private int filterMode;

    private int wrapMode;

    public TextureAttributes(int filterMode, int wrapMode) {
        this.filterMode = filterMode;
        this.wrapMode = wrapMode;
    }

    public int getFilterMode() {
        return filterMode;
    }

    public int getWrapMode() {
        return wrapMode;
    }
}
