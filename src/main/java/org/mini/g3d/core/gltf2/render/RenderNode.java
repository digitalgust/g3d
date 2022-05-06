/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;

import org.mini.g3d.core.BackendSuported;
import org.mini.g3d.core.gltf2.loader.data.GLTFNode;
import org.mini.g3d.core.vector.*;
import org.mini.glwrap.GLUtil;
import org.mini.gl.GLMath;

import java.util.ArrayList;
import java.util.List;

public class RenderNode implements BackendSuported {

    private static final String EXTRA_KEY = "_RenderNode";
    protected AABBf boundingBox;
    private RenderSkin skin;
    private final Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
    private final Vector3f translation = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private final Matrix4f worldTransform = new Matrix4f();
    private final Matrix4f worldTransform_backend = new Matrix4f();
    private final Matrix4f inverseWorldTransform = new Matrix4f();
    private final Matrix4f inverseWorldTransform_backend = new Matrix4f();
    private final Matrix4f normalMatrix = new Matrix4f();
    private final List<RenderNode> children = new ArrayList<>();
    private boolean changed;
    private Matrix4f localTransform = new Matrix4f();
    private Matrix4f localTransform_backend = new Matrix4f();
    private GLTFNode gltfnode;
    private RenderNode parent;

    public RenderNode(GLTFNode node, RenderNode parent) {
        gltfnode = node;
        this.parent = parent;
        if (node != null) {
            //node.getExtras().put(EXTRA_KEY, this);
            if (node.getMatrix() != null) {
                applyMatrix(node.getMatrix());
            } else {
                Vector3f scalef = node.getScale();
                scale.set(scalef);

                Quaternionf rotf = node.getRotation();
                rotation.set(rotf);

                Vector3f traf = node.getTranslation();
                translation.set(traf);
            }
            if (node.getSkin() != null) {
                this.skin = new RenderSkin(node.getSkin(), this);
            }
        }

        //Register as child
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public RenderNode from(GLTFNode node) {
        if (node == null) return null;
        if (node == gltfnode) {
            return this;
        }
        for (RenderNode child : children) {
            RenderNode rn = child.from(node);
            if (rn != null) return rn;
        }

        return null;
    }

    public RenderNode getTopParent() {
        if (parent != null) return parent.getTopParent();
        return this;
    }


    void addChild(RenderNode child) {
        this.children.add(child);
    }

    public List<RenderNode> getChildren() {
        return this.children;
    }

    private void applyMatrix(Matrix4f floatMatrix) {
        GLMath.mat4x4_dup(localTransform_backend.mat, floatMatrix.mat);
        GLMath.mat4x4_dup(localTransform.mat, floatMatrix.mat);
        localTransform.getScale(scale);
        //localTransform.getUnnormalizedRotation(rotation);
        Matrix4f.getRotation(rotation, localTransform);
        localTransform.getTranslation(translation);
        changed = true;
    }

    private Matrix4f getLocalTransform_backend() {
        if (localTransform_backend == null) {
            localTransform_backend = new Matrix4f();
            changed = true;
        }
        if (changed) {
            localTransform_backend.identity();
            Matrix4f.translationRotateScale(translation, rotation, scale, localTransform_backend);
            changed = false;
        }
        return this.localTransform_backend;
    }

    public Matrix4f getWorldTransform() {
        assert (!changed);
        return this.worldTransform;
    }

    public Matrix4f getInverseWorldTransform() {
        return inverseWorldTransform;
    }

    public void applyTransform(Matrix4f parentTransform) {
        Matrix4f localTransform = getLocalTransform_backend();
        Matrix4f.mul(parentTransform, localTransform, worldTransform_backend);
        Matrix4f.invert(worldTransform, inverseWorldTransform_backend);
        inverseWorldTransform_backend.transpose(normalMatrix);

        for (int i = 0, imax = children.size(); i < imax; i++) {
            RenderNode child = children.get(i);
            child.applyTransform(worldTransform_backend);
        }
    }

    public Matrix4f getNormalMatrix() {
        return normalMatrix;
    }

    /**
     * Get the axis aligned bounding box for this node
     *
     * @return
     */
    public AABBf getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new AABBf();
        }
        return boundingBox;
    }

    public Vector3f getTranslation() {
        this.changed = true;
        return this.translation;
    }

    public Vector3f getScale() {
        this.changed = true;
        return this.scale;
    }

    public Quaternionf getRotation() {
        this.changed = true;
        return this.rotation;
    }

    public RenderSkin getSkin() {
        return skin;
    }

    public void updateSkin() {
        for (int i = 0, imax = children.size(); i < imax; i++) {
            RenderNode child = children.get(i);
            child.updateSkin();
        }
    }

    @Override
    public void swap() {

        GLMath.mat4x4_dup(worldTransform.mat, worldTransform_backend.mat);
        GLMath.mat4x4_dup(inverseWorldTransform.mat, inverseWorldTransform_backend.mat);
        GLMath.mat4x4_dup(localTransform.mat, localTransform_backend.mat);
        for (int i = 0, imax = children.size(); i < imax; i++) {
            RenderNode child = children.get(i);
            child.swap();
        }
    }
}
