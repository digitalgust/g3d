/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of primitives to be rendered.  A node can contain one mesh.  A node's transform places the
 * mesh in the scene.
 */
public class GLTFMesh extends GLTFChildOfRootProperty {

    public void setPrimitives(List<GLTFMeshPrimitive> primitives) {
        this.primitives = primitives;
    }

    public void setWeights(float[] weights) {
        this.weights = weights;
    }

    /**
     * An array of primitives, each defining geometry to be rendered with a material. minItems 1
     */
    private List<GLTFMeshPrimitive> primitives = new ArrayList<>();

    /**
     * Array of weights to be applied to the Morph Targets. minItems 1
     */
    private float[] weights;

    public List<GLTFMeshPrimitive> getPrimitives() {
        return primitives;
    }

    public float[] getWeights() {
        return weights;
    }
}
