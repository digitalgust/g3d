package org.mini.g3d.core;

import org.mini.g3d.animation.gltf2.GLDriver;
import org.mini.g3d.particles.ParticleMaster;
import org.mini.g3d.shadowmap.ShadowMappingFrameBuffer;
import org.mini.g3d.water.WaterFrameBuffers;
import org.mini.glwrap.GLUtil;
import org.mini.gui.GForm;
import org.mini.gui.callback.GCmd;

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
        GLUtil.checkGlError("Game glinit 0.0");
        int smSize = 2048;
        shadowMappingFbo = new ShadowMappingFrameBuffer(smSize, smSize);
        GLUtil.checkGlError("Game glinit 0.1");
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

    @Override
    protected void finalize() {
        GForm.addCmd(new GCmd(() -> {
            cleanUp();
        }));

    }

    public void cleanUp() {
        GLUtil.checkGlError("0");
        if (masterRenderer != null) {
            masterRenderer.cleanUp();
        }
        GLUtil.checkGlError("0");
        if (waterFbos != null) {
            waterFbos.cleanUp();
        }
        GLUtil.checkGlError("0");
        if (mainFbo != null) {
            mainFbo.delete();
        }
        GLUtil.checkGlError("0");
        if (shadowMappingFbo != null) {
            shadowMappingFbo.delete();
        }
        GLUtil.checkGlError("0");
        ParticleMaster.cleanUp();
        GLUtil.checkGlError("0");
        GLDriver.cleanUp();
        GLUtil.checkGlError("0");
        DisplayManager.closeDisplay();
        GLUtil.checkGlError("0");
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
