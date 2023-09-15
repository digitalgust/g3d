/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.render;


import org.mini.g3d.animation.gltf2.GLDriver;
import org.mini.g3d.animation.gltf2.loader.data.GLTF;
import org.mini.g3d.animation.gltf2.loader.data.GLTFSampler;
import org.mini.g3d.animation.gltf2.loader.data.GLTFTextureInfo;
import org.mini.glwrap.GLUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

import static org.mini.gl.GL.*;

public class RenderTexture {

    private int mipLevel = 0;
    private GLTFSampler sampler;
    private Supplier<ByteBuffer> getData;
    private int type = GL_TEXTURE_2D; //TODO I think type should be called target

    private ByteBuffer data;
    private int width = -1;
    private int height = -1;
    private int pixBytes = -1;

    GLTFTextureInfo info;

    public RenderTexture(GLTFTextureInfo info) {
        this.info = info;
        this.sampler = info.getTexture().getSampler();
        getData = info.getTexture().getSourceImage()::getDirectByteBuffer;
    }

    //Initialize a texture not referenced by the glTF file
    public RenderTexture(String imagePath, int type) {
        this(imagePath, type, 0);
    }

    public RenderTexture(String imagePath, int type, int mipLevel) {
        if (imagePath != null) {
            getData = () -> GLTF.getDirectByteBuffer(imagePath);
        }
        this.type = type;
        this.mipLevel = mipLevel;
    }

    RenderTexture(InputStream stream, int type) {
        getData = () -> {
            try {
                //gust
                byte[] bytes = new byte[stream.available()];
                stream.read(bytes, 0, bytes.length);
//        var bytes = stream.readAllBytes();
                ByteBuffer dbb = ByteBuffer.allocateDirect(bytes.length);
                dbb.put(bytes);
                return dbb;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ByteBuffer.allocateDirect(0);
        };
        this.type = type;
        this.mipLevel = 0;
    }

    public RenderTexture(int type) {
        this.type = type;
        this.mipLevel = 0;
    }

    public void bindTexture() {
        int texid = GLDriver.getTexture(this, info);
        glBindTexture(getType(), texid);
    }


    public GLTFSampler getSampler() {
        return sampler;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public ByteBuffer loadData() {
        if (data == null) {
            ByteBuffer dataBuffer = getData.get();
            int[] whd = new int[3];
            byte[] b = GLUtil.image_parse_from_file_content(dataBuffer.array(), dataBuffer.arrayOffset(), dataBuffer.capacity(), whd);

            data = ByteBuffer.wrap(b);
            this.width = whd[0];
            this.height = whd[1];
            this.pixBytes = whd[2];
        }
        return data;
    }

    public int getTextureWidth() {
        return width;
    }

    public int getTextureHeight() {
        return height;
    }

    public int getPixBytes() {
        return pixBytes;
    }

    public boolean shouldGenerateMips() {
        int width = getTextureWidth();
        int height = getTextureHeight();
        //If an int is a power of 2 then only its highest bit is set.
        //Subtracting one sets all lower bits to one, therefore there is no overlap.
        return (width > 0 && (width & width - 1) == 0) && (height > 0 && (height & height - 1) == 0);

    }

    public int getMipLevel() {
        return mipLevel;
    }
}
