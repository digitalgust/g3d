/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;

import org.mini.g3d.core.BackendSuported;
import org.mini.g3d.core.gltf2.loader.data.*;

import java.util.ArrayList;
import java.util.List;

public class RenderAnimation implements BackendSuported {


    private final List<GLTFChannel> channels;
    private final List<GLTFAnimationSampler> samplers;
    //One interpolator per channel.
    private final List<Interpolator> interpolators = new ArrayList<>();

    AnimationClip clip;
    int prevKey = 0;
    float prevT = 0.0f;
    int nextKey = 0;
    float keyDelta = 0f;
    float timeNormal = 0f;

    public RenderAnimation(GLTFAnimation gltfAnimation, RenderNode rootRenderNode, AnimationClip clip) {
        this.channels = gltfAnimation.getChannels();
        this.samplers = gltfAnimation.getSamplers();
        this.clip = clip;


        for (GLTFChannel channel : channels) {
            if (channel.getTarget().getNode() != null) {
                interpolators.add(new Interpolator(this, channel, rootRenderNode));
            }
        }
    }

    public void advance(float totalTime, int clipIndex) {
        if (channels == null) {
            return;
        }
        GLTFAnimationSampler samplerEx = samplers.get(0);
        GLTFAccessor input = samplerEx.getInput();

        AnimationClip.Ani a = clip.getClips(samplerEx.getInput())[clipIndex];

        float offset = totalTime % (a.endAt - a.beginAt);
        float pos = a.beginAt + offset;
        nextKey = -1;
//        float beginInTable = input.getFloat(a.begin);
//        float endInTable = input.getFloat(a.end);

        if (prevKey < a.begin || prevKey >= a.end - 1 || prevT > pos || pos >= input.getFloat(a.end - 1)) {
            prevKey = a.begin;
        }
        this.prevT = pos;
        for (int i = this.prevKey + 1; i <= a.end; i++) {
            if (pos <= input.getFloat(i)) {
                nextKey = i;
                break;
            }
        }
        this.prevKey = nextKey - 1;

        keyDelta = input.getFloat(nextKey) - input.getFloat(prevKey);

        //Normalize t: [t0, t1] -> [0, 1]
        timeNormal = (pos - input.getFloat(prevKey)) / keyDelta;

        for (int i = 0, imax = interpolators.size(); i < imax; i++) {
            Interpolator interpolator = interpolators.get(i);
            GLTFChannel channel = interpolator.getChannel();
            GLTFAnimationSampler sampler = samplers.get(channel.getAnimationSamplerIndex());
            RenderNode node = interpolator.getRenderNode();

            switch (channel.getTarget().getPath()) {
                case TRANSLATION:
                    interpolator.interpolate(sampler, node.getTranslation());
                    break;
                case ROTATION:
                    interpolator.interpolate(sampler, node.getRotation());
                    break;
                case SCALE:
                    interpolator.interpolate(sampler, node.getScale());
                    break;
                case WEIGHTS:
                    if (node instanceof RenderMesh) {
                        RenderMesh mesh = (RenderMesh) node;
                        interpolator.interpolate(sampler, mesh.getWeights());
                    } else {
                        System.out.println("Error weights must be applied to RenderMesh");
                    }
                    break;

            }
        }
    }

    @Override
    public void swap() {
        if (channels == null) {
            return;
        }

        for (Interpolator interpolator : interpolators) {
            RenderNode node = interpolator.getRenderNode();
            node.swap();
        }
    }
}
