/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

/**
 * A perspective camera containing properties to create a perspective projection matrix.
 */
public class GLTFPerspective {

    /**
     * The floating-point aspect ratio of the field of view. When this is undefined, the aspect ratio
     * of the canvas is used.
     */
    private Float aspectRatio;

    /**
     * The floating-point vertical field of view in radians.
     */
    private Float yfov;

    /**
     * The floating-point distance to the far clipping plane. When defined, `zfar` must be greater
     * than `znear`. If `zfar` is undefined, runtime must use infinite projection matrix. TODO
     * infinite projection matrix
     */
    private Float zfar;

    /**
     * The floating-point distance to the near clipping plane.
     */
    private Float znear;

    public Float getAspectRatio() {
        return aspectRatio;
    }

    public Float getYfov() {
        return yfov;
    }

    public Float getZfar() {
        return zfar;
    }

    public Float getZnear() {
        return znear;
    }
}
