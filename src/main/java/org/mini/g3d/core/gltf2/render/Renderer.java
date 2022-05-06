/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;


import org.mini.g3d.core.Camera;
import org.mini.g3d.core.gltf2.AnimatedShader;
import org.mini.g3d.core.gltf2.GlUtil;
import org.mini.g3d.core.gltf2.ShaderCache;
import org.mini.g3d.core.gltf2.ShaderDebugType;
import org.mini.g3d.core.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.core.gltf2.loader.data.GLTFAlphaMode;
import org.mini.g3d.core.vector.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static org.mini.gl.GL.*;

public class Renderer {


    private List<RenderLight> visibleLights;

    private Matrix4f projMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f viewProjectionMatrix = new Matrix4f();
    private int nodeDrawLimit = -1;
    private boolean drawInvisibleNodes = false; //Draw all nodes on the scene tree

    private Camera camera;

    private final int[] debugBuf = new int[1];
    private final int[] debugEle = new int[1];

    private ShaderDebugType debugType = ShaderDebugType.NONE;

    //TODO global settings
    private boolean usePunctualLighting = false;
    private boolean useIBL = false;
    public static final boolean generateMipmaps = true;

    public Renderer() {
        visibleLights = new ArrayList<>();

        RenderLight light1 = new RenderLight(null, null);
//    RenderLight light2 = new RenderLight(null, null);

        //TODO set up a second default light
//    UniformLight ul2 = light2.getUniformLight();
//    ul2.position = ul2.position.add(-5, 2, -5);

        visibleLights.add(light1);

        //Setup debug box
        float[] debugBox = {
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f
        };
        glGenBuffers(1, debugBuf, 0);
        glBindBuffer(GL_ARRAY_BUFFER, debugBuf[0]);
        glBufferData(GL_ARRAY_BUFFER, debugBox.length * Float.BYTES, debugBox, 0, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        short[] debugElements = {
                0, 1, 2,
                2, 3, 0,
                1, 5, 6,
                6, 2, 1,
                7, 6, 5,
                5, 4, 7,
                4, 0, 3,
                3, 7, 4,
                4, 5, 1,
                1, 0, 4,
                3, 2, 6,
                6, 7, 3
        };

        glGenBuffers(1, debugEle, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, debugEle[0]);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, debugElements.length * Short.BYTES, debugElements, 0, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

    }

    public void draw(Camera camera, RenderNode rootNode, int targetDrawLimit) {
        //GLUtil.checkGlError("draw 0");
        this.camera = camera;
        //System.out.println("================================");

        nodeDrawLimit = targetDrawLimit;
        List<RenderMeshPrimitive> transparentNodes = new ArrayList<>();
        draw(rootNode, transparentNodes);

        //GLUtil.checkGlError("draw 1");
        //TODO sort by distance
        for (RenderMeshPrimitive renderMeshPrimitive : transparentNodes) {
            drawRenderObject(renderMeshPrimitive);
        }
        //GLUtil.checkGlError("draw 2");
    }

    /**
     * Walk down the RenderNode tree
     *
     * @param node
     */
    private void draw(RenderNode node, List<RenderMeshPrimitive> transparentNodes) {
        if (node instanceof RenderMeshPrimitive) {
            if (nodeDrawLimit != 0) {
                nodeDrawLimit--;
                RenderMeshPrimitive nodeObj = (RenderMeshPrimitive) node;
                if (nodeObj.getMaterial().getAlphaMode() == GLTFAlphaMode.BLEND) {
                    transparentNodes.add(nodeObj);
                } else {
                    //System.out.println(node);
                    drawRenderObject((RenderMeshPrimitive) node);
                }
            }
        } else if (drawInvisibleNodes) {
            if (nodeDrawLimit != 0) {
                nodeDrawLimit--;
                drawInvisibleNode(node);
            }
        }
        for (int i = 0, imax = node.getChildren().size(); i < imax; i++) {
            RenderNode child = node.getChildren().get(i);
            draw(child, transparentNodes);
        }
    }

    private void drawInvisibleNode(RenderNode node) {
        AnimatedShader shader = ShaderCache.getDebugShaderProgram();
        glUseProgram(shader.getProgramId());
        //GLUtil.checkGlError("drawInvisibleNode 1");

        this.projMatrix = camera.getProjectionMatrix();
        this.viewMatrix = camera.getViewMatrix();
        Matrix4f.mul(projMatrix, viewMatrix, viewProjectionMatrix);

        shader.load_u_ViewProjectionMatrix(viewProjectionMatrix);
        shader.load_u_ModelMatrix(node.getWorldTransform());
        shader.load_u_NormalMatrix(node.getNormalMatrix());
        shader.load_u_Exposure(0.1f);
        shader.load_u_Camera(camera.getPosition());

        //GLUtil.checkGlError("drawInvisibleNode 2");
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, debugEle[0]);
        glBindBuffer(GL_ARRAY_BUFFER, debugBuf[0]);

        int positionAttribute = shader.location_a_Position;

        glVertexAttribPointer(positionAttribute, 4, GL_FLOAT, GL_FALSE, 0, null, 0);
        glEnableVertexAttribArray(positionAttribute);

        //GLUtil.checkGlError("drawInvisibleNode 3");
        glDrawElements(GL_TRIANGLES, 32, GL_UNSIGNED_SHORT, null, 0);

        glDisableVertexAttribArray(positionAttribute);

        //GLUtil.checkGlError("drawInvisibleNode 4");
    }

    private void drawRenderObject(RenderMeshPrimitive rmp) {
        //GLUtil.checkGlError("drawRenderObject 0");
        if (rmp.isSkip()) {
            return;
        }
        RenderMaterial material = rmp.getMaterial();
        AnimatedShader shader = rmp.getShader();
        if (shader == null) {
            //select shader permutation, compile and link program.
            List<String> vertDefines = new ArrayList<>();
            pushVertParameterDefines(vertDefines, rmp);
            vertDefines.addAll(rmp.getDefines());


            List<String> fragDefines = new ArrayList<>();
            fragDefines.addAll(vertDefines);//Add all the vert defines, some are needed
            fragDefines.addAll(material.getDefines());
            if (usePunctualLighting) {
                fragDefines.add("USE_PUNCTUAL 1");
                fragDefines.add("LIGHT_COUNT " + visibleLights.size());
            }
            if (useIBL) {
                fragDefines.add("USE_IBL 1");
                fragDefines.add("USE_TEX_LOD 1");
                fragDefines.add("USE_HDR 1");
            }

            //DEBUG
            if (debugType != ShaderDebugType.NONE) {
                fragDefines.add("DEBUG_OUTPUT 1");
                fragDefines.add(debugType.getDefine());
            }

            int vertexHash = ShaderCache.selectShader(rmp.getShaderIdentifier(), vertDefines);
            //GLUtil.checkGlError("drawRenderObject 0.1");
            int fragmentHash = ShaderCache.selectShader(material.getShaderIdentifier(), fragDefines);
            //GLUtil.checkGlError("drawRenderObject 0.2");

            shader = ShaderCache.getShaderProgram(vertexHash, fragmentHash);
            shader.getAllUniformLocations(rmp, visibleLights.size());
            rmp.setShader(shader);
            //GLUtil.checkGlError("drawRenderObject 1");
        }
        shader.start();

        if (usePunctualLighting) {
            shader.load_u_Lights(visibleLights);
        }

        //GLUtil.checkGlError("drawRenderObject 1.3");

        this.projMatrix = camera.getProjectionMatrix();
        this.viewMatrix = camera.getViewMatrix();
        Matrix4f.mul(projMatrix, viewMatrix, viewProjectionMatrix);

        //Assert viewProjectionMatrix is filled out
        assert (!viewProjectionMatrix.toString().contains("nan"));

        shader.load_u_ViewProjectionMatrix(viewProjectionMatrix);
        shader.load_u_ModelMatrix(rmp.getWorldTransform());
//        System.out.println(rmp + "," + rmp.getWorldTransform());
        shader.load_u_NormalMatrix(rmp.getNormalMatrix());
        shader.load_u_Exposure(1.0f);
        shader.load_u_Camera(camera.getPosition());

        //GLUtil.checkGlError("drawRenderObject 1.4");

        boolean drawIndexed = rmp.getPrimitive().getIndicesAccessor() != null;

        if (drawIndexed) {
            GlUtil.setIndices(rmp.getPrimitive().getIndicesAccessor());
        }
        //GLUtil.checkGlError("drawRenderObject 1.5");

        updateAnimationUniforms(shader, rmp.getMesh(), rmp);
        //GLUtil.checkGlError("drawRenderObject 2");

        if (material.getGLTFMaterial().isDoubleSided()) {
            glDisable(GL_CULL_FACE);
        } else {
            glEnable(GL_CULL_FACE);
        }

        if (material.getGLTFMaterial().getAlphaMode() == GLTFAlphaMode.BLEND) {
            glEnable(GL_BLEND);
            glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
            glBlendEquation(GL_FUNC_ADD);
        } else {
            glDisable(GL_BLEND);
        }
        //GLUtil.checkGlError("drawRenderObject 3");

        int vertexCount = shader.bindAttributes(rmp.getGlAttributes());

        //GLUtil.checkGlError("drawRenderObject 4");

        shader.load_materialProperties(material);
        shader.load_materialTextures(material);

        //GLUtil.checkGlError("drawRenderObject 6");

        if (drawIndexed) {
            GLTFAccessor indexAccessor = rmp.getPrimitive().getIndicesAccessor();
            glDrawElements(rmp.getPrimitive().getMode(), indexAccessor.getCount(), indexAccessor.getGLType(), null, 0);
        } else {
            glDrawArrays(rmp.getPrimitive().getMode(), 0, vertexCount);
        }
        //GLUtil.checkGlError("drawRenderObject 7 " + this + " " + drawIndexed);

        shader.unbindAttributes();
        shader.stop();
        //GLUtil.checkGlError("drawRenderObject 10");

    }

    private void updateAnimationUniforms(AnimatedShader shader, RenderMesh mesh,
                                         RenderMeshPrimitive renderMeshPrimitive) {

        //GLUtil.checkGlError("updateAnimationUniforms 1");
        // Skinning
        if (mesh.getSkin() != null) {
            RenderSkin skin = mesh.getSkin();
            shader.load_u_jointMatrices(skin.getJointMatrices());
            shader.load_u_jointNormalMatrices(skin.getJointNormalMatrices());
        }
        if (renderMeshPrimitive.getPrimitive().getMorphTargets() != null
                && renderMeshPrimitive.getPrimitive().getMorphTargets().size() > 0) {
            if (mesh.getWeights() != null && mesh.getWeights().length > 0) {
                shader.load_u_morphWeights(mesh.getWeights());
            }
        }
    }

    private void pushVertParameterDefines(List<String> vertDefines,
                                          RenderMeshPrimitive renderMeshPrimitive) {
        //Skinning
        if (renderMeshPrimitive.getMesh().getSkin() != null) {
            RenderSkin skin = renderMeshPrimitive.getMesh().getSkin();
            vertDefines.add("USE_SKINNING 1");
            vertDefines.add("JOINT_COUNT " + skin.getJointCount());
        }

        //Morphing
        if (renderMeshPrimitive.getPrimitive().getMorphTargets() != null
                && renderMeshPrimitive.getPrimitive().getMorphTargets().size() > 0) {
            RenderMesh mesh = renderMeshPrimitive.getMesh();
            if (mesh.getWeights() != null && mesh.getWeights().length > 0) {
                vertDefines.add("USE_MORPHING 1");
                vertDefines.add("WEIGHT_COUNT " + Math.min(mesh.getWeights().length, 8));
            }
        }
    }


    public ShaderDebugType getDebugType() {
        return debugType;
    }

    public void setDebugType(ShaderDebugType debugType) {
        this.debugType = debugType;
    }
}
