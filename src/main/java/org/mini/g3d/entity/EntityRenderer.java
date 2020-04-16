package org.mini.g3d.entity;

import org.mini.g3d.core.MasterPass;
import org.mini.g3d.core.MasterRenderer;
import org.mini.g3d.core.MasterShader;
import org.mini.g3d.core.WorldCamera;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.textures.ModelTexture;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;

import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

public class EntityRenderer {

    private MasterShader shader;

    public EntityRenderer(MasterShader shader, WorldCamera camera) {
        camera.getProjectionDispatcher().register(new Runnable() {
            @Override
            public void run() {
                // Loads the shader, only has to be done once
                shader.start();
                Matrix4f projectionMatrix = camera.getProjectionMatrix();
                shader.loadProjectionMatrix(projectionMatrix);
                shader.start();
            }
        });
        this.shader = shader;
    }

    public void render(Map<TexturedModel, List<Entity>> entities) {
        for (TexturedModel model : entities.keySet()) {
            prepareTexturedModel(model);
            List<Entity> batch = entities.get(model);
            for (Entity entity : batch) {
                prepareInstance(entity);
                glDrawElements(GL_TRIANGLES, model.getRawModel().getVertexCount(), GL_UNSIGNED_INT, null, 0);//gust
                MasterPass.triangles += model.getRawModel().getVertexCount();
            }
            unbindTexturedModel();
        }
    }

    public void reloadProjectionMatrix(Matrix4f projectionMatrix) {
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    private void prepareTexturedModel(TexturedModel model) {
        RawModel rawModel = model.getRawModel();
        ModelTexture texture = model.getTexture();
        if (texture.isHasTransparency()) {
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
        MasterRenderer.enableCulling();
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

    private void prepareInstance(Entity entity) {
        Matrix4f transformationMatrix = G3dMath.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
        shader.loadTransformationMatrix(transformationMatrix);
        shader.loadOffset(entity.getTextureXOffset(), entity.getTextureYOffset());
    }

}
