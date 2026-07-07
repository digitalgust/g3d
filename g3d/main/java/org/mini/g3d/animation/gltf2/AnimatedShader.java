/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2;


import org.mini.g3d.animation.gltf2.render.*;
import org.mini.g3d.animation.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.glwrap.GLUtil;

import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

//Inspiration from shader.js and LWJGL3 book
public class AnimatedShader extends org.mini.g3d.core.ShaderProgram {
    //for drawElementsInstanced
    public static final int MAX_INSTANCED_SIZE = 100;


//    private Map<String, UniformData> uniforms = new HashMap<>();
//    private Map<String, Integer> attributes = new HashMap<>();

//    private List<String> unknownAttributes = new ArrayList<>();
//    private List<String> unknownUniforms = new ArrayList<>();


    int location_u_ViewProjectionMatrix;
    int[] location_u_ModelMatrix;
    int location_u_NormalMatrix;
    int location_u_Exposure;
    int location_u_Camera;
    int[] location_u_jointMatrix;
    int[] location_u_jointNormalMatrix;
    int location_u_jointMatrixTex;//using texture replace  u_jointMatrix
    int location_u_jointMatrixTexWidth;
    int location_u_frameIndex;
    int location_u_morphWeights;
    int[] location_u_Light_direction;
    int[] location_u_Light_range;
    int[] location_u_Light_color;
    int[] location_u_Light_intensity;
    int[] location_u_Light_position;
    int[] location_u_Light_innerConeCos;
    int[] location_u_Light_outerConeCos;
    int[] location_u_Light_type;
    int[] location_u_MaterialProperties;
    int[] location_u_MaterialTextures;
    // 新增贴图拆分相关变量
    int location_u_TextureGridSize;
    int location_u_TextureFrameIndex;
    //
    int[] location_attribute;
    public int location_a_Position;


    public AnimatedShader(int programID) {
        super(programID);
    }

    protected void getAllUniformLocations() {

    }

    public void getAllUniformLocations(RenderNode node, int lightCount) {

        //get location
        start();
        location_u_ViewProjectionMatrix = getUniformLocation("u_ViewProjectionMatrix");
        location_u_ModelMatrix = new int[MAX_INSTANCED_SIZE];
        for (int i = 0; i < MAX_INSTANCED_SIZE; i++) {
            location_u_ModelMatrix[i] = getUniformLocation("u_ModelMatrix[" + i + "]");
        }
        location_u_NormalMatrix = getUniformLocation("u_NormalMatrix");
        location_u_Exposure = getUniformLocation("u_Exposure");
        location_u_Camera = getUniformLocation("u_Camera");
        location_u_morphWeights = getUniformLocation("u_morphWeights");
        // 获取新增的uniform变量位置
        location_u_TextureGridSize = getUniformLocation("u_TextureGridSize");
        location_u_TextureFrameIndex = getUniformLocation("u_TextureFrameIndex");
        //GLUtil.checkGlError("getAllUniformLocations 0 ");
        if (node instanceof RenderMeshPrimitive) {
            RenderMeshPrimitive rmp = (RenderMeshPrimitive) node;

            //此处很重要, 在setIndices 中创建了vao, 并随后在enableAttribute 中创建 vbo
            //否则,如果没有创建 vao,则创建vbo时会报错.
            boolean drawIndexed = rmp.getGltfMeshPrimitive().getIndicesAccessor() != null;
            if (drawIndexed) {
                GLDriver.setIndices(rmp.getGltfMeshPrimitive().getIndicesAccessor());
            }
            //
            int c = 0;
            location_attribute = new int[rmp.getGlAttributes().size()];
            for (Map.Entry<String, GLTFAccessor> entry : rmp.getGlAttributes().entrySet()) {
                String attributeName = entry.getKey();
                GLTFAccessor accessor = entry.getValue();


                int location = glGetAttribLocation(getProgramId(), GLUtil.toCstyleBytes(attributeName));
                location_attribute[c++] = location;
                if (location < 0) {
                    continue;
                }
                GLDriver.enableAttribute(location, accessor);
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
                location_u_jointMatrixTex = getUniformLocation("u_jointMatrixTex");
                location_u_jointMatrixTexWidth = getUniformLocation("u_jointMatrixTexWidth");
                location_u_frameIndex = getUniformLocation("u_frameIndex");
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
        location_u_Light_direction = new int[lightCount];
        location_u_Light_range = new int[lightCount];
        location_u_Light_color = new int[lightCount];
        location_u_Light_intensity = new int[lightCount];
        location_u_Light_position = new int[lightCount];
        location_u_Light_innerConeCos = new int[lightCount];
        location_u_Light_outerConeCos = new int[lightCount];
        location_u_Light_type = new int[lightCount];
        for (int i = 0; i < lightCount; i++) {
            String p = "u_Lights[" + i + "].";
            location_u_Light_direction[i] = getUniformLocation(p + "direction");
            location_u_Light_range[i] = getUniformLocation(p + "range");
            location_u_Light_color[i] = getUniformLocation(p + "color");
            location_u_Light_intensity[i] = getUniformLocation(p + "intensity");
            location_u_Light_position[i] = getUniformLocation(p + "position");
            location_u_Light_innerConeCos[i] = getUniformLocation(p + "innerConeCos");
            location_u_Light_outerConeCos[i] = getUniformLocation(p + "outerConeCos");
            location_u_Light_type[i] = getUniformLocation(p + "type");
        }

        location_a_Position = glGetAttribLocation(getProgramId(), GLUtil.toCstyleBytes("a_Position"));
        stop();
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

    public void loadJointKFTextures(int slot, int jointTex) {
        glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, jointTex);
    }

    public void load_u_ViewProjectionMatrix(Matrix4f pm) {
        super.loadMatrix(location_u_ViewProjectionMatrix, pm);
    }

    public void load_u_ModelMatrix(Matrix4f[] pms) {
        for (int i = 0; i < MAX_INSTANCED_SIZE; i++) {
            super.loadMatrix(location_u_ModelMatrix[i], pms[i]);
        }
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

    public void load_u_jointMatrixTex(int tex) {
        super.loadUniform(location_u_jointMatrixTex, tex);
    }

    public void load_u_jointMatrixTexWidth(int texW) {
        super.loadUniform(location_u_jointMatrixTexWidth, texW);
    }

    public void load_u_frameIndex(int[] frameIndx) {
        super.loadIntArr(location_u_frameIndex, frameIndx);
    }

    public void load_u_TextureGridSize(int gridSize) {
        super.loadInt(location_u_TextureGridSize, gridSize);
    }

    public void load_u_TextureFrameIndex(int[] frameIndices) {
        super.loadIntArr(location_u_TextureFrameIndex, frameIndices);
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

    public void load_u_Lights(UniformLight[] lights, int count) {
        if (lights == null || count <= 0) return;
        int n = count;
        if (location_u_Light_type == null) return;
        if (n > location_u_Light_type.length) n = location_u_Light_type.length;
        if (n > lights.length) n = lights.length;
        for (int i = 0; i < n; i++) {
            UniformLight l = lights[i];
            if (location_u_Light_direction[i] >= 0) loadVector(location_u_Light_direction[i], l.direction);
            if (location_u_Light_range[i] >= 0) loadFloat(location_u_Light_range[i], l.range);
            if (location_u_Light_color[i] >= 0) loadVector(location_u_Light_color[i], l.color);
            if (location_u_Light_intensity[i] >= 0) loadFloat(location_u_Light_intensity[i], l.intensity);
            if (location_u_Light_position[i] >= 0) loadVector(location_u_Light_position[i], l.position);
            if (location_u_Light_innerConeCos[i] >= 0) loadFloat(location_u_Light_innerConeCos[i], l.innerConeCos);
            if (location_u_Light_outerConeCos[i] >= 0) loadFloat(location_u_Light_outerConeCos[i], l.outerConeCos);
            if (location_u_Light_type[i] >= 0) loadInt(location_u_Light_type[i], l.type);
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
            GLDriver.enableAttribute(location, accessor);
        }
        return vertCount;
    }


    public void unbindAttributes() {
        int i = 0;
        for (; i < location_attribute.length; i++) {
            int location = location_attribute[i];
            if (location < 0) {
                continue;
            }
            glDisableVertexAttribArray(location);
            //GLUtil.checkGlError("drawRenderObject 10");
        }
    }
}
