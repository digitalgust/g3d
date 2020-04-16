package org.mini.g3d.animation;

import org.mini.g3d.core.gltf2.loader.GLTFImporter;
import org.mini.g3d.core.gltf2.loader.data.*;
import org.mini.g3d.core.gltf2.render.RenderMesh;
import org.mini.g3d.core.gltf2.render.RenderMeshPrimitive;
import org.mini.g3d.core.gltf2.render.RenderNode;
import org.mini.g3d.core.gltf2.render.animation.RenderAnimation;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.textures.ModelTexture;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.entity.Entity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mini.gl.GL.glGenVertexArrays;

public class AnimatedModel extends Entity {

    Random random = new Random();
    Matrix4f mat = new Matrix4f();

    public AnimatedModel() {

    }

    public List<RenderAnimation> getAnimations() {
        return animations;
    }

    private List<RenderAnimation> animations = new ArrayList<>();

    public void setRootRenderNode(RenderNode rootRenderNode) {
        this.rootRenderNode = rootRenderNode;
        //mat.translate(new Vector3f(random.nextFloat() * 5f, 0, random.nextFloat() * 5f));
    }

    private RenderNode rootRenderNode = new RenderNode(null, null);

    public long getAnimationStartTime() {
        return animationStartTime;
    }

    public void setAnimationStartTime(long animationStartTime) {
        this.animationStartTime = animationStartTime;
    }

    private long animationStartTime;


    public RenderNode getRootRenderNode() {
        return rootRenderNode;
    }


    public void update() {
        animateNode();
        mat.setZero();
        G3dMath.createTransformationMatrix(getPosition(), getRotX(), getRotY(), getRotZ(), getScale(), mat);
        rootRenderNode.applyTransform(mat);
        rootRenderNode.updateSkin();
    }

    //  private static float debugStep = -0.25f;
    private void animateNode() {
        float animationTimeScale = 1.f;
        float elapsedTime =
                (System.currentTimeMillis() - animationStartTime) / 1000f * animationTimeScale;
//    debugStep += 0.25f;
//    float elapsedTime = debugStep;

        //TODO selecting animation

        for (RenderAnimation anim : animations) {
            anim.advance(elapsedTime);
        }
    }


    public void loadFile(String path) {

        File file = new File(path);
        GLTFImporter gltfImporter = new GLTFImporter();
        //Clear before loading

        RenderNode rootRenderNode = new RenderNode(null, null);
        setRootRenderNode(rootRenderNode);

        GLTF gltf;
        gltf = gltfImporter.load(file.getPath());
        if (gltf == null) {
            throw new RuntimeException();
        }

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
                getAnimations().add(new RenderAnimation(animation));
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

        int[] vao = {0};
        glGenVertexArrays(1, vao, 0);
//        glBindVertexArray(vao[0]);

        RawModel raw = new RawModel(vao[0], 0);
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
}
