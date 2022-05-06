/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader;

import org.mini.g3d.core.gltf2.loader.data.AnimationClip;
import org.mini.g3d.core.gltf2.loader.data.GLTF;
import org.mini.gui.GCallBack;
import org.mini.gui.GToolkit;
import org.mini.json.JsonParser;

import java.nio.ByteBuffer;

public class GLTFImporter {

    JsonParser<GLTF> mapper = new JsonParser<>();


    public GLTFImporter() {
        mapper.setClassLoader(GCallBack.getInstance().getApplication().getClass().getClassLoader());
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
                glbLoader.parseGLB(path);
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


    static public GLTF loadFile(String path) {

        GLTFImporter gltfImporter = new GLTFImporter();
        //Clear before loading

        GLTF gltf;
        path = path.replace('\\', '/');
        gltf = gltfImporter.load(path);
        if (gltf == null) {
            throw new RuntimeException();
        }
        return gltf;
    }


    static public AnimationClip loadAnimationClip(String path) {
        String json = GToolkit.readFileFromJarAsString(path, "utf-8");
        JsonParser parser = new JsonParser();
        parser.setClassLoader(GCallBack.getInstance().getApplication().getClass().getClassLoader());
        AnimationClip aniClip = (AnimationClip) parser.deserial(json, AnimationClip.class);
        return aniClip;
    }
}
