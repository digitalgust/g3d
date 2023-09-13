/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.render;

import org.mini.g3d.animation.gltf2.loader.data.GLTFNode;

public class RenderMesh extends RenderNode {
    private final float[] weights;

    public RenderMesh(GLTFNode node, RenderNode parent) {
        super(node, parent);
        assert (node.getMesh() != null);

        //gust
        float[] meshWeights = node.getMesh().getWeights();
        if (meshWeights != null) {
            weights = meshWeights.clone();
        } else {
            weights = null;
        }
    }

    @Override
    public RenderSkin getSkin() {
        if (replaced != null) {
            return replaced.getSkin();
        }
        return skin;
    }

    public float[] getWeights() {
        return weights;
    }

    @Override
    public void updateSkin() {
        if (this.getSkin() != null) {
            this.getSkin().computeJoints(this);
        }
        super.updateSkin();
    }


}
