/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * A view into a buffer generally representing a subset of the buffer.
 */
public class GLTFBufferView extends GLTFChildOfRootProperty {

    /**
     * The index of the buffer.
     */
    private GLTFBuffer buffer;

    public void setByteOffset(int byteOffset) {
        this.byteOffset = byteOffset;
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    /**
     * The offset into the buffer in bytes.
     */
    private int byteOffset = 0;
    /**
     * The total byte length of the buffer view.
     */
    private int byteLength = -1;
    /**
     * The target that the GPU buffer should be bound to. TODO "runtime must use it to determine data
     * usage, TODO otherwise it could be inferred from mesh accessor objects.
     */
    private GLTFBufferViewTarget bufferViewTarget;
    /**
     * The stride, in bytes, between vertex attributes. When this is not defined, data is tightly
     * packed. When two or more accessors use the same bufferView, this field must be defined.
     * <p>
     * TODO multipleOf 4
     */
    private int byteStride = 0;

    public void setBuffer(int index) {
        gltf.indexResolvers.add(() -> buffer = gltf.getBuffer(index));
    }

    /**
     * Converts json integer to Target enum
     */
    public void setTarget(int value) {
        this.bufferViewTarget = GLTFBufferViewTarget.getTarget(value);
    }

    public GLTFBufferViewTarget getTarget() {
        return this.bufferViewTarget;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    public int getByteLength() {
        return byteLength;
    }

    /**
     * @return
     */
    GLTFBuffer getDataBuffer() {
        return buffer;
    }

    /**
     * @return Buffer filled with data this BufferView points to
     */
    ByteBuffer getData(int byteOffset, int byteLength) {
        if (byteOffset + byteLength > this.byteLength) {
            throw new BufferUnderflowException();
        }
        return getDataBuffer().getData(this.byteOffset + byteOffset, byteLength);
    }

    /**
     * @return
     */
    int getByteStride() {
        return this.byteStride;
    }
}
