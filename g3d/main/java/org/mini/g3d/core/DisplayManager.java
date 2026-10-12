/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mini.g3d.core;

import org.mini.util.SysLog;

import java.io.InputStream;
import java.util.Properties;

import static org.mini.gl.GL.*;

public class DisplayManager {
    public static String G3D_VERSION = "1.0.0.0";
    private static final boolean volumetricFogEnabled;

    static {
        Properties prop = new Properties();
        boolean fogEnabled = true;
        try (InputStream input = DisplayManager.class.getResourceAsStream("/org/mini/g3d/res/version.properties")) {
            prop.load(input);
            G3D_VERSION = prop.getProperty("version", G3D_VERSION);
            // 缺少配置时保持旧版行为；false用于完全禁用体积雾的兼容性对照包。
            fogEnabled = Boolean.parseBoolean(prop.getProperty("volumetricFog.enabled", "true").trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        volumetricFogEnabled = fogEnabled;
    }

    public static boolean isVolumetricFogEnabled() {
        return volumetricFogEnabled;
    }

    static int width = 1024;
    static int height = 512;
    private static final int FPS_CAP = 120;

    private static long frameCount = 0;
    private static long lastFrameTime = System.currentTimeMillis();
    private static float delta;// 两帧之间的时间差
    private static float totalTime;// 初始化以来的总时间

    static String glVendor;
    static String glRenderer;
    static String glVersion;

    public static String getGlVersion() {
        if (glVersion == null) {
            createDisplay(width, height);
        }
        return glVersion;
    }

    public static void createDisplay(int w, int h) {
        width = w;
        height = h;

        byte[] name = glGetString(GL_VENDOR);
        byte[] biaoshifu = glGetString(GL_RENDERER);
        byte[] OpenGLVersion = glGetString(GL_VERSION);
        glVendor = new String(name);
        glRenderer = new String(biaoshifu);
        glVersion = new String(OpenGLVersion);
        SysLog.info("G3D|Vebder : " + glVendor);
        SysLog.info("G3D|Renderer : " + glRenderer);
        SysLog.info("G3D|Version : " + glVersion);
        SysLog.info("G3D|g3d : " + G3D_VERSION);
        SysLog.info("G3D|Volumetric fog enabled (version.properties): " + volumetricFogEnabled);
    }

    public static void updateDisplay() {

        long currentFrameTime = System.currentTimeMillis();
        delta = (currentFrameTime - lastFrameTime) / 1000.0f;
        //System.out.println("G3D|delta ms: " + (currentFrameTime - lastFrameTime));
        totalTime += delta;
        lastFrameTime = currentFrameTime;
        frameCount++;
    }

    public static boolean isOpenGLES() {
        return glVersion.toLowerCase().contains("opengl es");
    }

    /**
     * 离上上帧的时间间隔,单位为秒
     *
     * @return
     */
    public static float getFrameTimeSeconds() {
        return delta;
    }

    public static float getTime() {
        return totalTime;
    }

    public static void closeDisplay() {
    }

    public static long getCurrentTime() {
        return lastFrameTime;
    }

    public static long getFrameCount() {
        return frameCount;
    }

    /**
     * @return the width
     */
    public static int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public static int getHeight() {
        return height;
    }

}
