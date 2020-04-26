package org.mini.g3d.animation;

import org.mini.g3d.core.BackendSuported;
import org.mini.g3d.core.gltf2.loader.data.*;
import org.mini.g3d.core.gltf2.render.RenderMesh;
import org.mini.g3d.core.gltf2.render.RenderMeshPrimitive;
import org.mini.g3d.core.gltf2.render.RenderNode;
import org.mini.g3d.core.gltf2.render.RenderAnimation;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.textures.ModelTexture;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.entity.Entity;
import org.mini.nanovg.Gutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimatedModel extends Entity implements Cloneable, BackendSuported {

    Random random = new Random();
    protected Matrix4f transform = new Matrix4f();
    protected Matrix4f transform_backend = new Matrix4f();
    protected List<RenderAnimation> animations = new ArrayList<>();
    protected RenderNode rootRenderNode;
    protected long animationStartTime;
    int animationIndex = 0;

    public int getClipIndex() {
        return clipIndex;
    }

    public void setClipIndex(int clipIndex) {
        this.clipIndex = clipIndex;
    }

    protected int clipIndex;

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

    public static long cost;

    public void update() {
        long start = System.currentTimeMillis();
        animateNode();
        cost += System.currentTimeMillis() - start;
        transform_backend.setZero();
        G3dMath.createTransformationMatrix(position, rotX, rotY, rotZ, scale, transform_backend);
        rootRenderNode.applyTransform(transform_backend);
        rootRenderNode.updateSkin();
    }

    //  private static float debugStep = -0.25f;
    private void animateNode() {
        float animationTimeScale = 1.f;
        float elapsedTime = (System.currentTimeMillis() - animationStartTime) / 1000f * animationTimeScale;

        animations.get(animationIndex).advance(elapsedTime, clipIndex);
    }


    void loadGLTF(GLTF gltf) {


        rootRenderNode = new RenderNode(null, null);

        if (gltf.getExtensionsRequired() != null) {
            throw new RuntimeException("Extensions not supported. Loading next file");
        }

        GLTFScene scene = gltf.getDefaultScene();
        if (scene == null) {
            scene = gltf.getScenes().get(0);
        }

        //Generate RenderNodes for scene
        for (GLTFNode rootNode : scene.getRootNodes()) {
            processNodeChildren(rootNode, rootRenderNode);
        }

        //Generate Animations
        if (gltf.getAnimations() != null) {
            for (GLTFAnimation animation : gltf.getAnimations()) {
                getAnimations().add(new RenderAnimation(animation, rootRenderNode, gltf.getAnimationClip()));
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
//    System.out.println("Scaling scene by " + delta);

        //renderCamera.fitViewToScene(rootRenderNode);

        setAnimationStartTime(System.currentTimeMillis());


        RawModel raw = new RawModel(-1, 0);
        ModelTexture mt = new ModelTexture(-1);
        model = new TexturedModel(raw, mt);
    }

    private void processNodeChildren(GLTFNode node, RenderNode parent) {
        RenderNode renderNode;
        GLTFMesh mesh = node.getMesh();
        if (mesh != null) {
            GLTFMesh gltfMesh = mesh;
            renderNode = new RenderMesh(node, parent);
            for (GLTFMeshPrimitive primitive : gltfMesh.getPrimitives()) {
                System.out.println("Processing GLTFMesh. Name: " + gltfMesh.getName());
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

    @Override
    public void swap() {
        Gutil.mat4x4_dup(transform.mat, transform_backend.mat);
        rootRenderNode.swap();
    }
}
