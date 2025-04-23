package org.mini.g3d.animation;

import org.mini.g3d.animation.gltf2.loader.data.*;
import org.mini.g3d.animation.gltf2.render.RenderAnimation;
import org.mini.g3d.animation.gltf2.render.RenderMesh;
import org.mini.g3d.animation.gltf2.render.RenderMeshPrimitive;
import org.mini.g3d.animation.gltf2.render.RenderNode;
import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.entity.Entity;
import org.mini.util.SysLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于GLTF的动画模型
 * 需要使用 facebook FBX2glTF 或者 blender 进行转换, 此工具转换后执行效率高,保持层次架构不变,名称不变, 因此可以换装
 * https://github.com/facebookincubator/FBX2glTF
 * <p>
 *
 * <p>
 * Please using FBX2glTF tool translate fbx to gltf, but not blender
 */
public class AnimatedModel extends Entity implements Cloneable {

    GLTF gltf;
    protected Entity shadowNode;
    protected Matrix4f transform = new Matrix4f();
    AniGroup aniGroup;
    protected List<RenderAnimation> animations = new ArrayList<>();
    protected RenderNode rootRenderNode;
    protected long animationStartTime = DisplayManager.getCurrentTime();//必须设置 ,否则会初始动作不播放
    protected int clipIndex;
    int animationPlayTimes = 0;
    Runnable onPlayEndCallback;
    float animationTimeScale = 1.f;
    int curKeyFrame;

    public Entity getShadowNode() {
        return shadowNode;
    }

    public int getAniClipIndex() {
        return clipIndex;
    }

    public void setAniClipIndex(int clipIndex) {
        if (this.clipIndex != clipIndex) {
            setAnimationStartTime(DisplayManager.getCurrentTime());
        }
        this.clipIndex = clipIndex;
        animationPlayTimes = 0;
        onPlayEndCallback = null;
    }

    /**
     * 播放 clipIndex动作 times次 之后换为 afterClipIndex动作
     *
     * @param clipIndex
     * @param times
     */
    public void setAniClipIndex(int clipIndex, int times, Runnable onPlayEndCallback) {
        if (this.clipIndex != clipIndex) {
            setAnimationStartTime(DisplayManager.getCurrentTime());
        }
        //执行老的回调
        if (onPlayEndCallback != null) {
            onPlayEndCallback.run();
        }

        this.clipIndex = clipIndex;
        animationPlayTimes = times;
        this.onPlayEndCallback = onPlayEndCallback;
    }


    public AnimatedModel(GLTF gltf) {
        loadGLTF(gltf);
    }

    public List<RenderAnimation> getAnimations() {
        return animations;
    }


    public void setRootRenderNode(RenderNode rootRenderNode) {
        this.rootRenderNode = rootRenderNode;
        //transform.translate(new Vector3f(random.nextFloat() * 5f, 0, random.nextFloat() * 5f));
    }


    public long getAnimationStartTime() {
        return animationStartTime;
    }

    public void setAnimationStartTime(long animationStartTime) {
        this.animationStartTime = animationStartTime;
    }


    public RenderNode getRootRenderNode() {
        return rootRenderNode;
    }

    //    public void update() {
//        long start = System.currentTimeMillis();
//        animateNode();
//        transform_backend.setZero();
//        G3dMath.createTransformationMatrix(position, rotX, rotY, rotZ, scale, transform_backend);
//        //transform_backend.identity();
//        rootRenderNode.applyTransform(transform_backend);
//        rootRenderNode.updateSkin();
//    }
    public void update() {
        long curMill = DisplayManager.getCurrentTime();
        transform.setZero();
        G3dUtil.createTransformationMatrix(position, rotX, rotY, rotZ, scale, transform);

        float elapsedTime = (curMill - animationStartTime) / 1000f * animationTimeScale;
        if (aniGroup != null) {
            AniClip clip = aniGroup.getAniClips().get(clipIndex);
            elapsedTime = (elapsedTime % (clip.endAt - clip.beginAt));
            int curKF = clip.begin + (int) (elapsedTime / aniGroup.getFpt());
            if (curKF < clip.begin) {
                curKF = clip.begin;
            }
            if (curKF >= aniGroup.getKeyFrameMax()) {
                curKF = clip.begin;
            }
            curKeyFrame = curKF;

            //注释掉以前每帧计算模型变换矩阵的代码
//            animateNode(elapsedTime);
//            rootRenderNode.applyTransform(transform_backend);
//            rootRenderNode.updateSkin();
        } else {
            animateNode(elapsedTime);
            rootRenderNode.applyTransform(transform);
            rootRenderNode.updateSkin();
        }
        checkNextAni(elapsedTime);
    }

    //  private static float debugStep = -0.25f;
//    private void animateNode() {
//        float animationTimeScale = 1.f;
//        float elapsedTime = (System.currentTimeMillis() - animationStartTime) / 1000f * animationTimeScale;
//        if (!animations.isEmpty()) {
//            animations.get(0).advance(elapsedTime, clipIndex);
//        }
//
//        //如果播放次数限制
//        if (animationPlayTimes != 0) {
//            AniClip ac = aniGroup.getAniClips().get(clipIndex);
//            //这里加一个0.1f 以消除误差,否则会导致播放下一轮的首帧,比如死亡只放一遍倒下的动作
//            // ,但是由于异步更新线程的原因,会重新站起来一下
//            if (elapsedTime + 0.1f >= (ac.endAt - ac.beginAt) * animationPlayTimes) {
//                if (onPlayEndCallback != null) {
//                    Runnable callback = this.onPlayEndCallback;
//                    this.onPlayEndCallback = null;
//                    animationPlayTimes = 0;
//                    try {
//                        callback.run();
//                    } catch (Exception e) {
//                    }
//                }
//            }
//        }
//
//    }

    public void animateNode(float elapsedTime) {
        if (!animations.isEmpty()) {
            animations.get(0).advance(elapsedTime, clipIndex);
        }
    }

    void checkNextAni(float elapsedTime) {
        if (onPlayEndCallback != null) {
            //如果播放次数限制
            if (animationPlayTimes != 0) {
                AniClip ac = aniGroup.getAniClips().get(clipIndex);
                //这里加一个0.1f 以消除误差,否则会导致播放下一轮的首帧,比如死亡只放一遍倒下的动作
                // ,但是由于异步更新线程的原因,会重新站起来一下
                if (elapsedTime + 0.1f >= (ac.endAt - ac.beginAt) * animationPlayTimes) {
                    Runnable callback = this.onPlayEndCallback;
                    this.onPlayEndCallback = null;
                    animationPlayTimes = 0;
                    try {
                        callback.run();
                    } catch (Exception e) {
                    }
                }
            }
        }
    }


    public GLTF getGltf() {
        return gltf;
    }

    void loadGLTF(GLTF gltf) {
        this.gltf = gltf;

        rootRenderNode = genRenderNode(gltf);

        aniGroup = gltf.getAniGroup();
        //Generate Animations
        if (gltf.getAnimations() != null) {
            for (int i = 0; i < gltf.getAnimations().size(); i++) {
                GLTFAnimation animation = gltf.getAnimations().get(i);
                animations.add(new RenderAnimation(animation, rootRenderNode, gltf.getAniGroup()));
            }
        }

        Matrix4f sceneScale = new Matrix4f();
        rootRenderNode.applyTransform(sceneScale);

//    AABBf sceneExtends = new AABBf();
//    renderCamera.getSceneExtends(rootRenderNode, sceneExtends);
//    float minValue = Math.min(sceneExtends.minX, Math.min(sceneExtends.minY, sceneExtends.minZ));
//    float maxValue = Math.max(sceneExtends.maxX, Math.max(sceneExtends.maxY, sceneExtends.maxZ));
//    float delta = 1 / (maxValue - minValue);
//    sceneScale.scale(delta);
//    rootRenderNode.applyTransform(sceneScale);
//    SysLog.info("G3D|Scaling scene by " + delta);

        //renderCamera.fitViewToScene(rootRenderNode);


        RawModel raw = new RawModel(-1, 0);
        Texture mt = new Texture(-1);
        model = new TexturedModel(raw, mt);
    }

    private void processNodeChildren(GLTFNode node, RenderNode parent) {
        RenderNode renderNode;
        GLTFMesh mesh = node.getMesh();
        if (mesh != null) {
            GLTFMesh gltfMesh = mesh;
            renderNode = new RenderMesh(node, parent);
            for (int i = 0; i < gltfMesh.getPrimitives().size(); i++) {
                GLTFMeshPrimitive primitive = gltfMesh.getPrimitives().get(i);
                //SysLog.info("G3D|Processing GLTFMesh. Name: " + gltfMesh.getName());
                //Each primitive gets its own render object.
                new RenderMeshPrimitive(primitive, null, (RenderMesh) renderNode);
            }
        } else {
            renderNode = new RenderNode(node, parent);
        }
        GLTFCamera camera = node.getCamera();
        //if (camera != null) renderCamera.setGLTFCamera(camera);

        if (node.getChildren() != null)
            for (GLTFNode childNode : node.getChildren()) {
                processNodeChildren(childNode, renderNode);
            }
    }

    private RenderNode genRenderNode(GLTF gltf) {
        RenderNode renderNode = new RenderNode(this);
        if (gltf.getExtensionsRequired() != null) {
            throw new RuntimeException("Extensions not supported. Loading next file");
        }

        GLTFScene scene = gltf.getDefaultScene();
        if (scene == null) {
            scene = gltf.getScenes().get(0);
        }

        //Generate RenderNodes for scene
        for (GLTFNode rootNode : scene.getRootNodes()) {
            processNodeChildren(rootNode, renderNode);
        }
        return renderNode;
    }


    /**
     * 用gltf的mesh替换原模型组中名为slotName的mesh
     * 用于换装
     * slotName 可以是正则表达式, 比如:  WuQi_.*
     * <p>
     * 条件: 原模型中需要有一个动画控制的装备mesh, 替换装备mesh也必须是带动作的模型,但不需要做具体动作,只需要有动作结点
     * 原装备和替换装备结构: RenderMesh->RenderPrimitive+RenderSkin
     * 实现方法: 用替换装备mesh替换掉原模型中的装备mesh,原RenderMesh上有一个substitue字段, 用来存放要替换的装备mesh,
     * 在渲染阶段, 用原装备的substitue不为空时,则不渲染原模型 , 只渲染替换模型 , 并且在渲染shader中, 使用原模型的变换矩阵
     *
     * @param slotName
     * @param gltf
     */
    public void setSubstitute(String slotName, GLTF gltf) {
        RenderNode source = findRenderNode(slotName, rootRenderNode);
        if (source != null) {
            RenderNode renderNode = genRenderNode(gltf);
            RenderNode target = findRenderNode(slotName, renderNode);
            if (!source.validSubstitute(target)) {
                source.clearSubstitute();
                SysLog.error("G3D|Substitute fail :" + slotName);
            }
        }
    }


    /**
     * 找到命名为 slotname的结点
     *
     * @param slotName
     * @param renderNode
     * @return
     */
    static private RenderNode findRenderNode(String slotName, RenderNode renderNode) {
        GLTFNode gltfNode = renderNode.getGltfNode();
        if (gltfNode != null && gltfNode.getName() != null) {
            if (gltfNode.getName().matches(slotName)) {
                return renderNode;
            }
            //SysLog.info("G3D|QUERY :" + gltfNode.getName());
        }
        for (int i = 0, imax = renderNode.getChildren().size(); i < imax; i++) {
            RenderNode son = renderNode.getChildren().get(i);
            RenderNode result = findRenderNode(slotName, son);
            if (result != null) return result;
        }
        return null;
    }


    public float getAnimationTimeScale() {
        return animationTimeScale;
    }

    public void setAnimationTimeScale(float animationTimeScale) {
        this.animationTimeScale = animationTimeScale;
    }

    public int getCurKeyFrame() {
        return curKeyFrame;
    }

    public void setCurKeyFrame(int curKeyFrame) {
        this.curKeyFrame = curKeyFrame;
    }

    public Matrix4f getTransform() {
        return transform;
    }

}
