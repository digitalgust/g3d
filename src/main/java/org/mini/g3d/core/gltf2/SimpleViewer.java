/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2;


import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.core.gltf2.loader.GLTFImporter;
import org.mini.g3d.core.gltf2.loader.data.*;
import org.mini.g3d.core.gltf2.render.*;
import org.mini.g3d.core.gltf2.render.animation.RenderAnimation;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.glfw.Glfw;
import test.GlfwCallbackAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.mini.gl.GL.*;
import static org.mini.glfw.Glfw.*;

//Initial configuration from https://www.lwjgl.org/guide
public class SimpleViewer {

    public static int WIDTH = 800;
    public static int HEIGHT = 600;
    Glfw glfw = new Glfw();


    GlfwCallbackAdapter callback = new GlfwCallbackAdapter() {

        @Override
        public void error(int error, String description) {
            System.out.println(description);
        }

        @Override
        public void key(long window, int key, int scancode, int action, int mods) {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, Glfw.GLFW_TRUE); // We will detect this in the rendering loop
            }
            if (key == GLFW_KEY_SPACE && action == GLFW_RELEASE) {
                SimpleViewer.this.loadNextFile();
            }
            if (key == GLFW_KEY_W && action == GLFW_RELEASE) {
                if (wireframeMode) {
                    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                } else {
                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                }
                wireframeMode = !wireframeMode;
            }
            if (key == GLFW_KEY_O && action == GLFW_RELEASE) {
                System.out.println("Toggle limited render");
                limitedRender = !limitedRender;
                limitedRenderIndex = 0;
            }
            if (key == GLFW_KEY_P && action == GLFW_RELEASE) {
                //Normal p increases index
                System.out.println("Increasing limited render index");
                limitedRenderIndex++;
            }
            if (key == GLFW_KEY_D && action == GLFW_RELEASE) {
                ShaderDebugType dType = renderer.getDebugType();
                int next = (dType.ordinal() + 1) % ShaderDebugType.values().length;
                renderer.setDebugType(ShaderDebugType.values()[next]);
                System.out.println("Render debug type: " + renderer.getDebugType().name());
            }
        }

        @Override
        public void scroll(long window, double scrollX, double scrollY) {
            renderCamera.zoom((float) scrollY);
        }

        @Override
        public void mouseButton(long window, int button, boolean pressed) {
            if (button == GLFW_MOUSE_BUTTON_1) {
                if (pressed) {
                    SimpleViewer.this.mouseDown = true;
                }
                if (!pressed) {
                    SimpleViewer.this.mouseDown = false;
                }
            }
        }

        @Override
        public void cursorPos(long window, int xpos, int ypos) {
            float deltaX = (float) (xpos - SimpleViewer.this.lastMouseX);
            float deltaY = (float) (ypos - SimpleViewer.this.lastMouseY);

            SimpleViewer.this.lastMouseX = (float) xpos;
            SimpleViewer.this.lastMouseY = (float) ypos;

            if (mouseDown) {
                renderCamera.rotate(deltaX, deltaY);
            }
        }

        @Override
        public void drop(long window, int count, String[] paths) {

            System.out.println("Dropped files");
            SimpleViewer.this.loadFile(new File(paths[0]));
        }
    };

    private GLTFImporter gltfImporter;
    List<AnimatedModel> models = new ArrayList<>();
    private final RenderCamera renderCamera = new RenderCamera();
    private boolean wireframeMode = false; //Setting for showing wireframe. Toggled by 'w'
    private boolean limitedRender = false; //Setting - limits the number of primitives drawn
    private int limitedRenderIndex = 0; //Number of primitives to draw if in limited render mode

    private List<File> initialFileList;

    private int nextFileIndex = 0;

    private boolean mouseDown = false;
    private float lastMouseX;
    private float lastMouseY;

    // The window handle
    private long window;
    private Renderer renderer;

    public SimpleViewer() {

    }


    public SimpleViewer(List<File> fileList) {
        this.initialFileList = fileList;
    }

    public void run() {
        setupNativeWindow();
        init();
        loop();

        // Free the window callbacks and destroy the window
//    glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
//    glfwSetErrorCallback(null).free();
    }

    void setupNativeWindow() {
        glfwInit();
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_DEPTH_BITS, 16);
        glfwWindowHint(GLFW_TRANSPARENT_FRAMEBUFFER, GLFW_TRUE);
        window = glfwCreateWindow(WIDTH, HEIGHT, "hello glfw".getBytes(), 0, 0);
        if (window == 0) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetCallback(window, callback);

        int w = glfwGetFramebufferWidth(window);
        int h = glfwGetFramebufferHeight(window);
        System.out.println("w=" + w + "  ,h=" + h);

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
    }

    void init() {
        gltfImporter = new GLTFImporter();

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
        glClearDepth(1.0);

        glHint(GL_FRAGMENT_SHADER_DERIVATIVE_HINT,
                GL_NICEST); //Use a nicer calculation in fragment shaders

        //Need a default vertex array
        int[] vao = {0};
        glGenVertexArrays(1, vao, 0);
        glBindVertexArray(vao[0]);

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        for (File path : initialFileList) {
            loadFile(path);
        }

        renderer = new Renderer();
    }


    private void loadNextFile() {
        if (initialFileList == null || initialFileList.size() == 0) {
            return;
        }

        File next = initialFileList.get(nextFileIndex++);
        if (nextFileIndex >= initialFileList.size()) {
            nextFileIndex = 0;
        }
        System.out.println("==========================================================================");
        System.out.println("Loading new model: " + (nextFileIndex - 1) + " " + next.getAbsolutePath());
        glfwSetWindowTitle(window, next.getPath());
        renderCamera.reset();
        loadFile(next);
        System.out.println(
                "Finished Loading new model: " + (nextFileIndex - 1) + " " + next.getAbsolutePath());
    }

    private void loop() {
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            renderFrame();
        }
    }

    /**
     * Render a single frame to the window
     */
    void renderFrame() {
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
        for (AnimatedModel model : models) {
            model.update();
            if (model.getRootRenderNode() != null) {
                if (limitedRender) {
                    renderer.draw(renderCamera, model.getRootRenderNode(), limitedRenderIndex);
                } else {
                    renderer.draw(renderCamera, model.getRootRenderNode(), -1);
                }
            } else {
                System.out.println("No file loaded");
            }
        }
        glfwSwapBuffers(window); // swap the color buffers
        glfwPollEvents();
    }


    void loadFile(File file) {
        //Clear before loading
        AnimatedModel model = new AnimatedModel();
        models.add(model);
        RenderNode rootRenderNode = new RenderNode(null, null);
        model.setRootRenderNode(rootRenderNode);

        GLTF gltf;
        gltf = gltfImporter.load(file.getPath());
        if (gltf == null) {
            return;
        }

        if (gltf.getExtensionsRequired() != null) {
            System.out.println("Extensions not supported. Loading next file");
            loadNextFile();
            return;
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
                model.getAnimations().add(new RenderAnimation(animation));
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

        renderCamera.fitViewToScene(rootRenderNode);

        model.setAnimationStartTime(System.currentTimeMillis());
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
        if (camera != null) renderCamera.setGLTFCamera(camera);

        if (node.getChildren() != null)
            for (GLTFNode childNode : node.getChildren()) {
                processNodeChildren(childNode, renderNode);
            }
    }

    public void setNextFileIndex(int nextFileIndex) {
        this.nextFileIndex = nextFileIndex;
    }

    public RenderCamera getRenderCamera() {
        return renderCamera;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("THIS SHOULD SHOW IN THE FILE");

        List<File> files = new ArrayList<>();
        for (String arg : args) {
            if (arg.endsWith(".gltf") || arg.endsWith(".glb")) {
                files.add(new File(arg));
            }
        }
        files.add(new File("default/chicken/Chicken.gltf"));
        files.add(new File("Duck.gltf"));
        files.add(new File("GaiLun.gltf"));
        files.add(new File("AiXi.gltf"));
        if (files.size() == 0) {
            new SimpleViewer().run();
            System.out.println("Fix main loadRoot");
        } else {
            new SimpleViewer(files).run();
        }
    }

}
