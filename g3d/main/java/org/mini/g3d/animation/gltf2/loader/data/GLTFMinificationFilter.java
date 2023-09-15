/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Minification filter.  All valid values correspond to WebGL enums.
 */
public enum GLTFMinificationFilter {
    NEAREST(9728),
    LINEAR(9729),
    NEAREST_MIPMAP_NEAREST(9984),
    LINEAR_MIPMAP_NEAREST(9985),
    NEAREST_MIPMAP_LINEAR(9986),
    LINEAR_MIPMAP_LINEAR(9987);

    private static final Map<Integer, GLTFMinificationFilter> valueMap = new HashMap();

    static {
        for (GLTFMinificationFilter type : GLTFMinificationFilter.values()) {
            valueMap.put(type.getValue(), type);
        }
    }

    private int value;

    GLTFMinificationFilter(int value) {
        this.value = value;
    }

    static GLTFMinificationFilter getType(int value) {
        return valueMap.get(value);
    }

    public static Map<Integer, GLTFMinificationFilter> getValueMap() {
        return valueMap;
    }

    public int getValue() {
        return this.value;
    }
}
