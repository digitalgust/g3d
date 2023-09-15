/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

/**
 * Array of size `accessor.sparse.count` times number of components storing the displaced accessor
 * attributes pointed by `accessor.sparse.indices`.
 */
public class GLTFAccessorSparseValues extends GLTFProperty {

    /**
     * The index of the bufferView with sparse values. Referenced bufferView can't have ARRAY_BUFFER
     * or ELEMENT_ARRAY_BUFFER target.
     * <p>
     * required
     */
    private GLTFBufferView bufferView;
    /**
     * The offset relative to the start of the bufferView in bytes. Must be aligned.
     * <p>
     * default 0
     */
    private int byteOffset = 0;

    public GLTFBufferView getBufferView() {
        return bufferView;
    }

    private void setBufferView(int index) {
        gltf.indexResolvers.add(() -> bufferView = gltf.getBufferView(index));
    }

    int getByteOffset() {
        return byteOffset;
    }
}
