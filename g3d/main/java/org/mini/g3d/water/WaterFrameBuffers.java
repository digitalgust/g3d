package org.mini.g3d.water;

import org.mini.g3d.core.DisplayManager;
import org.mini.gl.GL;
import org.mini.glwrap.GLUtil;
import org.mini.gui.GForm;
import org.mini.gui.callback.GCmd;
import org.mini.util.SysLog;

import static org.mini.gl.GL.*;

public class WaterFrameBuffers {

    protected static final int REFLECTION_WIDTH = 256;  // 降低分辨率以增加兼容性
    private static final int REFLECTION_HEIGHT = 256;   // 使用2的幂次方尺寸

    protected static final int REFRACTION_WIDTH = 256;  // 降低分辨率以增加兼容性
    private static final int REFRACTION_HEIGHT = 256;   // 使用2的幂次方尺寸

    private int[] reflectionFrameBuffer = {-1};
    private int[] reflectionTexture = {-1};
    private int[] reflectionDepthBuffer = {-1};

    private int[] refractionFrameBuffer = {-1};
    private int[] refractionTexture = {-1};
    private int[] refractionDepthTexture = {-1};

    private boolean isInitialized = false;

    public WaterFrameBuffers() {//call when loading the game
        try {
            initialiseReflectionFrameBuffer();
            initialiseRefractionFrameBuffer();
            isInitialized = true;
            SysLog.info("G3D|WaterFrameBuffers initialized successfully");
        } catch (Exception e) {
            SysLog.error("G3D|Failed to initialize WaterFrameBuffers: " + e.getMessage());
            cleanUp(); // 清理已分配的资源
            isInitialized = false;
        }
    }

    public void cleanUp() {//call when closing the game
        if (reflectionFrameBuffer[0] != -1) {
            glDeleteFramebuffers(1, reflectionFrameBuffer, 0);
            reflectionFrameBuffer[0] = -1;
        }
        if (reflectionTexture[0] != -1) {
            glDeleteTextures(1, reflectionTexture, 0);
            reflectionTexture[0] = -1;
        }
        if (reflectionDepthBuffer[0] != -1) {
            glDeleteRenderbuffers(1, reflectionDepthBuffer, 0);
            reflectionDepthBuffer[0] = -1;
        }
        if (refractionFrameBuffer[0] != -1) {
            glDeleteFramebuffers(1, refractionFrameBuffer, 0);
            refractionFrameBuffer[0] = -1;
        }
        if (refractionTexture[0] != -1) {
            glDeleteTextures(1, refractionTexture, 0);
            refractionTexture[0] = -1;
        }
        if (refractionDepthTexture[0] != -1) {
            glDeleteTextures(1, refractionDepthTexture, 0);
            refractionDepthTexture[0] = -1;
        }
    }

    protected void finalize() {
        GForm.addCmd(new GCmd(() -> {
            cleanUp();
            SysLog.info("G3D|WaterFrameBuffers clean success");
        }));
    }

    public void bindReflectionFrameBuffer() {//call before rendering to this FBO
        if (!isInitialized) return;
        bindFrameBuffer(reflectionFrameBuffer[0], REFLECTION_WIDTH, REFLECTION_HEIGHT);
    }

    public void bindRefractionFrameBuffer() {//call before rendering to this FBO
        if (!isInitialized) return;
        bindFrameBuffer(refractionFrameBuffer[0], REFRACTION_WIDTH, REFRACTION_HEIGHT);
    }

    public void unbindCurrentFrameBuffer() {//call after rendering to texture
        if (!isInitialized) return;
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, DisplayManager.getWidth(), DisplayManager.getHeight());
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

    public boolean isInitialized() {
        return isInitialized;
    }

    private void initialiseReflectionFrameBuffer() {
        createFrameBuffer(reflectionFrameBuffer);
        createTextureAttachment(reflectionTexture, REFLECTION_WIDTH, REFLECTION_HEIGHT);
        createDepthBufferAttachment(reflectionDepthBuffer, REFLECTION_WIDTH, REFLECTION_HEIGHT);

        // 检查帧缓冲区是否完整
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            String statusMessage = getFramebufferStatusString(status);
            SysLog.error("G3D|WaterFrameBuffers Reflection FBO incomplete: " + statusMessage);
            throw new RuntimeException("Reflection framebuffer incomplete: " + statusMessage);
        }

        unbindCurrentFrameBuffer();
    }

    private void initialiseRefractionFrameBuffer() {
        createFrameBuffer(refractionFrameBuffer);
        createTextureAttachment(refractionTexture, REFRACTION_WIDTH, REFRACTION_HEIGHT);
        createDepthTextureAttachment(refractionDepthTexture, REFRACTION_WIDTH, REFRACTION_HEIGHT);

        // 检查帧缓冲区是否完整
        int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (status != GL_FRAMEBUFFER_COMPLETE) {
            String statusMessage = getFramebufferStatusString(status);
            SysLog.error("G3D|WaterFrameBuffers Refraction FBO incomplete: " + statusMessage);
            throw new RuntimeException("Refraction framebuffer incomplete: " + statusMessage);
        }

        unbindCurrentFrameBuffer();
    }

    // 获取帧缓冲区状态的描述信息
    private String getFramebufferStatusString(int status) {
        switch (status) {
            case GL_FRAMEBUFFER_COMPLETE:
                return "GL_FRAMEBUFFER_COMPLETE";
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                return "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                return "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
            case GL_FRAMEBUFFER_UNSUPPORTED:
                return "GL_FRAMEBUFFER_UNSUPPORTED";
            default:
                return "Unknown status: " + status;
        }
    }

    private void bindFrameBuffer(int frameBuffer, int width, int height) {
        glBindTexture(GL_TEXTURE_2D, 0); // 绑定前解绑所有纹理
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        glViewport(0, 0, width, height);
        GLUtil.checkGlError("Binding framebuffer " + frameBuffer);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // 清除帧缓冲区
    }


    private int createFrameBuffer(int[] tmp) {
        glGenFramebuffers(1, tmp, 0);
        int frameBuffer = tmp[0];
        if (frameBuffer <= 0) {
            throw new RuntimeException("Failed to create framebuffer: " + glGetError());
        }

        //generate name for frame buffer
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        //create the framebuffer
        glDrawBuffers(1, new int[]{GL_COLOR_ATTACHMENT0}, 0);//gust
        GLUtil.checkGlError("createFrameBuffer: glDrawBuffers");

        glReadBuffer(GL_COLOR_ATTACHMENT0);
        GLUtil.checkGlError("createFrameBuffer: glReadBuffer");

        return frameBuffer;
    }

    private int createTextureAttachment(int[] tmp, int width, int height) {
        glGenTextures(1, tmp, 0);
        int texture = tmp[0];
        if (texture <= 0) {
            throw new RuntimeException("Failed to create texture: " + glGetError());
        }

        glBindTexture(GL_TEXTURE_2D, texture);
        // 使用RGBA格式而非RGBA8，适配OpenGL ES
        GL.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, null, 0);
        GLUtil.checkGlError("createTextureAttachment: glTexImage2D");

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, texture, 0);
        GLUtil.checkGlError("createTextureAttachment: glFramebufferTexture2D");

        return texture;
    }

    private int createDepthTextureAttachment(int[] tmp, int width, int height) {
        glGenTextures(1, tmp, 0);
        int texture = tmp[0];
        if (texture <= 0) {
            throw new RuntimeException("Failed to create depth texture: " + glGetError());
        }

        glBindTexture(GL_TEXTURE_2D, texture);
        // 使用Android兼容的深度纹理格式
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT16, width, height, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, null, 0);
        GLUtil.checkGlError("createDepthTextureAttachment: glTexImage2D");

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, texture, 0);
        GLUtil.checkGlError("createDepthTextureAttachment: glFramebufferTexture2D");

        return texture;
    }

    private int createDepthBufferAttachment(int[] tmp, int width, int height) {
        glGenRenderbuffers(1, tmp, 0);
        int depthBuffer = tmp[0];
        if (depthBuffer <= 0) {
            throw new RuntimeException("Failed to create depth renderbuffer: " + glGetError());
        }

        glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, width, height);
        GLUtil.checkGlError("createDepthBufferAttachment: glRenderbufferStorage");

        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);
        GLUtil.checkGlError("createDepthBufferAttachment: glFramebufferRenderbuffer");

        return depthBuffer;
    }
}
