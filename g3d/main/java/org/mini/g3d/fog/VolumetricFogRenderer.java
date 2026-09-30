package org.mini.g3d.fog;

import org.mini.g3d.core.Scene;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.util.Loader;
import org.mini.g3d.core.vector.*;
import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.Camera;
import org.mini.glwrap.GLFrameBuffer;
import org.mini.glwrap.GLUtil;

import static org.mini.gl.GL.*;

/**
 * 全屏后处理式体积雾
 * <p>
 * 渲染分两步:
 * 1. 把加了雾的画面画进自己的fogFbo, 此时采样主FBO的颜色/深度纹理是合法的(主FBO未绑定)
 * 2. 再用拷贝着色器把fogFbo的内容写回主FBO
 * 不能在主FBO绑定状态下直接采样主FBO自己的附着纹理, 那是GL反馈回路, 属未定义行为
 */
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
    private final ScreenCopyShader copyShader;
    private final int perlinNoiseTexture;
    private final float noiseTextureSize;

    // 雾的着色结果先画到这里, 再拷回主FBO
    private GLFrameBuffer fogFbo;

    private final Vector3f fogColor = new Vector3f(0.8f, 0.85f, 0.9f);
    private final Vector4f fogArea = new Vector4f(0f, 0f, 100f, 100f); //地图矩形minX,minZ,maxX,maxZ, 矩形外是云海
    private float fogDensity = 0.95f; // 云海最浓处的浓度 0~1
    private float fogEdgeFade = 25f;  // 出界后云海增浓的过渡宽度
    private float fogHeight = 0f;     // 云海平面高度
    private float fogDepthFade = 20f; // 低于云面溶入云中的垂直过渡深度
    private float time;
    Loader loader = new Loader();

    /**
     * 把一个纹理原样画到当前绑定的帧缓冲上, 用于把fogFbo的内容拷回主FBO
     */
    static class ScreenCopyShader extends org.mini.g3d.core.ShaderProgram {

        private int location_srcTexture;

        private static final String VERTEX_FILE = "/org/mini/g3d/res/shader/screenCopyVertex.glsl";
        private static final String FRAGMENT_FILE = "/org/mini/g3d/res/shader/screenCopyFragment.glsl";

        ScreenCopyShader() {
            super(VERTEX_FILE, FRAGMENT_FILE);
        }

        @Override
        protected void getAllUniformLocations() {
            location_srcTexture = getUniformLocation("srcTexture");
        }

        @Override
        protected void bindAttributes() {
            bindAttribute(0, "position");
            bindAttribute(1, "textureCoordinates");
        }

        void connectTextureUnits() {
            loadInt(location_srcTexture, 0);
        }
    }

    public VolumetricFogRenderer(Matrix4f projectionMatrix) {
        quad = loader.loadToVAOWithoutNormals(POSITIONS, TEXTURE_COORDS, INDICES);
        shader = new VolumetricFogShader();
        copyShader = new ScreenCopyShader();

        noiseTextureSize = 64.0f;
        perlinNoiseTexture = loader.loadTexture3D("/org/mini/g3d/res/perlinnoise64.dat", (int) noiseTextureSize, (int) noiseTextureSize, (int) noiseTextureSize);

        shader.start();
        shader.connectTextureUnits();
        shader.stop();

        copyShader.start();
        copyShader.connectTextureUnits();
        copyShader.stop();

        time = 0.0f;
    }

    /**
     * 在主Fbo的begin..end之间调用
     */
    public void render(Scene scene, GLFrameBuffer mainFbo) {
        time += DisplayManager.getFrameTimeSeconds();

        ensureFogFbo(mainFbo.getTexWidth(), mainFbo.getTexHeight());

        Camera camera = scene.getCamera();

        // 全屏四边形不受剔除影响: 若前面某个渲染器改过glFrontFace/cullFace, 四边形会被整体剔除掉
        int[] cullEnabled = new int[1];
        glGetBooleanv(GL_CULL_FACE, cullEnabled, 0);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);

        // 第一步: 雾化, 画进fogFbo(此时主Fbo未绑定, 采样其纹理合法)
        // fogFbo.begin会保存当前绑定, end会恢复, 所以结束后仍绑定在主Fbo上
        fogFbo.begin();

        shader.start();
        shader.loadProjectionMatrix(camera.getProjectionMatrix());
        shader.loadViewMatrix(camera.getViewMatrix());
        shader.loadFogColor(fogColor);
        shader.loadFogDensity(fogDensity);
        shader.loadFogArea(fogArea);
        shader.loadFogEdgeFade(fogEdgeFade);
        shader.loadFogHeight(fogHeight);
        shader.loadFogDepthFade(fogDepthFade);
        shader.loadTime(time);
        shader.loadCameraPosition(camera.getPosition());

        loader.bindTexture(mainFbo.getColorTexture(), 0);
        loader.bindTexture(mainFbo.getDepthTexture(), 1);
        loader.bindTexture3D(perlinNoiseTexture, 2);

        drawQuad();
        shader.stop();

        fogFbo.end();

        // 第二步: 把雾化结果拷回主Fbo(当前绑定), fogFbo未绑定, 采样合法
        copyShader.start();
        copyShader.connectTextureUnits();
        loader.bindTexture(fogFbo.getColorTexture(), 0);

        drawQuad();
        copyShader.stop();

        // 恢复状态
        glEnable(GL_DEPTH_TEST);
        if (cullEnabled[0] != 0) {
            glEnable(GL_CULL_FACE);
        }
    }

    private void drawQuad() {
        loader.bindVAO(quad.getVaoID());
        loader.enableVertexAttribArray(0);
        loader.enableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, quad.getVertexCount(), GL_UNSIGNED_INT, null, 0);
        loader.disableVertexAttribArray(0);
        loader.disableVertexAttribArray(1);
        loader.unbindVAO();
    }

    private void ensureFogFbo(int w, int h) {
        if (fogFbo != null && fogFbo.getTexWidth() == w && fogFbo.getTexHeight() == h) {
            return;
        }
        if (fogFbo != null) {
            fogFbo.delete();
        }
        // 无需深度附着, 颜色纹理即可; 尺寸用主Fbo的纹理尺寸
        fogFbo = new GLFrameBuffer(w, h, 1f, false);
        fogFbo.gl_init();
    }

    public void setFogColor(Vector3f color) {
        this.fogColor.set(color);
    }

    public void setFogDensity(float density) {
        this.fogDensity = density;
    }

    public void setFogArea(float minX, float minZ, float maxX, float maxZ) {
        this.fogArea.set(minX, minZ, maxX, maxZ);
    }

    public void setFogEdgeFade(float edgeFade) {
        this.fogEdgeFade = edgeFade;
    }

    public void setFogHeight(float height) {
        this.fogHeight = height;
    }

    public void setFogDepthFade(float depthFade) {
        this.fogDepthFade = depthFade;
    }

    public void cleanUp() {
        shader.cleanUp();
        copyShader.cleanUp();
        loader.deleteTexture(perlinNoiseTexture);
        if (fogFbo != null) {
            fogFbo.delete();
            fogFbo = null;
        }
    }
}
