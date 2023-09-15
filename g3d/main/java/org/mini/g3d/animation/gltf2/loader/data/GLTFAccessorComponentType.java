/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

import java.util.HashMap;
import java.util.Map;

/**
 * Each entry implements int / float conversion according to the GLTF spec
 */
public enum GLTFAccessorComponentType {
    BYTE(5120, 1) {
        @Override
        public float intToFloat(int input) {
            return Math.max(input / 127.0f, -1.0f);
        }

        @Override
        public int floatToInt(float input) {
            return Math.round(input * 127.0f);
        }
    },
    UNSIGNED_BYTE(5121, 1) {
        @Override
        public float intToFloat(int input) {
            return input / 255.0f;
        }

        @Override
        public int floatToInt(float input) {
            return Math.round(input * 255.0f);
        }
    },
    SHORT(5122, 2) {
        @Override
        public float intToFloat(int input) {
            return Math.max(input / 32767.0f, -1.0f);
        }

        @Override
        public int floatToInt(float input) {
            return Math.round(input * 32767.0f);
        }
    },
    UNSIGNED_SHORT(5123, 2) {
        @Override
        public float intToFloat(int input) {
            return input / 65535.0f;
        }

        @Override
        public int floatToInt(float input) {
            return Math.round(input * 65535.0f);
        }
    },
    UNSIGNED_INT(5125, 4),
    FLOAT(5126, 4);

    private static final Map<Integer, GLTFAccessorComponentType> valueMap = new HashMap<>();

    static {
        for (GLTFAccessorComponentType type : GLTFAccessorComponentType.values()) {
            valueMap.put(type.getValue(), type);
        }
    }

    private int value;
    private int sizeInBytes;

    GLTFAccessorComponentType(int value, int sizeInBytes) {
        this.value = value;
        this.sizeInBytes = sizeInBytes;
    }

    public static GLTFAccessorComponentType getType(int value) {
        return valueMap.get(value);
    }

    public static Map<Integer, GLTFAccessorComponentType> getValueMap() {
        return valueMap;
    }

    public int getSizeInBytes() {
        return this.sizeInBytes;
    }

    public int getValue() {
        return this.value;
    }

    public float intToFloat(int input) {
        return (float) input;
    }

    public int floatToInt(float input) {
        return Math.round(input);
    }
}
