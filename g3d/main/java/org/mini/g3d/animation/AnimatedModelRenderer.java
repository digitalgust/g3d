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
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.skybox.DayAndNight;
import org.mini.gl.GLMath;
import org.mini.glwrap.GLUtil;
import org.mini.util.SysLog;

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
    static int[] textureFrameIndices = new int[AnimatedShader.MAX_INSTANCED_SIZE];

    private static final float EXPOSURE_NIGHT = 0.55f;
    private static final float EXPOSURE_DAY = 1.0f;

    private final UniformLight[] punctualLights = new UniformLight[]{new UniformLight()};
    private final Vector3f cachedSunColor = new Vector3f();



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

    public void render(Scene scene) {
        if (scene == null) return;
        render(scene, scene.getAnimatedModelsIterator());
    }

    public void render(Scene scene, Iterator<? extends AnimatedModel> animatedPlayersIterator) {
        if (scene == null) return;

        ICamera camera = scene.getCamera();
        float exposure = computeExposure(scene);
        updateSunLight(scene);

        for (; animatedPlayersIterator.hasNext(); ) {
            AnimatedModel p = animatedPlayersIterator.next();
            renderer.draw(camera, p.getRootRenderNode(), -1);
        }

        List<RenderMeshPrimitive> batch = G3dUtil.getCachedList();

        for (GLTFSkin gltfSkin : pendingRenders.keySet()) {
            Map<GLTFMeshPrimitive, List<RenderMeshPrimitive>> primitiveListMap = pendingRenders.get(gltfSkin);
            for (GLTFMeshPrimitive gmp : primitiveListMap.keySet()) {
                List<RenderMeshPrimitive> list = primitiveListMap.get(gmp);
                if (list.isEmpty()) continue;
                while (!list.isEmpty()) {
                    int cnt = 0;
                    for (int i = list.size() - 1; i >= 0; i--) {
                        batch.add(list.get(i));
                        list.remove(i);
                        cnt++;
                        if (cnt >= AnimatedShader.MAX_INSTANCED_SIZE) break;
                    }
                    drawRenderObject(camera, batch, exposure, punctualLights);
                    batch.clear();
                }
            }
        }

        G3dUtil.putCachedList(batch);
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
            renderer.draw(camera, p.getRootRenderNode(), -1);
        }

        List<RenderMeshPrimitive> batch = G3dUtil.getCachedList();

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
                    drawRenderObject(camera, batch, 1.0f, null);
                    batch.clear();
                }
            }
        }

        G3dUtil.putCachedList(batch);
    }

    private void updateSunLight(Scene scene) {
        UniformLight ul = punctualLights[0];
        ul.type = 0;
        ul.direction.set(scene.getSun().getDirection());
        computeSunColor(scene, cachedSunColor);
        ul.color.set(cachedSunColor);
        ul.intensity = 1.0f;
        ul.range = -1;
        ul.position.set(0, 0, 0);
        ul.innerConeCos = 0;
        ul.outerConeCos = (float) (Math.PI / 4);
    }

    private static float computeExposure(Scene scene) {
        float t = getDayNightFactor(scene);
        return EXPOSURE_NIGHT + (EXPOSURE_DAY - EXPOSURE_NIGHT) * t;
    }

    private static void computeSunColor(Scene scene, Vector3f out) {
        float t = getDayNightFactor(scene);
        out.x = Scene.SUN_COLOR_NIGHT.x + (Scene.SUN_COLOR_DAY.x - Scene.SUN_COLOR_NIGHT.x) * t;
        out.y = Scene.SUN_COLOR_NIGHT.y + (Scene.SUN_COLOR_DAY.y - Scene.SUN_COLOR_NIGHT.y) * t;
        out.z = Scene.SUN_COLOR_NIGHT.z + (Scene.SUN_COLOR_DAY.z - Scene.SUN_COLOR_NIGHT.z) * t;
    }

    private static float getDayNightFactor(Scene scene) {
        int seg = scene.getDayAndNight().getSegment();
        float p = scene.getDayAndNight().getPercentInSeg();
        if (seg == DayAndNight.NIGHT) return 0f;
        if (seg == DayAndNight.DAY) return 1f;
        if (seg == DayAndNight.NIGHT_TO_DAY) return p;
        if (seg == DayAndNight.DAY_TO_NIGHT) return 1f - p;
        return 1f;
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


    public static void drawRenderObject(ICamera camera, List<RenderMeshPrimitive> rmps, float exposure, UniformLight[] lights) {
        if (rmps.isEmpty()) return;
//        GLUtil.checkGlError("drawRenderObject 0");
        RenderMeshPrimitive rmp = rmps.get(0);
        if (rmp.isSkip()) {
            return;
        }
        RenderMaterial material = rmp.getMaterial();
        AnimatedShader shader = rmp.getShader();
        if (shader == null) {
            //select shader permutation, compile and link program.
            List<String> vertDefines = G3dUtil.getCachedList();
            pushVertParameterDefines(vertDefines, rmp);
            vertDefines.addAll(rmp.getDefines());


            List<String> fragDefines = G3dUtil.getCachedList();
            fragDefines.addAll(vertDefines);//Add all the vert defines, some are needed
            fragDefines.addAll(material.getDefines());
            int lightCount = lights == null ? 0 : lights.length;
            if (lightCount > 0) {
                fragDefines.add("USE_PUNCTUAL 1");
                fragDefines.add("LIGHT_COUNT " + lightCount);
            }
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

//            int vertexHash = ShaderCache.selectShader(rmp.getShaderIdentifier(), vertDefines);
//            GLUtil.checkGlError("drawRenderObject 0.1");
//            int fragmentHash = ShaderCache.selectShader(material.getShaderIdentifier(), fragDefines);
//            GLUtil.checkGlError("drawRenderObject 0.2");
//            shader = ShaderCache.getShaderProgram(vertexHash, fragmentHash);


            shader = ShaderCache.getShaderProgram(rmp.getShaderIdentifier(), vertDefines, material.getShaderIdentifier(), fragDefines);
//            GLUtil.checkGlError("drawRenderObject 0.3");

            shader.getAllUniformLocations(rmp, lights == null ? 0 : lights.length);
//            GLUtil.checkGlError("drawRenderObject 0.4");
            rmp.setShader(shader);
//            GLUtil.checkGlError("drawRenderObject 1");

            // clean up
            G3dUtil.putCachedList(vertDefines);
            G3dUtil.putCachedList(fragDefines);
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
                if (curFK >= 0 && curFK < rmp.getModelMatrics().length) {//有效性检查
                    //用世界变换矩阵 * 模型矩阵当前帧的矩阵，得到最终结果
                    Matrix4f modelMat = rmp.getModelMatrics()[curFK];
                    Matrix4f worldMat = rmp.getAnimatedModel().getTransform();
                    GLMath.mat4x4_mul(modelMatrics[i].mat, worldMat.mat, modelMat.mat);
//
//                Matrix4f normMat = rmp.getModelNormMatrics()[curFK];
//                GLMath.mat4x4_mul(tmp.mat, worldMat.mat, normMat.mat);
//                shader.load_u_NormalMatrix(tmp);
                } else {
                    SysLog.warn("G3D|keyframe index out of range:" + curFK + " / " + rmp.getModelMatrics().length);
                    GLMath.mat4x4_dup(modelMatrics[i].mat, rmp.getWorldTransform().mat);
                }
            } else {
                GLMath.mat4x4_dup(modelMatrics[i].mat, rmp.getWorldTransform().mat);
//                shader.load_u_ModelMatrix(modelMatrics);
//                shader.load_u_NormalMatrix(rmp.getNormalMatrix());
            }
        }
        shader.load_u_ModelMatrix(modelMatrics);

        shader.load_u_Exposure(exposure);
        shader.load_u_Camera(camera.getPosition());
        if (lights != null && lights.length > 0) {
            shader.load_u_Lights(lights, lights.length);
        }

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

        int textureGridSize = rmp.getGltfMeshPrimitive().gltf.getTextureGridSize();
        shader.load_u_TextureGridSize(textureGridSize);
        shader.load_u_TextureFrameIndex(textureFrameIndices);

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
                    
                    textureFrameIndices[i] = mesh.getAnimatedModel().getTextureFrameIndex();
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
