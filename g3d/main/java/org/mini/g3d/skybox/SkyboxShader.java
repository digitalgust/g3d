package org.mini.g3d.skybox;

import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

public class SkyboxShader extends ShaderProgram {

    private static final String VERTEX_FILE = "/org/mini/g3d/res/shader/skyboxVertex.glsl";
    private static final String FRAGMENT_FILE = "/org/mini/g3d/res/shader/skyboxFragment.glsl";

    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_fogColour;
    private int location_cubeMap;
    private int location_cubeMap2;
    private int location_blendFactor;

    private static final float ROTATE_SPEED = 0.1f;

    private float rotation = 0;

    Matrix4f viewMatrix = new Matrix4f();

    public SkyboxShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        super.loadMatrix(location_projectionMatrix, matrix);
    }

    public void loadViewMatrix(ICamera camera) {
        camera.getViewMatrix(viewMatrix);
        viewMatrix.mat[Matrix4f.M30] = 0;
        viewMatrix.mat[Matrix4f.M31] = 0;
        viewMatrix.mat[Matrix4f.M32] = 0;
        rotation += ROTATE_SPEED * DisplayManager.getFrameTimeSeconds();
        Matrix4f.rotate((float) Math.toRadians(rotation), 0, 1, 0, viewMatrix, viewMatrix);
        super.loadMatrix(location_viewMatrix, viewMatrix);
    }

    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_fogColour = super.getUniformLocation("fogColour");
        location_cubeMap = super.getUniformLocation("cubeMap");
        location_cubeMap2 = super.getUniformLocation("cubeMap2");
        location_blendFactor = super.getUniformLocation("blendFactor");
    }

    public void loadBlendFactor(float blend) {
        super.loadFloat(location_blendFactor, blend);
    }

    public void connectTextureUnits() {
        super.loadInt(location_cubeMap, 0);
        super.loadInt(location_cubeMap2, 1);
    }

    protected void loadFogColour(Vector3f fogColor) {
        super.loadVector(location_fogColour, fogColor);
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

}
