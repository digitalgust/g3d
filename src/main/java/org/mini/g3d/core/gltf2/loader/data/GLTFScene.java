/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

import java.util.LinkedHashSet;
import java.util.Set;

public class GLTFScene extends GLTFChildOfRootProperty {

    /**
     * The indices of each root node. minItems 1
     */
    private Set<GLTFNode> rootNodes;

    /**
     * Set of GLTFNode that are the root nodes for this GLTFScene
     */
    public Set<GLTFNode> getRootNodes() {
        return rootNodes;
    }

    public void setNodes(Set<Integer> indexSet) {
        gltf.indexResolvers.add(() -> {
            rootNodes = new LinkedHashSet<>();
            indexSet.forEach(index -> rootNodes.add(gltf.getNode(index)));
        });
    }
}
