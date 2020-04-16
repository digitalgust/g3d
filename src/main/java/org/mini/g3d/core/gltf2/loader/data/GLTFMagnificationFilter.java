/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

import java.util.HashMap;
import java.util.Map;

public enum GLTFMagnificationFilter {
    NEAREST(9728),
    LINEAR(9729);

    private static final Map<Integer, GLTFMagnificationFilter> valueMap = new HashMap<>();

    static {
        for (GLTFMagnificationFilter type : GLTFMagnificationFilter.values()) {
            valueMap.put(type.getValue(), type);
        }
    }

    private int value;

    GLTFMagnificationFilter(int value) {
        this.value = value;
    }

    static GLTFMagnificationFilter getType(int value) {
        return valueMap.get(value);
    }

    public static Map<Integer, GLTFMagnificationFilter> getValueMap() {
        return valueMap;
    }

    public int getValue() {
        return this.value;
    }
}
