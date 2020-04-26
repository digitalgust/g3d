/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

/**
 * Reference to a texture.
 */
public class GLTFTextureInfo extends GLTFProperty {

    /**
     * The index of the texture.
     */
    private GLTFTexture texture;

    public void setTexCoord(int texCoord) {
        this.texCoord = texCoord;
    }

    /**
     * This integer value is used to construct a string in the format `TEXCOORD_<set index>` which is
     * a reference to a key in mesh.primitives.attributes (e.g. A value of `0` corresponds to
     * `TEXCOORD_0`). Mesh must have corresponding texture coordinate attributes for the material to
     * be applicable to it.
     */
    private int texCoord = 0;

    public int getTexCoord() {
        return this.texCoord;
    }

    public GLTFTexture getTexture() {
        return texture;
    }

    public void setIndex(int index) {
        gltf.indexResolvers.add(() -> texture = gltf.getTexture(index));
    }

    public static class GLTFNormalTextureInfo extends GLTFTextureInfo {

        /**
         * The scalar multiplier applied to each normal vector of the texture. This value scales the
         * normal vector using the formula: `scaledNormal =  normalize((<sampled normal texture value> *
         * 2.0 - 1.0) * vec3(<normal scale>, <normal scale>, 1.0))`. This value is ignored if
         * normalTexture is not specified. This value is linear.
         */
        private float scale = 1.0f;

        public float getScale() {
            return scale;
        }
    }

    /**
     * Material Occlusion Texture Info
     */
    public static class GLTFOcclusionTextureInfo extends GLTFTextureInfo {

        /**
         * A scalar multiplier controlling the amount of occlusion applied. A value of 0.0 means no
         * occlusion. A value of 1.0 means full occlusion. This value affects the resulting color using
         * the formula: `occludedColor = lerp(color, color * <sampled occlusion texture value>,
         * <occlusion strength>)`. This value is ignored if the corresponding texture is not specified.
         * This value is linear.
         */
        private float strength = 1.0f;

        public float getStrength() {
            return strength;
        }
    }


    /**
     * none of gltf
     * ============================================
     */

    int texid = -1;

    public int getTexid() {
        return texid;
    }

    public void setTexid(int texid) {
        this.texid = texid;
    }

}
