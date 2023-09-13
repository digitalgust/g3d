package org.mini.g3d.skybox;

import org.mini.g3d.core.*;
import org.mini.g3d.core.vector.Matrix4f;

import static org.mini.gl.GL.*;

public class SkyboxRenderer extends AbstractRenderer {

    private SkyboxShader shader;
    private float time = 0;

    public SkyboxRenderer() {
        shader = new SkyboxShader();
        shader.start();
        shader.connectTextureUnits();
        shader.stop();
    }

    public void render(Scene scene) {
        Skybox box = scene.getSkybox();
        if (box == null) return;
        shader.start();
        shader.loadViewMatrix(scene.getCamera());
        Matrix4f projectionMatrix = scene.getCamera().getSkyBoxProjectionMatrix();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.loadFogColour(scene.getFogColor());
        glBindVertexArray(box.getVaoID());
        glEnableVertexAttribArray(0);
        glActiveTexture(GL_TEXTURE0);
        bindTextures(scene);
        glDrawArrays(GL_TRIANGLES, 0, box.getVertexCount());
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.stop();
    }

    private void bindTextures(Scene scene) {
        Skybox box = scene.getSkybox();
        float sec = DisplayManager.getFrameTimeSeconds();
        time += sec * 1000;
        time %= 24000;
        int texture1 = -1;
        int texture2 = -1;
        float blendFactor;

        int segOfDay = scene.getDayAndNight().getSegment();
        blendFactor = scene.getDayAndNight().getPercentInSeg();
        switch (segOfDay) {
            case DayAndNight.NIGHT: {
                texture1 = box.getNightTexture();
                texture2 = box.getNightTexture();
                break;
            }
            case DayAndNight.NIGHT_TO_DAY: {
                texture1 = box.getNightTexture();
                texture2 = box.getTexture();
                break;
            }
            case DayAndNight.DAY: {
                texture1 = box.getTexture();
                texture2 = box.getTexture();
                break;
            }
            case DayAndNight.DAY_TO_NIGHT: {
                texture1 = box.getTexture();
                texture2 = box.getNightTexture();
                break;
            }
        }

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture1);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture2);
        shader.loadBlendFactor(blendFactor);
    }

}
