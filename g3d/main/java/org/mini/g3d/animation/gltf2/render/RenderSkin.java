/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.render;

import org.mini.g3d.animation.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.animation.gltf2.loader.data.GLTFNode;
import org.mini.g3d.animation.gltf2.loader.data.GLTFSkin;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.util.SysLog;

import java.util.ArrayList;
import java.util.List;

public class RenderSkin {

    GLTFSkin gltfSkin;
    private final Runnable jointResolver;
    private List<RenderNode> joints; //Need to retain order for calculation
    private final RenderNode skeletonRootNode;
    private final GLTFAccessor ibmAccessor;
    private List<Matrix4f> jointMatrices;
    private List<Matrix4f> jointNormalMatrices;

    //
    RenderSkin substitute;

    public RenderSkin(GLTFSkin skin, RenderNode parent) {
        this.gltfSkin = skin;
        this.jointMatrices = new ArrayList<>();
        this.jointNormalMatrices = new ArrayList<>();
        this.skeletonRootNode = parent.getTopParent().findRenderNode(skin.getSkeletonRootNode());
        this.ibmAccessor = skin.getInverseBindMatricesAccessor();
        this.jointResolver = () -> {
            RenderSkin.this.joints = new ArrayList<>();
            for (GLTFNode node : skin.getJoints()) {
                RenderNode rn = parent.getTopParent().findRenderNode(node);
                if (rn != null) {
                    RenderSkin.this.joints.add(rn);
                }
            }
            for (int i = 0, imax = RenderSkin.this.joints.size(); i < imax; i++) {
                jointMatrices.add(new Matrix4f());
                jointNormalMatrices.add(new Matrix4f());
            }
        };
    }

    public void computeJoints(RenderMesh mesh) {
        for (int i = 0, imax = getJoints().size(); i < imax; i++) {
            RenderNode renderNode = getJoints().get(i);
            Matrix4f jointMatrix = jointMatrices.get(i);
            if (ibmAccessor == null) {
                Matrix4f.identity(jointMatrix); //ibm accessor is optional
            } else {
                loadMatrix(jointMatrix, i * 16); //Inverse bind matrix for joint
            }

            Matrix4f.mul(renderNode.getWorldTransform(), jointMatrix, jointMatrix);//Global transform of joint node
            Matrix4f.mul(mesh.getInverseWorldTransform(), jointMatrix, jointMatrix);//Global transform of mesh node

            Matrix4f normalMatrix = jointNormalMatrices.get(i);
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

    public boolean validSubstitute(RenderSkin target) {
        if (target.getJoints().size() != getJoints().size()) {
            SysLog.warn("G3D|Substitue joint size not equials");
            return false;
        }
        substitute = target;
        return true;
    }

    public void clearSubstitute() {
        substitute = null;
    }

    public String toString() {
        if (gltfSkin != null) return gltfSkin.getName();
        return super.toString();
    }

    public GLTFSkin getGltfSkin() {
        return gltfSkin;
    }
}
