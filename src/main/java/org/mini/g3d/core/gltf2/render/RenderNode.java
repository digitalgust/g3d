/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;

import org.mini.g3d.core.gltf2.loader.data.GLTFNode;
import org.mini.g3d.core.vector.AABBf;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Quaternionf;
import org.mini.g3d.core.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RenderNode {

    private static final String EXTRA_KEY = "_RenderNode";
    protected AABBf boundingBox;
    private RenderSkin skin;
    private Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
    private Vector3f translation = new Vector3f();
    private Quaternionf rotation = new Quaternionf();
    private final Matrix4f worldTransform = new Matrix4f();
    private final Matrix4f inverseWorldTransform = new Matrix4f();
    private final Matrix4f normalMatrix = new Matrix4f();
    private final List<RenderNode> children = new ArrayList<>();
    private boolean changed;
    private Matrix4f localTransform = null;

    public RenderNode(GLTFNode node, RenderNode parent) {

        if (node != null) {
            node.getExtras().put(EXTRA_KEY, this);
            if (node.getMatrix() != null) {
                applyMatrix(node.getMatrix());
            } else {
                Vector3f scalef = node.getScale();
                scale = new Vector3f(scalef);

                Quaternionf rotf = node.getRotation();
                rotation = new Quaternionf().set(rotf);

                Vector3f traf = node.getTranslation();
                translation = new Vector3f(traf);
            }
            if (node.getSkin() != null) {
                this.skin = new RenderSkin(node.getSkin());
            }
        }

        //Register as child
        if (parent != null) {
            parent.addChild(this);
        }
    }

    public static RenderNode from(GLTFNode node) {
        if (node == null) return null;
        Map extras = node.getExtras();
        if (extras.containsKey(EXTRA_KEY)) {
            return (RenderNode) extras.get(EXTRA_KEY);
        } else {
            return null;
        }
    }


    void addChild(RenderNode child) {
        this.children.add(child);
    }

    public List<RenderNode> getChildren() {
        return this.children;
    }

    private void applyMatrix(Matrix4f floatMatrix) {
        Matrix4f matrix = new Matrix4f(floatMatrix);
        localTransform = matrix;
        matrix.getScale(scale);
        matrix.getUnnormalizedRotation(rotation);
        matrix.getTranslation(translation);
        changed = true;
    }

    private Matrix4f getLocalTransform() {
        if (localTransform == null) {
            localTransform = new Matrix4f();
            changed = true;
        }
        if (changed) {
            localTransform.identity();
            Matrix4f.translationRotateScale(translation, rotation, scale, localTransform);
            changed = false;
        }
        return this.localTransform;
    }

    public Matrix4f getWorldTransform() {
        assert (!changed);
        return this.worldTransform;
    }

    public Matrix4f getInverseWorldTransform() {
        return inverseWorldTransform;
    }

    public void applyTransform(Matrix4f parentTransform) {
        Matrix4f localTransform = getLocalTransform();
        Matrix4f.mul(parentTransform, localTransform, worldTransform);
        Matrix4f.invert(worldTransform, inverseWorldTransform);
        inverseWorldTransform.transpose(normalMatrix);

        for (RenderNode child : children) {
            child.applyTransform(this.getWorldTransform());
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
        for (RenderNode child : children) {
            child.updateSkin();
        }
    }
}
