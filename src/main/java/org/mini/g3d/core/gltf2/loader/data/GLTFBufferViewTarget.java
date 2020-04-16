/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

import java.util.HashMap;
import java.util.Map;

public enum GLTFBufferViewTarget {
    ARRAY_BUFFER(34962),
    ELEMENT_ARRAY_BUFFER(34963);

    private static final Map<Integer, GLTFBufferViewTarget> valueMap = new HashMap<>();

    static {
        for (GLTFBufferViewTarget bufferViewTarget : GLTFBufferViewTarget.values()) {
            valueMap.put(bufferViewTarget.getValue(), bufferViewTarget);
        }
    }

    private int value;

    GLTFBufferViewTarget(int value) {
        this.value = value;
    }

    static GLTFBufferViewTarget getTarget(int value) {
        return valueMap.get(value);
    }

    public static Map<Integer, GLTFBufferViewTarget> getValueMap() {
        return valueMap;
    }

    int getValue() {
        return this.value;
    }
}
