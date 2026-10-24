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
import org.mini.g3d.core.vector.Vector4f;
import org.mini.g3d.entity.Entity;
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
    private final Vector4f cutawayTarget = new Vector4f();
    private final Vector4f cutawaySettings = new Vector4f();
    private final Vector4f targetView = new Vector4f();

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
        render(scene, true);
    }

    public void render(Scene scene, boolean mainPass) {
        if (scene.getTerrain() == null) return;

        shader.start();
        shader.loadSkyColour(scene.getFogColor());
        shader.loadLights(scene.getLightIterator());
        shader.loadViewMatrix(scene.getCamera());
        Matrix4f projectionMatrix = scene.getCamera().getProjectionMatrix();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.loadCameraPosition(scene.getCamera().getPosition());
        shader.loadLightPos(scene.getSun().getPosition());
        shader.loadTime(DisplayManager.getTime());
        shader.loadTransparencyDistance(scene.getCamera().getDistanceFromTarget() - 1f);
        Entity target = scene.getCamera().getLookatTarget();
        boolean cutaway = mainPass && scene.isTerrainCutaway() && target != null;
        cutawaySettings.set(cutaway ? 1f : 0f, 0.35f, 1.5f, scene.getTerrainCutawayOpacity());
        cutawayTarget.set(0f, 0f, 0f, 0f);
        if (cutaway) {
            Vector3f feet = target.getPosition();
            cutawayTarget.set(feet.x, feet.y, feet.z, 1f);
            Matrix4f.transform(scene.getCamera().getViewMatrix(), cutawayTarget, targetView);
            // 默认只点透主角前方的地形；正 range 仅用于场景显式配置的后方扩展。
            cutawayTarget.w = -targetView.z + scene.getTerrainCutawayRange();
        }
        shader.loadCutaway(cutawayTarget, cutawaySettings);

        Terrain terrain = scene.getTerrain();
        //for(){
        prepareTerrain(terrain);
        loadModelMatrix(terrain);
        loadDepthBiasMPVMatrix(terrain);

        RawModel model = terrain.getModel();
        //整块不透明绘制, 裙边的渐隐带在shader里用多级点透(discard)实现:
        //保留的片元不透明且写深度, 不再需要alpha混合与关深度写, 避免深度缓冲被半透明带污染
        glDrawElements(GL_TRIANGLES, model.getVertexCount(), GL_UNSIGNED_INT, null, 0);
        MainFrameBuffer.triangles += model.getVertexCount();
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
        glEnableVertexAttribArray(3);

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
        glDisableVertexAttribArray(3);
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
