/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;

import org.mini.g3d.core.BackendSuported;
import org.mini.g3d.core.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.core.gltf2.loader.data.GLTFNode;
import org.mini.g3d.core.gltf2.loader.data.GLTFSkin;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.nanovg.Gutil;

import java.util.ArrayList;
import java.util.List;

public class RenderSkin implements BackendSuported {

    private final Runnable jointResolver;
    private List<RenderNode> joints; //Need to retain order for calculation
    private final RenderNode skeletonRootNode;
    private final GLTFAccessor ibmAccessor;
    private final List<Matrix4f> jointMatrices;
    private final List<Matrix4f> jointMatrices_backend;
    private final List<Matrix4f> jointNormalMatrices;
    private final List<Matrix4f> jointNormalMatrices_backend;

    public RenderSkin(GLTFSkin skin, RenderNode parent) {
        this.jointMatrices = new ArrayList<>();
        this.jointNormalMatrices = new ArrayList<>();
        this.jointMatrices_backend = new ArrayList<>();
        this.jointNormalMatrices_backend = new ArrayList<>();
        this.skeletonRootNode = parent.getTopParent().from(skin.getSkeletonRootNode());
        this.ibmAccessor = skin.getInverseBindMatricesAccessor();
        this.jointResolver = new Runnable() {
            @Override
            public void run() {
                RenderSkin.this.joints = new ArrayList<>();
                for (GLTFNode node : skin.getJoints()) {
                    RenderNode rn = parent.getTopParent().from(node);
                    if (rn != null) {
                        RenderSkin.this.joints.add(rn);
                    }
                }
            }
        };
    }

    public void computeJoints(RenderMesh mesh) {
        for (int i = 0, imax = getJoints().size(); i < imax; i++) {
            RenderNode renderNode = getJoints().get(i);
            if (i == jointMatrices.size()) {
                jointMatrices.add(new Matrix4f());
                jointMatrices_backend.add(new Matrix4f());
                jointNormalMatrices.add(new Matrix4f());
                jointNormalMatrices_backend.add(new Matrix4f());
            }
            Matrix4f jointMatrix = jointMatrices_backend.get(i);
            if (ibmAccessor == null) {
                Matrix4f.identity(jointMatrix); //ibm accessor is optional
            } else {
                //TODO check if Matrix4fStack would be better
                loadMatrix(jointMatrix, i * 16); //Inverse bind matrix for joint
            }

            Matrix4f.mul(renderNode.getWorldTransform(), jointMatrix, jointMatrix);//Global transform of joint node
            Matrix4f.mul(mesh.getInverseWorldTransform(), jointMatrix, jointMatrix);//Global transform of mesh node

            Matrix4f normalMatrix = jointNormalMatrices_backend.get(i);
            Matrix4f.load(jointMatrix, normalMatrix);
            Matrix4f.invert(normalMatrix, normalMatrix);
            Matrix4f.transpose(normalMatrix, normalMatrix);
        }
    }

    private List<RenderNode> getJoints() {
        if (joints == null) {
            jointResolver.run();
        }
        return joints;
    }

    private float[] loadMatrix(Matrix4f m, int primitiveIndex) {
        float[] store = m.mat;
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

    public List<Matrix4f> getJointMatrices() {
        return jointMatrices;
    }

    public List<Matrix4f> getJointNormalMatrices() {
        return jointNormalMatrices;
    }

    @Override
    public void swap() {
        for (int i = 0, imax = getJoints().size(); i < imax; i++) {
            RenderNode renderNode = getJoints().get(i);
            Gutil.mat4x4_dup(jointMatrices.get(i).mat, jointMatrices_backend.get(i).mat);
            Gutil.mat4x4_dup(jointNormalMatrices.get(i).mat, jointNormalMatrices_backend.get(i).mat);
        }
    }
}
