package org.mini.g3d.animation.gltf2;

import org.mini.apploader.GApplication;
import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.animation.gltf2.loader.GLTFImporter;
import org.mini.g3d.animation.gltf2.loader.data.*;
import org.mini.g3d.animation.gltf2.render.RenderMesh;
import org.mini.g3d.animation.gltf2.render.RenderMeshPrimitive;
import org.mini.g3d.animation.gltf2.render.RenderNode;
import org.mini.g3d.animation.gltf2.render.RenderSkin;
import org.mini.g3d.core.Camera;
import org.mini.g3d.core.RenderEngine;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.glfw.Glfw;
import org.mini.gui.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 把动画关键帧， 每帧每个骨骼的变换矩阵预先算出来，存入一个浮点值纹理中，并缓存到文件，之后不用重新计算
 */

public class ExportJointsKeyFrameMatrics extends GApplication {
    GForm form;
    List<AnimatedModel> models = new ArrayList<>();
    Random random = new Random();
    static GImage jointImg;
    static final String EXT_MODEL_MAT = ".modelmat";
    static final String EXT_JOINT_MAT = ".jointmat";

    @Override
    public GForm getForm() {
        if (form != null) {
            return form;
        }
        GToolkit.setStyle(new GStyleDark());

        form = new GForm(null);
        MonitorGLPanel glp = new MonitorGLPanel(form);
        glp.setName("MONITOR");
        glp.setBack(true);
        form.add(glp);

        glp.setSize(GCallBack.getInstance().getDeviceWidth(), GCallBack.getInstance().getDeviceHeight());
        GButton bt_load = new GButton(form, "LOAD", 0, 0, 50, 20);
        bt_load.setActionListener(gObject -> {
            String s = "/res/ani/xyv/Xian_Nv_DaoShi";
            GLTF gltf = export(s + ".gltf", s + ".json");
            for (int i = 0; i < 200; i++) {
                AnimatedModel am = new AnimatedModel(gltf);
                am.setPosition(new Vector3f(random.nextFloat() * 100 % 40f, 0f, random.nextFloat() * 100 % 40f));
                am.setAniClipIndex(Math.abs(random.nextInt() % gltf.getAniGroup().getFullAniIndex()));
                //am.setAniClipIndex(0);
                //am.setAnimationStartTime(Math.abs(System.currentTimeMillis() - (int) (random.nextInt() % 10000)));
                models.add(am);
            }
//            GLTF gltf1 = export("/res/ani/mashroom.gltf", null);
//            AnimatedModel am = new AnimatedModel(gltf1);
//            am.setPosition(new Vector3f(random.nextFloat() * 100 % 5f, 0f, random.nextFloat() * 100 % 5f));
//            models.add(am);

            MonitorGLPanel mgp = form.findByName("MONITOR");
            if (mgp != null) {
                mgp.getScene().addAnimatedModels(models);
                mgp.getScene().getCamera().setLookatTarget(models.get(0));
                mgp.getScene().getCamera().setOffsetToTarget(0f, 1f, 0f);

            }
        });
        form.add(bt_load);

        GButton bt_exit = new GButton(form, "EXIT", 0, 25, 50, 20);
        bt_exit.setActionListener(gObject -> {
            closeApp();
        });
        form.add(bt_exit);
        return form;
    }

    static class Token {
        int curKeyFrame;
        int totalKeyFrames;
    }

    public static GLTF export(String gltfPath, String aniPath) {
        GLTF gltf = GLTFImporter.loadFile(gltfPath);
        if (gltf == null) return null;
        AniGroup aniGroup = GLTFImporter.loadAniGroup(aniPath);
        if (aniGroup == null) return gltf;
        return export(gltf, aniGroup);
    }

    /**
     * 导出GLTF模型的动画骨骼的矩阵， 包括每一个关键帧
     * 矩阵总数 ： 关键帧数 * 骨骼数
     * 把数据存入一个RGBA32F的纹理里面，
     *
     * @param gltf
     * @param aniGroup
     * @return
     */
    public static GLTF export(GLTF gltf, AniGroup aniGroup) {
        if (aniGroup == null) return gltf;
        System.out.println("[G3D][INFO]export " + gltf.getSource());
        gltf.setAniGroup(aniGroup);

        String gltffn = gltf.getSource();
        gltffn = fixFileName(gltffn);
        String jointfn = gltffn + ".jointmat";
        String appRoot = GCallBack.getInstance().getApplication().getSaveRoot();
        File file = new File(appRoot + "/" + jointfn);

        if (file.exists()) {
            loadMatricsFromFile(gltf, appRoot, gltffn);

        } else {

            AnimatedModel model = new AnimatedModel(gltf);

            int fullani = aniGroup.getFullAniIndex();
            model.setAniClipIndex(fullani);
            float animationTimeScale = 1.f;

            int aniBegin = aniGroup.getAniClips().get(fullani).begin;
            int aniEnd = aniGroup.getAniClips().get(fullani).end;

            //算出每帧的骨骼变换和模型变换
            Token token = new Token();
            token.totalKeyFrames = aniEnd;
            Vector3f position = new Vector3f();
            float rotX = 0f, rotY = 0f, rotZ = 0f;
            float scale = 1.f;
            Matrix4f transform = model.getTransform();
            transform.setZero();
            G3dUtil.createTransformationMatrix(position, rotX, rotY, rotZ, scale, transform);


            for (int i = aniBegin; i < aniEnd; i++) {
                try {
                    float elapsedTime = i * AniGroup.FPT * animationTimeScale;
                    model.animateNode(elapsedTime);

                    RenderNode rootRenderNode = model.getRootRenderNode();
                    rootRenderNode.applyTransform(transform);
                    rootRenderNode.updateSkin();

                    token.curKeyFrame = i;
                    findMesh(rootRenderNode, token);
                } catch (Exception e) {
                    System.out.println("[G3D][WARN]keyframe out of bound :" + i);
                    e.printStackTrace();
                    break;
                }
            }

            saveMatricsToFile(gltf, appRoot, gltffn);
        }
        return gltf;
    }

    static void loadMatricsFromFile(GLTF gltf, String appRoot, String fn) {
        try {
            FileInputStream fis = new FileInputStream(appRoot + "/" + fn + EXT_JOINT_MAT);
            DataInputStream dis = new DataInputStream(fis);
            dis.readUTF();//fileName
            boolean usingHalfFloat = dis.readBoolean();
            int imgW = dis.readInt();
            byte[] b = new byte[imgW * imgW * 4 * (usingHalfFloat ? 2 : 4)];// RGBA 四分量
            int dataLen = dis.readInt();
            dis.readFully(b, 0, dataLen);
            dis.close();
            List<GLTFSkin> skins = gltf.getSkins();
            if (!skins.isEmpty()) {
                GLTFSkin skin = skins.get(0);
                loadJointMatTexture(b, imgW, usingHalfFloat, skin);
            }

            List<GLTFMesh> meshes = gltf.getMeshes();
            for (int i = 0, imax = meshes.size(); i < imax; i++) {
                GLTFMesh mesh = meshes.get(i);
                String mname = mesh.getName();
                if (mname == null || "".equals(mname)) {
                    System.out.println("[G3D][WARN]Mesh need a name , matrix can't save :" + gltf.getSource());
                    continue;
                }
                mname = fixFileName(mname);
                fis = new FileInputStream(appRoot + "/" + fn + "_" + mname + EXT_MODEL_MAT);
                dis = new DataInputStream(fis);
                List<GLTFMeshPrimitive> primitives = mesh.getPrimitives();
                int primSize = dis.readInt();
                if (primitives.size() != primSize) {
                    System.out.println("[G3D][INFO]primtives changed in :" + gltf.getSource() + " mesh: " + mesh.getName());
                }
                for (int j = 0; j < primitives.size(); j++) {
                    GLTFMeshPrimitive gmp = primitives.get(j);
                    int matCnt = dis.readInt();
                    if (matCnt < 0) {
                        continue;
                    }
                    Matrix4f[] mats = new Matrix4f[matCnt];
                    gmp.setModelMatrics(mats);
                    for (int k = 0; k < mats.length; k++) {
                        Matrix4f ma = new Matrix4f();
                        mats[k] = ma;
                        for (int m = 0; m < ma.mat.length; m++) {
                            ma.mat[m] = dis.readFloat();
                        }
                    }

                }
                fis.close();
            }
            System.out.println("[G3D][INFO]GLTF joints keyframe and model matrics load success :" + gltf.getSource());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveMatricsToFile(GLTF gltf, String appRoot, String fn) {

        List<GLTFSkin> skins = gltf.getSkins();
        if (skins.size() > 1) {
            System.out.println("[G3D][WARN] not support multiple skins in one gltf :" + gltf.getSource());
        }
        if (!skins.isEmpty()) {
            GLTFSkin skin = skins.get(0);
            List<Matrix4f>[] keyFramesMats = skin.getJointKeyFrameMatrics();
            boolean usingHalfFloat = true;
            if (keyFramesMats != null) {
                int len = keyFramesMats.length;//关键帧数量
                if (len > 0) {
                    int pixels = len * keyFramesMats[0].size(); //骨骼数
                    pixels *= 4;//每个矩阵有p个像素需要存
                    int sqrt = (int) Math.ceil(Math.sqrt(pixels));//差一个字节都不行
                    int imgW = 64;
                    while (imgW < sqrt) {
                        imgW <<= 1;
                    }
                    int bytesPerPixel = 4 * (usingHalfFloat ? 2 : 4);//每像素n浮点，每浮点4字节(半浮点2字节)
                    ByteBuffer imgData = ByteBuffer.allocateDirect(imgW * imgW * bytesPerPixel);
                    float min = Float.MAX_VALUE;
                    float max = Float.MIN_VALUE;
                    for (int i = 0; i < keyFramesMats.length; i++) {
                        List<Matrix4f> jointsMats = keyFramesMats[i];
//                        if (jointsMats == null) {
//                            int debug = 1;
//                        }
                        for (int j = 0, jmax = jointsMats.size(); j < jmax; j++) {
                            Matrix4f mat = jointsMats.get(j);
                            for (int k = 0; k < mat.mat.length; k++) {
                                float v = mat.mat[k];
                                if (v > max) max = v;
                                if (v < min) min = v;
//                                if (imgData.position() >= imgData.capacity()) {
//                                    int debug = 1;
//                                }
                                //32bit float co 16bit float
                                if (usingHalfFloat) {
                                    int x = Float.floatToIntBits(v * .2f + .5f);//move value range to (0.0-1.0)
                                    short h1 = (short) (((x >> 16) & 0x8000) | ((((x & 0x7f800000) - 0x38000000) >> 13) & 0x7c00) | ((x >> 13) & 0x03ff));

                                    imgData.putShort(h1);//half float 2bytes
                                } else {
                                    imgData.putFloat(v * .1f + .5f);
                                }
                            }
                        }
                    }
                    skin.setJointKeyFrameMatrics(null);//release
                    skin.setJointKeyFrameNormMatrics(null);
                    System.out.println("[G3D][INFO]Joint keyframe matrics max=" + max + "    min=" + min + " saved bytes = " + imgData.capacity() + " texture width = " + imgW);

                    try {
                        FileOutputStream fos = new FileOutputStream(appRoot + "/" + fn + EXT_JOINT_MAT);
                        DataOutputStream dos = new DataOutputStream(fos);
                        dos.writeUTF(gltf.getSource());
                        dos.writeBoolean(usingHalfFloat);
                        dos.writeInt(imgW);
                        dos.writeInt(imgData.position());
                        dos.write(imgData.array(), 0, imgData.position());
                        dos.close();

                        List<GLTFMesh> meshes = gltf.getMeshes();
                        for (int i = 0, imax = meshes.size(); i < imax; i++) {
                            GLTFMesh mesh = meshes.get(i);
                            String mname = mesh.getName();
                            if (mname == null || "".equals(mname)) {
                                System.out.println("[G3D][WARN]Mesh need a name , matrix can't save :" + gltf.getSource());
                                continue;
                            }
                            mname = fixFileName(mname);
                            fos = new FileOutputStream(appRoot + "/" + fn + "_" + mname + EXT_MODEL_MAT);
                            dos = new DataOutputStream(fos);
                            List<GLTFMeshPrimitive> primitives = mesh.getPrimitives();
                            dos.writeInt(primitives.size());
                            for (int j = 0; j < primitives.size(); j++) {
                                GLTFMeshPrimitive gmp = primitives.get(j);
                                Matrix4f[] mats = gmp.getModelMatrics();
                                if (mats != null) {
                                    dos.writeInt(mats.length);
                                    for (int k = 0; k < mats.length; k++) {
                                        Matrix4f ma = mats[k];
                                        for (int m = 0; m < ma.mat.length; m++) {
                                            dos.writeFloat(ma.mat[m]);
                                        }
                                    }
                                } else {
                                    dos.writeInt(-1);
                                }
                            }
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    loadJointMatTexture(imgData.array(), imgW, usingHalfFloat, skin);
                }
            }
        }
    }

    public static void clearCacheFiles() {
        try {
            String appRoot = GCallBack.getInstance().getApplication().getSaveRoot();
            File[] files = new File(appRoot).listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    String fn = file.getName();
                    if (fn.endsWith(EXT_MODEL_MAT) || fn.endsWith(EXT_JOINT_MAT)) {
                        return true;
                    }
                    return false;
                }
            });
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File f = files[i];
                    f.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String fixFileName(String fn) {

        fn = fn.replace("/", "_");
        fn = fn.replace("\\", "_");
        fn = fn.replace(":", "_");
        return fn;
    }

    static void loadJointMatTexture(byte[] data, int imgW, boolean usingHalfFloat, GLTFSkin skin) {
        int tex = 0;
        if (usingHalfFloat) {
            tex = skin.getLoader().loadTextureRGBA16F(data, imgW, imgW);
        } else {
            tex = skin.getLoader().loadTextureRGBA32F(data, imgW, imgW);
        }
        jointImg = GImage.createImage(tex, imgW, imgW);
        skin.setJointKFTex(jointImg.getGLTextureId());
        skin.setJointKFTexWidth(imgW);
    }


    void float32vsfloat16() {

        //Half to float:
        short h = 0;
        float f = ((h & 0x8000) << 16) | (((h & 0x7c00) + 0x1C000) << 13) | ((h & 0x03FF) << 13);

        //Float to half:
        int x = Float.floatToIntBits(f);
        short h1 = (short) (((x >> 16) & 0x8000) | ((((x & 0x7f800000) - 0x38000000) >> 13) & 0x7c00) | ((x >> 13) & 0x03ff));
    }

    private static void findMesh(RenderNode node, Token token) {
        if (node instanceof RenderMeshPrimitive) {
            cacheMatrics((RenderMeshPrimitive) node, token);
        }
        for (int i = 0, imax = node.getChildren().size(); i < imax; i++) {
            RenderNode child = node.getChildren().get(i);
            RenderNode subst = child.getReplacer();

            if (subst != null) {//如果存在换装,则不渲染原模型 , 只渲染替换模型
                findMesh(subst, token);
            } else {
                findMesh(child, token);
            }
        }
    }

    private static void cacheMatrics(RenderMeshPrimitive rmp, Token token) {
        if (rmp.isSkip()) {
            return;
        }
        RenderMesh mesh = rmp.getMesh();
        GLTFMeshPrimitive gmp = rmp.getGltfMeshPrimitive();
        if (gmp != null) {
            {
                Matrix4f[] modelMatrics = gmp.getModelMatrics();
                if (modelMatrics == null) {
                    modelMatrics = new Matrix4f[token.totalKeyFrames];
                    gmp.setModelMatrics(modelMatrics);
                }
                Matrix4f m = new Matrix4f();
                Matrix4f.load(rmp.getWorldTransform(), m);
                modelMatrics[token.curKeyFrame] = m;
                if (token.curKeyFrame < 3) {

                    //System.out.println(token.curKeyFrame + "====================\n" + m.toString());
                }
            }
            {
                Matrix4f[] modelNormMatrics = gmp.getModelNormMatrics();
                if (modelNormMatrics == null) {
                    modelNormMatrics = new Matrix4f[token.totalKeyFrames];
                    gmp.setModelNormMatrics(modelNormMatrics);
                }
                Matrix4f m = new Matrix4f();
                Matrix4f.load(rmp.getNormalMatrix(), m);//
                modelNormMatrics[token.curKeyFrame] = m;
            }
        }

        if (mesh.getSkin() != null) {
            RenderSkin skin = mesh.getSkin();
            GLTFSkin gltfSkin = skin.getGltfSkin();

            ArrayList[] keyframesJoints = gltfSkin.getJointKeyFrameMatrics();
            if (keyframesJoints == null) {
                keyframesJoints = new ArrayList[token.totalKeyFrames];
                gltfSkin.setJointKeyFrameMatrics(keyframesJoints);
            }
            ArrayList[] keyframesJointsNorm = gltfSkin.getJointKeyFrameNormMatrics();
            if (keyframesJointsNorm == null) {
                keyframesJointsNorm = new ArrayList[token.totalKeyFrames];
                gltfSkin.setJointKeyFrameNormMatrics(keyframesJointsNorm);
            }

            if (keyframesJoints[token.curKeyFrame] == null) {
                ArrayList<Matrix4f> jointList = new ArrayList<>();
                keyframesJoints[token.curKeyFrame] = jointList;
                List<Matrix4f> jointMats = skin.getJointMatrices();
                for (int i = 0; i < jointMats.size(); i++) {
                    Matrix4f mat = new Matrix4f();
                    Matrix4f.load(jointMats.get(i), mat);
                    jointList.add(mat);
                }
            } else {
            }

            if (keyframesJointsNorm[token.curKeyFrame] == null) {
                ArrayList<Matrix4f> jointNormList = new ArrayList<>();
                keyframesJointsNorm[token.curKeyFrame] = jointNormList;
                List<Matrix4f> jointNormMats = skin.getJointNormalMatrices();
                for (int i = 0; i < jointNormMats.size(); i++) {
                    Matrix4f mat = new Matrix4f();
//                    Matrix4f.load(jointNormMats.get(i), mat);
                    jointNormList.add(mat);
                }
            } else {
                //System.out.println(gltfSkin + ", jointNorm " + token.curKeyFrame);
            }
        }
    }


    class MonitorGLPanel extends GOpenGLPanel {
        Scene scene;
        RenderEngine renderEngine;

        public MonitorGLPanel(GForm form) {
            super(form);
        }

        @Override
        public boolean paint(long vg) {
            super.paint(vg);
            float dx = 5f, dy = 75f;
            dy += 15f;
            GToolkit.drawText(vg, dx, dy, 300f, 20f, "fps: " + GCallBack.getInstance().getFps(), 12f, GToolkit.getStyle().getTextFontColor());
            dy += 15f;
            if (renderEngine != null) {
                GToolkit.drawText(vg, dx, dy, 300f, 20f, "shadow: " + renderEngine.getShadowMappingFbo().cost + " render: " + renderEngine.getMainFbo().cost + " update: " + update, 12f, GToolkit.getStyle().getTextFontColor());
            }
            if (jointImg != null) {
                float imgW = jointImg.getWidth();
                float imgH = jointImg.getHeight();
                imgW = imgW > 512 ? imgW / 2f : imgW;
                imgH = imgH > 512 ? imgH / 2f : imgH;
                float dimgx = getW() - imgW;
                //GToolkit.drawRect(vg, dimgx, 0, imgW, imgH, GToolkit.nvgRGBA(0xffffffff), true);
                //GToolkit.drawImage(vg, jointImg, dimgx, 0, imgW, imgH, false, 1.0f);
            }
            return true;
        }

        long update = 0;
        AnimatedModel model;

        @Override
        public void gl_paint() {

            scene.update();
            renderEngine.renderScene(scene);

            if (models != null) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < models.size(); i++) {
                    AnimatedModel model = models.get(i);
                    model.update();
                    this.model = model;

                }
                update = System.currentTimeMillis() - start;
            }
        }

        @Override
        public void gl_init() {
            scene = new Scene();
            Camera camera = new Camera(getW(), getH(), Camera.FOV, Camera.NEAR_PLANE, Camera.FAR_PLANE);
            camera.setDistanceFromTarget(45f);
            scene.setCamera(camera);
            renderEngine = new RenderEngine();
            renderEngine.gl_init(getW(), getH());
            setGlRendereredImg(renderEngine.getMainFbo().getFboimg());
        }

        @Override
        public void gl_destroy() {

        }

        public Scene getScene() {
            return scene;
        }


        @Override
        public boolean dragEvent(int button, float dx, float dy, float x, float y) {
            if (scene == null) return false;

            Camera camera = scene.getCamera();
            if (camera != null) {
                float a = camera.getAngleAroundTarget();
                float adjx = dx * 0.5f;
                camera.setAngleAroundTarget(a - adjx);
                float pitch = camera.getPitch();
                float adjy = dy * 0.3f;
                if (pitch + adjy > 2f && pitch + adjy < 70f) {
                    camera.setPitch(pitch + adjy);
                }
                return false;
            }
            return false;
        }

        @Override
        public void keyEventGlfw(int key, int scanCode, int action, int mods) {
            super.keyEventGlfw(key, scanCode, action, mods);
            if (key == Glfw.GLFW_KEY_SPACE && action == Glfw.GLFW_PRESS) {
            }
        }
    }
}
