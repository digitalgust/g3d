package org.mini.g3d.water;


import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.DisplayManager;
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
import org.mini.util.SysLog;

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

    boolean isInitialized = false;
    boolean isAndroid = false;

    public WaterRenderer(WaterFrameBuffers fbos) {
        this.fbos = fbos;
        try {
            this.quad = QuadGenerator.generateQuad(loader);
            this.shader = new WaterShader();
            this.normalMap = new Texture(loader.loadTexture(NORMAL_MAP));
            int texid = loader.loadTexture(DUDV_MAP, true, false, false);
            this.dudvTexture = new Texture(texid);

            // 确保纹理加载成功
            if (this.normalMap.getTextureID() <= 0 || texid <= 0) {
                throw new RuntimeException("Failed to load water textures");
            }

            shader.start();
            shader.connectTextureUnits();
            shader.stop();

            isInitialized = true;
            SysLog.info("G3D|WaterRenderer initialized successfully");
        } catch (Exception e) {
            SysLog.error("G3D|Failed to initialize WaterRenderer: " + e.getMessage());
            cleanUp(); // 清理部分初始化的资源
            isInitialized = false;
        }
    }

    public void render(List<WaterTile> tiles, ICamera camera, Vector3f lightDir) {
        if (!isInitialized || tiles.isEmpty() || !fbos.isInitialized()) {
            return;
        }

        try {
            prepareRender(camera, lightDir);

            // 查询是否有任何错误
            int error = glGetError();
            if (error != GL_NO_ERROR) {
                SysLog.error("G3D|Water render prepare error: " + error);
            }

            synchronized (tiles) {
                for (int i = 0; i < tiles.size(); i++) {
                    WaterTile water = tiles.get(i);
                    Matrix4f modelMatrix = createModelMatrix(water.getX(), water.getHeight(), water.getZ(), water.getTileSize());
                    shader.loadModelMatrix(modelMatrix);
                    shader.loadWaterColor(water.getWaterColor());
                    shader.loadWaterHeight(water.getHeight());

                    // 渲染水面
                    GL.glDrawElements(GL.GL_TRIANGLES, quad.getVertexCount(), GL.GL_UNSIGNED_INT, null, 0);

                    // 检查渲染后的错误
                    error = glGetError();
                    if (error != GL_NO_ERROR) {
                        SysLog.error("G3D|Water render draw error: " + error + " for water: " + water);
                    }
                }
            }
            finish();
        } catch (Exception e) {
            SysLog.error("G3D|Error rendering water: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void cleanUp() {
        if (shader != null) {
            shader.cleanUp();
            shader = null;
        }
    }

    private void prepareRender(ICamera camera, Vector3f lightDir) {
        shader.start();
        shader.loadProjectionMatrix(camera.getProjectionMatrix());
        shader.loadViewMatrix(camera.getViewMatrix());
        shader.loadCameraPosition(camera.getPosition());

        moveFactor += 0.001f;
        moveFactor %= 1;
        shader.loadMoveFactor(moveFactor);
        shader.loadLightDirection(lightDir);

        // 绑定顶点数组并启用顶点属性
        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);

        // 绑定纹理
        bindTextures();

        // 设置渲染状态
        doRenderSettings();
    }

    private void bindTextures() {
        // 在Android上，确保所有纹理都已正确绑定
        for (int i = 0; i < 5; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, 0); // 先解绑所有纹理
        }

        // 按照顺序绑定纹理
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

        // 验证纹理绑定
        int[] boundTexture = new int[1];
        glActiveTexture(GL_TEXTURE0);
        glGetIntegerv(GL_TEXTURE_BINDING_2D, boundTexture, 0);
        if (boundTexture[0] != fbos.getReflectionTexture()) {
            SysLog.error("G3D|Failed to bind reflection texture: expected " + fbos.getReflectionTexture() + ", got " + boundTexture[0]);
        }
    }

    private void doRenderSettings() {
        glEnable(GL_DEPTH_TEST);

        // 设置混合模式
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // 禁用面剔除以确保水面在所有角度可见
        glDisable(GL_CULL_FACE);
    }

    private void finish() {
        // 重新启用面剔除
        glEnable(GL_CULL_FACE);

        // 禁用混合
        glDisable(GL_BLEND);

        // 禁用顶点属性并解绑顶点数组
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        // 停止着色器
        shader.stop();
    }

    private Matrix4f createModelMatrix(float x, float y, float z, float scale) {
        modelMatrix.identity();
        GLMath.mat4x4_translate(modelMatrix.mat, x, y, z);
        modelMatrix1.identity();
        GLMath.mat4x4_scale_aniso(modelMatrix1.mat, modelMatrix.mat, scale, scale, scale);
        return modelMatrix1;
    }

    public boolean isInitialized() {
        return isInitialized && fbos.isInitialized();
    }
}
