/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

/**
 * The material's alpha rendering mode enumeration specifying the interpretation of the alpha value
 * of the main factor and texture.
 */
public enum GLTFAlphaMode {
    /**
     * The alpha value is ignored and the rendered output is fully opaque.
     */
    OPAQUE,

    /**
     * The rendered output is either fully opaque or fully transparent depending on the alpha value
     * and the specified alpha cutoff value.
     */
    MASK,

    /**
     * The alpha value is used to composite the source and destination areas. The rendered output is
     * combined with the background using the normal painting operation (i.e. the Porter and Duff over
     * operator).
     */
    BLEND
}
