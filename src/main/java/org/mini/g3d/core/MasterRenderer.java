package org.mini.g3d.core;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.animation.AnimatedModelRenderer;
import org.mini.g3d.animation.AnimatedModelShader;
import org.mini.g3d.entity.EntityRenderer;
import org.mini.g3d.skybox.Skybox;
import org.mini.g3d.skybox.SkyboxRenderer;
import org.mini.g3d.terrain.TerrainRenderer;
import org.mini.g3d.terrain.TerrainShader;
import org.mini.nanovg.Gutil;

import java.util.List;

import static org.mini.gl.GL.*;

public class MasterRenderer extends AbstractRenderer {

    public static final float FOV = 70;
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 500;

    float fogTime;
    private static float FOG_RED = 0.f;
    private static float FOG_GREEN = 0.f;
    private static float FOG_BLUE = 0.f;


    private MasterShader masterShader = new MasterShader();
    private EntityRenderer enitiyRenderer;

    private TerrainRenderer terrainRenderer;
    private TerrainShader terrainShader = new TerrainShader();

    // ADDED Variables needed to render animated entity
    private AnimatedModelShader animatedModelShader = new AnimatedModelShader();
    private AnimatedModelRenderer animatedModelRenderer;

    private SkyboxRenderer skyboxRenderer;

    public MasterRenderer(WorldCamera camera) {
        enableCulling();

        enitiyRenderer = new EntityRenderer(masterShader, camera);
        terrainRenderer = new TerrainRenderer(terrainShader, camera);
        animatedModelRenderer = new AnimatedModelRenderer(animatedModelShader, camera);
        skyboxRenderer = new SkyboxRenderer(camera);
    }


    public static void enableCulling() {
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    public static void disableCulling() {
        glDisable(GL_CULL_FACE);
    }

    public void render(Camera camera, List<Light> lights, AnimatedModel animatedPlayer, Skybox box) {

        updateFogColor();
        prepare();
        masterShader.start();
        masterShader.loadSkyColour(FOG_RED, FOG_GREEN, FOG_BLUE);
        masterShader.loadLights(lights);
        masterShader.loadViewMatrix(camera);
        enitiyRenderer.render(entities);
        masterShader.stop();
        Gutil.checkGlError(this.getClass().getCanonicalName()+"render 1");

//        animatedModelShader.start();
//        animatedModelShader.loadSkyColor(FOG_RED, FOG_GREEN, FOG_BLUE);
//        animatedModelShader.loadLights(lights);
//        animatedModelShader.loadViewMatrix(camera);
        animatedModelRenderer.render(camera, animatedPlayer);
//        animatedModelShader.stop();
        Gutil.checkGlError(this.getClass().getCanonicalName()+"render 2");

        terrainShader.start();
        terrainShader.loadSkyColour(FOG_RED, FOG_GREEN, FOG_BLUE);
        terrainShader.loadLights(lights);
        terrainShader.loadViewMatrix(camera);
        terrainRenderer.render(terrains);
        terrainShader.stop();
        Gutil.checkGlError(this.getClass().getCanonicalName()+"render 3");

        skyboxRenderer.render(camera, box, FOG_RED, FOG_GREEN, FOG_BLUE);

        clear();
    }


    public void cleanUp() {
        masterShader.cleanUp();
        terrainShader.cleanUp();
    }

    public void prepare() {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(FOG_RED, FOG_GREEN, FOG_BLUE, 1);
    }


    private void updateFogColor() {
        float sec = EngineManager.getFrameTimeSeconds();
        fogTime += sec * 1000;
        fogTime %= 24000;
        if (fogTime >= 0 && fogTime < 5000) {
            FOG_RED = FOG_GREEN = FOG_BLUE = 0.1f;
        } else if (fogTime >= 5000 && fogTime < 8000) {
            FOG_RED = FOG_GREEN = FOG_BLUE = 0.1f + (fogTime - 5000) / 3000 * 0.4f;
        } else if (fogTime >= 8000 && fogTime < 21000) {
            FOG_RED = FOG_GREEN = FOG_BLUE = 0.5f;
        } else {
            FOG_RED = FOG_GREEN = FOG_BLUE = 0.5f - (fogTime - 21000) / 3000 * 0.4f;
        }
    }

}
