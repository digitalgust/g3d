package org.mini.g3d.core;

import org.mini.g3d.animation.AnimatedModelRenderer;
import org.mini.g3d.entity.EntityRenderer;
import org.mini.g3d.fog.VolumetricFogRenderer;
import org.mini.g3d.gui.GuiRenderer;
import org.mini.g3d.particles.ParticleMaster;
import org.mini.g3d.particles.ParticleRenderer;
import org.mini.g3d.shadowmap.ShadowMappingFrameBuffer;
import org.mini.g3d.shadowmap.ShadowMappingRenderer;
import org.mini.g3d.skybox.SkyboxRenderer;
import org.mini.g3d.terrain.TerrainRenderer;
import org.mini.g3d.water.WaterFrameBuffers;
import org.mini.g3d.water.WaterRenderer;
import org.mini.glwrap.GLUtil;

import static org.mini.gl.GL.*;

public class MasterRenderer extends AbstractRenderer {

    MainFrameBuffer mainFbo;
    ShadowMappingFrameBuffer shadowMappingFbo;
    WaterFrameBuffers waterFbos;

    private ShadowMappingRenderer shadowMappingRenderer;
    private EntityRenderer enitiyRenderer;
    private TerrainRenderer terrainRenderer;
    private AnimatedModelRenderer animatedModelRenderer;
    private SkyboxRenderer skyboxRenderer;
    private WaterRenderer waterRenderer;
    private GuiRenderer guiRenderer;
    private ParticleRenderer particleRenderer;
    private VolumetricFogRenderer fogRenderer;

    public MasterRenderer(MainFrameBuffer mainFbo, ShadowMappingFrameBuffer shadowMappingFbo, WaterFrameBuffers waterFrameBuffers) {
        this.mainFbo = mainFbo;
        this.shadowMappingFbo = shadowMappingFbo;
        this.waterFbos = waterFrameBuffers;

        shadowMappingRenderer = new ShadowMappingRenderer(shadowMappingFbo);
        enitiyRenderer = new EntityRenderer();
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "init EntityRenderer");
        terrainRenderer = new TerrainRenderer(shadowMappingFbo);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "init TerrainRenderer");
        animatedModelRenderer = new AnimatedModelRenderer();
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "init AnimatedModelRenderer");
        skyboxRenderer = new SkyboxRenderer();
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "init SkyboxRenderer");
        waterRenderer = new WaterRenderer(waterFbos);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "init WaterRenderer");
        guiRenderer = new GuiRenderer();
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "init GuiRenderer");
        particleRenderer = new ParticleRenderer();
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "init ParticleRenderer");
        fogRenderer = null;  // 将在第一次渲染时初始化
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "init FogRenderer");
    }

    public void render(Scene scene) {
        renderShadowPass(scene);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "renderShadowPass");

        glEnable(GL_CLIP_DISTANCE0);
        renderWaterRefractionPass(scene);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "renderWaterRefractionPass");
        renderWaterReflectionPass(scene);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "renderWaterReflectionPass");
        glDisable(GL_CLIP_DISTANCE0);

        renderMainPass(scene);
    }

    private void renderMainPass(Scene scene) {
        mainFbo.begin();
        prepare();
        enableCulling();

        enitiyRenderer.render(scene);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "renderMainPass enitiyRenderer");

        animatedModelRenderer.render(scene.getCamera(), scene.getAnimatedModelsIterator());
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "renderMainPass animatedModelRenderer");

        terrainRenderer.render(scene);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + "renderMainPass terrainRenderer");

        skyboxRenderer.render(scene);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " renderMainPass skyboxRenderer");

        waterRenderer.render(scene.getWaters(), scene.getCamera(), scene.getSun().getDirection());
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " renderMainPass waterRenderer");

        particleRenderer.render(ParticleMaster.getParticles(), scene);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " renderMainPass particleRenderer");


        if (scene.isVolumetricFog()) {
            // 在主帧缓冲结束后渲染体积雾效
            if (fogRenderer == null) {
                fogRenderer = new VolumetricFogRenderer(scene.getCamera().getProjectionMatrix());
            }
            // 启用混合
            fogRenderer.render(scene, mainFbo.getColorTexture(), mainFbo.getDepthTexture());
            GLUtil.checkGlError(this.getClass().getCanonicalName() + " renderMainPass fogRenderer");
        }
        guiRenderer.render(scene.getGuis());
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " renderMainPass guiRenderer");

        mainFbo.end();
    }

    private void renderShadowPass(Scene scene) {
        if (scene.getTerrain() == null) return;

        scene.setShadowRender(true);
        shadowMappingFbo.begin();

        shadowMappingRenderer.render(scene);
        shadowMappingFbo.end();
        scene.setShadowRender(false);
    }

    private void renderWaterRefractionPass(Scene scene) {
        if (scene.getWaters().isEmpty()) return;

        waterFbos.bindRefractionFrameBuffer();
        prepare();
        terrainRenderer.render(scene);
        waterFbos.unbindCurrentFrameBuffer();
    }

    private void renderWaterReflectionPass(Scene scene) {
        if (scene.getWaters().isEmpty()) return;

        waterFbos.bindReflectionFrameBuffer();
        prepare();
        scene.getCamera().reflect(scene.getWaters().get(0).getHeight());
        enitiyRenderer.render(scene);
        terrainRenderer.render(scene);
        skyboxRenderer.render(scene);
        waterFbos.unbindCurrentFrameBuffer();
        scene.getCamera().reflect(scene.getWaters().get(0).getHeight());
    }

    public void cleanUp() {
        shadowMappingRenderer.cleanUp();
        enitiyRenderer.cleanUp();
        terrainRenderer.cleanUp();
        animatedModelRenderer.cleanUp();
        skyboxRenderer.cleanUp();
        waterRenderer.cleanUp();
        guiRenderer.cleanUp();
        particleRenderer.cleanUp();
        if (fogRenderer != null) {
            fogRenderer.cleanUp();
        }
    }

    void prepare() {
        glEnable(GL_DEPTH_TEST);
        glClearColor(0, 0, 0, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void resetShadowMap() {
        shadowMappingRenderer.reset();
    }
}
