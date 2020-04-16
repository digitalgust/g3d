/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;


import org.mini.g3d.core.vector.Quaternionf;
import org.mini.g3d.core.vector.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A typed view into a bufferView. A bufferView contains raw binary data. An accessor provides a
 * typed view into a bufferView or a subset of a bufferView similar to how WebGL
 */
public class GLTFAccessor extends GLTFChildOfRootProperty {

    public GLTFAccessorComponentType getComponentType() {
        return componentType;
    }

    /**
     * The data type of components in the attribute. All valid values correspond to WebGL enums. The
     * corresponding typed arrays are `Int8Array`, `Uint8Array`, `Int16Array`, `Uint16Array`,
     * `Uint32Array`, and `Float32Array`, respectively.  5125 (UNSIGNED_INT) is only allowed when the
     * accessor contains indices, i.e., the accessor is only referenced by `primitive.indices`.
     */
    private GLTFAccessorComponentType componentType;
    /**
     * Specifies whether integer data values should be normalized (`true`) to [0, 1] (for unsigned
     * types) or [-1, 1] (for signed types), or converted directly (`false`) when they are accessed.
     * This property is defined only for accessors that contain vertex attributes or animation output
     * data.
     */
    private boolean normalized = false;

    public void setType(String type) {
        this.type = GLTFAccessorDataType.valueOf(type);
    }

    public void setMax(float[] max) {
        this.max = max;
    }

    public void setMin(float[] min) {
        this.min = min;
    }

    /**
     * Specifies if the attribute is a scalar, vector, or matrix.
     */
    private GLTFAccessorDataType type;
    /**
     * Maximum value of each component in this attribute.  Array elements must be treated as having
     * the same data type as accessor's `componentType`. Both min and max arrays have the same length.
     * The length is determined by the value of the type property; it can be 1, 2, 3, 4, 9, or 16.
     * <p>
     * `normalized` property has no effect on array values: they always correspond to the actual
     * values stored in the buffer. When accessor is sparse, this property must contain max values of
     * accessor data with sparse substitution applied.
     * <p>
     */
    private float[] max;
    /**
     * Minimum value of each component in this attribute.  Array elements must be treated as having
     * the same data type as accessor's `componentType`. Both min and max arrays have the same length.
     * The length is determined by the value of the type property; it can be 1, 2, 3, 4, 9, or 16.
     * <p>
     * `normalized` property has no effect on array values: they always correspond to the actual
     * values stored in the buffer. When accessor is sparse, this property must contain min values of
     * accessor data with sparse substitution applied.
     * <p>
     * //min items 1 max items 16
     */
    private float[] min;
    /**
     * The bufferView. When not defined, accessor must be initialized with zeros; `sparse` property or
     * extensions could override zeros with actual values.
     */
    private GLTFBufferView bufferView;

    public int getByteOffset() {
        return byteOffset;
    }

    public void setByteOffset(int byteOffset) {
        this.byteOffset = byteOffset;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /**
     * The offset relative to the start of the bufferView in bytes. This must be a multiple of the
     * size of the component data type.
     */
    private int byteOffset = 0;
    /**
     * The number of attributes referenced by this accessor, not to be confused with the number of
     * bytes or number of components.
     */
    private int count;
    /**
     *
     */
    private GLTFAccessorSparse sparse;
    private ByteBuffer data;

    public void setComponentType(int value) {
        this.componentType = GLTFAccessorComponentType.getType(value);
    }

    /**
     * Get referenced BufferView
     *
     * @return
     */
    private GLTFBufferView getBufferView() {
        return bufferView;
    }

    public void setBufferView(int index) {
        gltf.indexResolvers.add(() -> bufferView = gltf.getBufferView(index));
    }

    /**
     * 3 layers of length to calculate the length in bytes if byteStride is defined it includes the
     * element size
     *
     * @return the size of the entire Accessor in bytes
     */
    public int getSizeInBytes() {
        return getElementIndexAsByteIndex(count);
    }

    private int getElementIndexAsByteIndex(int elementIndex) {
        int elementSizeInBytes =
                getType().getPrimitiveCount() * getPrimitiveType().getSizeInBytes();
        int byteStride = getByteStride() - elementSizeInBytes;
        if (getByteStride() > 0) {
            return ((elementIndex - 1) * byteStride) + (elementIndex * elementSizeInBytes);
        }
        return elementIndex * elementSizeInBytes;
    }

    private int getPrimitiveIndexAsByteIndex(int primitiveIndex) {
        int primitiveSizeInBytes = getPrimitiveType().getSizeInBytes();
        int elementByteIndex = getElementIndexAsByteIndex(
                primitiveIndex / type.getPrimitiveCount());
        return elementByteIndex
                + (primitiveIndex % type.getPrimitiveCount()) * primitiveSizeInBytes;
    }

    /**
     * @return a Buffer containing data this Accessor references //TODO sparse
     */
    public ByteBuffer getData() {
        if (data != null) {
            return data;
        }
        if (sparse != null) {
            //TODO
//      sparse.
        }
        //Don't set data, most large buffers are only used once.
        //Data will be set by getFloat
        return this.getBufferView().getData(byteOffset, getSizeInBytes());
    }

    public float getFloat(int index) {
        if (data == null) {
            data = getData();
            assert (data.order() == ByteOrder.LITTLE_ENDIAN);
        }
        int byteIndex = getPrimitiveIndexAsByteIndex(index);
        switch (componentType) {
            case FLOAT:
                return data.getFloat(byteIndex);
            case BYTE:
                return GLTFAccessorComponentType.BYTE.intToFloat(data.get(byteIndex));
            case UNSIGNED_BYTE:
                return GLTFAccessorComponentType.UNSIGNED_BYTE.intToFloat(data.get(byteIndex));
            case SHORT:
                return GLTFAccessorComponentType.SHORT.intToFloat(data.getShort(byteIndex));
            case UNSIGNED_SHORT:
                return GLTFAccessorComponentType.UNSIGNED_SHORT.intToFloat(data.getShort(byteIndex));
            case UNSIGNED_INT:
                return GLTFAccessorComponentType.UNSIGNED_INT.intToFloat(data.getInt(byteIndex));
        }
        return 0f;
    }

    public GLTFBufferViewTarget getTarget() {
        return this.getBufferView().getTarget();
    }

    /**
     * The stride is how many bytes should be ignored between each value read from the buffer
     *
     * @return the stride in bytes.
     */
    public int getByteStride() {
        return this.getBufferView().getByteStride();
    }

    public int getCount() {
        return this.count;
    }

    public int getPrimitiveCount() {
        return this.count * this.getType().getPrimitiveCount();
    }

    public int getGLType() {
        return this.componentType.getValue();
    }

    public boolean isNormalized() {
        return normalized;
    }

    public GLTFAccessorComponentType getPrimitiveType() {
        return componentType;
    }

    public GLTFAccessorDataType getType() {
        return type;
    }

    public float[] getMax() {
        return max;
    }

    public float[] getMin() {
        return min;
    }

    public void readInto(int elementIndex, Quaternionf dest) {
        elementIndex *= this.getType().getPrimitiveCount();
        dest.set(this.getFloat(elementIndex++), this.getFloat(elementIndex++),
                this.getFloat(elementIndex++), this.getFloat(elementIndex));
    }

    public void readInto(int elementIndex, Vector3f dest) {
        elementIndex *= this.getType().getPrimitiveCount();
        dest.set(this.getFloat(elementIndex++), this.getFloat(elementIndex++),
                this.getFloat(elementIndex));
    }

    public void readInto(int elementIndex, float[] dest) {
        elementIndex *= this.getType().getPrimitiveCount() * dest.length;
        for (int i = 0; i < dest.length; i++) {
            dest[i] = this.getFloat(elementIndex++);
        }
    }
}
