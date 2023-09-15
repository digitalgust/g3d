/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

import java.util.HashMap;
import java.util.Map;

public class GLTFProperty {

    /**
     * Hold reference to parent gltf reference for linking
     */
    public GLTF gltf;
    /**
     * Dictionary object with extension-specific objects.
     */
    private Map<String, Object> extensions;
    /**
     * Application-specific data.
     */
    private Map<String, Object> extras;

    public Map<String, Object> getExtensions() {
        if (extensions == null) {
            extensions = new HashMap<>();
        }
        return extensions;
    }

    public Map<String, Object> getExtras() {
        if (extras == null) {
            extras = new HashMap<>();
        }
        return extras;
    }
}
