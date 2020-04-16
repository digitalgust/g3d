/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

import java.util.*;

public class GLTFMeshPrimitive extends GLTFProperty {

    /**
     * A dictionary object, where each key corresponds to mesh attribute semantic and each value is
     * the index of the accessor containing attribute's data.
     */
    private Map<String, GLTFAccessor> attributes;
    /**
     * The type of primitives to render. All valid values correspond to WebGL enums.
     * <p>
     * default - 4 = TRIANGLES
     */
    private int mode = 4;
    /**
     * The index of the accessor that contains mesh indices. When this is not defined, the primitives
     * should be rendered without indices using `drawArrays()`. When defined, the accessor must
     * contain indices: the `bufferView` referenced by the accessor should have a `target` equal to
     * 34963 (ELEMENT_ARRAY_BUFFER); `componentType` must be 5121 (UNSIGNED_BYTE), 5123
     * (UNSIGNED_SHORT) or 5125 (UNSIGNED_INT), the latter may require enabling additional hardware
     * support; `type` must be `\
     */
    private GLTFAccessor indicesAccessor;
    /**
     * The index of the material to apply to this primitive when rendering.
     */
    private GLTFMaterial material;
    /**
     * A dictionary object specifying attributes displacements in a Morph Target, where each key
     * corresponds to one of the three supported attribute semantic (`POSITION`, `NORMAL`, or
     * `TANGENT`) and each value is the index of the accessor containing the attribute displacements'
     * data.
     */
    private List<Map<String, GLTFAccessor>> morphTargets;

    /**
     * Get a Map of references to Accessors for the AdditionalProperties of this MeshPrimitive
     *
     * @return null if attributes is null
     */
    public Map<String, GLTFAccessor> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Integer> indexAttributes) {
        attributes = new HashMap<>();
        gltf.indexResolvers.add(() -> {
            indexAttributes.forEach((key, value) -> attributes.put(key, gltf.getAccessor(value)));
        });
    }

    /**
     * Get a reference to the Accessor for this MeshPrimitive
     */
    public GLTFAccessor getIndicesAccessor() {
        return indicesAccessor;
    }

    public void setIndices(int index) {
        gltf.indexResolvers.add(() -> indicesAccessor = gltf.getAccessor(index));
    }

    /**
     * Get a reference to Material for this MeshPrimitive
     */
    public GLTFMaterial getMaterial() {
        return material;
    }

    public void setMaterial(int index) {
        gltf.indexResolvers.add(() -> material = gltf.getMaterial(index));
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    /**
     * Get the Mode for this MeshPrimitive
     */
    public int getMode() {
        return this.mode;
    }

    public List<Map<String, GLTFAccessor>> getMorphTargets() {
        return morphTargets;
    }

    private void setMorphTargets(List<Map<String, Integer>> stringIndexMapList) {
        gltf.indexResolvers.add(() -> {
            morphTargets = new ArrayList<>();
            for (Map<String, Integer> source : stringIndexMapList) {
                Map<String, GLTFAccessor> accessorMap = new HashMap<>();
                for (Map.Entry<String, Integer> entry : source.entrySet()) {
                    accessorMap.put(entry.getKey(), gltf.getAccessor(entry.getValue()));
                }
                morphTargets.add(accessorMap);
            }
        });
    }

}
