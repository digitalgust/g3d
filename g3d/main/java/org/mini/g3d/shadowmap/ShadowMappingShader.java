package org.mini.g3d.shadowmap;

import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.vector.Matrix4f;

public class ShadowMappingShader extends ShaderProgram {

    private static final String VERTEX_FILE = "/org/mini/g3d/res/shader/shadowMappingVertex.glsl";
    private static final String FRAGMENT_FILE = "/org/mini/g3d/res/shader/shadowMappingFragment.glsl";

    private int location_depthMVP;

    public ShadowMappingShader() {
        super(VERTEX_FILE, FRAGMENT_FILE);
    }

    protected void bindAttributes() {
        // 绑定常规顶点属性
        super.bindAttribute(0, "position");
        super.bindAttribute(1, "uv");
        super.bindAttribute(2, "normal");
        
        // 绑定实例化属性
        super.bindAttribute(3, "instancePosition");
        super.bindAttribute(4, "instanceRotation");
        super.bindAttribute(5, "instanceScale");
        super.bindAttribute(6, "instanceTextureOffset");
        super.bindAttribute(7, "instanceTransparency");
    }

    protected void getAllUniformLocations() {
        location_depthMVP = super.getUniformLocation("depthMVP");
    }

    public void loadDepthMVP(Matrix4f depthMVP) {
        super.loadMatrix(location_depthMVP, depthMVP);
    }
}
