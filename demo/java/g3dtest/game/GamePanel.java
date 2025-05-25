package g3dtest.game;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.animation.gltf2.ExportJointsKeyFrameMatrics;
import org.mini.g3d.animation.gltf2.loader.GLTFImporter;
import org.mini.g3d.animation.gltf2.loader.data.AniGroup;
import org.mini.g3d.animation.gltf2.loader.data.GLTF;
import org.mini.g3d.core.*;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.objmodel.ModelData;
import org.mini.g3d.core.objmodel.OBJFileLoader;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.core.util.Loader;
import org.mini.g3d.core.util.MousePicker;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.gui.GuiTexture;
import org.mini.g3d.particles.Effect;
import org.mini.g3d.particles.EffectMaster;
import org.mini.g3d.particles.controler.EmitterDirectionControler;
import org.mini.g3d.particles.controler.EmitterLocationControler;
import org.mini.g3d.shadowmap.ShadowMappingRenderer;
import org.mini.g3d.skybox.Skybox;
import org.mini.g3d.terrain.Terrain;
import org.mini.g3d.terrain.TerrainTexture;
import org.mini.g3d.terrain.TerrainTexturePack;
import org.mini.g3d.water.WaterTile;
import org.mini.gui.*;
import org.mini.gui.callback.GCallBack;
import org.mini.hmi.widget.*;
import org.mini.util.SysLog;

import java.util.*;

import static org.mini.gl.GL.*;

public class GamePanel extends GOpenGLPanel {
    int renderCount = 0;
    int logicCount = 0;


    //Camera camera;
    Camera camera;
    Terrain terrain;
    Loader loader = new Loader();
    public static TexturedModel shodowModel;

    MousePicker picker;

    Player player;
    Map<String, GLTF> gltfData = new HashMap<>();

    WidgetContainer widgets;
    ViewMover viewMover;
    Joystick joystick;
    WButton jumpBtn;
    Scene scene;
    RenderEngine renderEngine;

    Light lightOnHead;

    public GamePanel(GForm gf) {
        super(gf, 0f, 0f, 1f, 1f);
    }

    public GamePanel(GForm gf, int x, int y, int w, int h) {
        super(gf, (float) x, (float) y, (float) w, (float) h);
    }

    @Override
    public void gl_init() {

        scene = new Scene();
        camera = new Camera(getW(), getH(), Camera.FOV, Camera.NEAR_PLANE, Camera.FAR_PLANE);
        scene.setCamera(camera);
        //scene.getSun().setPosition(-300, 500, 0);//Z须等于0,要不会导致阴影贴图旋转
        renderEngine = new RenderEngine();
        renderEngine.gl_init(getW(), getH());
        setGlRendereredImg(renderEngine.getMainFbo().getFboimg());
        scene.getDayAndNight().setSecondsPerDay(144);
        //Gutil.checkGlError("Game glinit 0.1");

        picker = new MousePicker(scene);

        shodowModel = loader.loadTexturedModel("/res/models/sball.obj", "/res/textures/sball.png", 1);
        Random random = new Random();


        //Gutil.checkGlError("Game glinit 2");
        // Terrain
        //textures to paint the terrain with
        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("/res/textures/terrain/floor_sad_0.png"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("/res/textures/terrain/floor_river_0.png"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("/res/textures/terrain/floor_sad_3.png"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("/res/textures/terrain/floor_fens_0.png"));
        // creating the package of textures
        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, bTexture, gTexture);
        // getting the design, pattern of how we are painting the terrain.
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("/res/textures/terrain/blendMap1.png"));
        GImage img = GImage.createImageFromJar("/res/textures/terrain/heightmap1.png");
        terrain = new Terrain(img.getWidth(), img.getHeight(), loader, texturePack, blendMap, "/res/textures/terrain/heightmap1.png", 1.5f);
        scene.setTerrain(terrain);
        //Gutil.checkGlError("Game glinit 3");
        // Terrain end

        // Models
        // Environment Models
        TexturedModel tree = loadTexturedModel("/res/models/pine.obj", "/res/textures/pine.png", 1, loader);
        TexturedModel fern = loadTexturedModel("/res/models/fern.obj", "/res/textures/fern_atlas_texture.png", 2, loader);
        fern.getTexture().setHasTransparency(true);
        TexturedModel grass = loadTexturedModel("/res/models/grassModel.obj", "/res/textures/diffuse.png", 3, loader);
        grass.getTexture().setHasTransparency(true);
        grass.getTexture().setUseFakeLighting(true);
        TexturedModel flower = loadTexturedModel("/res/models/grassModel.obj", "/res/textures/flower.png", 1, loader);
        flower.getTexture().setHasTransparency(true);
        flower.getTexture().setUseFakeLighting(true);
        TexturedModel lamp = loadTexturedModel("/res/models/lamp.obj", "/res/textures/lamp.png", 1, loader);
        float mapSize = terrain.getMapScale();
        float tx, ty, tz;
        for (int i = 0; i < 30; i++) {
            tx = Math.abs(random.nextFloat()) * terrain.getCols() * mapSize;
            tz = Math.abs(random.nextFloat()) * terrain.getRows() * mapSize;
            ty = terrain.getHeightOfTerrain(tx, tz);
            scene.addEntity(new Entity(tree, new Vector3f(tx, ty, tz), 0f, 0f, 0f, 1.0f));
            tx = Math.abs(random.nextFloat()) * terrain.getCols() * mapSize;
            tz = Math.abs(random.nextFloat()) * terrain.getRows() * mapSize;
            ty = terrain.getHeightOfTerrain(tx, tz);
            scene.addEntity(new Entity(grass, random.nextInt(6), new Vector3f(tx, ty, tz), 0f, 0f, 0f, 1.f));
            tx = Math.abs(random.nextFloat()) * terrain.getCols() * mapSize;
            tz = Math.abs(random.nextFloat()) * terrain.getRows() * mapSize;
            ty = terrain.getHeightOfTerrain(tx, tz);
            scene.addEntity(new Entity(flower, new Vector3f(tx, ty, tz), 0f, 0f, 0f, 1.5f));
            tx = Math.abs(random.nextFloat()) * terrain.getCols() * mapSize;
            tz = Math.abs(random.nextFloat()) * terrain.getRows() * mapSize;
            ty = terrain.getHeightOfTerrain(tx, tz);
            scene.addEntity(new Entity(fern, random.nextInt(4), new Vector3f(tx, ty, tz), 0f, 0f, 0f, 1.0f));

        }

        //Gutil.checkGlError("Game glinit 4");
        // Environment Models end
        // GUI
        // Health Bar
        scene.addGuiTex(new GuiTexture(loader.loadTexture("/res/textures/health.png"), 1, 0, new Vector2f(-0.745f, 0.94f), new Vector2f(0.25f, 0.25f)));
        // GUI end

        for (int i = 0; i < 10; i++) {
            tx = Math.abs(random.nextFloat()) * terrain.getCols() * mapSize;
            tz = Math.abs(random.nextFloat()) * terrain.getRows() * mapSize;
            ty = terrain.getHeightOfTerrain(tx, tz);
            modelWithLight(lamp, 1, terrain, tx, ty, tz, "Green", 1);
        }

        //Gutil.checkGlError("Game glinit 5");
        // Models end
        // Animation

        GLTF gltf = GLTFImporter.loadFile("/res/ani/Xian_Nan_YiSheng.gltf");
        gltfData.put(gltf.getSource(), gltf);
        AniGroup aniGroup = GLTFImporter.loadAniGroup("/res/ani/Xian_Nan_YiSheng.json");
        if (gltf.getAniGroup() == null) {
            ExportJointsKeyFrameMatrics.export(gltf, aniGroup);
        }
        float px = terrain.getCols() / 3 * terrain.getMapScale();
        float pz = terrain.getRows() / 3 * terrain.getMapScale();
        float py = terrain.getHeightOfTerrain(px, pz);
        player = new Player(gltf, new Vector3f(px, py, pz), 0, 90, 0, 1f, shodowModel);
        scene.addAnimatedModel(player);
        camera.setLookatTarget(player);
        camera.setDistanceFromTarget(30f);
        camera.setHeightOfLand(2f);
        Vector3f ppos = player.getPosition();
        lightOnHead = new Light(ppos.x, ppos.y + 3f, ppos.z, 1f, 0f, 1f, 0, 0.5f, 0f);
        scene.addLight(lightOnHead);

        GLTF gltf1 = GLTFImporter.loadFile("/res/ani/Xian_Nv_DaoShi.gltf");
        gltfData.put(gltf1.getSource(), gltf1);
        AniGroup aniGroup1 = GLTFImporter.loadAniGroup("/res/ani/Xian_Nv_DaoShi.json");
        if (gltf1.getAniGroup() == null) {
            ExportJointsKeyFrameMatrics.export(gltf1, aniGroup1);
        }

        for (int i = 0; i < 300; i++) {
            px = random.nextFloat() * terrain.getCols() * terrain.getMapScale();
            pz = random.nextFloat() * terrain.getRows() * terrain.getMapScale();
            py = terrain.getHeightOfTerrain(px, pz);
            Player p = new Player(gltf1, new Vector3f(px, py, pz), 0, 90, 0, 1f, shodowModel);
            p.setAniClipIndex(random.nextInt(aniGroup1.getFullAniIndex()));
            p.setRotY(random.nextInt(360));
            scene.addAnimatedModel(p);
        }

        px = 0f;
        pz = 0f;
        py = terrain.getHeightOfTerrain(px, pz) + 2f;
        scene.getWaters().add(new WaterTile(px, pz, py, terrain.getCols() * terrain.getMapScale()));

        //Gutil.checkGlError("Game glinit 9");
        String[] b0 = {"/res/textures/skybox/b_0_right.png", "/res/textures/skybox/b_0_left.png", "/res/textures/skybox/b_0_top.png", "/res/textures/skybox/b_0_bottom.png", "/res/textures/skybox/b_0_back.png", "/res/textures/skybox/b_0_front.png"};
        String[] b1 = {"/res/textures/skybox/b_1_right.png", "/res/textures/skybox/b_1_left.png", "/res/textures/skybox/b_1_top.png", "/res/textures/skybox/b_1_bottom.png", "/res/textures/skybox/b_1_back.png", "/res/textures/skybox/b_1_front.png"};
        Skybox skybox = new Skybox(loader, b0, b1);
        scene.setSkybox(skybox);

        picker = new MousePicker(scene);

        widgets = new WidgetContainer(this, 0, 0, getW(), getH());
        viewMover = new ViewMover("", 0, 0, getW(), getH());
        viewMover.setCamera(camera);
        widgets.add(viewMover);
        joystick = new Joystick("/res/textures/yellowball.png", "/res/textures/redball.png", 0, getH() - 200, 200, 200);
        widgets.add(joystick);
        jumpBtn = new WButton("/res/textures/stuff_48.png", "TouchMe", getW() - 100f, 100, 32f, 32f);
        jumpBtn.setListener(new WidgetListener() {
            @Override
            public void action(Widget widget) {
                int i = random.nextInt(5);
                switch (i) {
                    case 0:
                        showFalldown(player.getPosition());
                        break;
                    case 1:
                        showFire(player.getPosition());
                        break;
                    case 2:
                        showBomb(player.getPosition());
                        break;
                    case 3:
                        showCircle(player.getPosition());
                        break;
                    case 4:
                        showTest(player.getPosition());
                        break;
                    default:
                        showColorChange(player.getPosition());
                }
                SysLog.info("G3D|player jump");
            }
        });
        widgets.add(jumpBtn);

    }


    static float[] textcolor = {1.f, 1.f, 1.f, 1.f};

    @Override
    public boolean paint(long vg) {
        super.paint(vg);
        if (widgets == null) {
            return true;
        }
        widgets.paint(vg);


        String cos = "player :" + (int) player.getPosition().x + ", " + (int) player.getPosition().y + ", " + (int) player.getPosition().z;
        float dx = 5f, dy = 75f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, cos, 12f, textcolor);
        dy += 15f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, "camera: " + (int) camera.getPosition().x + ", " + (int) camera.getPosition().y + ", " + (int) camera.getPosition().z, 12f, textcolor);
        dy += 15f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, "fps: " + GCallBack.getInstance().getFps(), 12f, textcolor);
        dy += 15f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, "/res/shadow/render trig: " + ShadowMappingRenderer.triangles + "/" + MainFrameBuffer.triangles, 12f, textcolor);
        dy += 15f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, "ani models: " + scene.getAnimatedModelSize(), 12f, textcolor);

        ShadowMappingRenderer.triangles = 0;
        MainFrameBuffer.triangles = 0;
        return true;
    }

    long cost, update;

    @Override
    public void gl_paint() {
        renderCount++;

        long start = System.currentTimeMillis();
        if (!isGLInited()) {
            return;
        }
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.f, 1.f);
        // MainLoop

        //Gutil.checkGlError(this.getClass().getName() + " gl_paint 0");
        scene.update();

        //update

        collisionMultipleTerrainsAnimatedModel(player, terrain); // Move method for player is inside this method
        if (joystick.isTouched()) {
            float playerRotY = joystick.getDirection() + camera.getAngleAroundTarget() - 90;
            //SysLog.info("G3D|old roty:" + player.getRotY() + "   playerRotY:" + playerRotY);
            player.setRotY(playerRotY);
            player.moveForward();
        } else {
            player.moveStop();
        }
        lightOnHead.setPosition(player.getPosition());



        updateAnimatedModel();

        renderEngine.renderScene(scene);

        cost = System.currentTimeMillis() - start;

    }

    private void updateAnimatedModel() {
        if (player == null) {
            return;
        }

        for (Iterator<AnimatedModel> it = scene.getAnimatedModelsIterator(); it.hasNext(); ) {
            AnimatedModel sp = it.next();
            sp.update();
        }
    }

    long aniCost = 0;


    @Override
    public void gl_destroy() {
        cleanup();
    }

    public void cleanup() {
        loader.cleanUp();

    }


    private void modelWithLight(TexturedModel model, int modelSize,
                                Terrain currentTerrain, float xLocation, float yLocation, float zLocation, String colour, int colourStrength) {
        int rgb_RED = 0;
        int rgb_GREEN = 0;
        int rgb_BLUE = 0;
        if (colour == "Red") {
            rgb_RED = 1;
            rgb_RED += colourStrength;
        } else if (colour == "Green") {
            rgb_GREEN = 1;
            rgb_GREEN += colourStrength;
        } else if (colour == "Blue") {
            rgb_BLUE = 1;
            rgb_BLUE += colourStrength;
        }

        Light light = new Light(new Vector3f(xLocation, yLocation, zLocation), new Vector3f(rgb_RED, rgb_GREEN, rgb_BLUE), new Vector3f(.0f, 0.01f, 1.002f));

        Entity e = new Entity(model, new Vector3f(xLocation, currentTerrain.getHeightOfTerrain(xLocation, zLocation), zLocation), 0f, 0f, 0f, modelSize);

        scene.addEntity(e);
        //scene.addLight(light);
    }

    private static void collisionMultipleTerrainsAnimatedModel(Player player, Terrain terrain) {
        player.move(terrain);
    }

    private static TexturedModel loadTexturedModel(String modelFileName, String textureFileName, int numberOfRows, Loader loader) {
        final ModelData data = OBJFileLoader.loadOBJ(modelFileName);
        final RawModel rawModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());
        Texture temp = new Texture(loader.loadTexture(textureFileName));
        if (numberOfRows > 1) {
            temp.setNumberOfRows(numberOfRows);
        }
        return new TexturedModel(rawModel, temp);
    }


    @Override
    public boolean dragEvent(int button, float dx, float dy, float x, float y) {
        return widgets.dragEvent(button, dx, dy, x, y);
    }

    @Override
    public boolean scrollEvent(float scrollX, float scrollY, float x, float y) {
        Camera cam = scene.getCamera();
        float dis2tgt = cam.getDistanceFromTarget() - (scrollY * .2f);
        //SysLog.info("G3D|dis3tgt = " + dis2tgt + "  /  " + cam.getDistanceFromTarget());
        if (dis2tgt <= 30 && dis2tgt > 5) {
            cam.setDistanceFromTarget(dis2tgt);
        }
        return super.scrollEvent(scrollX, scrollY, x, y);
    }

    @Override
    public void keyEventGlfw(int key, int scanCode, int action, int mods) {
        widgets.keyEvent(key, scanCode, action, mods);
    }


    @Override
    public void mouseButtonEvent(int button, boolean pressed, int x, int y) {
        widgets.mouseButtonEvent(button, pressed, x, y);

    }

    @Override
    public void touchEvent(int touchid, int phase, int x, int y) {
        widgets.touchEvent(touchid, phase, x, y);
    }

    public void cursorPosEvent(int x, int y) {

        picker.cursorPos(x, y);
    }


    public void exit() {
    }


    public static void showFalldown(Vector3f pos) {
        String STR_PE_1 = GToolkit.readFileFromJarAsString("/res/effect/demo/multi_emitter.json", "utf-8");
        Effect pe1 = EffectMaster.parseEffect(STR_PE_1);
        EmitterLocationControler etc = new EmitterLocationControler(pos, null);
        pe1.addControler(etc);
        EffectMaster.add(pe1);
    }

    public static void showFadeInFadeOut(Vector3f loc) {
        String STR_PE_2 = GToolkit.readFileFromJarAsString("/res/effect/demo/fadein_fadeout.json", "utf-8");
        Effect pe1 = EffectMaster.parseEffect(STR_PE_2);
        EmitterLocationControler etc = new EmitterLocationControler(loc, null);
        pe1.addControler(etc);
        EffectMaster.add(pe1);
    }

    public static void showFromTo(Vector3f from, Vector3f to) {
        String STR_PE_3 = GToolkit.readFileFromJarAsString("/res/effect/demo/from_to.json", "utf-8");
        Effect pe1 = EffectMaster.parseEffect(STR_PE_3);
        Vector3f v = new Vector3f();
        Vector3f.sub(to, from, v);
        EmitterLocationControler etc = new EmitterLocationControler(from, null);
        pe1.addControler(etc);
        EmitterDirectionControler edc = new EmitterDirectionControler(v, null);
        pe1.addControler(edc);

        EffectMaster.add(pe1);
    }


    public static void showFire(Vector3f loc) {
        String STR_PE_4 = GToolkit.readFileFromJarAsString("/res/effect/demo/direction_controler.json", "utf-8");
        Effect pe1 = EffectMaster.parseEffect(STR_PE_4);
        EmitterLocationControler etc = new EmitterLocationControler(loc, null);
        pe1.addControler(etc);
        EffectMaster.add(pe1);
    }


    public static void showCircle(Vector3f loc) {
        String STR_PE_5 = GToolkit.readFileFromJarAsString("/res/effect/demo/circle_modifier.json", "utf-8");
        Effect pe1 = EffectMaster.parseEffect(STR_PE_5);
        EmitterLocationControler etc = new EmitterLocationControler(loc, null);
        pe1.addControler(etc);
        EffectMaster.add(pe1);
    }

    public static void showColorChange(Vector3f loc) {
        String STR_PE_6 = GToolkit.readFileFromJarAsString("/res/effect/demo/color_modifier.json", "utf-8");
        Effect pe1 = EffectMaster.parseEffect(STR_PE_6);
        EmitterLocationControler etc = new EmitterLocationControler(loc, null);
        pe1.addControler(etc);
        EffectMaster.add(pe1);
    }

    public static void showBomb(Vector3f loc) {
        String STR_PE_7 = GToolkit.readFileFromJarAsString("/res/effect/demo/normal.json", "utf-8");
        Effect pe1 = EffectMaster.parseEffect(STR_PE_7);
        EmitterLocationControler etc = new EmitterLocationControler(loc, null);
        pe1.addControler(etc);
        EffectMaster.add(pe1);
    }


    public static void showNumber(Vector3f loc, int val) {
        String STR_PE_7 = GToolkit.readFileFromJarAsString("/res/effect/demo/normal.json", "utf-8");
        Effect pe1 = EffectMaster.parseEffect(STR_PE_7);
        EmitterLocationControler etc = new EmitterLocationControler(loc, null);
        pe1.addControler(etc);
        EffectMaster.add(pe1);
    }


    public static void showTest(Vector3f loc) {
        String STR_PE_7 = GToolkit.readFileFromJarAsString("/res/effect/demo/test.json", "utf-8");
        Effect pe1 = EffectMaster.parseEffect(STR_PE_7);
        EmitterLocationControler etc = new EmitterLocationControler(loc, null);
        pe1.addControler(etc);
        EffectMaster.add(pe1);
    }
}
