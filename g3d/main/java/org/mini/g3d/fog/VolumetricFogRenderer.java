package org.mini.g3d.fog;

import org.mini.g3d.core.Scene;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.util.Loader;
import org.mini.g3d.core.vector.*;
import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.Camera;
import org.mini.glwrap.GLUtil;

import static org.mini.gl.GL.*;

public class VolumetricFogRenderer {

    private static final float[] POSITIONS = {
        -1.0f,  1.0f, 0.0f,  // 左上
        -1.0f, -1.0f, 0.0f,  // 左下
         1.0f,  1.0f, 0.0f,  // 右上
         1.0f, -1.0f, 0.0f   // 右下
    };
    private static final float[] TEXTURE_COORDS = {
        0.0f, 1.0f,  // 左上（V=1.0，对应纹理顶部）
        0.0f, 0.0f,  // 左下（V=0.0，对应纹理底部）
        1.0f, 1.0f,  // 右上（V=1.0，对应纹理顶部）
        1.0f, 0.0f   // 右下（V=0.0，对应纹理底部）
    };
    private static final int[] INDICES = {
        0, 1, 2,  // 第一个三角形
        2, 1, 3   // 第二个三角形
    };

    private final RawModel quad;
    private final VolumetricFogShader shader;
    private final int perlinNoiseTexture;
    private final float noiseTextureSize;

    private Vector3f fogColor = new Vector3f(0.8f, 0.85f, 0.9f);
    private float fogDensity;
    private float fogGradient;
    private float time;
    Loader loader = new Loader();

    public VolumetricFogRenderer(Matrix4f projectionMatrix) {
        // 最终雾参数调整（降低密度和梯度）
        fogColor = new Vector3f(0.8f, 0.85f, 0.9f);  // 保持淡蓝色
//        fogColor = new Vector3f(0.4f, 0.4f, 0.0f);  // 保持淡蓝色
        fogDensity = 0.05f;  // 原0.025f→0.05f（增大基础密度，配合放大倍数40000）
        fogGradient = 1.0f;  // 保持梯度为1（线性增长）
        quad = loader.loadToVAOWithoutNormals(POSITIONS, TEXTURE_COORDS, INDICES);
        shader = new VolumetricFogShader();

        noiseTextureSize = 64.0f;
        perlinNoiseTexture = loader.loadTexture3D("/org/mini/g3d/res/perlinnoise64.dat", (int) noiseTextureSize, (int) noiseTextureSize, (int) noiseTextureSize);

        shader.start();
        shader.connectTextureUnits();
        shader.stop();

        time = 0.0f;
    }

    public void render(Scene scene, int sceneTexture, int depthTexture) {
        time += DisplayManager.getFrameTimeSeconds();

        Camera camera = scene.getCamera();
        shader.start();
        shader.loadProjectionMatrix(camera.getProjectionMatrix());
        shader.loadViewMatrix(camera.getViewMatrix());
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " loadViewMatrix");
        shader.loadFogColor(fogColor);
        shader.loadFogDensity(fogDensity);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " loadFogDensity");
        shader.loadFogGradient(fogGradient);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " loadFogGradient");
        shader.loadPlanes(camera.getNear(), camera.getFar());
        shader.loadTime(time);
        shader.loadCameraPosition(camera.getPosition());
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " loadCameraPosition");
        shader.loadNoiseTextureSize(noiseTextureSize);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " loadNoiseTextureSize");

        // 启用混合
        glEnable(GL_BLEND);
        // 使用标准alpha混合模式
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " blendFunc");
        
        // 禁用深度测试
        glDisable(GL_DEPTH_TEST);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " disableDepthTest");

        // 绑定场景纹理和深度纹理
        loader.bindTexture(sceneTexture, 0);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " bindTextures0");
        loader.bindTexture(depthTexture, 1);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " bindTextures1");
        loader.bindTexture3D(perlinNoiseTexture, 2);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " bindTextures2");

        // 渲染全屏四边形
        loader.bindVAO(quad.getVaoID());
        loader.enableVertexAttribArray(0);
        loader.enableVertexAttribArray(1);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " enableAttribArrays");
        glDrawElements(GL_TRIANGLES, quad.getVertexCount(), GL_UNSIGNED_INT, null, 0);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " drawElements");
        loader.disableVertexAttribArray(0);
        loader.disableVertexAttribArray(1);
        loader.unbindVAO();
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " unbindVAO");

        // 恢复状态
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        GLUtil.checkGlError(this.getClass().getCanonicalName() + " restoreState");

        shader.stop();
    }

    public void setFogColor(Vector3f color) {
        this.fogColor = color;
    }

    public void setFogDensity(float density) {
        this.fogDensity = density;
    }

    public void setFogGradient(float gradient) {
        this.fogGradient = gradient;
    }

    public void cleanUp() {
        shader.cleanUp();
        loader.deleteTexture(perlinNoiseTexture);
    }
}