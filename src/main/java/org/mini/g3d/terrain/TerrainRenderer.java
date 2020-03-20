package org.mini.g3d.terrain;

import org.mini.g3d.core.MasterPass;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

import java.util.List;

import static org.mini.gl.GL.*;

public class TerrainRenderer {

    private TerrainShader shader;

    public TerrainRenderer(TerrainShader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.connectTextureUnits();
        shader.stop();
    }

    public void render(List<Terrain> terrains) {
        for (Terrain terrain : terrains) {

            prepareTerrain(terrain);
            loadModelMatrix(terrain);
            loadDepthBiasMPVMatrix(terrain);

            glDrawElements(GL_TRIANGLES, terrain.getModel().getVertexCount(), GL_UNSIGNED_INT, null, 0);//gust
            MasterPass.triangles += terrain.getModel().getVertexCount();
            unbindTexturedModel();
        }
    }

    public void reloadProjectionMatrix(Matrix4f projectionMatrix) {
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
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
        glBindTexture(GL_TEXTURE_2D, terrain.getShdowMappingTexture());
    }

    private void unbindTexturedModel() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

    private void loadModelMatrix(Terrain terrain) {
        Matrix4f transformationMatrix = G3dMath.createTransformationMatrix(new Vector3f(terrain.getX(), 0, terrain.getZ()), 0, 0, 0, 1);
        shader.loadTransformationMatrix(transformationMatrix);
    }

    private void loadDepthBiasMPVMatrix(Terrain terrain) {
        shader.loadDepthBiasMVPMatrix(terrain.getDepthBiasMVP());
    }

}
