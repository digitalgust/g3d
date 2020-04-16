/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

import java.util.List;

/**
 * A keyframe animation.
 */
public class GLTFAnimation extends GLTFChildOfRootProperty {

    public void setChannels(List<GLTFChannel> channels) {
        this.channels = channels;
    }

    public void setSamplers(List<GLTFAnimationSampler> samplers) {
        this.samplers = samplers;
    }

    /**
     * An array of channels, each of which targets an animation's sampler at a node's property.
     * Different channels of the same animation can't have equal targets. minItems 1
     */
    private List<GLTFChannel> channels;

    /**
     * An array of samplers that combines input and output accessors with an interpolation algorithm
     * to define a keyframe graph (but not its target). minItems 1
     */
    private List<GLTFAnimationSampler> samplers;


    public List<GLTFChannel> getChannels() {
        return channels;
    }

    public List<GLTFAnimationSampler> getSamplers() {
        return samplers;
    }
}
