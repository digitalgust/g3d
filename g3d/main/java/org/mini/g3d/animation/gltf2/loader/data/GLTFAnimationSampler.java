/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

/**
 * Combines input and output accessors with an interpolation algorithm to define a keyframe graph
 * (but not its target).
 */
public class GLTFAnimationSampler extends GLTFProperty {

    /**
     * The index of an accessor containing keyframe input values, e.g., time. That accessor must have
     * componentType `FLOAT`. The values represent time in seconds with `time[0] >= 0.0`, and strictly
     * increasing values, i.e., `time[n + 1] > time[n]`.
     */
    private GLTFAccessor input;
    /**
     * The index of an accessor containing keyframe output values. When targeting translation or scale
     * paths, the `accessor.componentType` of the output values must be `FLOAT`. When targeting
     * rotation or morph weights, the `accessor.componentType` of the output values must be `FLOAT` or
     * normalized integer. For weights, each output element stores `SCALAR` values with a count equal
     * to the number of morph targets.
     */
    private GLTFAccessor output;

    public void setInterpolation(String interpolation) {
        this.interpolation = GLTFInterpolation.valueOf(interpolation);
    }

    /**
     * Interpolation algorithm.
     */
    private GLTFInterpolation interpolation = GLTFInterpolation.LINEAR;

    public GLTFAccessor getInput() {
        return input;
    }

    public void setInput(int index) {
        gltf.indexResolvers.add(() -> input = gltf.getAccessor(index));
    }

    public GLTFAccessor getOutput() {
        return output;
    }

    public void setOutput(int index) {
        gltf.indexResolvers.add(() -> output = gltf.getAccessor(index));
    }

    public GLTFInterpolation getInterpolation() {
        return interpolation;
    }

    /**
     * Interpretation algorithm.
     */
    public enum GLTFInterpolation {
        LINEAR,
        STEP,
        CUBICSPLINE
    }
}
