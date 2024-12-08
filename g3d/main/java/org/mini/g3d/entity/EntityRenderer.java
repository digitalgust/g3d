package org.mini.g3d.entity;

import org.mini.g3d.core.*;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.vector.Matrix4f;

import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

public class EntityRenderer extends AbstractRenderer {

    private EntityShader shader = new EntityShader();
    Matrix4f cachedTransform = new Matrix4f();

    public EntityRenderer() {

    }

    public void render(Scene scene) {
        shader.start();
        shader.loadSkyColour(scene.getFogColor());
        shader.loadLights(scene.getLightIterator());
        shader.loadViewMatrix(scene.getCamera());
        Matrix4f projectionMatrix = scene.getCamera().getProjectionMatrix();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.loadTransparencyDistance(scene.getCamera().getDistanceFromTarget() - 1f);

        Map<TexturedModel, List<Entity>> entities = scene.getEntitieMap();
        entities.forEach((texturedModel, batch) -> {
            //multithread , if scene.clear() may batch is null
            if (texturedModel == null || batch == null) {
                return;
            }
            if (texturedModel.getTexture() != null) {
                prepareTexturedModel(texturedModel);
                for (Entity entity : batch) {
                    prepareInstance(entity);
                    glDrawElements(GL_TRIANGLES, texturedModel.getRawModel().getVertexCount(), GL_UNSIGNED_INT, null, 0);//gust
                    MainFrameBuffer.triangles += texturedModel.getRawModel().getVertexCount();
                }
                unbindTexturedModel();
            }
        });
        shader.stop();
    }

    private void prepareTexturedModel(TexturedModel model) {
        RawModel rawModel = model.getRawModel();
        Texture texture = model.getTexture();
        if (rawModel.isCullingBack()) {
            MasterRenderer.enableCulling();
        } else {
            MasterRenderer.disableCulling();
        }

        glBindVertexArray(rawModel.getVaoID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        shader.loadNumberOfRows(texture.getNumberOfRows());
        shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
        shader.setLightning(texture.isUseFakeLightning());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getID());
    }

    private void unbindTexturedModel() {
        enableCulling();
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

    private void prepareInstance(Entity entity) {
        Matrix4f transformationMatrix = G3dUtil.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale(), cachedTransform);
        shader.loadTransformationMatrix(transformationMatrix);
        shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
        shader.loadTransparency(entity.getTransparency());
    }

}
