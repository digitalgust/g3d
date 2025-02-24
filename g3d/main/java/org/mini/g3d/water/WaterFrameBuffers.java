package org.mini.g3d.water;

import org.mini.g3d.core.DisplayManager;
import org.mini.gl.GL;
import org.mini.gui.GForm;
import org.mini.gui.callback.GCmd;

import static org.mini.gl.GL.*;

public class WaterFrameBuffers {

    protected static final int REFLECTION_WIDTH = 640;
    private static final int REFLECTION_HEIGHT = 360;

    protected static final int REFRACTION_WIDTH = 640;
    private static final int REFRACTION_HEIGHT = 360;

    private int[] reflectionFrameBuffer = {-1};
    private int[] reflectionTexture = {-1};
    private int[] reflectionDepthBuffer = {-1};

    private int[] refractionFrameBuffer = {-1};
    private int[] refractionTexture = {-1};
    private int[] refractionDepthTexture = {-1};

    public WaterFrameBuffers() {//call when loading the game
        initialiseReflectionFrameBuffer();
        initialiseRefractionFrameBuffer();
    }

    public void cleanUp() {//call when closing the game
        glDeleteFramebuffers(1, reflectionFrameBuffer, 0);
        glDeleteTextures(1, reflectionTexture, 0);
        glDeleteRenderbuffers(1, reflectionDepthBuffer, 0);
        glDeleteFramebuffers(1, refractionFrameBuffer, 0);
        glDeleteTextures(1, refractionTexture, 0);
        glDeleteTextures(1, refractionDepthTexture, 0);
    }

    protected void finalize() {
        GForm.addCmd(new GCmd(() -> {
            cleanUp();
            System.out.println("[G3D][INFO]WaterFrameBuffers clean success");
        }));
    }

    public void bindReflectionFrameBuffer() {//call before rendering to this FBO
        bindFrameBuffer(reflectionFrameBuffer[0], REFLECTION_WIDTH, REFLECTION_HEIGHT);
    }

    public void bindRefractionFrameBuffer() {//call before rendering to this FBO
        bindFrameBuffer(refractionFrameBuffer[0], REFRACTION_WIDTH, REFRACTION_HEIGHT);
    }

    public void unbindCurrentFrameBuffer() {//call after rendering to texture
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, DisplayManager.getWidth(), DisplayManager.getHeight());
        //glFinish();
    }

    public int getReflectionTexture() {//get the resulting texture
        return reflectionTexture[0];
    }

    public int getRefractionTexture() {//get the resulting texture
        return refractionTexture[0];
    }

    public int getRefractionDepthTexture() {//get the resulting depth texture
        return refractionDepthTexture[0];
    }

    private void initialiseReflectionFrameBuffer() {
        reflectionFrameBuffer[0] = createFrameBuffer();
        reflectionTexture[0] = createTextureAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
        reflectionDepthBuffer[0] = createDepthBufferAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
        unbindCurrentFrameBuffer();
    }

    private void initialiseRefractionFrameBuffer() {
        refractionFrameBuffer[0] = createFrameBuffer();
        refractionTexture[0] = createTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
        refractionDepthTexture[0] = createDepthTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
        unbindCurrentFrameBuffer();
    }

    private void bindFrameBuffer(int frameBuffer, int width, int height) {
        GL.glBindTexture(GL.GL_TEXTURE_2D, 0);
        GL.glBindFramebuffer(GL.GL_FRAMEBUFFER, frameBuffer);
        GL.glViewport(0, 0, width, height);
    }

    int[] tmp = {0};

    private int createFrameBuffer() {
        glGenFramebuffers(1, tmp, 0);
        int frameBuffer = tmp[0];
        //generate name for frame buffer
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        //create the framebuffer
        glDrawBuffers(1, new int[]{GL_COLOR_ATTACHMENT0}, 0);//gust
        //GLUtil.checkGlError("createFrameBuffer 1");//orange
        glReadBuffer(GL_COLOR_ATTACHMENT0);
        //indicate that we will always render to color attachment 0
        return frameBuffer;
    }

    private int createTextureAttachment(int width, int height) {
        glGenTextures(1, tmp, 0);
        int texture = tmp[0];
        glBindTexture(GL_TEXTURE_2D, texture);
        GL.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, null, 0);//gust
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);//gust
        return texture;
    }

    private int createDepthTextureAttachment(int width, int height) {
        glGenTextures(1, tmp, 0);
        int texture = tmp[0];
        glBindTexture(GL_TEXTURE_2D, texture);
        //todo GL_DEPTH_COMPONENT32 ? GL_DEPTH_COMPONENT
        glTexImage2D(GL_TEXTURE_2D, 0,/*ios*/ GL_DEPTH_COMPONENT16, width, height, 0, GL_DEPTH_COMPONENT, /*ios*/GL_UNSIGNED_SHORT, null, 0);//gust
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture, 0);
        return texture;
    }

    private int createDepthBufferAttachment(int width, int height) {
        glGenRenderbuffers(1, tmp, 0);
        int depthBuffer = tmp[0];
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width,
                height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER, depthBuffer);
        return depthBuffer;
    }

}
