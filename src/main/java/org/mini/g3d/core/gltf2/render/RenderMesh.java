/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;

import org.mini.g3d.core.BackendSuported;
import org.mini.g3d.core.gltf2.loader.data.GLTFNode;

public class RenderMesh extends RenderNode implements BackendSuported {
    private final float[] weights;
    private final float[] weights_backend;

    public RenderMesh(GLTFNode node, RenderNode parent) {
        super(node, parent);
        assert (node.getMesh() != null);

        //gust
        float[] meshWeights = node.getMesh().getWeights();
        if (meshWeights != null) {
            weights = meshWeights.clone();
            weights_backend = meshWeights.clone();
        } else {
            weights = null;
            weights_backend = null;
        }
    }

    public float[] getWeights_backend() {
        return weights_backend;
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


    public void swap() {
        super.swap();
        if (this.getSkin() != null) {
            this.getSkin().swap();
        }
        if (weights_backend != null && weights != null) {
            System.arraycopy(weights_backend, 0, weights, 0, weights.length);
        }
    }
}
