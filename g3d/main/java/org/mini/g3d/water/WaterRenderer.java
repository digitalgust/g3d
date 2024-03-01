package org.mini.g3d.water;


import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.util.Loader;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.gl.GL;
import org.mini.gl.GLMath;
import org.mini.glwrap.GLUtil;

import java.util.List;

import static org.mini.gl.GL.*;

public class WaterRenderer extends AbstractRenderer {

    private static final String DUDV_MAP = "/org/mini/g3d/res/water/waterDUDV.png";
    private static final String NORMAL_MAP = "/org/mini/g3d/res/water/normal.png";
    Loader loader = new Loader();
    // private static final float WAVE_SPEED = 0.03f;

    private RawModel quad;
    private WaterShader shader;
    private WaterFrameBuffers fbos;

    private float moveFactor = 0;

    private Texture dudvTexture;
    private Texture normalMap;
    Matrix4f modelMatrix = new Matrix4f();
    Matrix4f modelMatrix1 = new Matrix4f();

    public WaterRenderer(WaterFrameBuffers fbos) {
        this.shader = new WaterShader();
        this.fbos = fbos;
        this.quad = QuadGenerator.generateQuad(loader);
        this.normalMap = new Texture(loader.loadTexture(NORMAL_MAP));
        int texid = loader.loadTexture(DUDV_MAP, true, false, false);
        this.dudvTexture = new Texture(texid);//Texture.newTexture(DUDV_MAP).anisotropic().create();

        shader.start();
        shader.connectTextureUnits();
        shader.stop();
        GLUtil.checkGlError("init shader " + WaterShader.class);
    }

    public void render(List<WaterTile> tiles, ICamera camera, Vector3f lightDir) {
        prepareRender(camera, lightDir);
        synchronized (tiles) {
            for (WaterTile water : tiles) {
                Matrix4f modelMatrix = createModelMatrix(water.getX(), water.getHeight(), water.getZ(), water.getTileSize());
                shader.loadModelMatrix(modelMatrix);
                shader.loadWaterColor(water.getWaterColor());
                GL.glDrawElements(GL.GL_TRIANGLES, quad.getVertexCount(), GL.GL_UNSIGNED_INT, null, 0);
                GLUtil.checkGlError("render " + water);
            }
        }
        finish();
    }

    @Override
    public void cleanUp() {
    }

    private void prepareRender(ICamera camera, Vector3f lightDir) {
//        GLUtil.checkGlError("0");
        shader.start();
        shader.loadProjectionMatrix(camera.getProjectionMatrix());
        shader.loadViewMatrix(camera.getViewMatrix());
        shader.loadCameraPosition(camera.getPosition());

//        GLUtil.checkGlError("1");
        moveFactor += 0.001f;
        moveFactor %= 1;
        shader.loadMoveFactor(moveFactor);
        shader.loadLightDirection(lightDir);
//        GLUtil.checkGlError("2");

        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
//        GLUtil.checkGlError("2.5");

        bindTextures();
//        GLUtil.checkGlError("3");
        doRenderSettings();
    }

    private void bindTextures() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fbos.getReflectionTexture());
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, fbos.getRefractionTexture());
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, dudvTexture.getTextureID());
        glActiveTexture(GL_TEXTURE3);
        glBindTexture(GL_TEXTURE_2D, normalMap.getTextureID());
        glActiveTexture(GL_TEXTURE4);
        glBindTexture(GL_TEXTURE_2D, fbos.getRefractionDepthTexture());
    }

    private void doRenderSettings() {
        //glEnable(GL_DEPTH_TEST);

        //glEnable(GL_MULTISAMPLE);

        //glEnable(GL_CULL_FACE);
        //glCullFace(GL_BACK);

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void finish() {
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.stop();
    }

    private Matrix4f createModelMatrix(float x, float y, float z, float scale) {
//        Matrix4f modelMatrix = new Matrix4f();
//        Matrix4f.translate(new Vector3f(x, y, z), modelMatrix, modelMatrix);
//        Matrix4f.scale(new Vector3f(scale, scale, scale), modelMatrix, modelMatrix);
//        return modelMatrix;
        modelMatrix.identity();
//        GLMath.mat4x4_translate_in_place(modelMatrix.mat, x, y, z);
        GLMath.mat4x4_translate(modelMatrix.mat, x, y, z);
        modelMatrix1.identity();
        GLMath.mat4x4_scale_aniso(modelMatrix1.mat, modelMatrix.mat, scale, scale, scale);
        return modelMatrix1;
    }

}
