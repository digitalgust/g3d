/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;

import org.mini.g3d.core.gltf2.loader.data.*;
import org.mini.g3d.core.vector.Quaternionf;
import org.mini.g3d.core.vector.Vector3f;

public class Interpolator {


    private RenderAnimation renderAnimation;
    private final GLTFChannel channel;
    private RenderNode renderNode;


    private final Quaternionf endQ = new Quaternionf();
    Vector3f endV = new Vector3f();

    public Interpolator(RenderAnimation renderAnimation, GLTFChannel channel, RenderNode rootRenderNode) {
        this.renderAnimation = renderAnimation;
        this.channel = channel;
        GLTFNode node = channel.getTarget().getNode();
        if (node != null) {
            renderNode = rootRenderNode.from(node);//gust
            if (renderNode == null) throw new NullPointerException();
        }
    }


    public void interpolate(GLTFAnimationSampler sampler, Vector3f vectorDest) {
        GLTFAccessor output = sampler.getOutput();

        //Translate time to key frame

        //No interpolation for single key frame animations
        if (output.getCount() == 1) {
            output.readInto(0, vectorDest);
            return;
        }
        int prevKey = renderAnimation.prevKey;
        int nextKey = renderAnimation.nextKey;
        float keyDelta = renderAnimation.keyDelta;
        float tn = renderAnimation.timeNormal;


        output.readInto(prevKey, vectorDest);
        output.readInto(nextKey, endV);

        switch (sampler.getInterpolation()) {
            case STEP:
                output.readInto(prevKey, vectorDest);
                break;
            case CUBICSPLINE:
                float[] spline = cubicSpline(prevKey, nextKey, output, keyDelta, tn, 3);
                vectorDest.set(spline[0], spline[1], spline[2]);
                break;
            case LINEAR:
                vectorDest.lerp(endV, tn, vectorDest);
                break;
            default:
                System.out.println("Not implemented");
        }
    }

    public void interpolate(GLTFAnimationSampler sampler, Quaternionf quantDest) {
        GLTFAccessor output = sampler.getOutput();

        //Translate time to key frame

        //No interpolation for single key frame animations
        if (output.getCount() == 1) {
            output.readInto(0, quantDest);
            return;
        }
        int prevKey = renderAnimation.prevKey;
        int nextKey = renderAnimation.nextKey;
        float keyDelta = renderAnimation.keyDelta;
        float tn = renderAnimation.timeNormal;

        switch (sampler.getInterpolation()) {
            case CUBICSPLINE:
                //GLTF requires cubic spline interpolation for quaternions
                float[] spline = cubicSpline(prevKey, nextKey, output, keyDelta, tn, 4);
                quantDest.set(spline[0], spline[1], spline[2], spline[3]);
                quantDest.normalise(quantDest);
                break;
            case LINEAR:
                output.readInto(prevKey, quantDest);
                output.readInto(nextKey, endQ);
                quantDest.slerp(endQ, tn, quantDest);
                Quaternionf.normalise(quantDest, quantDest);
                break;
            case STEP:
                output.readInto(prevKey, quantDest);
                break;
        }
    }

    public void interpolate(GLTFAnimationSampler sampler, float[] floatArrayDest) {
        GLTFAccessor output = sampler.getOutput();

        //Translate time to key frame

        //No interpolation for single key frame animations
        if (output.getCount() == 1) {
            System.out.println("Unhandled single key frame");
            return;
        }
        int prevKey = renderAnimation.prevKey;
        int nextKey = renderAnimation.nextKey;
        float keyDelta = renderAnimation.keyDelta;
        float tn = renderAnimation.timeNormal;

        switch (sampler.getInterpolation()) {
            case STEP:
                output.readInto(prevKey, floatArrayDest);
                break;
            case CUBICSPLINE:
                float[] spline = cubicSpline(prevKey, nextKey, output, keyDelta, tn, 3);
                for (int i = 0; i < floatArrayDest.length; i++) {
                    floatArrayDest[i] = spline[i];
                }
                break;
            case LINEAR:
                linear(prevKey, nextKey, output, tn, floatArrayDest.length, floatArrayDest);
                break;
            default:
                System.out.println("Not implemented");
        }
    }

    private float[] cubicSpline(int prevKey, int nextKey, GLTFAccessor output, float keyDelta, float t, int stride) {

        //Scale by 3, because each output entry consists of two tangents and one data point.
        int prevIndex = prevKey * stride * 3;
        int nextIndex = nextKey * stride * 3;

        int A = 0;
        int V = 1 * stride;
        int B = 2 * stride;

        float tSq = t * t;
        float tCub = t * t * t;

        // We assume that the components in output are laid out like this: in-tangent, point, out-tangent.
        // https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#appendix-c-spline-interpolation
        float[] ret = new float[stride];
        for (int i = 0; i < stride; ++i) {
            float v0 = output.getFloat(prevIndex + i + V);
            float a = keyDelta * output.getFloat(nextIndex + i + A);
            float b = keyDelta * output.getFloat(prevIndex + i + B);
            float v1 = output.getFloat(nextIndex + i + V);

            ret[i] = ((2 * tCub - 3 * tSq + 1) * v0)
                    + ((tCub - 2 * tSq + t) * b)
                    + ((-2 * tCub + 3 * tSq) * v1)
                    + ((tCub - tSq) * a);

        }
        return ret;
    }

    private void linear(int prevKey, int nextKey, GLTFAccessor output, float tn, int stride,
                        float[] dest) {
        float[] prevFloats = new float[stride];
        output.readInto(prevKey, prevFloats);
        float[] nextFloats = new float[stride];
        output.readInto(nextKey, nextFloats);
        for (int i = 0; i < stride; i++) {
            dest[i] = prevFloats[i] * (1 - tn) + nextFloats[i] * tn;
        }
    }

    public GLTFChannel getChannel() {
        return channel;
    }

    public RenderNode getRenderNode() {
        return this.renderNode;
    }
}
