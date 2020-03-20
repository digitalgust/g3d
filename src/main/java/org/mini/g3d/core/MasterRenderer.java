package org.mini.g3d.core;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.animation.AnimatedModelRenderer;
import org.mini.g3d.animation.AnimatedModelShader;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.entity.EntityRenderer;
import org.mini.g3d.skybox.Skybox;
import org.mini.g3d.skybox.SkyboxRenderer;
import org.mini.g3d.terrain.TerrainRenderer;
import org.mini.g3d.terrain.TerrainShader;
import org.mini.nanovg.Gutil;

import java.util.List;

import static org.mini.gl.GL.*;

public class MasterRenderer extends AbstractRenderer {

    private static final float FOV = 70;
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 500;

    float fogTime;
    private static float FOG_RED = 0.f;
    private static float FOG_GREEN = 0.f;
    private static float FOG_BLUE = 0.f;

    private Matrix4f projectionMatrix;
    private Matrix4f skyBoxProjectionMatrix;

    private MasterShader masterShader = new MasterShader();
    private EntityRenderer enitiyRenderer;

    private TerrainRenderer terrainRenderer;
    private TerrainShader terrainShader = new TerrainShader();

    // ADDED Variables needed to render animated entity
    private AnimatedModelShader animatedModelShader = new AnimatedModelShader();
    private AnimatedModelRenderer animatedModelRenderer;

    private SkyboxRenderer skyboxRenderer;

    public MasterRenderer() {
        enableCulling();
        createProjectionMatrix();
        skyBoxProjectionMatrix = createSkyboxProjectionMatrix();
        enitiyRenderer = new EntityRenderer(masterShader, projectionMatrix);
        terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
        animatedModelRenderer = new AnimatedModelRenderer(animatedModelShader, projectionMatrix);
        skyboxRenderer = new SkyboxRenderer(skyBoxProjectionMatrix);
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

        animatedModelShader.start();
        animatedModelShader.loadSkyColor(FOG_RED, FOG_GREEN, FOG_BLUE);
        animatedModelShader.loadLights(lights);
        animatedModelShader.loadViewMatrix(camera);
        animatedModelRenderer.render(animatedPlayer);
        animatedModelShader.stop();

        terrainShader.start();
        terrainShader.loadSkyColour(FOG_RED, FOG_GREEN, FOG_BLUE);
        terrainShader.loadLights(lights);
        terrainShader.loadViewMatrix(camera);
        terrainRenderer.render(terrains);
        terrainShader.stop();

        skyboxRenderer.render(camera, box, FOG_RED, FOG_GREEN, FOG_BLUE);

        clear();
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
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

    public void reloadProjectionMatrix() {
        enitiyRenderer.reloadProjectionMatrix(projectionMatrix);
        animatedModelRenderer.reloadProjectionMatrix(projectionMatrix);
        terrainRenderer.reloadProjectionMatrix(projectionMatrix);
        skyBoxProjectionMatrix = createSkyboxProjectionMatrix();
        skyboxRenderer.reloadProjectionMatrix(skyBoxProjectionMatrix);
    }

    public void createProjectionMatrix() {
//        float aspectRatio = (float) EngineManager.getWidth() / (float) EngineManager.getHeight();
//        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
//        float x_scale = y_scale / aspectRatio;
//        float frustum_length = FAR_PLANE - NEAR_PLANE;
//        projectionMatrix = new Matrix4f();
//        projectionMatrix.mat[Matrix4f.M00] = x_scale;
//        projectionMatrix.mat[Matrix4f.M11] = y_scale;
//        projectionMatrix.mat[Matrix4f.M22] = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
//        projectionMatrix.mat[Matrix4f.M23] = -1;
//        projectionMatrix.mat[Matrix4f.M32] = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
//        projectionMatrix.mat[Matrix4f.M33] = 0;

        projectionMatrix = new Matrix4f();
        float aspectRatio = (float) EngineManager.getWidth() / (float) EngineManager.getHeight();
        Gutil.mat4x4_perspective(projectionMatrix.mat, FOV, aspectRatio, NEAR_PLANE, FAR_PLANE);
//        Gutil.mat4x4_ortho(projectionMatrix.mat, -1500.0f, 1500.0f, -1500.0f, 1500.0f, 0.1f, 3000.0f);
    }

    private Matrix4f createSkyboxProjectionMatrix() {

        Matrix4f projection = new Matrix4f();
        float aspectRatio = (float) EngineManager.getWidth() / (float) EngineManager.getHeight();
        Gutil.mat4x4_perspective(projection.mat, FOV, aspectRatio, NEAR_PLANE, FAR_PLANE * 8);
        return projection;
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
