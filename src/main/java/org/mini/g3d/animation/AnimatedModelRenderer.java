package org.mini.g3d.animation;

import org.mini.g3d.animation.gltf2.AnimatedShader;
import org.mini.g3d.animation.gltf2.GLDriver;
import org.mini.g3d.animation.gltf2.ShaderCache;
import org.mini.g3d.animation.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.animation.gltf2.loader.data.GLTFAlphaMode;
import org.mini.g3d.animation.gltf2.loader.data.GLTFMeshPrimitive;
import org.mini.g3d.animation.gltf2.loader.data.GLTFSkin;
import org.mini.g3d.animation.gltf2.render.*;
import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.gl.GLMath;
import org.mini.glwrap.GLUtil;

import java.util.*;

import static org.mini.gl.GL.*;


/**
 * Handles the rendering of a animated model/entity
 * The pose that the animated model will be rendered in
 * is determined by the joint transforms
 *
 * @author Glenn Arne Christensen
 */
public class AnimatedModelRenderer extends AbstractRenderer {


    private GLTFRenderer renderer;

    static Matrix4f[] modelMatrics = new Matrix4f[AnimatedShader.MAX_INSTANCED_SIZE];
    static int[] keyFrames = new int[AnimatedShader.MAX_INSTANCED_SIZE];


    // 合批 RenderMeshPrimitive 时，
    // 需要他的 GLTFSkin 和 GLTFMeshPrimitive 都相同才可以合
    // 比如角色替换武器过后，被替换的武器RenderMeshPrimitive，他使用角色的Skin ，但使用自己的 GLTFMeshPrimitive
    static Map<GLTFSkin, Map<GLTFMeshPrimitive, List<RenderMeshPrimitive>>> pendingRenders = new HashMap();


    private static Matrix4f viewProjectionMatrix = new Matrix4f();

    /**
     * Initializes the shader program used for rendering animated models.
     */
    public AnimatedModelRenderer() {
        renderer = new GLTFRenderer();
        for (int i = 0; i < 100; i++) {
            modelMatrics[i] = new Matrix4f();
        }
    }


    /**
     * Renders the animated entity. Works the same as rendering a entity,
     * but notice with a animated model we have to enable five attributes
     * of the VAO before we render the animated entity. This is because
     * we need to have the joints and weights
     */
    public void render(ICamera camera, Iterator<? extends AnimatedModel> animatedPlayersIterator) {
        for (; animatedPlayersIterator.hasNext(); ) {
            AnimatedModel p = animatedPlayersIterator.next();
//        animatedModelShader.start();
//        animatedModelShader.loadSkyColor(FOG_RED, FOG_GREEN, FOG_BLUE);
//        animatedModelShader.loadLights(lights);
//        animatedModelShader.loadViewMatrix(camera);
            renderer.draw(camera, p.getRootRenderNode(), -1);
//        animatedModelShader.stop();
        }

        List<RenderMeshPrimitive> batch = new ArrayList<>();

        for (GLTFSkin gltfSkin : pendingRenders.keySet()) {
            Map<GLTFMeshPrimitive, List<RenderMeshPrimitive>> primitiveListMap = pendingRenders.get(gltfSkin);
            for (GLTFMeshPrimitive gmp : primitiveListMap.keySet()) {
                List<RenderMeshPrimitive> list = primitiveListMap.get(gmp);
                if (list.isEmpty()) continue;
//                for (int i = 0, imax = list.size(); i < imax; i++) {
//                    RenderMeshPrimitive rmp = list.get(i);
//                    drawRenderObject(camera, rmp);
//                }
                while (!list.isEmpty()) {
                    int cnt = 0;
                    for (int i = list.size() - 1; i >= 0; i--) {
                        batch.add(list.get(i));
                        list.remove(i);
                        cnt++;
                        if (cnt >= AnimatedShader.MAX_INSTANCED_SIZE) break;
                    }
                    drawRenderObject(camera, batch);
                    batch.clear();
                }
            }
        }
    }


    public static void putPendingRmp(RenderMeshPrimitive rmp) {
        RenderSkin renderSkin = rmp.getMesh().getSkin();
        GLTFSkin gltfSkin = renderSkin == null ? null : renderSkin.getGltfSkin();
        Map<GLTFMeshPrimitive, List<RenderMeshPrimitive>> primitiveListMap = pendingRenders.get(gltfSkin);
        if (primitiveListMap == null) {
            primitiveListMap = new HashMap<>();
            pendingRenders.put(gltfSkin, primitiveListMap);
        }

        GLTFMeshPrimitive gmp = rmp.getGltfMeshPrimitive();
        List<RenderMeshPrimitive> list = primitiveListMap.get(gmp);
        if (list == null) {
            list = new ArrayList<>();
            primitiveListMap.put(gmp, list);
        }
        list.add(rmp);
    }


    public static void drawRenderObject(ICamera camera, List<RenderMeshPrimitive> rmps) {
        if (rmps.isEmpty()) return;
        GLUtil.checkGlError("drawRenderObject 0");
        RenderMeshPrimitive rmp = rmps.get(0);
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
//            if (usePunctualLighting) {
//                fragDefines.add("USE_PUNCTUAL 1");
//                fragDefines.add("LIGHT_COUNT " + visibleLights.size());
//            }
//            if (useIBL) {
//                fragDefines.add("USE_IBL 1");
//                fragDefines.add("USE_TEX_LOD 1");
//                fragDefines.add("USE_HDR 1");
//            }

            //DEBUG
//            if (debugType != ShaderDebugType.NONE) {
//                fragDefines.add("DEBUG_OUTPUT 1");
//                fragDefines.add(debugType.getDefine());
//            }

            int vertexHash = ShaderCache.selectShader(rmp.getShaderIdentifier(), vertDefines);
//            GLUtil.checkGlError("drawRenderObject 0.1");
            int fragmentHash = ShaderCache.selectShader(material.getShaderIdentifier(), fragDefines);
//            GLUtil.checkGlError("drawRenderObject 0.2");

            shader = ShaderCache.getShaderProgram(vertexHash, fragmentHash);
//            GLUtil.checkGlError("drawRenderObject 0.3");

            shader.getAllUniformLocations(rmp, 0);
//            GLUtil.checkGlError("drawRenderObject 0.4");
            rmp.setShader(shader);
//            GLUtil.checkGlError("drawRenderObject 1");
        }
        shader.start();

//        if (usePunctualLighting) {
//            shader.load_u_Lights(visibleLights);
//        }

//        GLUtil.checkGlError("drawRenderObject 1.3");

        Matrix4f projMatrix = camera.getProjectionMatrix();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f.mul(projMatrix, viewMatrix, viewProjectionMatrix);

        //Assert viewProjectionMatrix is filled out
        //assert (!viewProjectionMatrix.toString().contains("nan"));

        shader.load_u_ViewProjectionMatrix(viewProjectionMatrix);

        for (int i = 0; i < rmps.size(); i++) {
            rmp = rmps.get(i);
            if (rmp.getModelMatrics() != null) {
                int curFK = rmp.getAnimatedModel().getCurKeyFrame();

                //用世界变换矩阵 * 模型矩阵当前帧的矩阵，得到最终结果
                Matrix4f modelMat = rmp.getModelMatrics()[curFK];
                Matrix4f worldMat = rmp.getAnimatedModel().getTransform();
                GLMath.mat4x4_mul(modelMatrics[i].mat, worldMat.mat, modelMat.mat);
//
//                Matrix4f normMat = rmp.getModelNormMatrics()[curFK];
//                GLMath.mat4x4_mul(tmp.mat, worldMat.mat, normMat.mat);
//                shader.load_u_NormalMatrix(tmp);

            } else {
                GLMath.mat4x4_dup(modelMatrics[i].mat, rmp.getWorldTransform().mat);
//                shader.load_u_ModelMatrix(modelMatrics);
//                shader.load_u_NormalMatrix(rmp.getNormalMatrix());
            }
        }
        shader.load_u_ModelMatrix(modelMatrics);

        shader.load_u_Exposure(1.0f);
        shader.load_u_Camera(camera.getPosition());

//        GLUtil.checkGlError("drawRenderObject 1.4");

        boolean drawIndexed = rmp.getGltfMeshPrimitive().getIndicesAccessor() != null;

        if (drawIndexed) {
            GLDriver.setIndices(rmp.getGltfMeshPrimitive().getIndicesAccessor());
        }
//        GLUtil.checkGlError("drawRenderObject 1.5");

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
//        GLUtil.checkGlError("drawRenderObject 3");

        int vertexCount = shader.bindAttributes(rmp.getGlAttributes());
//        GLUtil.checkGlError("drawRenderObject 4");

        shader.load_materialProperties(material);
        shader.load_materialTextures(material);
//        GLUtil.checkGlError("drawRenderObject 6");

        updateAnimationUniforms(shader, rmps);
//        GLUtil.checkGlError("drawRenderObject 6.5");

        if (drawIndexed) {
            GLTFAccessor indexAccessor = rmp.getGltfMeshPrimitive().getIndicesAccessor();
            glDrawElementsInstanced(rmp.getGltfMeshPrimitive().getMode(), indexAccessor.getCount(), indexAccessor.getGLType(), null, 0, rmps.size());
        } else {
            glDrawArrays(rmp.getGltfMeshPrimitive().getMode(), 0, vertexCount);
        }
//        GLUtil.checkGlError("drawRenderObject 7 " + drawIndexed);

        shader.unbindAttributes();
//        GLUtil.checkGlError("drawRenderObject 8 " + drawIndexed);
        shader.stop();
//        GLUtil.checkGlError("drawRenderObject 10");

    }

    private static void updateAnimationUniforms(AnimatedShader shader,
                                                List<RenderMeshPrimitive> rmps) {

//        GLUtil.checkGlError("updateAnimationUniforms 1");
        // Skinning
        RenderMeshPrimitive rmp = rmps.get(0);
        RenderMesh mesh = rmp.getMesh();
        RenderSkin skin = mesh.getSkin();
        if (skin != null) {
            GLTFSkin gltfSkin = skin.getGltfSkin();
            if (gltfSkin.getJointKFTex() != -1) {

                int slot = rmp.getMaterial().getTexturesMap().size() + 1;
                shader.loadJointKFTextures(slot, gltfSkin.getJointKFTex());
//                GLUtil.checkGlError("updateAnimationUniforms 2");
                shader.load_u_jointMatrixTex(slot);
//                GLUtil.checkGlError("updateAnimationUniforms 3");
                shader.load_u_jointMatrixTexWidth(gltfSkin.getJointKFTexWidth());
//                GLUtil.checkGlError("updateAnimationUniforms 4");

                for (int i = 0; i < rmps.size(); i++) {
                    rmp = rmps.get(i);
                    mesh = rmp.getMesh();
                    int curKF = mesh.getAnimatedModel().getCurKeyFrame();
                    keyFrames[i] = curKF;
//                    shader.load_u_jointMatrices(gltfSkin.getJointKeyFrameMatrics()[curKF]);
//                    shader.load_u_jointNormalMatrices(gltfSkin.getJointKeyFrameNormMatrics()[curKF]);
                    // GLUtil.checkGlError("updateAnimationUniforms 5");
                }
//                    GLUtil.checkGlError("updateAnimationUniforms 6");
                shader.load_u_frameIndex(keyFrames);
            } else {
                shader.load_u_jointMatrices(skin.getJointMatrices());
                shader.load_u_jointNormalMatrices(skin.getJointNormalMatrices());
            }
        }
        if (rmp.getGltfMeshPrimitive().getMorphTargets() != null
                && rmp.getGltfMeshPrimitive().getMorphTargets().size() > 0) {
            if (mesh.getWeights() != null && mesh.getWeights().length > 0) {
                shader.load_u_morphWeights(mesh.getWeights());
            }
        }
    }

    private static void pushVertParameterDefines(List<String> vertDefines,
                                                 RenderMeshPrimitive renderMeshPrimitive) {
        //Skinning
        if (renderMeshPrimitive.getMesh().getSkin() != null) {
            RenderSkin skin = renderMeshPrimitive.getMesh().getSkin();
            vertDefines.add("USE_SKINNING 1");
            vertDefines.add("JOINT_COUNT " + skin.getJointCount());
        }

        //Morphing
        if (renderMeshPrimitive.getGltfMeshPrimitive().getMorphTargets() != null
                && renderMeshPrimitive.getGltfMeshPrimitive().getMorphTargets().size() > 0) {
            RenderMesh mesh = renderMeshPrimitive.getMesh();
            if (mesh.getWeights() != null && mesh.getWeights().length > 0) {
                vertDefines.add("USE_MORPHING 1");
                vertDefines.add("WEIGHT_COUNT " + Math.min(mesh.getWeights().length, 8));
            }
        }
    }

}
