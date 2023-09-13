/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.render;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.animation.gltf2.loader.data.GLTFNode;
import org.mini.g3d.animation.gltf2.loader.data.GLTFSkin;
import org.mini.g3d.core.vector.AABBf;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Quaternionf;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.gl.GLMath;

import java.util.ArrayList;
import java.util.List;

public class RenderNode {

    private static final String EXTRA_KEY = "_RenderNode";
    protected AABBf boundingBox;
    protected RenderSkin skin;
    private final Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
    private final Vector3f translation = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private final Matrix4f worldTransform = new Matrix4f();
    private final Matrix4f inverseWorldTransform = new Matrix4f();
    private final Matrix4f normalMatrix = new Matrix4f();
    private final List<RenderNode> children = new ArrayList<>();
    private boolean changed;
    private Matrix4f localTransform = new Matrix4f();
    private GLTFNode gltfnode;
    private RenderNode parent;
    AnimatedModel animatedModel;

    //替换装备位,具有相同名字的结点会在渲染时被替换
    RenderNode replacer;//新武器
    RenderNode replaced;//老武器， 模型原本的武器

    public RenderNode(AnimatedModel animatedModel) {
        this.animatedModel = animatedModel;
    }

    /**
     * @param node
     * @param parent
     */
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
                this.skin = getTopParent().findRenderSkin(node.getSkin());//相同蒙皮使用同一实例, 这样可以减少蒙皮重复计算
//                if (this.skin != null) {
//                    int debug = 1;
//                }
                if (this.skin == null) {
                    this.skin = new RenderSkin(node.getSkin(), this);
                }
            }
        }

        //Register as child
        if (parent != null) {
            parent.addChild(this);
        }
        changed = true;
    }

    /**
     * 从体系中查找某GLTFNode同源的RenderNode
     *
     * @param node
     * @return
     */
    public RenderNode findRenderNode(GLTFNode node) {
        if (node == null) return null;
        if (node == gltfnode) {
            return this;
        }
        for (RenderNode child : children) {
            RenderNode rn = child.findRenderNode(node);
            if (rn != null) return rn;
        }

        return null;
    }

    /**
     * 从体系中查找GLTFSkin同源的RenderSkin
     *
     * @param gltfSkin
     * @return
     */
    public RenderSkin findRenderSkin(GLTFSkin gltfSkin) {
        if (gltfSkin == null) return null;
        if (gltfnode != null && gltfSkin == gltfnode.getSkin()) {
            return getSkin();
        }
        for (RenderNode child : children) {
            RenderSkin rn = child.findRenderSkin(gltfSkin);
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
        GLMath.mat4x4_dup(localTransform.mat, floatMatrix.mat);
        localTransform.getScale(scale);
        //localTransform.getUnnormalizedRotation(rotation);
        Matrix4f.getRotation(rotation, localTransform);
        localTransform.getTranslation(translation);
        changed = true;
    }

    protected Matrix4f getLocalTransform() {
        if (changed) {
            localTransform.identity();
            Matrix4f.translationRotateScale(translation, rotation, scale, localTransform);
            changed = false;
        }
        return this.localTransform;
    }

    public Matrix4f getWorldTransform() {
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

        for (int i = 0, imax = children.size(); i < imax; i++) {
            RenderNode child = children.get(i);
            child.applyTransform(worldTransform);
        }

        if (replacer != null) {//复制变换矩阵到替换模型上面
            GLMath.mat4x4_dup(replacer.worldTransform.mat, worldTransform.mat);
            GLMath.mat4x4_dup(replacer.inverseWorldTransform.mat, inverseWorldTransform.mat);
            GLMath.mat4x4_dup(replacer.localTransform.mat, localTransform.mat);
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
            for (int i = 0, imax = children.size(); i < imax; i++) {
                RenderNode child = children.get(i);
                boundingBox.union(child.getBoundingBox());
            }
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

    public void setSkin(RenderSkin s) {
        skin = s;
    }


    public AnimatedModel getAnimatedModel() {
        if (parent != null) {
            return parent.getAnimatedModel();
        }
        return animatedModel;
    }

    public RenderNode getParent() {
        return parent;
    }

    public GLTFNode getGltfNode() {
        return gltfnode;
    }

    public void clearSubstitute() {
        replacer = null;
        for (int i = 0, imax = children.size(); i < imax; i++) {
            children.get(i).clearSubstitute();
        }
        if (this.getSkin() != null) {
            this.getSkin().clearSubstitute();
        }
    }


    public boolean validSubstitute(RenderNode target) {

        //确保原装备和替换装备具有相同的模型结构
        if (children.size() != target.children.size()) {
            System.out.println("[G3D][WARN]Substitute children size not equials");
            return false;
        }
        for (int i = 0, imax = children.size(); i < imax; i++) {
            RenderNode child = children.get(i);
            child.validSubstitute(target.children.get(i));
        }
        if (this.getSkin() != null) {
            if (target.getSkin() != null) {
                boolean ret = this.getSkin().validSubstitute(target.getSkin());
                if (!ret) {
                    return false;
                }
            } else {
                System.out.println("[G3D][WARN]Sustitute skin dose not exist");
                return false;
            }
        }
        this.replacer = target;
        target.replaced = this;
        return true;
    }


    public RenderNode getReplaced() {
        return replaced;
    }

    public RenderNode getReplacer() {
        return replacer;
    }


    public void updateSkin() {
        for (int i = 0, imax = children.size(); i < imax; i++) {
            RenderNode child = children.get(i);
            child.updateSkin();
        }
    }

    public String toString() {
        if (gltfnode != null) return gltfnode.getName();
        return super.toString();
    }
}
