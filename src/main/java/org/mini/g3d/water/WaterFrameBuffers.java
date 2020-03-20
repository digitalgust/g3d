package org.mini.g3d.water;

import org.mini.g3d.core.EngineManager;
import org.mini.gl.GL;
import static org.mini.gl.GL.GL_COLOR_ATTACHMENT0;
import static org.mini.gl.GL.GL_DEPTH_ATTACHMENT;
import static org.mini.gl.GL.GL_DEPTH_COMPONENT;
import static org.mini.gl.GL.GL_FLOAT;
import static org.mini.gl.GL.GL_FRAMEBUFFER;
import static org.mini.gl.GL.GL_LINEAR;
import static org.mini.gl.GL.GL_RENDERBUFFER;
import static org.mini.gl.GL.GL_RGB;
import static org.mini.gl.GL.GL_TEXTURE_2D;
import static org.mini.gl.GL.GL_TEXTURE_MAG_FILTER;
import static org.mini.gl.GL.GL_TEXTURE_MIN_FILTER;
import static org.mini.gl.GL.GL_UNSIGNED_BYTE;
import static org.mini.gl.GL.glBindFramebuffer;
import static org.mini.gl.GL.glBindRenderbuffer;
import static org.mini.gl.GL.glBindTexture;
import static org.mini.gl.GL.glDeleteFramebuffers;
import static org.mini.gl.GL.glDeleteRenderbuffers;
import static org.mini.gl.GL.glDeleteTextures;
import static org.mini.gl.GL.glDrawBuffers;
import static org.mini.gl.GL.glFramebufferRenderbuffer;
import static org.mini.gl.GL.glFramebufferTexture2D;
import static org.mini.gl.GL.glGenFramebuffers;
import static org.mini.gl.GL.glGenRenderbuffers;
import static org.mini.gl.GL.glGenTextures;
import static org.mini.gl.GL.glRenderbufferStorage;
import static org.mini.gl.GL.glTexImage2D;
import static org.mini.gl.GL.glTexParameteri;
import static org.mini.gl.GL.glViewport;

public class WaterFrameBuffers {

    protected static final int REFLECTION_WIDTH = 320;
    private static final int REFLECTION_HEIGHT = 180;

    protected static final int REFRACTION_WIDTH = 1280;
    private static final int REFRACTION_HEIGHT = 720;

    private int reflectionFrameBuffer;
    private int reflectionTexture;
    private int reflectionDepthBuffer;

    private int refractionFrameBuffer;
    private int refractionTexture;
    private int refractionDepthTexture;

    public WaterFrameBuffers() {//call when loading the game
        initialiseReflectionFrameBuffer();
        initialiseRefractionFrameBuffer();
    }

    public void cleanUp() {//call when closing the game
        glDeleteFramebuffers(1, new int[]{reflectionFrameBuffer}, 0);
        glDeleteTextures(1, new int[]{reflectionTexture}, 0);
        glDeleteRenderbuffers(1, new int[]{reflectionDepthBuffer}, 0);
        glDeleteFramebuffers(1, new int[]{refractionFrameBuffer}, 0);
        glDeleteTextures(1, new int[]{refractionTexture}, 0);
        glDeleteTextures(1, new int[]{refractionDepthTexture}, 0);
    }

    public void bindReflectionFrameBuffer() {//call before rendering to this FBO
        bindFrameBuffer(reflectionFrameBuffer, REFLECTION_WIDTH, REFLECTION_HEIGHT);
    }

    public void bindRefractionFrameBuffer() {//call before rendering to this FBO
        bindFrameBuffer(refractionFrameBuffer, REFRACTION_WIDTH, REFRACTION_HEIGHT);
    }

    public void unbindCurrentFrameBuffer() {//call to switch to default frame buffer
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, EngineManager.getWidth(), EngineManager.getHeight());
    }

    public int getReflectionTexture() {//get the resulting texture
        return reflectionTexture;
    }

    public int getRefractionTexture() {//get the resulting texture
        return refractionTexture;
    }

    public int getRefractionDepthTexture() {//get the resulting depth texture
        return refractionDepthTexture;
    }

    private void initialiseReflectionFrameBuffer() {
        reflectionFrameBuffer = createFrameBuffer();
        reflectionTexture = createTextureAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
        reflectionDepthBuffer = createDepthBufferAttachment(REFLECTION_WIDTH, REFLECTION_HEIGHT);
        unbindCurrentFrameBuffer();
    }

    private void initialiseRefractionFrameBuffer() {
        refractionFrameBuffer = createFrameBuffer();
        refractionTexture = createTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
        refractionDepthTexture = createDepthTextureAttachment(REFRACTION_WIDTH, REFRACTION_HEIGHT);
        unbindCurrentFrameBuffer();
    }

    private void bindFrameBuffer(int frameBuffer, int width, int height) {
        glBindTexture(GL_TEXTURE_2D, 0);//To make sure the texture isn't bound
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glViewport(0, 0, width, height);
    }

    int[] tmp = {0};

    private int createFrameBuffer() {
        glGenFramebuffers(1, tmp, 0);
        int frameBuffer = tmp[0];
        //generate name for frame buffer
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        //create the framebuffer
        glDrawBuffers(1, new int[]{GL_COLOR_ATTACHMENT0}, 0);//gust
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
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, null, 0);//gust
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture, 0);
        return texture;
    }

    private int createDepthBufferAttachment(int width, int height) {
        glGenRenderbuffers(1, tmp, 0);
        int depthBuffer = tmp[0];
        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, width,
                height);
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT,
                GL_RENDERBUFFER, depthBuffer);
        return depthBuffer;
    }

}
