/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

public class GLTFChildOfRootProperty extends GLTFProperty {

    public void setName(String name) {
        this.name = name;
    }

    /**
     * The user-defined name of this object. This is not necessarily unique, e.g., an accessor and a
     * buffer could have the same name, or two accessors could even have the same name.
     */
    private String name;

    public String getName() {
        return name;
    }
}
