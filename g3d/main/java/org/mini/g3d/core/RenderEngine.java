package org.mini.g3d.core;

import org.mini.g3d.animation.gltf2.GLDriver;
import org.mini.g3d.particles.ParticleMaster;
import org.mini.g3d.shadowmap.ShadowMappingFrameBuffer;
import org.mini.g3d.water.WaterFrameBuffers;
import org.mini.glwrap.GLUtil;

import static org.mini.gl.GL.*;
import static org.mini.gl.GL.glClearColor;

public class RenderEngine {

    MasterRenderer masterRenderer;

    MainFrameBuffer mainFbo;
    ShadowMappingFrameBuffer shadowMappingFbo;
    WaterFrameBuffers waterFbos;

    public void renderScene(Scene scene) {
        DisplayManager.updateDisplay();

        synchronized (scene.getLock()) {
            masterRenderer.render(scene);
        }
    }

    public void clearMainFbo() {
        if (masterRenderer != null) {
            mainFbo.begin();
            {
                glEnable(GL_DEPTH_TEST);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                glClearColor(0, 0, 0, 1);
            }
            mainFbo.end();
        }
    }

    public void gl_init(float w, float h) {

        DisplayManager.createDisplay((int) w, (int) h);
        int smSize = 2048;
        shadowMappingFbo = new ShadowMappingFrameBuffer(smSize, smSize);
        shadowMappingFbo.gl_init();
        GLUtil.checkGlError("Game glinit 0.3");

        mainFbo = new MainFrameBuffer((int) w, (int) h);
        mainFbo.gl_init();
        GLUtil.checkGlError("Game glinit 0.5");

        waterFbos = new WaterFrameBuffers();
        GLUtil.checkGlError("Game glinit 0.8");

        masterRenderer = new MasterRenderer(mainFbo, shadowMappingFbo, waterFbos);
    }

    public void onScreenReSize(int w, int h) {
        if (mainFbo == null) {
            return;
        }
        DisplayManager.createDisplay((int) w, (int) h);
        mainFbo = new MainFrameBuffer(w, h);
        mainFbo.gl_init();
    }


    public void cleanup() {
        ParticleMaster.cleanUp();
        GLDriver.cleanUp();
        DisplayManager.closeDisplay();
    }


    public MainFrameBuffer getMainFbo() {
        return mainFbo;
    }

    public MasterRenderer getMasterRenderer() {
        return masterRenderer;
    }

    public ShadowMappingFrameBuffer getShadowMappingFbo() {
        return shadowMappingFbo;
    }
}
