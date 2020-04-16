/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

/**
 * An orthographic camera containing properties to create an orthographic projection matrix.
 */
public class GLTFOrthographic extends GLTFProperty {

    /**
     * The floating-point horizontal magnification of the view. Must not be zero.
     */
    private float xmag;

    /**
     * The floating-point vertical magnification of the view. Must not be zero.
     */
    private float ymag;

    /**
     * The floating-point distance to the far clipping plane. `zfar` must be greater than `znear`.
     */
    private float zfar;

    /**
     * The floating-point distance to the near clipping plane.
     */
    private float znear;

    public float getXmag() {
        return xmag;
    }

    public float getYmag() {
        return ymag;
    }

    public float getZfar() {
        return zfar;
    }

    public float getZnear() {
        return znear;
    }
}
