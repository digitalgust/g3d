package org.mini.g3d.shadowmap;

import org.mini.gl.GL;
import org.mini.gl.warp.GLShadowMapping;
import org.mini.gui.GImage;
import org.mini.nanovg.Gutil;

import static org.mini.gl.GL.*;

public class ShadowMappingPass extends GLShadowMapping {

    public ShadowMappingPass(int w, int h) {
        super(w, h);
    }

    @Override
    public void gl_init() {
        super.gl_init();
    }


    GImage genGrayImage() {

        glBindTexture(GL_TEXTURE_2D, getTexture());
        float[] data = new float[getTexWidth() * getTexHeight()];
        GL.glGetTexImage(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, GL_FLOAT, data, 0);
        glBindTexture(GL_TEXTURE_2D, 0);
        byte[] rgb = new byte[data.length * 3];
        for (int i = 0; i < data.length; i++) {
            byte b = (byte) (data[i] * 255);
            rgb[i * 3] = b;
            rgb[i * 3 + 1] = b;
            rgb[i * 3 + 2] = b;
        }
        int texid = Gutil.genTexture2D(rgb, getTexWidth(), getTexHeight(), GL_RGB, GL_RGB);
        GImage shadowMapImg = GImage.createImage(texid, getTexWidth(), getTexHeight());
        return shadowMapImg;
    }
}
