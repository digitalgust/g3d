package org.mini.g3d.core;

import org.mini.glwrap.GLFrameBuffer;

import static org.mini.gl.GL.*;

public class MainFrameBuffer extends GLFrameBuffer {
    public static int triangles = 0;

    public MainFrameBuffer(int w, int h) {
        super(w, h);

    }

    @Override
    public void gl_init() {
        super.gl_init();

        // GLFrameBuffer默认对深度纹理使用GL_LINEAR。OpenGL ES中，深度纹理
        // 在GL_TEXTURE_COMPARE_MODE=GL_NONE时必须使用最近点过滤，否则纹理
        // 不完整，体积雾读不到场景深度。深度反解也不能混合相邻像素的深度。
        int[] previousTexture = new int[1];
        glGetIntegerv(GL_TEXTURE_BINDING_2D, previousTexture, 0);
        glBindTexture(GL_TEXTURE_2D, getDepthTexture());
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_NONE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, previousTexture[0]);
    }


}
