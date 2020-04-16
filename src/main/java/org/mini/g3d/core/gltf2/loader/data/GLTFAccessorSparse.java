/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

public class GLTFAccessorSparse extends GLTFProperty {

    /**
     * Number of entries stored in the sparse array.
     */
    private int count = -1;

    /**
     * Index array of size `count` that points to those accessor attributes that deviate from their
     * initialization value. Indices must strictly increase.
     */
    private GLTFAccessorSparseIndices indices;

    /**
     * Array of size `count` times number of components, storing the displaced accessor attributes
     * pointed by `indices`. Substituted values must have the same `componentType` and number of
     * components as the base accessor.
     * <p>
     * required
     */
    private GLTFAccessorSparseValues values;

    public int getCount() {
        return count;
    }

    public GLTFAccessorSparseIndices getIndices() {
        return indices;
    }

    public GLTFAccessorSparseValues getValues() {
        return values;
    }
}
