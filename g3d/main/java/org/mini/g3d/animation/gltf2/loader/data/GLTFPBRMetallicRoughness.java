/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;


import org.mini.g3d.core.vector.Vector4f;

/**
 * A set of parameter values that are used to define the metallic-roughness material model from
 * Physically-Based Rendering (PBR) methodology.
 */
public class GLTFPBRMetallicRoughness extends GLTFProperty {

    /**
     * The RGBA components of the base color of the material. The fourth component (A) is the alpha
     * coverage of the material. The `alphaMode` property specifies how alpha is interpreted. These
     * values are linear. If a baseColorTexture is specified, this value is multiplied with the texel
     * values.
     */
    private Vector4f baseColorFactor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    public void setBaseColorTexture(GLTFTextureInfo baseColorTexture) {
        this.baseColorTexture = baseColorTexture;
    }

    /**
     * The base color texture. The first three components (RGB) are encoded with the sRGB transfer
     * function. They specify the base color of the material. If the fourth component (A) is present,
     * it represents the linear alpha coverage of the material. Otherwise, an alpha of 1.0 is assumed.
     * The `alphaMode` property specifies how alpha is interpreted. The stored texels must not be
     * premultiplied.
     */
    private GLTFTextureInfo baseColorTexture;

    /**
     * The metalness of the material. A value of 1.0 means the material is a metal. A value of 0.0
     * means the material is a dielectric. Values in between are for blending between metals and
     * dielectrics such as dirty metallic surfaces. This value is linear. If a
     * metallicRoughnessTexture is specified, this value is multiplied with the metallic texel
     * values.
     */
    private float metallicFactor = 0.2f;  //this value is 1.0 the model is black

    public void setMetallicFactor(float metallicFactor) {
        this.metallicFactor = metallicFactor;
    }

    public void setRoughnessFactor(float roughnessFactor) {
        this.roughnessFactor = roughnessFactor;
    }

    public void setMetallicRoughnessTexture(GLTFTextureInfo metallicRoughnessTexture) {
        this.metallicRoughnessTexture = metallicRoughnessTexture;
    }

    /**
     * The roughness of the material. A value of 1.0 means the material is completely rough. A value
     * of 0.0 means the material is completely smooth. This value is linear. If a
     * metallicRoughnessTexture is specified, this value is multiplied with the roughness texel
     * values.
     */
    private float roughnessFactor = 0.8f;

    /**
     * The metallic-roughness texture. The metalness values are sampled from the B channel. The
     * roughness values are sampled from the G channel. These values are linear. If other channels are
     * present (R or A), they are ignored for metallic-roughness calculations.
     */
    private GLTFTextureInfo metallicRoughnessTexture;

    public void setBaseColorFactor(Vector4f baseColorFactor) {
        this.baseColorFactor = baseColorFactor;
    }

    public Vector4f getBaseColorFactor() {
        return baseColorFactor;
    }

    public GLTFTextureInfo getBaseColorTexture() {
        return baseColorTexture;
    }

    public float getMetallicFactor() {
        return metallicFactor;
    }

    public float getRoughnessFactor() {
        return roughnessFactor;
    }

    public GLTFTextureInfo getMetallicRoughnessTexture() {
        return metallicRoughnessTexture;
    }
}
