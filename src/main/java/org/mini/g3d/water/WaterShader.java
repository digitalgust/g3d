package org.mini.g3d.water;


import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

public class WaterShader extends ShaderProgram {

    private final static String VERTEX_FILE = "/org/mini/g3d/res/shader/waterVertex.glsl";
    private final static String FRAGMENT_FILE = "/org/mini/g3d/res/shader/waterFragment.glsl";


    private int location_modelMatrix;
    private int location_viewMatrix;
    private int location_projectionMatrix;
    private int location_moveFactor;
    private int location_waterColor;
    private int location_cameraPosition;
    private int location_lightDirection;
    private int location_reflectionTexture;
    private int location_refractionTexture;
    private int location_dudvMap;
    private int location_normalMap;
    private int location_depthMap;


    public WaterShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }


    public void connectTextureUnits() {
        super.loadInt(location_reflectionTexture, 0);
        super.loadInt(location_refractionTexture, 1);
        super.loadInt(location_dudvMap, 2);
        super.loadInt(location_normalMap, 3);
        super.loadInt(location_depthMap, 4);
    }

    @Override
    protected void bindAttributes() {
        bindAttribute(0, "position");
    }

    @Override
    protected void getAllUniformLocations() {
        location_projectionMatrix = getUniformLocation("projectionMatrix");
        location_viewMatrix = getUniformLocation("viewMatrix");
        location_modelMatrix = getUniformLocation("modelMatrix");
        location_moveFactor = getUniformLocation("moveFactor");
        location_waterColor = getUniformLocation("waterColor");
        location_cameraPosition = getUniformLocation("cameraPosition");
        location_lightDirection = getUniformLocation("lightDirection");
        location_reflectionTexture = getUniformLocation("reflectionTexture");
        location_refractionTexture = getUniformLocation("refractionTexture");
        location_dudvMap = getUniformLocation("dudvMap");
        location_normalMap = getUniformLocation("normalMap");
        location_depthMap = getUniformLocation("depthMap");
    }

    public void loadProjectionMatrix(Matrix4f projection) {
        loadMatrix(location_projectionMatrix, projection);
    }

    public void loadViewMatrix(Matrix4f viewMatrix) {
        loadMatrix(location_viewMatrix, viewMatrix);
    }

    public void loadModelMatrix(Matrix4f modelMatrix) {
        loadMatrix(location_modelMatrix, modelMatrix);
    }

    public void loadCameraPosition(Vector3f pos) {
        loadVector(location_cameraPosition, pos);
    }

    public void loadMoveFactor(float moveFactor) {
        loadFloat(location_moveFactor, moveFactor);
    }


    public void loadLightDirection(Vector3f direction) {
        loadVector(location_lightDirection, direction);
    }

    public void loadWaterColor(Vector3f waterColor) {
        loadVector(location_waterColor, waterColor);
    }

}
