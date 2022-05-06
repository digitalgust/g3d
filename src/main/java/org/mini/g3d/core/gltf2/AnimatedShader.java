/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2;


import org.mini.g3d.core.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.core.gltf2.render.*;
import org.mini.g3d.core.gltf2.render.UniformLight;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.glwrap.GLUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

//Inspiration from shader.js and LWJGL3 book
public class AnimatedShader extends org.mini.g3d.core.ShaderProgram {


//    private Map<String, UniformData> uniforms = new HashMap<>();
//    private Map<String, Integer> attributes = new HashMap<>();

//    private List<String> unknownAttributes = new ArrayList<>();
//    private List<String> unknownUniforms = new ArrayList<>();


    int location_u_ViewProjectionMatrix;
    int location_u_ModelMatrix;
    int location_u_NormalMatrix;
    int location_u_Exposure;
    int location_u_Camera;
    int[] location_u_jointMatrix;
    int[] location_u_jointNormalMatrix;
    int location_u_morphWeights;
    int[] location_u_Lights;
    int[] location_u_MaterialProperties;
    int[] location_u_MaterialTextures;
    //
    int[] location_attribute;
    public int location_a_Position;


    public AnimatedShader(int v, int f) {
        super(v, f);
    }

    protected void getAllUniformLocations() {

    }

    public void getAllUniformLocations(RenderNode node, int lightCount) {

        //get location
        glUseProgram(getProgramId());
        location_u_ViewProjectionMatrix = getUniformLocation("u_ViewProjectionMatrix");
        location_u_ModelMatrix = getUniformLocation("u_ModelMatrix");
        location_u_NormalMatrix = getUniformLocation("u_NormalMatrix");
        location_u_Exposure = getUniformLocation("u_Exposure");
        location_u_Camera = getUniformLocation("u_Camera");
        location_u_morphWeights = getUniformLocation("u_morphWeights");
        if (node instanceof RenderMeshPrimitive) {
            RenderMeshPrimitive rmp = (RenderMeshPrimitive) node;
            //
            int c = 0;
            location_attribute = new int[rmp.getGlAttributes().size()];
            for (Map.Entry<String, GLTFAccessor> entry : rmp.getGlAttributes().entrySet()) {
                String attributeName = entry.getKey();
                GLTFAccessor accessor = entry.getValue();


                int location = glGetAttribLocation(getProgramId(), GLUtil.toUtf8(attributeName));
                location_attribute[c++] = location;
                if (location < 0) {
                    continue;
                }
                GlUtil.enableAttribute(location, accessor);
            }

            RenderMesh mesh = rmp.getMesh();
            if (mesh.getSkin() != null) {
                RenderSkin skin = mesh.getSkin();
                int size = skin.getJointMatrices().size();
                location_u_jointMatrix = new int[size];
                location_u_jointNormalMatrix = new int[size];
                for (int i = 0; i < size; i++) {
                    location_u_jointMatrix[i] = getUniformLocation("u_jointMatrix[" + i + "]");
                    location_u_jointNormalMatrix[i] = getUniformLocation("u_jointNormalMatrix[" + i + "]");
                }
            }

            //
            RenderMaterial material = rmp.getMaterial();
            Map<String, Object> map = material.getProperties();
            location_u_MaterialProperties = new int[map.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                location_u_MaterialProperties[i] = getUniformLocation(entry.getKey());
                i++;
            }
            Map<String, RenderTexture> texs = material.getTexturesMap();
            location_u_MaterialTextures = new int[texs.size()];
            i = 0;
            for (Map.Entry<String, RenderTexture> entry : texs.entrySet()) {
                location_u_MaterialTextures[i] = getUniformLocation(entry.getKey());
            }
        }
        //
        location_u_Lights = new int[lightCount];
        for (int i = 0; i < lightCount; i++) {
            location_u_Lights[i] = getUniformLocation("u_Lights[" + i + "]");
        }

        location_a_Position = glGetAttribLocation(getProgramId(), GLUtil.toUtf8("a_Position"));
    }


    public void load_materialProperties(RenderMaterial material) {
        int i = 0;
        for (Map.Entry<String, Object> entry : material.getProperties().entrySet()) {
            loadUniform(location_u_MaterialProperties[i++], entry.getValue());
        }
    }

    public void load_materialTextures(RenderMaterial material) {
        int i = 0;
        int texSlot = 1;
        for (Map.Entry<String, RenderTexture> entry : material.getTexturesMap()
                .entrySet()) {
            loadInt(location_u_MaterialTextures[i++], texSlot++);
        }
        bindTextures(material);
    }

    private void bindTextures(RenderMaterial material) {
        int i = 0;
        int texSlot = 1;
        for (Map.Entry<String, RenderTexture> entry : material.getTexturesMap()
                .entrySet()) {
            RenderTexture renderTexture = entry.getValue();
            //Activate a texture slot for new texture
            glActiveTexture(GL_TEXTURE0 + texSlot++);
            //Bind renderTexture to slot
            renderTexture.bindTexture();
        }
    }

    public void load_u_ViewProjectionMatrix(Matrix4f pm) {
        super.loadMatrix(location_u_ViewProjectionMatrix, pm);
    }

    public void load_u_ModelMatrix(Matrix4f pm) {
        super.loadMatrix(location_u_ModelMatrix, pm);
    }

    public void load_u_NormalMatrix(Matrix4f pm) {
        super.loadMatrix(location_u_NormalMatrix, pm);
    }

    public void load_u_Exposure(float pm) {
        super.loadFloat(location_u_Exposure, pm);
    }

    public void load_u_Camera(Vector3f pm) {
        super.loadVector(location_u_Camera, pm);
    }

    public void load_u_morphWeights(float[] pm) {
        super.loadFloatArr(location_u_morphWeights, pm);
    }

    public void load_u_jointMatrices(List<Matrix4f> list) {
        for (int i = 0, imax = list.size(); i < imax; i++) {
            Matrix4f pm = list.get(i);
            super.loadMatrix(location_u_jointMatrix[i], pm);
        }
    }

    public void load_u_jointNormalMatrices(List<Matrix4f> list) {
        for (int i = 0, imax = list.size(); i < imax; i++) {
            Matrix4f pm = list.get(i);
            super.loadMatrix(location_u_jointNormalMatrix[i], pm);
        }
    }

    public void load_u_Lights(List<RenderLight> list) {
        Field[] fields = UniformLight.class.getDeclaredFields();
        int i = 0;
        for (RenderLight rlight : list) {
            for (Field field : fields) {
                String uniformName = "u_Lights[" + i + "]." + field.getName();
                try {
                    //setUniform(uniformName, field.get(rlight.getUniformLight()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            i++;
        }
    }

    protected void bindAttributes() {
    }

    public int bindAttributes(Map<String, GLTFAccessor> getGlAttribute) {
        int vertCount = 0;
        int i = 0;
        for (GLTFAccessor accessor : getGlAttribute.values()) {
            int location = location_attribute[i++];
            vertCount += accessor.getCount();
            if (location < 0) {
                continue;
            }
            GlUtil.enableAttribute(location, accessor);
        }
        return vertCount;
    }


    public void unbindAttributes() {
        int i = 0;
        for (; i < location_attribute.length; i++) {
            glDisableVertexAttribArray(location_attribute[i]);
        }
    }
}
