/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

/**
 * A camera's projection.  A node can reference a camera to apply a transform to place the camera in
 * the scene.
 */
public class GLTFCamera extends GLTFChildOfRootProperty {

    /**
     * An orthographic camera containing properties to create an orthographic projection matrix.
     */
    private GLTFOrthographic orthographic;

    /**
     * A perspective camera containing properties to create a perspective projection matrix.
     */
    private GLTFPerspective perspective;

    /**
     * Specifies if the camera uses a perspective or orthographic projection.  Based on this, either
     * the camera's `perspective` or `orthographic` property will be defined.
     */
    private GLTFCameraType type;

    private void setCameraType(String type) {
        this.type = GLTFCameraType.valueOf(type.toUpperCase());
    }

    public GLTFOrthographic getOrthographic() {
        return orthographic;
    }

    public GLTFPerspective getPerspective() {
        return perspective;
    }

    public GLTFCameraType getType() {
        return type;
    }

    public enum GLTFCameraType {
        PERSPECTIVE,
        ORTHOGRAPHIC
    }
}
