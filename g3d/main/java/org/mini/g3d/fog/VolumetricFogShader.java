package org.mini.g3d.fog;

import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.glwrap.GLUtil;

import static org.mini.gl.GL.*;

public class VolumetricFogShader extends ShaderProgram {

    private static final String VERTEX_FILE = "/org/mini/g3d/res/shader/volumetricFogVertex.glsl";
    private static final String FRAGMENT_FILE = "/org/mini/g3d/res/shader/volumetricFogFragment.glsl";

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_fogColor;
    private int location_fogDensity;
    private int location_fogGradient;
    private int location_nearPlane;
    private int location_farPlane;
    private int location_time;
    private int location_cameraPosition;
    private int location_noiseTextureSize;

    public VolumetricFogShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        GLUtil.checkGlError(this.getClass().getCanonicalName());
        location_fogColor = super.getUniformLocation("fogColor");
        location_fogDensity = super.getUniformLocation("fogDensity");
        location_fogGradient = super.getUniformLocation("fogGradient");
        location_nearPlane = super.getUniformLocation("nearPlane");
        GLUtil.checkGlError(this.getClass().getCanonicalName());
        location_farPlane = super.getUniformLocation("farPlane");
        location_time = super.getUniformLocation("time");
        location_cameraPosition = super.getUniformLocation("cameraPosition");
        location_noiseTextureSize = super.getUniformLocation("noiseTextureSize");
        GLUtil.checkGlError(this.getClass().getCanonicalName());
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoordinates");
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        super.loadMatrix(location_projectionMatrix, matrix);
    }

    public void loadViewMatrix(Matrix4f matrix) {
        super.loadMatrix(location_viewMatrix, matrix);
    }

    public void loadFogColor(Vector3f color) {
        super.loadVector(location_fogColor, color);
    }

    public void loadFogDensity(float density) {
        super.loadFloat(location_fogDensity, density);
    }

    public void loadFogGradient(float gradient) {
        super.loadFloat(location_fogGradient, gradient);
    }

    public void loadPlanes(float nearPlane, float farPlane) {
        super.loadFloat(location_nearPlane, nearPlane);
        super.loadFloat(location_farPlane, farPlane);
    }

    public void loadTime(float time) {
        super.loadFloat(location_time, time);
    }

    public void loadCameraPosition(Vector3f position) {
        super.loadVector(location_cameraPosition, position);
    }

    public void loadNoiseTextureSize(float size) {
        super.loadFloat(location_noiseTextureSize, size);
    }

    public void connectTextureUnits() {
        // 交换depthTexture和sceneTexture的绑定单元，与渲染时的绑定顺序一致
        super.loadInt(super.getUniformLocation("sceneTexture"), 0);  // 场景纹理对应单元0
        super.loadInt(super.getUniformLocation("depthTexture"), 1);  // 深度纹理对应单元1
        super.loadInt(super.getUniformLocation("perlinNoise"), 2);
        GLUtil.checkGlError(this.getClass().getCanonicalName());
    }
}