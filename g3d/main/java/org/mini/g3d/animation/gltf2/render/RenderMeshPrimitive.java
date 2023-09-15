/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.render;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.animation.gltf2.AnimatedShader;
import org.mini.g3d.animation.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.animation.gltf2.loader.data.GLTFMeshPrimitive;
import org.mini.g3d.animation.gltf2.loader.data.GLTFNode;
import org.mini.g3d.core.vector.AABBf;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Container object for a GLTFMeshPrimitive and some metadata used by renderer
 * KhronosGroup/glTF-Sample-Viewer puts this data in GLTFMeshPrimitive directly
 */
public class RenderMeshPrimitive extends RenderNode {


    //====================================================
    //for cache the program id

    public AnimatedShader getShader() {
        return shader;
    }

    public void setShader(AnimatedShader shader) {
        this.shader = shader;
    }

    AnimatedShader shader = null;

    //====================================================

    /**
     * Very similar to GLTFMeshPrimitive.getAttributes but the string is a variable in the shader
     * code
     */
    private final Map<String, GLTFAccessor> glAttributes = new HashMap<>();
    private final List<String> defines = new ArrayList<>();
    private boolean skip = true; //Spec defines if position does not exist then skip
    private boolean hasWeights = false;
    private boolean hasJoints = false;

    private final RenderMesh mesh;
    private final GLTFMeshPrimitive gltfPrimitive;
    private RenderMaterial material;

    public RenderMeshPrimitive(GLTFMeshPrimitive primitive, GLTFNode node, RenderMesh parentNode) {
        super(node, parentNode);
        this.gltfPrimitive = primitive;
        this.mesh = parentNode;

        if (primitive.getMaterial() != null)
            this.material = new RenderMaterial(primitive.getMaterial());

        for (String key : primitive.getAttributes().keySet()) {
            //TODO error checking for max vertex attribs

            GLTFAccessor accessor = primitive.getAttributes().get(key);
            switch (key) {
                case "POSITION":
                    this.skip = false;
                    glAttributes.put("a_Position", accessor);
                    break;
                case "NORMAL":
                    defines.add("HAS_NORMALS 1");
                    glAttributes.put("a_Normal", accessor);
                    break;
                case "TANGENT":
                    defines.add("HAS_TANGENTS 1");
                    glAttributes.put("a_Tangent", accessor);
                    break;
                case "TEXCOORD_0":
                    defines.add("HAS_UV_SET1 1");
                    glAttributes.put("a_UV1", accessor);
                    break;
                case "TEXCOORD_1":
                    defines.add("HAS_UV_SET2 1");
                    glAttributes.put("a_UV2", accessor);
                    break;
                case "COLOR_0":
                    //如果有贴图,则忽略掉顶点色
                    if (primitive.getAttributes().get("TEXCOORD_0") == null && primitive.getAttributes().get("TEXCOORD_1") == null) {
                        //e.g. VEC3 , VEC4
                        defines.add("HAS_VERTEX_COLOR_" + accessor.getType().name() + " 1");
                        glAttributes.put("a_Color", accessor);
                    }
                    break;
                case "JOINTS_0":
                    hasJoints = true;
                    defines.add("HAS_JOINT_SET1 1");
                    glAttributes.put("a_Joint1", accessor);
                    break;
                case "WEIGHTS_0":
                    hasWeights = true;
                    defines.add("HAS_WEIGHT_SET1 1");
                    glAttributes.put("a_Weight1", accessor);
                    break;
                case "JOINTS_1":
                    hasJoints = true;
                    defines.add("HAS_JOINT_SET2 1");
                    glAttributes.put("a_Joint2", accessor);
                    break;
                case "WEIGHTS_1":
                    hasWeights = true;
                    defines.add("HAS_WEIGHT_SET2 1");
                    glAttributes.put("a_Weight2", accessor);
                    break;
                default:
                    System.err.println("[G3D][ERROR]Unknown attribute: " + key);
            }
        }

        if (primitive.getMorphTargets() != null) {
            int i = 0;
            for (Map<String, GLTFAccessor> map : primitive.getMorphTargets()) {
                //TODO test for max attributes

                for (Entry<String, GLTFAccessor> entry : map.entrySet()) {
                    String attribute = entry.getKey();
                    GLTFAccessor accessor = entry.getValue();

                    switch (attribute) {
                        case "POSITION":
                            defines.add("HAS_TARGET_POSITION" + i + " 1");
                            glAttributes.put("a_Target_Position" + i, accessor);
                            break;
                        case "NORMAL":
                            defines.add("HAS_TARGET_NORMAL" + i + " 1");
                            glAttributes.put("a_Target_Normal" + i, accessor);
                            break;
                        case "TANGENT":
                            defines.add("HAS_TARGET_TANGENT" + i + " 1");
                            glAttributes.put("a_Target_Tangent" + i, accessor);
                            break;
                        default:
                            System.out.println("[G3D][WARN]Unhandled morph target: " + attribute);
                            break;
                    }
                }
                i++;
            }
        }
    }

    @Override
    public AABBf getBoundingBox() {
        if (boundingBox == null) {
            boundingBox = new AABBf();
            GLTFAccessor accessor = this.getGltfMeshPrimitive().getAttributes().get("POSITION");

            if (accessor == null) {
                return boundingBox;
            }

            float[] maxList = accessor.getMax();
            Vector3f max = new Vector3f(maxList[0], maxList[1], maxList[2]);

            float[] minList = accessor.getMin();
            Vector3f min = new Vector3f(minList[0], minList[1], minList[2]);

            boundingBox.union(max).union(min).transform(getLocalTransform());
        }
        return boundingBox;
    }

    public Map<String, GLTFAccessor> getGlAttributes() {
        return glAttributes;
    }

    public List<String> getDefines() {
        return defines;
    }

    public String getShaderIdentifier() {
        return "gltfVertex.glsl";
    }

    public boolean isSkip() {
        return skip;
    }

    public GLTFMeshPrimitive getGltfMeshPrimitive() {
        return gltfPrimitive;
    }

    public Matrix4f[] getModelMatrics() {
        Matrix4f[] joints = null;
        if (gltfPrimitive != null) {
            joints = gltfPrimitive.getModelMatrics();
        }
        if (joints == null) {
            if (replaced != null && replaced instanceof RenderMeshPrimitive) {//找到了被替换武器
                RenderMeshPrimitive rmp = (RenderMeshPrimitive) replaced;
                if (rmp.getGltfMeshPrimitive() != null) {
                    joints = rmp.getGltfMeshPrimitive().getModelMatrics();
                }
            }
        }
        return joints;
    }

    public Matrix4f[] getModelNormMatrics() {
        Matrix4f[] jointsNorm = null;
        if (gltfPrimitive != null) {
            jointsNorm = gltfPrimitive.getModelNormMatrics();
        }
        if (jointsNorm == null) {
            if (replaced != null && replaced instanceof RenderMeshPrimitive) {//找到了被替换武器
                RenderMeshPrimitive rmp = (RenderMeshPrimitive) replaced;
                if (rmp.getGltfMeshPrimitive() != null) {
                    jointsNorm = rmp.getGltfMeshPrimitive().getModelNormMatrics();
                }
            }
        }
        return jointsNorm;
    }

    @Override
    public AnimatedModel getAnimatedModel() {
        if (replaced != null) {
            return replaced.getAnimatedModel();
        } else {
            return super.getAnimatedModel();
        }
    }

    public RenderMaterial getMaterial() {
        if (material != null) {
            return material;
        } else {
            return RenderMaterial.defaultMaterial;
        }
    }

    public RenderMesh getMesh() {
        return mesh;
    }
}
