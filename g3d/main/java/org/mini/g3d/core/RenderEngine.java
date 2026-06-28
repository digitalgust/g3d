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
        // 显式释放旧 FBO 的 GL 句柄，避免依赖 finalize()。
        // 若不在这里 delete()，旧对象只能靠 GC 触发 finalize() 回收，
        // 而新 MainFrameBuffer 分配时 GL 驱动可能复用刚被 GC 删除的 id，
        // 造成新旧 FBO 句柄冲突。
        mainFbo.delete();
        mainFbo = new MainFrameBuffer(w, h);
        mainFbo.gl_init();
    }

    private boolean cleanedUp = false;

    @Override
    protected void finalize() {
        GForm.addCmd(new GCmd(() -> {
            cleanUp();
        }));

    }
    public void cleanUp() {
        // 防止显式 cleanUp 与 GC 触发的 finalize 重复执行，
        // 否则会重复 delete 子对象的 GL 句柄（含已被驱动复用的 id）。
        if (cleanedUp) {
            return;
        }
        cleanedUp = true;
        if (masterRenderer != null) {
            masterRenderer.cleanUp();
        }
        if (waterFbos != null) {
            waterFbos.cleanUp();
        }
        if (mainFbo != null) {
            mainFbo.delete();
        }
        if (shadowMappingFbo != null) {
            shadowMappingFbo.delete();
        }
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
