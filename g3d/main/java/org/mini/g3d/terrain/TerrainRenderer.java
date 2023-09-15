package org.mini.g3d.terrain;

import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.MainFrameBuffer;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.util.Loader;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.shadowmap.ShadowMappingFrameBuffer;

import static org.mini.gl.GL.*;

public class TerrainRenderer extends AbstractRenderer {

    private static final String CLOUDNOISE = "/org/mini/g3d/res/water/normal.png";
    //    private static final String CLOUDNOISE = "cloud/cloud";
    Loader loader = new Loader();
    private Texture noiseTex;

    private TerrainShader shader = new TerrainShader();

    ShadowMappingFrameBuffer shadowMappingFbo;

    Matrix4f defaultDepthBiasMVP = new Matrix4f();
    Matrix4f cachedTransform = new Matrix4f();

    /**
     * 带阴影渲染, 可以为空
     *
     * @param shadowMappingFbo
     */
    public TerrainRenderer(ShadowMappingFrameBuffer shadowMappingFbo) {

        this.noiseTex = new Texture(loader.loadTexture(CLOUDNOISE));
        this.shadowMappingFbo = shadowMappingFbo;
        shader.start();
        shader.connectTextureUnits();
        shader.stop();
    }

    public void render(Scene scene) {
        if (scene.getTerrain() == null) return;

        shader.start();
        shader.loadSkyColour(scene.getFogColor());
        shader.loadLights(scene.getLights());
        shader.loadViewMatrix(scene.getCamera());
        Matrix4f projectionMatrix = scene.getCamera().getProjectionMatrix();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.loadCameraPosition(scene.getCamera().getPosition());
        shader.loadLightPos(scene.getSun().getPosition());
        shader.loadTime(DisplayManager.getTime());

        Terrain terrain = scene.getTerrain();
        //for(){
        prepareTerrain(terrain);
        loadModelMatrix(terrain);
        loadDepthBiasMPVMatrix(terrain);

        glDrawElements(GL_TRIANGLES, terrain.getModel().getVertexCount(), GL_UNSIGNED_INT, null, 0);//gust
        MainFrameBuffer.triangles += terrain.getModel().getVertexCount();
        unbindTexturedModel();
        //}

        shader.stop();
    }

    private void prepareTerrain(Terrain terrain) {
        RawModel rawModel = terrain.getModel();

        glBindVertexArray(rawModel.getVaoID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);

        bindTextures(terrain);
        shader.loadShineVariables(1, 0);
    }

    private void bindTextures(Terrain terrain) {
        TerrainTexturePack texturePack = terrain.getTexturePack();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texturePack.getBackgroundTexture().getTextureID());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, texturePack.getrTexture().getTextureID());
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, texturePack.getgTexture().getTextureID());
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, texturePack.getbTexture().getTextureID());
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, terrain.getBlendMap().getTextureID());
        glActiveTexture(GL_TEXTURE5);
        glBindTexture(GL_TEXTURE_2D, noiseTex.getTextureID());
        if (shadowMappingFbo != null) {
            glActiveTexture(GL_TEXTURE6);
            glBindTexture(GL_TEXTURE_2D, shadowMappingFbo.getShadowMappingTexture());
        }
    }

    private void unbindTexturedModel() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

    private void loadModelMatrix(Terrain terrain) {
        Matrix4f transformationMatrix = G3dUtil.createTransformationMatrix(new Vector3f(0, 0, 0), 0, 0, 0, 1, cachedTransform);
        shader.loadTransformationMatrix(transformationMatrix);
    }

    private void loadDepthBiasMPVMatrix(Terrain terrain) {
        shader.loadDepthBiasMVPMatrix(shadowMappingFbo == null ? defaultDepthBiasMVP : shadowMappingFbo.getDepthBiasMVP());
    }

}
