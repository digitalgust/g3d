/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;


import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Quaternionf;
import org.mini.g3d.core.vector.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A node in the node hierarchy.  When the node contains `skin`, all `mesh.primitives` must contain
 * `JOINTS_0` and `WEIGHTS_0` attributes.  A node can have either a `matrix` or any combination of
 * `translation`/`rotation`/`scale` (TRS) properties. TRS properties are converted to matrices and
 * postmultiplied in the `T * R * S` order to compose the transformation matrix; first the scale is
 * applied to the vertices, then the rotation, and then the translation. If none are provided, the
 * transform is the identity. When a node is targeted for animation (referenced by an
 * animation.channel.target), only TRS properties may be present; `matrix` will not be present.
 */
public class GLTFNode extends GLTFChildOfRootProperty {

    /**
     * The index of the camera referenced by this node.
     */
    private GLTFCamera camera;
    /**
     * The indices of this node's children. minItems 1
     */
    private Set<GLTFNode> children;
    /**
     * The index of the skin referenced by this node. When a skin is referenced by a node within a
     * scene, all joints used by the skin must belong to the same scene.
     */
    private GLTFSkin skin;

    public void setMatrix(Matrix4f matrix) {
        this.matrix = matrix;
    }

    /**
     * A floating-point 4x4 transformation matrix stored in column-major order. minItems 16 maxItems
     * 16
     */
    private Matrix4f matrix;
    /**
     * The index of the mesh in this node.
     */
    private GLTFMesh mesh;

    public void setRotation(Quaternionf rotation) {
        this.rotation = rotation;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public void setTranslation(Vector3f translation) {
        this.translation = translation;
    }

    /**
     * The node's unit Quaternionf rotation in the order {x, y, z, w}, where w is the scalar. maxItems
     * 4 minItems 4
     */
    private Quaternionf rotation = new Quaternionf(0, 0, 0, 1.0f);
    /**
     * The node's non-uniform scale, given as the scaling factors along the x, y, and z axes. minItems
     * 3 maxItems 3
     */
    private Vector3f scale = new Vector3f(1.0f, 1.0f, 1.0f);
    /**
     * The node's translation along the x, y, and z aces. minItems 3 maxItems 3
     */
    private Vector3f translation = new Vector3f(0.0f, 0.0f, 0.0f);

    public void setWeights(List<Float> weights) {
        this.weights = weights;
    }

    /**
     * The weights of the instantiated Morph Target. Number of elements must match number of Morph
     * Targets of used mesh. minItems 1
     */
    private List<Float> weights;

    public Set<GLTFNode> getChildren() {
        return children;
    }

    public void setChildren(Set<Integer> indexSet) {
        gltf.indexResolvers.add(() -> {
            children = new LinkedHashSet<>();
            indexSet.forEach(index -> children.add(gltf.getNode(index)));
        });
    }

    void addSelfAndAllDescendants(List<GLTFNode> nodeList) {
        nodeList.add(this);
        for (GLTFNode node : children) {
            if (!nodeList.contains(node)) {
                node.addSelfAndAllDescendants(nodeList);
            }
        }

//        getChildren().ifPresent(children -> children.stream()
//                .filter(gltfNode -> !nodeList.contains(gltfNode))
//                .forEach(gltfNode -> gltfNode.addSelfAndAllDescendants(nodeList)));
    }

    List<GLTFNode> filter(List<GLTFNode> nodeList) {
        List<GLTFNode> list = new ArrayList<>();
        for (GLTFNode node : children) {
            if (!nodeList.contains(node)) {
                list.add(node);
            }
        }
        return list;
    }


    public GLTFSkin getSkin() {
        return skin;
    }

    public void setSkin(int index) {
        gltf.indexResolvers.add(() -> skin = gltf.getSkin(index));
    }

    public GLTFCamera getCamera() {
        return camera;
    }

    public void setCamera(int index) {
        gltf.indexResolvers.add(() -> camera = gltf.getCamera(index));
    }

    public GLTFMesh getMesh() {
        return mesh;
    }

    public void setMesh(int index) {
        gltf.indexResolvers.add(() -> mesh = gltf.getMesh(index));
    }

    public Matrix4f getMatrix() {
        return matrix;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public List<Float> getWeights() {
        return weights;
    }
}
