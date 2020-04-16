package org.mini.g3d.animation;

import org.mini.g3d.core.Camera;
import org.mini.g3d.core.Light;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;

import java.util.List;

import static org.mini.g3d.core.MasterShader.MAX_LIGHTS;


/**
 * Implemented the same way as done for a entity,
 * only difference here is adding the two extra attributes:
 * joint ids and weights
 *
 * @author Glenn Arne Christensen
 */

public class AnimatedModelShader extends org.mini.g3d.core.ShaderProgram {

    private static final String VERTEX_SHADER = "/res/shader/animatedEntityVertex.shader";
    private static final String FRAGMENT_SHADER = "/res/shader/animatedEntityFragment.shader";

    private int location_transformationMatrix;
    private int location_projectionMatrix;
    private int location_viewMatrix;
    private int location_lightPosition[];
    private int location_lightColour[];
    private int location_attenuation[];
    private int location_shineDamper;
    private int location_reflectivity;
    private int location_useFakeLighting;
    private int location_skyColour;
    private int location_numberOfRows;
    private int location_offset;
    private int location_jointTransforms[];

    private static final int MAX_JOINTS = 50; // The max number of joints allowed in a skeleton

    /**
     * Creates a shader program for the AnimatedModelRenderer and
     * sends the vertex and fragment shader path to the shaderProgram
     */
    public AnimatedModelShader() {
        super(VERTEX_SHADER, FRAGMENT_SHADER);
    }

    // Links up the variables in the vertexShader to the AnimatedModelShader
    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
        super.bindAttribute(3, "jointIndices");
        super.bindAttribute(4, "weights");
    }

    // Gets the location of the uniform variable found in animatedEntityVertex
    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
        location_shineDamper = super.getUniformLocation("shineDamper");
        location_reflectivity = super.getUniformLocation("reflectivity");
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

        location_jointTransforms = new int[MAX_JOINTS];
        for (int i = 0; i < MAX_JOINTS; i++) {
            location_jointTransforms[i] = super.getUniformLocation("jointTransforms[" + i + "]");
        }


    }

    // Load up values to the damper and reflectivity
    public void loadShineVariables(float damper, float refectivity) {
        super.loadFloat(location_shineDamper, damper);
        super.loadFloat(location_reflectivity, refectivity);
    }

    public void loadNumberOfRows(int numberOfRows) {
        super.loadFloat(location_numberOfRows, numberOfRows);
    }

    public void loadOffset(float x, float y) {
        super.loadVector2D(location_offset, new Vector2f(x, y));
    }

    public void loadSkyColor(float r, float g, float b) {
        super.loadVector(location_skyColour, new Vector3f(r, g, b));
    }

    public void loadFakeLightingVariable(boolean useFakeLighting) {
        super.loadBoolean(location_useFakeLighting, useFakeLighting);
    }

    // Takes inn the matrix we want to load up
    public void loadTransformationMatrix(Matrix4f matrix) {
        super.loadMatrix(location_transformationMatrix, matrix);
    }

    // Takes inn a light we want to load up
    public void loadLights(List<Light> lights) {
        for (int i = 0; i < MAX_LIGHTS; i++) {
            if (i < lights.size()) {
                super.loadVector(location_lightPosition[i], lights.get(i).getPosition());
                super.loadVector(location_lightColour[i], lights.get(i).getColour());
                super.loadVector(location_attenuation[i], lights.get(i).getAttentuation());
            } else {
                super.loadVector(location_lightPosition[i], new Vector3f(0, 0, 0));
                super.loadVector(location_lightColour[i], new Vector3f(0, 0, 0));
                super.loadVector(location_attenuation[i], new Vector3f(1, 0, 0));
            }
        }
    }

    Matrix4f empty = new Matrix4f();
    // Takes inn the joint transforms we want to load up
    public void loadJointTransforms(Matrix4f[] transforms) {
        for (int i = 0; i < MAX_JOINTS; i++) {
            if (i < transforms.length) {
                super.loadMatrix(location_jointTransforms[i], transforms[i]);
            } else {
                super.loadMatrix(location_jointTransforms[i], empty);
            }
        }
    }

    // Takes inn the projection matrix we want to load up
    public void loadProjectionMatrix(Matrix4f projection) {
        super.loadMatrix(location_projectionMatrix, projection);
    }

    // Takes inn the view matrix we want to load up
    public void loadViewMatrix(Camera camera) {
        super.loadMatrix(location_viewMatrix, camera.getViewMatrix());
    }

}