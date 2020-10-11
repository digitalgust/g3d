/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mini.g3d.core;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.mini.gl.GL.*;

public class EngineManager {

    public static final String RES_LOC = "/res/";


    static int width = 1280;
    static int height = 720;
    private static final int FPS_CAP = 120;

    private static long lastFrameTime;
    private static float delta;



    static String glVendor;
    static String glRenderer;
    static String glVersion;

    public static String getGlVersion() {
        return glVersion;
    }

    public static void createDisplay(int w, int h) {
        width = w;
        height = h;

        lastFrameTime = getCurrentTime();

        byte[] name = glGetString(GL_VENDOR);
        byte[] biaoshifu = glGetString(GL_RENDERER);
        byte[] OpenGLVersion = glGetString(GL_VERSION);
        glVendor = new String(name);
        glRenderer = new String(biaoshifu);
        glVersion = new String(OpenGLVersion);
        System.out.println("Vebder : " + glVendor);
        System.out.println("Renderer : " + glRenderer);
        System.out.println("Version : " + glVersion);
    }

    public static void updateDisplay() {

        long currentFrameTime = getCurrentTime();
        delta = (currentFrameTime - lastFrameTime) / 1000.0f;

        lastFrameTime = getCurrentTime();
    }

    public static float getFrameTimeSeconds() {
        return delta;
    }

    public static void closeDisplay() {
    }

    private static long getCurrentTime() {
        return System.currentTimeMillis();
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


    static public byte[] loadFileFromJar(String fileName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            InputStream is = EngineManager.class.getResourceAsStream(fileName);
            byte[] b = new byte[4096];
            if (is != null) {
                int r;
                while ((r = is.read(b)) > 0) {
                    baos.write(b, 0, r);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

}
