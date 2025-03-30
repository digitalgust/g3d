/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader;

import org.mini.g3d.animation.gltf2.loader.data.GLTF;
import org.mini.util.SysLog;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GLBLoader {


    private enum ChunkType {
        JSON(0x4E4F534A),
        BIN(0x004E4942);

        private int type;

        ChunkType(int type) {
            this.type = type;
        }

        static ChunkType findTypeString(int type) {
            for (ChunkType eVal : ChunkType.values()) {
                if (eVal.type == type) {
                    return eVal;
                }
            }
            return null;
        }
    }

    private static final byte[] magic = "glTF".getBytes();

    private Map<ChunkType, ByteBuffer> chunkBufferMap = new HashMap();
    private GLTFImporter importer;

    GLBLoader(GLTFImporter importer) {
        this.importer = importer;
    }

    void parseGLB(String path) throws IOException {
        //12-byte preamble `header`
        //One or more `chunks` that contain JSON and binary data
        //Possible to reference external resources and other chunks

        ByteBuffer glb = GLTF.getDirectByteBuffer(path);

        assert (glb.order() == ByteOrder.LITTLE_ENDIAN);

        //Read magic
        byte[] magicRead = new byte[magic.length];
        glb.get(magicRead);
        if (!Arrays.equals(magicRead, magic)) {
            SysLog.error("G3D|GLB file is not a valid glb file.");
            return;
        }

        //TODO check version
        int version = glb.getInt();
        int length = glb.getInt();

        try {
            while (glb.hasRemaining()) {
                int chunkLength = glb.getInt();
                ChunkType chunkType = ChunkType.findTypeString(glb.getInt());
//        ByteBuffer chunkBuffer = glb.slice(glb.position(), chunkLength);
                int pos = glb.position();
                int limit = glb.limit();
                glb.position(pos);
                glb.limit(pos + chunkLength);
                ByteBuffer chunkBuffer = glb.slice().order(ByteOrder.LITTLE_ENDIAN);
                glb.position(pos);
                glb.limit(limit);
                chunkBuffer.position(0);
                chunkBufferMap.put(chunkType, chunkBuffer);

                glb.position(glb.position() + chunkLength);
            }
        } catch (Exception e) {
            SysLog.error("G3D|Error loading glb file: " + e.getMessage());
        }
    }

    ByteBuffer jsonData() {
        return chunkBufferMap.get(ChunkType.JSON);
    }

    ByteBuffer binData() {
        return chunkBufferMap.get(ChunkType.BIN);
    }
}
