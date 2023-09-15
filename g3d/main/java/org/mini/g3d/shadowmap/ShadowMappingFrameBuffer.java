package org.mini.g3d.shadowmap;

import org.mini.g3d.core.vector.Matrix4f;
import org.mini.gl.GL;
import org.mini.glwrap.GLShadowMapping;
import org.mini.glwrap.GLUtil;
import org.mini.gui.GImage;

import static org.mini.gl.GL.*;

public class ShadowMappingFrameBuffer extends GLShadowMapping {

    private Matrix4f depthBiasMVP = new Matrix4f();


    public ShadowMappingFrameBuffer(int w, int h) {
        super(w, h);
    }

    /**
     * for debug
     */
    int grayTextureId = -1;

    public GImage genGrayImage() {

        glBindTexture(GL_TEXTURE_2D, getTexture());
        short[] data = new short[getTexWidth() * getTexHeight()];
        //glfw run//GL.glGetTexImage(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, data, 0);
//        GLUtil.checkGlError("error on get shadow map image");
        glBindTexture(GL_TEXTURE_2D, 0);
        byte[] rgb = new byte[data.length * 3];
        for (int i = 0; i < data.length; i++) {
            byte b = (byte) (data[i] >>> 8);
            rgb[i * 3] = b;
            rgb[i * 3 + 1] = b;
            rgb[i * 3 + 2] = b;
        }
        int texid = GLUtil.genTexture2D(rgb, getTexWidth(), getTexHeight(), GL_RGB, GL_RGB);
        grayTextureId = texid;
        GImage shadowMapImg = GImage.createImage(texid, getTexWidth(), getTexHeight());
        return shadowMapImg;
    }


    public int getShadowMappingTexture() {
        return getTexture();
    }

    public void setDepthBiasMVP(Matrix4f depthBiasMVP) {
        this.depthBiasMVP = depthBiasMVP;
    }

    public Matrix4f getDepthBiasMVP() {
        return depthBiasMVP;
    }

}
