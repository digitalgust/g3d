/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

/**
 * A texture and its sampler.
 */
public class GLTFTexture extends GLTFChildOfRootProperty {

    private static final GLTFSampler defaultSampler = new GLTFSampler();

    /**
     * The index of the sampler used by this texture. When undefined, a sampler with repeat wrapping
     * and auto filtering should be used.
     */
    private GLTFSampler sampler = defaultSampler;
    /**
     * The index of the image used by this texture. When undefined, it is expected that an extension
     * or other mechanism will supply an alternate texture source, otherwise behavior is undefined.
     */
    private GLTFImage sourceImage;

    public GLTFImage getSourceImage() {
        return sourceImage;
    }

    public void setSource(int index) {
        gltf.indexResolvers.add(() -> sourceImage = gltf.getImage(index));
    }

    public GLTFSampler getSampler() {
        return sampler;
    }

    public void setSampler(int index) {
        gltf.indexResolvers.add(() -> sampler = gltf.getSampler(index));
    }
}
