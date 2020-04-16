package org.mini.g3d.skybox;

import org.mini.g3d.core.Camera;
import org.mini.g3d.core.EngineManager;
import org.mini.g3d.core.WorldCamera;
import org.mini.g3d.core.vector.Matrix4f;

import static org.mini.gl.GL.*;

public class SkyboxRenderer {

    private SkyboxShader shader;
    private float time = 0;

    public SkyboxRenderer(WorldCamera camera) {
        camera.getProjectionDispatcher().register(new Runnable() {
            @Override
            public void run() {
                shader.start();
                Matrix4f projectionMatrix = camera.getSkyBoxProjectionMatrix();
                shader.loadProjectionMatrix(projectionMatrix);
                shader.stop();
            }
        });
        shader = new SkyboxShader();
        shader.start();
        shader.connectTextureUnits();
        shader.stop();
    }

    public void render(Camera camera, Skybox box, float r, float g, float b) {
        shader.start();
        shader.loadViewMatrix(camera);
        shader.loadFogColour(r, g, b);
        glBindVertexArray(box.getVaoID());
        glEnableVertexAttribArray(0);
        glActiveTexture(GL_TEXTURE0);
        bindTextures(box);
        glDrawArrays(GL_TRIANGLES, 0, box.getVertexCount());
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.stop();
    }

    private void bindTextures(Skybox box) {
        float sec = EngineManager.getFrameTimeSeconds();
        time += sec * 1000;
        time %= 24000;
        int texture1;
        int texture2;
        float blendFactor;
        if (time >= 0 && time < 5000) {
            texture1 = box.getNightTexture();
            texture2 = box.getNightTexture();
            blendFactor = (time - 0) / (5000 - 0);
        } else if (time >= 5000 && time < 8000) {
            texture1 = box.getNightTexture();
            texture2 = box.getTexture();
            blendFactor = (time - 5000) / (8000 - 5000);
        } else if (time >= 8000 && time < 21000) {
            texture1 = box.getTexture();
            texture2 = box.getTexture();
            blendFactor = (time - 8000) / (21000 - 8000);
        } else {
            texture1 = box.getTexture();
            texture2 = box.getNightTexture();
            blendFactor = (time - 21000) / (24000 - 21000);
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture2);
        shader.loadBlendFactor(blendFactor);
    }

    public void reloadProjectionMatrix(Matrix4f projectionMatrix) {
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }
}
