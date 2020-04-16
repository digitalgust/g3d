/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

import java.util.LinkedHashSet;

/**
 * Joints and matrices defining a skin.
 */
public class GLTFSkin extends GLTFChildOfRootProperty {

    /**
     * The index of the accessor containing the floating-point 4x4 inverse-bind matrices.  The default
     * is that each matrix is a 4x4 identity matrix, which implies that inverse-bind matrices were
     * pre-applied.
     */
    private GLTFAccessor inverseBindMatricesAccessor;
    /**
     * The index of the node used as a skeleton root. The node must be the closest common root of the
     * joints hierarchy or a direct or indirect parent node of the closest common root.
     */
    private GLTFNode skeletonRoot;
    /**
     * Indices of skeleton nodes, used as joints in this skin.  The array length must be the same as
     * the `count` property of the `inverseBindMatrices` accessor (when defined). Must be ordered
     */
    private LinkedHashSet<GLTFNode> joints;

    public void setSkeleton(int index) {
        gltf.indexResolvers.add(() -> skeletonRoot = gltf.getNode(index));
    }

    public GLTFAccessor getInverseBindMatricesAccessor() {
        return inverseBindMatricesAccessor;
    }

    public void setInverseBindMatrices(int index) {
        gltf.indexResolvers.add(() -> inverseBindMatricesAccessor = gltf.getAccessor(index));
    }

    public GLTFNode getSkeletonRootNode() {
        return skeletonRoot;
    }

    public LinkedHashSet<GLTFNode> getJoints() {
        return joints;
    }

    //It is essential that the join length is preserved
    public void setJoints(LinkedHashSet<Integer> indexSet) {
        gltf.indexResolvers.add(() -> {
            joints = new LinkedHashSet<>();
            indexSet.forEach(index -> joints.add(gltf.getNode(index)));
        });
    }
}
