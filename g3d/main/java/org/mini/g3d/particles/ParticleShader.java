package org.mini.g3d.particles;

import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.vector.Matrix4f;

public class ParticleShader extends ShaderProgram {

    private static final String VERTEX_FILE = "/org/mini/g3d/res/shader/particleVertex.glsl";
    private static final String FRAGMENT_FILE = "/org/mini/g3d/res/shader/particleFragment.glsl";

    private int location_numberOfRows;
    private int location_projectionMatrix;
    private int location_viewMatrix;

    public ParticleShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    @Override
    protected void getAllUniformLocations() {
        location_numberOfRows = super.getUniformLocation("numberOfRows");
        location_projectionMatrix = super.getUniformLocation("projectionMatrix");
        location_viewMatrix = super.getUniformLocation("viewMatrix");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "particlePosition");
        super.bindAttribute(2, "particleRotation");
        super.bindAttribute(3, "particleScale");
        super.bindAttribute(4, "particleOrientCamera");
        super.bindAttribute(5, "texOffsets");
        super.bindAttribute(6, "blendFactor");
        super.bindAttribute(7, "blendColor");
    }

    protected void loadNumberOfRows(float numberOfRows) {
        super.loadFloat(location_numberOfRows, numberOfRows);
    }

    protected void loadProjectionMatrix(Matrix4f projectionMatrix) {
        loadMatrix(location_projectionMatrix, projectionMatrix);
    }

    protected void loadViewMatrix(Matrix4f viewMatrix) {
        loadMatrix(location_viewMatrix, viewMatrix);
    }
}
