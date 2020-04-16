/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;

import org.mini.g3d.core.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.core.gltf2.loader.data.GLTFNode;
import org.mini.g3d.core.gltf2.loader.data.GLTFSkin;
import org.mini.g3d.core.vector.Matrix4f;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RenderSkin {

    private final Runnable jointResolver;
    private LinkedHashSet<RenderNode> joints; //Need to retain order for calculation
    private final RenderNode skeletonRootNode;
    private final GLTFAccessor ibmAccessor;
    private final List<Matrix4f> jointMatrices;
    private final List<Matrix4f> jointNormalMatrices;

    public RenderSkin(GLTFSkin skin) {
        this.jointMatrices = new ArrayList<>();
        this.jointNormalMatrices = new ArrayList<>();
        this.skeletonRootNode = RenderNode.from(skin.getSkeletonRootNode());
        this.ibmAccessor = skin.getInverseBindMatricesAccessor();
        this.jointResolver = new Runnable() {
            @Override
            public void run() {
                RenderSkin.this.joints = new LinkedHashSet<>();
                for (GLTFNode node : skin.getJoints()) {
                    RenderNode rn = RenderNode.from(node);
                    if (rn != null) {
                        RenderSkin.this.joints.add(rn);
                    }
                }
            }
        };
    }

    public void computeJoints(RenderMesh mesh) {
        int i = 0;
        for (RenderNode renderNode : getJoints()) {
            if (i == jointMatrices.size()) {
                jointMatrices.add(new Matrix4f());
                jointNormalMatrices.add(new Matrix4f());
            }
            Matrix4f jointMatrix = jointMatrices.get(i);
            if (ibmAccessor == null) {
                jointMatrix.identity(); //ibm accessor is optional
            } else {
                //TODO check if Matrix4fStack would be better
                jointMatrix.load(loadMatrix(i * 16)); //Inverse bind matrix for joint
            }

            Matrix4f.mul(renderNode.getWorldTransform(), jointMatrix, jointMatrix);//Global transform of joint node
            Matrix4f.mul(mesh.getInverseWorldTransform(), jointMatrix, jointMatrix);//Global transform of mesh node

            Matrix4f normalMatrix = jointNormalMatrices.get(i);
            normalMatrix.load(jointMatrix);
            normalMatrix.invert();
            normalMatrix.transpose();

            i++;
        }
    }

    private Set<RenderNode> getJoints() {
        if (joints == null) {
            jointResolver.run();
        }
        return joints;
    }

    private float[] loadMatrix(int primitiveIndex) {
        float[] store = new float[16];
        for (int i = 0; i < 16; i++) {
            store[i] = ibmAccessor.getFloat(primitiveIndex++);
        }
        return store;
    }

    public int getJointCount() {
        if (joints == null) {
            jointResolver.run();
        }
        return this.joints.size();
    }

    public Matrix4f[] getJointMatrices() {
        return jointMatrices.toArray(new Matrix4f[0]);
    }

    public Matrix4f[] getJointNormalMatrices() {
        return jointNormalMatrices.toArray(new Matrix4f[0]);
    }
}
