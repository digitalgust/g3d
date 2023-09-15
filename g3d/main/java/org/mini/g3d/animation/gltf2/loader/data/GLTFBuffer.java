/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader.data;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * A buffer points to binary geometry, animation, or skins. TODO some buffers can be copied directly
 * to GPU
 */
public class GLTFBuffer extends GLTFChildOfRootProperty {

    /**
     * The uri of the buffer. Relative paths are relative to the .gltf file. Instead of referencing an
     * external file, the uri can also be a data-uri. Will be null if referencing a glb buffer
     */
    private String uri;

    public void setUri(String uri) {
        this.uri = uri;
        resolveBufferData();
    }

    public void setByteLength(int byteLength) {
        this.byteLength = byteLength;
    }

    /**
     * The length of the buffer in bytes.
     */
    private int byteLength = -1;
    /**
     * Java nio Buffer holding data
     */
    private ByteBuffer buffer;

    public String getUri() {
        return uri;
    }

    public int getByteLength() {
        return byteLength;
    }

    /**
     * @return the String for this buffer's URI
     */
    public String getScheme() {
        return this.uri;
    }

    /**
     * Load the data referenced by this Buffer into a java.nio.Buffer
     *
     * @return java.nio.Buffer with relevant data
     */
    public ByteBuffer getData(int start, int length) {
        if (start + length > this.byteLength) {
            throw new BufferUnderflowException();
        }
        if (buffer == null) {
            buffer = gltf.getBin();
        }
        //gust
//    return buffer.slice(start, length).order(ByteOrder.LITTLE_ENDIAN);
        int pos = buffer.position();
        int limit = buffer.limit();
        buffer.position(start);
        buffer.limit(start + length);
        ByteBuffer bb = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
        buffer.position(pos);
        buffer.limit(limit);

        bb.position(0);
        bb.limit(length);
        return bb;
    }

    /**
     * Loads the buffer according to uri
     * <p>
     * if URI is underfined it must be referencing the bin chunk of this glb
     *
     * @throws IOException
     */
    private void resolveBufferData() {
        if (uri == null || uri.length() == 0) {
            this.buffer = gltf.getBin();
        } else if (uri.startsWith("data:application/octet-stream;base64,")) {
            try {
                int offset = uri.indexOf(",") + 1;
                int len = uri.length() - offset;
                byte[] b = javax.microedition.io.Base64.decode(uri, offset, len);
                this.buffer = ByteBuffer.wrap(b);
                this.buffer.position(this.buffer.limit());
                this.buffer.order(ByteOrder.LITTLE_ENDIAN);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.buffer = GLTF.getDirectByteBuffer(gltf.getRelativePath() + uri);
        }
        //All glTF buffers are little endian
        assert (this.buffer.order() == ByteOrder.LITTLE_ENDIAN);
    }
}
