package org.mini.g3d.gui;
 
import org.mini.g3d.core.vector.Matrix4f;
 
import org.mini.g3d.core.ShaderProgram;
 
public class GuiShader extends ShaderProgram {
     
    private static final String VERTEX_FILE = "/res/shader/guiVertex.shader";
    private static final String FRAGMENT_FILE = "/res/shader/guiFragment.shader";
     
    private int location_transformationMatrix;
 
    public GuiShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }
     
    public void loadTransformation(Matrix4f matrix){
        super.loadMatrix(location_transformationMatrix, matrix);
    }
 
    @Override
    protected void getAllUniformLocations() {
        location_transformationMatrix = super.getUniformLocation("transformationMatrix");
    }
 
    @Override
    protected void bindAttributes() {
        super.bindAttribute(0, "position");
    }
     
 
}