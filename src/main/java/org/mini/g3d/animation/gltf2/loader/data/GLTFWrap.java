/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

import java.util.HashMap;
import java.util.Map;

public enum GLTFWrap {
    CLAMP_TO_EDGE(33071),
    MIRRORED_REPEAT(33648),
    REPEAT(10497);

    private static final Map<Integer, GLTFWrap> valueMap = new HashMap();

    static {
        for (GLTFWrap type : GLTFWrap.values()) {
            valueMap.put(type.getValue(), type);
        }
    }

    private int value;

    GLTFWrap(int value) {
        this.value = value;
    }

    static GLTFWrap getType(int value) {
        return valueMap.get(value);
    }

    public static Map<Integer, GLTFWrap> getValueMap() {
        return valueMap;
    }

    public int getValue() {
        return this.value;
    }
}
