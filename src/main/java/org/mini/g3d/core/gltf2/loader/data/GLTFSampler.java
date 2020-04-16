/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

/**
 * Texture sampler properties for filtering and wrapping modes.
 */
public class GLTFSampler extends GLTFChildOfRootProperty {

    /**
     * Magnification filter.  Valid values correspond to WebGL enums: `9728` (NEAREST) and `9729`
     * (LINEAR). TODO LINEAR is an unofficial default for now
     */
    private GLTFMagnificationFilter magFilter = GLTFMagnificationFilter.LINEAR;

    /**
     * Minification filter.  All valid values correspond to WebGL enums. TODO LINEAR_MIPMAP_LINEAR is
     * an unofficial default for now
     */
    private GLTFMinificationFilter minFilter = GLTFMinificationFilter.LINEAR_MIPMAP_LINEAR;

    /**
     * S (U) wrapping mode.  All valid values correspond to WebGL enums.
     */
    private GLTFWrap wrapS = GLTFWrap.REPEAT;

    /**
     * T (V) wrapping mode.  All valid values correspond to WebGL enums.
     */
    private GLTFWrap wrapT = GLTFWrap.REPEAT;

    public GLTFMagnificationFilter getMagFilter() {
        return magFilter;
    }

    /**
     *
     */
    private void setMagFilter(int value) {
        this.magFilter = GLTFMagnificationFilter.getType(value);
    }

    public GLTFMinificationFilter getMinFilter() {
        return minFilter;
    }

    /**
     *
     */
    private void setMinFilter(int value) {
        this.minFilter = GLTFMinificationFilter.getType(value);
    }

    public GLTFWrap getWrapS() {
        return wrapS;
    }

    /**
     *
     */
    private void setWrapS(int value) {
        this.wrapS = GLTFWrap.getType(value);
    }

    public GLTFWrap getWrapT() {
        return wrapT;
    }

    /**
     *
     */
    private void setWrapT(int value) {
        this.wrapT = GLTFWrap.getType(value);
    }
}
