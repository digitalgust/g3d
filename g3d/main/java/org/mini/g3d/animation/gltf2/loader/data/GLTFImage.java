/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

import org.mini.gui.GToolkit;
import org.mini.util.SysLog;

import java.nio.ByteBuffer;

/**
 * Image data used to create a texture. Image can be referenced by URI or `bufferView` index.
 * `mimeType` is required in the latter case.
 */
public class GLTFImage extends GLTFChildOfRootProperty {


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * The uri of the image.  Relative paths are relative to the .gltf file.  Instead of referencing
     * an external file, the uri can also be a data-uri.  The image format must be jpg or png.
     */
    private String uri;

    /**
     * The image's MIME type. Required if `bufferView` is defined.
     * <p>
     * TODO either "image/jpeg" or "image/png"
     */
    private String mimeType;

    /**
     * The index of the bufferView that contains the image. Use this instead of the image's uri
     * property.
     */
    private GLTFBufferView bufferView;

    public void setBufferView(int index) {
        gltf.indexResolvers.add(() -> bufferView = gltf.getBufferView(index));
    }

    /**
     * Returns the data for this image in a buffer.
     *
     * @return TODO
     */
    public ByteBuffer getDirectByteBuffer() {
        ByteBuffer imgBuffer = null;
        if (bufferView != null) {
            imgBuffer = bufferView.getData(0, bufferView.getByteLength());
        } else {
            String pstr = gltf.getRelativePath() + uri;
            imgBuffer = GLTF.getDirectByteBuffer(pstr);
            if (imgBuffer == null) {
                //SysLog.info("G3D|Image data load fail " + pstr);
                byte[] data = GToolkit.readFileFromJar("/org/mini/g3d/res/pink.png");
                imgBuffer = ByteBuffer.wrap(data);
            }
        }
        return imgBuffer;
    }

    public String getMimeType() {
        return mimeType;
    }

}
