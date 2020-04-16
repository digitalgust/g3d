/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader;

import org.mini.g3d.core.gltf2.loader.data.GLTF;
import org.mini.json.JsonParser;

import java.nio.ByteBuffer;

public class GLTFImporter {

    JsonParser<GLTF> mapper = new JsonParser();


    public GLTFImporter() {
        mapper.registerModule(GLTFJsonModule.getModule());
    }

    public GLTF load(String path) {
        try {
            String jsonStr = null;
            GLTF gltf = new GLTF();
            gltf.setSource(path, GLTF.ResourceFrom.JAR);

            if (path.endsWith(".glb")) {
                System.out.println("Loading .glb file: " + path);
                GLBLoader glbLoader = new GLBLoader(this);
                glbLoader.procesGLB(path);
                ByteBuffer jasonb = glbLoader.jsonData();
                byte[] b = new byte[jasonb.capacity()];
                jasonb.get(b);
                jsonStr = new String(b, "utf-8");

                gltf.setBin(glbLoader.binData());
            } else {
                ByteBuffer jasonb = GLTF.getDirectByteBuffer(path);
                byte[] b = new byte[jasonb.capacity()];
                jasonb.rewind();
                jasonb.get(b);
                jsonStr = new String(b, "utf-8");
            }

            JsonParser.InjectableValues v = new JsonParser.InjectableValues();
            v.addValue(GLTF.class, gltf);
            mapper.setInjectableValues(v);
            gltf = mapper.deserial(jsonStr, GLTF.class);
            gltf.applyLookupMap();


            return gltf;
        } catch (Exception e) {
            System.out.println("Error loading gltf file: " + path.toString());
            e.printStackTrace();
            return null;
        }
    }


}
