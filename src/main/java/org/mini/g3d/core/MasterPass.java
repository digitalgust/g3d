package org.mini.g3d.core;

import org.mini.gl.warp.GLFrameBuffer;

public class MasterPass extends GLFrameBuffer {
    public static int triangles = 0;

    public MasterPass(int w, int h) {
        super(w, h);

    }

    public void gl_init() {
        super.gl_init();
    }


}
