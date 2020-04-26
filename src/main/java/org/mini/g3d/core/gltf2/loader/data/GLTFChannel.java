/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

/**
 * Targets an animation's sampler at a node's property.
 */
public class GLTFChannel extends GLTFProperty {

    /**
     * The index of a sampler in this animation used to compute the value for the target, e.g., a
     * node's translation, rotation, or scale (TRS).
     */
    private int indexAnimationSampler = -1;

    public void setSampler(int sampler) {
        this.indexAnimationSampler = sampler;
    }

    public void setTarget(GLTFAnimationChannelTarget target) {
        this.target = target;
    }

    /**
     * The index of the node and TRS property to target.
     */
    private GLTFAnimationChannelTarget target;

    public int getAnimationSamplerIndex() {
        return indexAnimationSampler;
    }

    public GLTFAnimationChannelTarget getTarget() {
        return target;
    }
}
