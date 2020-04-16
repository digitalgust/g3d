/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2;

public enum ShaderDebugType {
    NONE(""),
    METALLIC("DEBUG_METALLIC 1"),
    ROUGHNESS("DEBUG_ROUGHNESS 1"),
    NORMAL("DEBUG_NORMAL 1"),
    BASECOLOR("DEBUG_BASECOLOR 1"),
    OCCLUSION("DEBUG_OCCLUSION 1"),
    F0("DEBUG_F0 1"),
    ALPHA("DEBUG_ALPHA 1");

    private String define;

    ShaderDebugType(String define) {
        this.define = define;
    }

    public String getDefine() {
        return define;
    }
}
