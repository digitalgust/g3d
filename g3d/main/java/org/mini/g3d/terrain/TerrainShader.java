package org.mini.g3d.terrain;

import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.Light;
import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.entity.EntityShader;

import java.util.Iterator;
import java.util.List;

import static org.mini.g3d.entity.EntityShader.MAX_LIGHTS;

public class TerrainShader extends ShaderProgram {

    private static final String VERTEX_FILE = "/org/mini/g3d/res/shader/terrainVertex.glsl";
    private static final String FRAGMENT_FILE = "/org/mini/g3d/res/shader/terrainFragment.glsl";

    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_depthBiasMVPMatrix;
    private int location_lightPosition[];
    private int location_lightColour[];
    private int location_attenuation[];
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_skyColour;
    private int location_backgroundTexture;
    private int location_rTexture;
    private int location_gTexture;
    private int location_bTexture;
    private int location_blendMap;
    private int location_shadowMap;
    private int location_cameraPosition;
    private int location_lightPos;
    private int location_noiseTexture;
    private int location_itime;
    private int location_transparencyDistance;

    //
    Vector3f defLightPos = new Vector3f();
    Vector3f defLightColor = new Vector3f();
    Vector3f defLightAttenuation = new Vector3f(1, 0, 0);

    public TerrainShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    protected String preProcessShader(String shader, int type) {
        return EntityShader.defineMaxLights(shader);
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
        location_depthBiasMVPMatrix = super.getUniformLocation("depthBiasMVPMatrix");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
        location_skyColour = super.getUniformLocation("skyColour");
        location_backgroundTexture = super.getUniformLocation("backgroundTexture");
        location_rTexture = super.getUniformLocation("rTexture");
        location_gTexture = super.getUniformLocation("gTexture");
        location_bTexture = super.getUniformLocation("bTexture");
        location_blendMap = super.getUniformLocation("blendMap");
        location_shadowMap = super.getUniformLocation("shadowMap");
        location_cameraPosition = getUniformLocation("cameraPos");
        location_lightPos = getUniformLocation("lightPos");
        location_noiseTexture = getUniformLocation("noisetex");
        location_itime = getUniformLocation("itime");
        location_transparencyDistance = getUniformLocation("transparencyDistance");

        location_lightPosition = new int[MAX_LIGHTS];
        location_lightColour = new int[MAX_LIGHTS];
        location_attenuation = new int[MAX_LIGHTS];

        for (int i = 0; i < MAX_LIGHTS; i++) {
            location_lightPosition[i] = super.getUniformLocation("lightPosition[" + i + "]");
            location_lightColour[i] = super.getUniformLocation("lightColour[" + i + "]");
            location_attenuation[i] = super.getUniformLocation("attenuation[" + i + "]");
        }
    }

    public void connectTextureUnits() {
        super.loadInt(location_backgroundTexture, 0);
        super.loadInt(location_rTexture, 1);
        super.loadInt(location_gTexture, 2);
        super.loadInt(location_bTexture, 3);
        super.loadInt(location_blendMap, 4);
        super.loadInt(location_noiseTexture, 5);
        super.loadInt(location_shadowMap, 6);
    }

    public void loadSkyColour(Vector3f fogColor) {
        super.loadVector(location_skyColour, fogColor);
    }

    public void loadTransparencyDistance(float transparencyDistance) {
        super.loadFloat(location_transparencyDistance, transparencyDistance);
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
                super.loadVector(location_lightPosition[i], defLightPos);
                super.loadVector(location_lightColour[i], defLightColor);
                super.loadVector(location_attenuation[i], defLightAttenuation);
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
        super.loadMatrix(location_viewMatrix, camera.getViewMatrix());
    }

    public void loadTime(float itime) {
        loadFloat(location_itime, itime);
    }

    public void loadDepthBiasMVPMatrix(Matrix4f depthBiasMVPMatrix) {
        super.loadMatrix(location_depthBiasMVPMatrix, depthBiasMVPMatrix);
    }

    public void loadCameraPosition(Vector3f pos) {
        loadVector(location_cameraPosition, pos);
    }

    public void loadLightPos(Vector3f pos) {
        loadVector(location_lightPos, pos);
    }

}
