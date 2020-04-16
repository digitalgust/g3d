/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;

import org.mini.g3d.core.gltf2.loader.data.GLTFNode;
import org.mini.g3d.core.gltf2.render.light.UniformLight;

public class RenderLight extends RenderNode {
    private final UniformLight uLight;

    public RenderLight(GLTFNode node, RenderNode parent) {
        super(node, parent);
        uLight = new UniformLight();
    }

    public UniformLight getUniformLight() {
        return uLight;
    }
}
