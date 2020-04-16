package g3dtest.game;

import org.mini.g3d.core.*;
import org.mini.g3d.core.converter.ModelData;
import org.mini.g3d.core.converter.OBJFileLoader;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.textures.ModelTexture;
import org.mini.g3d.core.toolbox.MousePicker;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.widget.*;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.gui.GuiRenderer;
import org.mini.g3d.gui.GuiTexture;
import org.mini.g3d.particles.*;
import org.mini.g3d.shadowmap.ShadowMappingPass;
import org.mini.g3d.shadowmap.ShadowMappingRenderer;
import org.mini.g3d.skybox.Skybox;
import org.mini.g3d.terrain.Terrain;
import org.mini.g3d.terrain.TerrainTexture;
import org.mini.g3d.terrain.TerrainTexturePack;
import org.mini.g3d.water.WaterRenderer;
import org.mini.g3d.water.WaterShader;
import org.mini.g3d.water.WaterTile;
import org.mini.gui.GCallBack;
import org.mini.gui.GOpenGLPanel;
import org.mini.gui.GToolkit;
import org.mini.nanovg.Gutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.mini.gl.GL.*;

public class GamePanel extends GOpenGLPanel {

    List<Light> lights = new ArrayList<>();
    List<Terrain> terrains = new ArrayList<>();
    List<Entity> entities = new ArrayList<>();
    List<WaterTile> waters = new ArrayList<>();
    List<GuiTexture> guis = new ArrayList<>();
    //Camera camera;
    WorldCamera camera;
    Terrain terrain;
    Loader loader;

    MasterPass masterPass;
    MasterRenderer masterRenderer;
    ShadowMappingPass shadowMappingPass;
    ShadowMappingRenderer shadowMappingRenderer;


    WaterShader waterShader;
    MousePicker picker;
    WaterRenderer waterRenderer;
    GuiRenderer guiRenderer;

    Light svetielko;
    Entity lampa;
    Entity ball;
    Player player;

    ParticleSystemComplex particleSystemFire;
    ParticleSystemSimple particleSystemSimple;
    List<ParticleSystem> particles;

    Skybox skybox;
    public static Light sun;

    WidgetContainer widgets;
    ViewMover viewMover;
    Joystick joystick;
    WButton jumpBtn;

    public GamePanel() {
        super(0f, 0f, 1f, 1f);
    }

    public GamePanel(int x, int y, int w, int h) {
        super((float) x, (float) y, (float) w, (float) h);
    }

    @Override
    public void gl_init() {

        EngineManager.createDisplay((int) getW(), (int) getH());

        loader = new Loader();
        Gutil.checkGlError("Game glinit 0.1");
        Random random = new Random();

        int smSize = 1024;
        shadowMappingPass = new ShadowMappingPass(smSize, smSize);
        shadowMappingPass.gl_init();
        Gutil.checkGlError("Game glinit 0.3");
        shadowMappingRenderer = new ShadowMappingRenderer(shadowMappingPass);

        masterPass = new MasterPass((int) getW(), (int) getH());
        masterPass.gl_init();
        setGlRendereredImg(masterPass.getFboimg());
        Gutil.checkGlError("Game glinit 0.5");

        camera = new WorldCamera(EngineManager.getWidth(), EngineManager.getHeight(), MasterRenderer.FOV, MasterRenderer.NEAR_PLANE, MasterRenderer.FAR_PLANE);
        masterRenderer = new MasterRenderer(camera);

        Gutil.checkGlError("Game glinit 0.8");
        guiRenderer = new GuiRenderer(loader);
        Gutil.checkGlError("Game glinit 1");
        // the sun
        sun = new Light(new Vector3f(-200, 500, 0), new Vector3f(1.0f, 1.0f, 1.0f));
        lights.add(sun);

        Gutil.checkGlError("Game glinit 2");
        // Terrain
        //textures to paint the terrain with
        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("textures/terrain/grassy"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("textures/terrain/mud"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("textures/terrain/path"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("textures/terrain/pinkFlowers"));
        // creating the package of textures
        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, bTexture, gTexture);
        // getting the design, pattern of how we are painting the terrain.
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("textures/terrain/blendMap"));
        // Create 4 different terrains, although same heightMap, texturePack and blendMap.
//        terrains.add(new Terrain(0, -1, loader, texturePack, blendMap, "textures/terrain/heightmap"));
//        terrains.add(new Terrain(-1, -1, loader, texturePack, blendMap, "textures/terrain/blendmap_GOT"));
//        terrains.add(new Terrain(-1, 0, loader, texturePack, blendMap, "textures/terrain/heightmap"));
        terrain = new Terrain(0, 0, loader, texturePack, blendMap, "textures/terrain/heightmap", shadowMappingRenderer);
        terrains.add(terrain);
        Gutil.checkGlError("Game glinit 3");
        // Terrain end
        // Models
        // Environment Models
        TexturedModel tree = loadTexturedModel("models/pine", "textures/pine", 1, loader);
        TexturedModel fern = loadTexturedModel("models/fern", "textures/fern_atlas_texture", 2, loader);
        fern.getTexture().setHasTransparency(true);
        TexturedModel grass = loadTexturedModel("models/grassModel", "textures/diffuse", 3, loader);
        grass.getTexture().setHasTransparency(true);
        grass.getTexture().setUseFakeLighting(true);
        TexturedModel flower = loadTexturedModel("models/grassModel", "textures/flower", 1, loader);
        flower.getTexture().setHasTransparency(true);
        flower.getTexture().setUseFakeLighting(true);
        TexturedModel lamp = loadTexturedModel("models/lamp/lamp", "models/lamp/lamp", 1, loader);
        TexturedModel ballmode = loadTexturedModel("models/sun", "textures/grassy", 1, loader);
        //TODO: have to use the check system from CollisionMultipleTerrains. but the "check-system" has to be a own method, so it can be used multiple places.
        for (int i = 0; i < 30; i++) {
            float x;
            float z = random.nextFloat() * (Terrain.SIZE);
            entities.add(new Entity(tree, new Vector3f(x = random.nextFloat() * Terrain.SIZE, terrains.get(0).getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 3.0f));
            entities.add(new Entity(grass, random.nextInt(6), new Vector3f(x = random.nextFloat() * Terrain.SIZE, terrains.get(0).getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 1.5f));
            entities.add(new Entity(flower, new Vector3f(x = random.nextFloat() * Terrain.SIZE, terrains.get(0).getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 1.5f));
            entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x = random.nextFloat() * Terrain.SIZE, terrains.get(0).getHeightOfTerrain(x, z), z), 0f, 0f, 0f, 1.0f));

        }
        ball = new Entity(ballmode, random.nextInt(4), new Vector3f(0, terrains.get(0).getHeightOfTerrain(0, 0), 0), 0f, 0f, 0f, 1.0f);
        entities.add(ball);

        Gutil.checkGlError("Game glinit 4");
        // Environment Models end
        // GUI
        // Health Bar
        guis.add(new GuiTexture(loader.loadTexture("textures/health"), new Vector2f(-0.745f, 0.94f), new Vector2f(0.25f, 0.25f)));
        // GUI end

        modelWithLight(entities, lights, lamp, 1, terrains.get(0), 10, 10, 250, "Green", 0);
        modelWithLight(entities, lights, lamp, 1, terrains.get(0), 120, 10, 275, "Green", 0);
        modelWithLight(entities, lights, lamp, 1, terrains.get(0), 230, 10, 300, "Green", 0);
        modelWithLight(entities, lights, lamp, 1, terrains.get(0), 340, 10, 325, "Green", 0);
        modelWithLight(entities, lights, lamp, 1, terrains.get(0), 450, 10, 350, "Green", 0);
        modelWithLight(entities, lights, lamp, 1, terrains.get(0), 660, 10, 105, "Green", 0);
        modelWithLight(entities, lights, lamp, 1, terrains.get(0), 770, 10, 200, "Green", 0);
        modelWithLight(entities, lights, lamp, 1, terrains.get(0), 80, 10, 425, "Green", 0);
        modelWithLight(entities, lights, lamp, 1, terrains.get(0), 750, 10, 350, "Green", 0);

        Gutil.checkGlError("Game glinit 5");
        // Models end
        // Animation

        player = new Player("res/ani/AiXi.gltf", new Vector3f(200, -4, 300), 0, 90, 0, 5f);


        Gutil.checkGlError("Game glinit 8");
        waterShader = new WaterShader();
        waterRenderer = new WaterRenderer(loader, waterShader, camera);

        waters.add(new WaterTile(75, 75, -7));

        Gutil.checkGlError("Game glinit 9");
        skybox = new Skybox(loader);

        picker = new MousePicker(camera, terrain);

        particles = new ArrayList();
        createParticleSystems(particles);
        ParticleManager.init(loader, camera);
        Gutil.checkGlError("Game glinit 10");

        widgets = new WidgetContainer(this, 0, 0, getW(), getH());
        viewMover = new ViewMover("/res/textures/view.png", getW() * .5f, 0, getW() * .5f, getH());
        viewMover.setCamera(camera);
        widgets.add(viewMover);
        joystick = new Joystick("/res/textures/yellowball.png", "/res/textures/redball.png", 0, getH() - 200, 200, 200);
        widgets.add(joystick);
        jumpBtn = new WButton("/res/textures/view.png", getW() - 100f, 100, 32f, 32f);
        jumpBtn.setListener(new WidgetListener() {
            @Override
            public void action(Widget widget) {
                player.jump();
                System.out.println("player jump");
            }
        });
        widgets.add(jumpBtn);

        camera.setLookatTarget(player);
        //reload all projection matrix
        camera.getProjectionDispatcher().dispatch();
    }


    private List<ParticleSystem> createParticleSystems(List<ParticleSystem> particleSystems) {
        ParticleTexture particleTextureFire = new ParticleTexture(loader.loadTexture("textures/fire"), 8, true);
        particleSystemFire = new ParticleSystemComplex(particleTextureFire, 10f, 1f, -0.01f, 2f, 4f);
        particleSystemFire.setLifeError(0.1f).setSpeedError(0.25f).setScaleError(0.5f).randomizeRotation();
        particleSystems.add(particleSystemFire);

        ParticleTexture particleTexture1 = new ParticleTexture(loader.loadTexture("textures/particleStar"), 8, true);
        ParticleTexture particleTexture2 = new ParticleTexture(loader.loadTexture("textures/particleAtlas"), 8, true);
        particleSystemSimple = new ParticleSystemSimple(particleTextureFire, 100f, 1f, -0.01f, 2f);
        particleSystems.add(particleSystemSimple);

        return particleSystems;
    }


    private static void renderEntities(MasterRenderer renderer, List<Entity> entities) {
        for (Entity entity : entities) {
            renderer.processEntity(entity);
        }
    }

    private static void renderTerrain(MasterRenderer renderer, List<Terrain> terrains) {
        for (Terrain terrain : terrains) {
            renderer.processTerrain(terrain);
        }
    }

    public boolean update(long vg) {
        super.update(vg);
        if (widgets == null) {
            return true;
        }
        widgets.update(vg);

        if (shadowMappingPass != null) {
            //GToolkit.drawImage(vg, shadowMappingPass.getImage(), 0, 0, 200, 200);
        }

        String cos = "player :" + (int) player.getPosition().x + ", " + (int) player.getPosition().y + ", " + (int) player.getPosition().z;
        float dx = 5f, dy = 75f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, cos, 12f, GToolkit.getStyle().getTextFontColor());
        dy += 15f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, "camera: " + (int) camera.getPosition().x + ", " + (int) camera.getPosition().y + ", " + (int) camera.getPosition().z, 12f, GToolkit.getStyle().getTextFontColor());
        dy += 15f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, "fps: " + GCallBack.getInstance().getFps(), 12f, GToolkit.getStyle().getTextFontColor());
        dy += 15f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, "shadow/render trig: " + ShadowMappingRenderer.triangles + "/" + MasterPass.triangles, 12f, GToolkit.getStyle().getTextFontColor());
        dy += 15f;
        GToolkit.drawText(vg, dx, dy, 300f, 20f, "shadow/render/total cost: " + shadowMappingPass.cost + " / " + masterPass.cost + " / " + cost, 12f, GToolkit.getStyle().getTextFontColor());

        ShadowMappingRenderer.triangles = 0;
        MasterPass.triangles = 0;
        return true;
    }

    long cost;

    @Override
    public void gl_paint() {
        cost = System.currentTimeMillis();
        if (!isGLInited()) {
            return;
        }
        glEnable(GL_POLYGON_OFFSET_FILL);
        glPolygonOffset(1.f, 1.f);
        // MainLoop

        Gutil.checkGlError(this.getClass().getName()+" gl_paint 0");
        shadowMappingPass.begin();
        shadowMappingRenderer.processTerrain(terrain);
        for (Entity entity : entities) {
            shadowMappingRenderer.processEntity(entity);
        }
        //shadowMappingRenderer.processEntity(player);
        shadowMappingRenderer.render(sun);
        shadowMappingPass.end();
        Gutil.checkGlError(this.getClass().getName()+" gl_paint 1");



        masterPass.begin();
        renderTerrain(masterRenderer, terrains);
        renderEntities(masterRenderer, entities); // All entities are gone if this is commented away. Player is still present
        // Render with animation
        masterRenderer.render(camera, lights, player, skybox);
        waterRenderer.render(camera, waters);
        ParticleManager.renderParticles(camera);
        // render without animation
        guiRenderer.render(guis);
        masterPass.end();
        Gutil.checkGlError(this.getClass().getName()+" gl_paint 2");

        collisionMultipleTerrainsAnimatedModel(player, terrains); // Move method for player is inside this method
//			collisionCameraTerrain(camera, terrains);
        camera.update();
        if (joystick.isTouched()) {
            float playerRotY = joystick.getDirection() + camera.getAngleAroundMaster() - 90;
            //System.out.println("old roty:" + player.getRotY() + "   playerRotY:" + playerRotY);
            player.setRotY(playerRotY);
            player.moveForward();
        } else {
            player.moveStop();
        }
        player.update();

        Vector3f cpos = camera.getPosition();
        float hight = terrain.getHeightOfTerrain(cpos.x, cpos.z);
        if (hight > cpos.y) {
            camera.calculatePitch(hight);
        }


        particleSystemFire.generateParticles(player.getPosition());
        //particleSystemSimple.generateParticles(player.getPosition());
        ParticleManager.update(camera);

        EngineManager.updateDisplay();
        cost = System.currentTimeMillis() - cost;
    }

    @Override
    public void gl_destroy() {
        cleanup();
    }

    public void cleanup() {
        waterShader.cleanUp();
        guiRenderer.cleanUp();
        masterRenderer.cleanUp();
        loader.cleanUp();
        ParticleManager.cleanUp();
        EngineManager.closeDisplay();

    }


    private static void modelWithLight(List<Entity> entities, List<Light> lights, TexturedModel model, int modelSize,
                                       Terrain currentTerrain, float xLocation, float yLocation, float zLocation, String colour, int colourStrength) {
        int rgb_RED = 0;
        int rgb_GREEN = 0;
        int rgb_BLUE = 0;
        if (colour == "Red") {
            rgb_RED = 2;
            rgb_RED += colourStrength;
        } else if (colour == "Green") {
            rgb_GREEN = 2;
            rgb_GREEN += colourStrength;
        } else if (colour == "Blue") {
            rgb_BLUE = 2;
            rgb_BLUE += colourStrength;
        }

        lights.add(new Light(new Vector3f(xLocation, yLocation, zLocation), new Vector3f(rgb_RED, rgb_GREEN, rgb_BLUE), new Vector3f(1.0f, 0.01f, 0.002f)));

        entities.add(new Entity(model, new Vector3f(xLocation, currentTerrain.getHeightOfTerrain(xLocation, zLocation), zLocation), 0f, 0f, 0f, modelSize));

    }

    private static void collisionMultipleTerrainsAnimatedModel(Player player, List<Terrain> terrains) {
        int px = (int) player.getPosition().x;
        int pz = (int) player.getPosition().z;

        player.move(terrains.get(0));
//        if (px <= 800 && px >= 0 && pz >= -800 && pz <= 0) {       // left: 0 to 800 and z: 0 to -800
//            player.update(terrains.get(0), animatedEntity);
//        } else if (px >= -800 && px <= 0 && pz >= -800 && pz <= 0) {	// left: 0 to -800 and z: 0 to -800
//            player.update(terrains.get(1), animatedEntity);
//        } else if (px >= -800 && px <= 0 && pz <= 800 && pz >= 0) {	// left: 0 to -800 and z: 0 to 800
//            player.update(terrains.get(2), animatedEntity);
//        } else if (px <= 800 && px >= 0 && pz <= 800 && pz >= 0) {	// left: 0 to 800 and z: 0 to 800
//            player.update(terrains.get(3), animatedEntity);
//        }
    }

    private static TexturedModel loadTexturedModel(String modelFileName, String textureFileName, int numberOfRows, Loader loader) {
        final ModelData data = OBJFileLoader.loadOBJ(modelFileName);
        final RawModel rawModel = loader.loadToVAO(data.getVertices(), data.getTextureCoords(), data.getNormals(), data.getIndices());

        ModelTexture temp = new ModelTexture(loader.loadTexture(textureFileName));
        if (numberOfRows > 1) {
            temp.setNumberOfRows(numberOfRows);
        }
        return new TexturedModel(rawModel, temp);
    }


    public boolean dragEvent(float dx, float dy, float x, float y) {
        return widgets.dragEvent(dx, dy, x, y);
    }


    @Override
    public void keyEvent(int key, int scanCode, int action, int mods) {
        widgets.keyEvent(key, scanCode, action, mods);
    }


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


    public void reSize() {
        if (masterPass == null) {
            return;
        }
        int w = (int) getW();
        int h = (int) getH();
        masterPass = new MasterPass(w, h);
        masterPass.gl_init();
        setGlRendereredImg(masterPass.getFboimg());

        EngineManager.createDisplay((int) getW(), (int) getH());
        camera.setView(w, h);
        camera.getProjectionDispatcher().dispatch();

        widgets.setSize(getW(), getH());
        joystick.setLocation(0, getH() - joystick.getH());
        viewMover.setSize(getW() * .5f, getH());
        viewMover.setLocation(getW() - viewMover.getW(), getH() - viewMover.getH());
        jumpBtn.setLocation(getW() - 100, 100);
    }
}
