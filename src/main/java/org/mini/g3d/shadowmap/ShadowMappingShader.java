package org.mini.g3d.shadowmap;

import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.vector.Matrix4f;

public class ShadowMappingShader extends ShaderProgram {


    private static final String VERTEX_FILE = "/res/shader/shadowMappingVertex.shader";
    private static final String FRAGMENT_FILE = "/res/shader/shadowMappingFragment.shader";

    private int location_depthMVP;

    public ShadowMappingShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    protected void bindAttributes() {
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "textureCoords");
        super.bindAttribute(2, "normal");
    }

    protected void getAllUniformLocations() {
        location_depthMVP = super.getUniformLocation("depthMVP");

    }

    public void loadDepthMVP(Matrix4f depthMVP) {
        super.loadMatrix(location_depthMVP, depthMVP);
    }


}
