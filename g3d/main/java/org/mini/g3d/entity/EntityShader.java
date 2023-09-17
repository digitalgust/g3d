package org.mini.g3d.entity;

import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.Light;
import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;

import java.util.Iterator;
import java.util.List;

public class EntityShader extends ShaderProgram {

    public static final int MAX_LIGHTS = 4;

    private static final String VERTEX_FILE = "/org/mini/g3d/res/shader/entityVertex.glsl";
    private static final String FRAGMENT_FILE = "/org/mini/g3d/res/shader/entityFragment.glsl";

    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_lightPosition[];
    private int location_lightColour[];
    private int location_attenuation[];
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_useFakeLightning;
    private int location_skyColour;
    private int location_numberOfRows;
    private int location_offset;


    Vector3f defaultLightPosition = new Vector3f(0, 0, 0);
    Vector3f defaultLightColour = new Vector3f(0, 0, 0);
    Vector3f defaultLightAttenuation = new Vector3f(1, 0, 0);

    //avoid new an instance
    Vector2f cachedOffset = new Vector2f();

    public EntityShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    protected String preProcessShader(String shader, int type) {
        return defineMaxLights(shader);
    }

    public static String defineMaxLights(String shader) {
        shader = shader.replace("${MAX_LIGHT_DEFINE_IN_PROGRAM}",
                "\n#define MAX_LIGHT " + MAX_LIGHTS + "\n");
        return shader;
    }

    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
    }

    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
        location_useFakeLightning = super.getUniformLocation("useFakeLightning");
        location_skyColour = super.getUniformLocation("skyColour");
        location_numberOfRows = super.getUniformLocation("numberOfRows");
        location_offset = super.getUniformLocation("offset");

        location_lightPosition = new int[MAX_LIGHTS];
        location_lightColour = new int[MAX_LIGHTS];
        location_attenuation = new int[MAX_LIGHTS];

        for (int i = 0; i < MAX_LIGHTS; i++) {
            location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
            location_lightColour[i] = super.getUniformLocation("lightColour[" + i + "]");
            location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
        }
    }

    public void loadNumberOfRows(int numberOfRows) {
        super.loadFloat(location_numberOfRows, numberOfRows);
    }

    public void loadOffset(float x, float y) {
        cachedOffset.x = x;
        cachedOffset.y = y;
        super.loadVector2D(location_offset, cachedOffset);
    }

    public void loadSkyColour(Vector3f fogColor) {
        super.loadVector(location_skyColour, fogColor);
    }

    public void loadShineVariables(float shineDamper, float reflectivity) {
        super.loadFloat(location_reflectivity, reflectivity);
        super.loadFloat(location_shineDamper, shineDamper);
    }

    public void loadLights(Iterator<Light> it) {
        for (int i = 0; i < MAX_LIGHTS; i++) {
            if (it.hasNext()) {
                Light light = it.next();
                super.loadVector(location_lightPosition[i], light.getPosition());
                super.loadVector(location_lightColour[i], light.getColour());
                super.loadVector(location_attenuation[i], light.getAttentuation());
            } else {
                super.loadVector(location_lightPosition[i], defaultLightPosition);
                super.loadVector(location_lightColour[i], defaultLightColour);
                super.loadVector(location_attenuation[i], defaultLightAttenuation);
            }
        }
    }

    public void loadTransformationMatrix(Matrix4f matrix) {
        super.loadMatrix(location_transformationMatrix, matrix);
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        super.loadMatrix(location_projectionMatrix, matrix);
    }

    public void loadViewMatrix(ICamera camera) {
//        Matrix4f viewMatrix = new Matrix4f();
//        float[] eye = new float[]{GamePanel.sun.getPosition().left, GamePanel.sun.getPosition().top, GamePanel.sun.getPosition().z};
//        GLUtil.mat4x4_look_at(viewMatrix.mat, eye, new float[]{-GamePanel.sun.getPosition().left, -GamePanel.sun.getPosition().top, -GamePanel.sun.getPosition().z}, new float[]{0.0f, 1.0f, 0.0f});
        super.loadMatrix(location_viewMatrix, camera.getViewMatrix());
    }

    public void setLightning(float useFakeLightning) {
        super.loadFloat(location_useFakeLightning, useFakeLightning);
    }

}
