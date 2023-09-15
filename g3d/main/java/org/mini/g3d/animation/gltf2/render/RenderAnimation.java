/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.render;

import org.mini.g3d.animation.gltf2.loader.data.*;

import java.util.ArrayList;
import java.util.List;

public class RenderAnimation {

    private final List<GLTFChannel> channels;
    private final List<GLTFAnimationSampler> samplers;
    //One interpolator per channel.
    private final List<Interpolator> interpolators = new ArrayList<>();

    AniGroup aniGroup;
    int prevKey = 0;
    float prevT = 0.0f;
    int nextKey = 0;
    float keyDelta = 0f;
    float timeNormal = 0f;

    public RenderAnimation(GLTFAnimation gltfAnimation, RenderNode rootRenderNode, AniGroup aniGroup) {
        this.channels = gltfAnimation.getChannels();
        this.samplers = gltfAnimation.getSamplers();
        this.aniGroup = aniGroup;


        for (GLTFChannel channel : channels) {
            if (channel.getTarget().getNode() != null) {
                interpolators.add(new Interpolator(this, channel, rootRenderNode));
            }
        }
    }

    public void advance(float totalTime, int clipIndex) {
        long startAt = System.nanoTime() / 1000000;
        if (channels == null) {
            return;
        }
        GLTFAnimationSampler samplerEx = samplers.get(0);
        GLTFAccessor input = samplerEx.getInput();

        AniClip a = aniGroup.getAniClips(samplerEx.getInput())[clipIndex];

        float pos = a.beginAt;
        float progressOfMotion = a.endAt - a.beginAt;
        if (progressOfMotion != 0) {
            float offset = totalTime % (progressOfMotion);
            pos += offset;
        }

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
        float pf = input.getFloat(prevKey);
        timeNormal = (pos - pf) / keyDelta;

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
                        System.out.println("[G3D][ERROR]Error weights must be applied to RenderMesh");
                    }
                    break;

            }
        }
    }

}
