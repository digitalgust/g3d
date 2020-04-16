/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render.animation;

import org.mini.g3d.core.gltf2.loader.data.GLTFAnimation;
import org.mini.g3d.core.gltf2.loader.data.GLTFAnimationSampler;
import org.mini.g3d.core.gltf2.loader.data.GLTFChannel;
import org.mini.g3d.core.gltf2.render.RenderMesh;
import org.mini.g3d.core.gltf2.render.RenderNode;

import java.util.ArrayList;
import java.util.List;

public class RenderAnimation {


    private final GLTFAnimation gltfAnimation;
    private final List<GLTFChannel> channels;
    private final List<GLTFAnimationSampler> samplers;
    //One interpolator per channel.
    private final List<Interpolator> interpolators = new ArrayList<>();


    public RenderAnimation(GLTFAnimation gltfAnimation) {
        this.gltfAnimation = gltfAnimation;
        this.channels = gltfAnimation.getChannels();
        this.samplers = gltfAnimation.getSamplers();

        for (GLTFChannel channel : channels) {
            if (channel.getTarget().getNode() != null) {
                interpolators.add(new Interpolator(channel));
            }
        }
    }

    public void advance(float totalTime) {
        if (channels == null) {
            return;
        }

        for (Interpolator interpolator : interpolators) {
            GLTFChannel channel = interpolator.getChannel();
            GLTFAnimationSampler sampler = samplers.get(channel.getAnimationSamplerIndex());
            RenderNode node = interpolator.getRenderNode();

            switch (channel.getTarget().getPath()) {
                case TRANSLATION:
                    interpolator.interpolate(totalTime, sampler, node.getTranslation());
                    break;
                case ROTATION:
                    interpolator.interpolate(totalTime, sampler, node.getRotation());
                    break;
                case SCALE:
                    interpolator.interpolate(totalTime, sampler, node.getScale());
                    break;
                case WEIGHTS:
                    if (node instanceof RenderMesh) {
                        RenderMesh mesh = (RenderMesh) node;
                        interpolator.interpolate(totalTime, sampler, mesh.getWeights());
                    } else {
                        System.out.println("Error weights must be applied to RenderMesh");
                    }
                    break;

            }
        }
    }
}
