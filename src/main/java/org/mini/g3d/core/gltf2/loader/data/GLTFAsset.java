/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

/**
 * Metadata about the glTF asset.
 */
public class GLTFAsset extends GLTFProperty {

    /**
     * A copyright message suitable for display to credit the content creator.
     */
    private String copyright;

    /**
     * Tool that generated this glTF model. Useful for debugging.
     */
    private String generator;

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * The glTF version that this asset targets. TODO pattern "^[0-9]+\\.[0-9]+$"
     */
    private String version;

    /**
     * The minimum glTF version that this asset targets. TODO pattern "^[0-9]+\\.[0-9]+$"
     */
    private String minVersion;

    public String getCopyright() {
        return copyright;
    }

    public String getGenerator() {
        return generator;
    }

    public String getVersion() {
        return version;
    }

    public String getMinVersion() {
        return minVersion;
    }
}
