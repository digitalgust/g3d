package org.mini.g3d.water;

import org.mini.g3d.core.Camera;
import org.mini.g3d.core.Loader;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.gl.GL;

import java.util.List;

import static org.mini.gl.GL.*;

public class WaterRenderer {

    private RawModel quad;
    private WaterShader shader;

    public WaterRenderer(Loader loader, WaterShader shader, Matrix4f projectionMatrix) {
        this.shader = shader;
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
        setUpVAO(loader);
    }

    public void render(List<WaterTile> water, Camera camera) {
        prepareRender(camera);
        for (WaterTile tile : water) {
            Matrix4f modelMatrix = G3dMath.createTransformationMatrix(
                    new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()), 0, 0, 0,
                    WaterTile.TILE_SIZE);
            shader.loadModelMatrix(modelMatrix);
            glDrawArrays(GL_TRIANGLES, 0, quad.getVertexCount());
        }
        unbind();
    }

    public void reloadProjectionMatrix(Matrix4f projectionMatrix) {
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    private void prepareRender(Camera camera) {
//            glEnable(GL.GL_ALPHA_TEST);
        glEnable(GL.GL_BLEND);
        shader.start();
        shader.loadViewMatrix(camera);
        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
    }

    private void unbind() {
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.stop();
    }

    private void setUpVAO(Loader loader) {
        // Just left and z vectex positions here, top is set to 0 in v.shader
        float[] vertices = {-1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1};
        quad = loader.loadToVAO(vertices, 2);
    }

}
