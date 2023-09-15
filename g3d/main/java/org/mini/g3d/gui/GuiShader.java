package org.mini.g3d.gui;

import org.mini.g3d.core.vector.Matrix4f;

import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.vector.Vector2f;

public class GuiShader extends ShaderProgram {

    private static final String VERTEX_FILE = "/org/mini/g3d/res/shader/guiVertex.glsl";
    private static final String FRAGMENT_FILE = "/org/mini/g3d/res/shader/guiFragment.glsl";

    private int location_numberOfRows;
    private int location_texOffsets;
    private int location_transformationMatrix;

    public GuiShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    public void loadTransformation(Matrix4f matrix) {
        super.loadMatrix(location_transformationMatrix, matrix);
    }

    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
        location_numberOfRows = super.getUniformLocation("numberOfRows");
        location_texOffsets = super.getUniformLocation("texOffsets");
    }

    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }

    protected void loadNumberOfRows(float numberOfRows) {
        super.loadFloat(location_numberOfRows, numberOfRows);
    }

    protected void loadTexOffsets(Vector2f texOffsets) {
        super.loadVector2D(location_texOffsets, texOffsets);
    }

}