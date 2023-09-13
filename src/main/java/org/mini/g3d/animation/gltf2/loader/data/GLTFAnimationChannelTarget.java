/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

/**
 * The index of the node and TRS property that an animation channel targets.
 */
public class GLTFAnimationChannelTarget extends GLTFProperty {

    /**
     * The index of the node to target.
     */
    private GLTFNode node;
    /**
     * The name of the node's TRS property to modify, or the \
     */
    private GLTFPath path;

    public GLTFNode getNode() {
        return node;
    }

    public void setNode(int index) {
        gltf.indexResolvers.add(() -> node = gltf.getNode(index));
    }

    public GLTFPath getPath() {
        return path;
    }

    public void setPath(String string) {
        this.path = GLTFPath.valueOf(string.toUpperCase());
    }

    public enum GLTFPath {
        TRANSLATION,
        ROTATION,
        SCALE,
        WEIGHTS
    }
}
