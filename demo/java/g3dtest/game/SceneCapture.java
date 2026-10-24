package g3dtest.game;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.core.Light;
import org.mini.g3d.core.MainFrameBuffer;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.gui.callback.GCallBack;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.particles.*;
import org.mini.g3d.core.vector.Vector4f;
import org.mini.gui.GToolkit;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.util.Loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;

import static org.mini.gl.GL.*;

/** Fixed views and raw framebuffer exports for visually comparing shader changes. */
final class SceneCapture {
    private static final String[] VIEWS = {"day-close", "day-wide", "night"};
    private static final String[] PLATFORM_VIEWS = {"platform-front", "platform-side", "platform-standing"};
    private static final String[] GLOW_VIEWS = {"crystal-day", "crystal-night", "crystal-occlusion"};
    private static final String[] PARTICLE_VIEWS = {"particles-batch", "particles-reused", "particles-budget"};
    private final File directory;
    private final boolean particles;
    private Emitter limitedEmitter;
    private Emitter expiredEmitter;
    private int savedParticleBudget;
    private final boolean platform;
    private final boolean glow;
    private TexturedModel crystalModel;
    private Loader glowLoader;
    private final Entity[] crystals = new Entity[3];
    private Entity occluder;
    private AnimatedModel crystalBackgroundActor;
    private float originalX, originalZ;
    private int frame;

    private SceneCapture(String path, String mode) {
        directory = new File(path);
        directory.mkdirs();
        this.platform = "platform".equals(mode);
        this.particles = "particles".equals(mode);
        this.glow = "glow".equals(mode) || particles;
    }

    static SceneCapture fromSystemProperty() {
        String path = System.getProperty("g3d.captureDir");
        String mode = "standard";
        File config = new File(GCallBack.getInstance().getApplication().getSaveRoot(), "scene-capture.properties");
        if (path == null && config.isFile()) {
            try (FileInputStream input = new FileInputStream(config)) {
                Properties properties = new Properties();
                properties.load(input);
                path = properties.getProperty("capture.dir");
                mode = properties.getProperty("capture.scene", "standard");
            } catch (Exception ex) {
                throw new RuntimeException("Cannot read capture configuration", ex);
            }
        }
        return path == null || path.length() == 0 ? null : new SceneCapture(path, mode);
    }

    String heightMap() {
        if (glow) return "/res/textures/terrain/review_flat_heightmap.png";
        return platform ? "/res/textures/terrain/cutaway_heightmap.png" : "/res/textures/terrain/heightmap1.png";
    }

    void prepare(Scene scene, Light playerLight) {
        if (frame == 0) {
            // Separate pre-existing initialization errors from errors in the captured frames.
            int error;
            while ((error = glGetError()) != GL_NO_ERROR) {
                System.out.println("SCENE_CAPTURE initialization GL error: " + error);
            }
        }
        int view = frame / 4;
        boolean night = glow ? view == 1 : !platform && view == 2;
        scene.getDayAndNight().setSecondsPerDay(86400);
        scene.getDayAndNight().reset();
        scene.getDayAndNight().setTime(night ? 2 * 3600 : 12 * 3600);
        scene.getSun().setColor(night ? Scene.SUN_COLOR_NIGHT : Scene.SUN_COLOR_DAY);
        scene.setFogColor(night ? Scene.FOG_COLOR_NIGHT : Scene.FOG_COLOR_DAY);
        // Only keep the sun and the same local fill in both shader versions.
        scene.getLights().clear();
        scene.getLights().add(scene.getSun());
        scene.getLights().add(playerLight);
        playerLight.setColor(night ? new Vector3f(.65f, .7f, .85f) : new Vector3f());
        scene.getCamera().setPitch(view == 1 ? 28f : 38f);
        scene.getCamera().setAngleAroundTarget(view == 1 ? 65f : 0f);
        scene.getCamera().setDistanceFromTarget(view == 1 ? 65f : 32f);
        if (platform) preparePlatform(scene, view);
        if (glow) prepareGlow(scene, view);
        if (particles) prepareParticles(scene, view);
        for (Iterator<AnimatedModel> it = scene.getAnimatedModelsIterator(); it.hasNext();) {
            it.next().setAnimationTimeScale(0f);
        }
    }

    private void prepareGlow(Scene scene, int view) {
        Entity player = scene.getCamera().getLookatTarget();
        scene.getWaters().clear();
        if (crystalModel == null) {
            for (Iterator<AnimatedModel> it = scene.getAnimatedModelsIterator(); it.hasNext();) {
                AnimatedModel model = it.next();
                if (model != player) {
                    crystalBackgroundActor = model;
                    break;
                }
            }
            scene.clearAnimatedModels();
            scene.addAnimatedModel((AnimatedModel) player);
            if (crystalBackgroundActor != null) scene.addAnimatedModel(crystalBackgroundActor);
            scene.getEntitieMap().clear();
            // Loader owns the GL resources; retain it for the entire capture session.
            glowLoader = new Loader();
            crystalModel = glowLoader.loadTexturedModel("/res/models/i287_crystal.ply", "/res/textures/i287.png", 1);
            float[] opacities = {1f, .5f, .25f};
            for (int i = 0; i < crystals.length; i++) {
                float x = 190f + (i - 1) * 6f;
                float y = scene.getTerrain().getHeightOfTerrain(x, 186f);
                crystals[i] = new Entity(crystalModel, new Vector3f(x, y + .8f, 186f), 0f, 0f, 0f, 4f);
                crystals[i].setTransparency(opacities[i]);
                scene.addEntity(crystals[i]);
            }
            TexturedModel rock = glowLoader.loadTexturedModel("/res/models/sball.ply", "/res/textures/sball.png", 1);
            occluder = new Entity(rock, new Vector3f(196f, 19.4f, 182f), 0f, 0f, 0f, 4f);
        }
        float floor = scene.getTerrain().getHeightOfTerrain(190f, 190f);
        player.getPosition().set(190f, floor, 190f);
        player.setScale(1.5f);
        if (crystalBackgroundActor != null) {
            crystalBackgroundActor.getPosition().set(196f, floor, 190f);
            crystalBackgroundActor.setScale(1.5f);
        }
        scene.getCamera().setLookatTarget(player);
        scene.getCamera().setPitch(26f);
        scene.getCamera().setDistanceFromTarget(21f);
        scene.getCamera().setAngleAroundTarget(0f);
        if (view == 2 && frame % 4 == 0) scene.addEntity(occluder);
    }

    /** A visual load scene, including the old >2000-instance failure and mixed states. */
    private void prepareParticles(Scene scene, int view) {
        if (frame % 4 != 0) return;
        if (frame == 0) savedParticleBudget = ParticleMaster.getMaxParticles();
        EffectMaster.cleanUp();
        ParticleMaster.clear();
        System.out.println("PARTICLE_CAPTURE cleared: live=" + ParticleMaster.getActiveCount()
                + " batches=" + ParticleMaster.getParticles().size() + " pool=" + ParticleMaster.getPooledCount());
        ParticleMaster.setMaxParticles(view == 2 ? 2500 : savedParticleBudget);
        float floor = scene.getTerrain().getHeightOfTerrain(190f, 186f);
        Emitter sparks = particleEmitter("particle_whiteball.png", true, true,
                184f, floor + 3f, 186f, .13f, new Vector4f(1f, .55f, .12f, .65f));
        sparks.locationError.set(2f, 2f, 1f);
        sparks.min = 2400;
        sparks.terminate();
        // Same image: ordinary alpha, additive, and overlay must remain different batches.
        Emitter smoke = particleEmitter("particle_whiteball.png", false, true,
                190f, floor + 3f, 186f, 5f, new Vector4f(.1f, .5f, 1f, .6f));
        smoke.min = 1;
        smoke.terminate();
        Emitter smoke2 = particleEmitter("particle_redball.png", false, true,
                190.5f, floor + 3f, 184f, 4f, new Vector4f(1f, .3f, .2f, .6f));
        smoke2.min = 1;
        smoke2.terminate();
        Emitter overlay = particleEmitter("particle_whiteball.png", true, false,
                196f, floor + 3f, 185f, 1f, new Vector4f(.1f, 1f, .5f, .8f));
        overlay.min = 1;
        overlay.terminate();
        // Also exercise the same image with another atlas row count.
        Emitter atlas = particleEmitter("particle_whiteball.png", true, true,
                194f, floor + 4f, 185f, .5f, new Vector4f(1f, 1f, 1f, .6f));
        atlas.setImg(atlas.getImg(), 2, true, true);
        atlas.fps = 12;
        atlas.min = 1;
        atlas.terminate();
        expiredEmitter = particleEmitter("particle_00.png", true, true,
                190f, floor + 3f, 186f, 1f, new Vector4f(1f, 1f, 1f, 1f));
        expiredEmitter.particleLife = .001f;
        expiredEmitter.min = 2;
        expiredEmitter.terminate();
        limitedEmitter = particleEmitter("particleStar.png", true, true,
                196f, floor + 3f, 186f, .2f, new Vector4f(.2f, 1f, .6f, .8f));
        limitedEmitter.locationError.set(1f, 1f, 1f);
        limitedEmitter.pps = 100000f;
        limitedEmitter.maxAlive = view == 2 ? 500 : 64;
        Effect effect = new Effect(); // A valid effect with no controllers.
        effect.emitters.add(limitedEmitter);
        EffectMaster.add(effect);
        System.out.println("PARTICLE_CAPTURE burst=" + sparks.getAliveCount()
                + " budget=" + ParticleMaster.getMaxParticles());
    }

    private Emitter particleEmitter(String image, boolean additive, boolean depth,
                                    float x, float y, float z, float scale, Vector4f color) {
        Emitter emitter = new Emitter();
        emitter.setImg(GToolkit.getCachedImageFromJar("/res/textures/" + image), 1, additive, depth);
        emitter.location = new Vector3f(x, y, z);
        emitter.scale = scale;
        emitter.particleLife = 1000f;
        emitter.color.set(color);
        emitter.startup();
        return emitter;
    }

    private void preparePlatform(Scene scene, int view) {
        // The flat fixture floor would otherwise sit below the demo's large water plane.
        scene.getWaters().clear();
        Entity player = scene.getCamera().getLookatTarget();
        if (frame == 0) {
            originalX = player.getPosition().x;
            originalZ = player.getPosition().z;
        }
        float z = view == 2 ? originalZ - 12f : originalZ;
        player.getPosition().set(originalX, scene.getTerrain().getHeightOfTerrain(originalX, z), z);
        scene.getCamera().setLookatTarget(player);
        scene.getCamera().setPitch(38f);
        scene.getCamera().setAngleAroundTarget(view == 1 ? 28f : 0f);
        scene.getCamera().setDistanceFromTarget(32f);
        // Characters spread beside the player, outside a player-only reveal hole.
        int index = 0;
        float[] offsets = {-10f, -6f, -3f, 3f, 6f, 10f};
        for (Iterator<AnimatedModel> it = scene.getAnimatedModelsIterator(); it.hasNext() && index < offsets.length;) {
            AnimatedModel model = it.next();
            if (model == player) continue;
            float x = originalX + offsets[index];
            float enemyZ = originalZ + (index % 2 == 0 ? 1f : 4f);
            model.getPosition().set(x, scene.getTerrain().getHeightOfTerrain(x, enemyZ), enemyZ);
            model.setRotY(0f);
            index++;
        }
    }

    void capture(MainFrameBuffer fbo) {
        int renderError = glGetError();
        if (renderError != GL_NO_ERROR) {
            System.out.println("SCENE_CAPTURE render GL error at frame " + frame + ": " + renderError);
            throw new RuntimeException("Render GL error: " + renderError);
        }
        if (++frame % 4 != 0) return;
        int view = frame / 4 - 1;
        if (particles) {
            System.out.println("PARTICLE_CAPTURE view=" + view + " live=" + ParticleMaster.getActiveCount()
                    + " emitterLive=" + limitedEmitter.getAliveCount()
                    + " expiredLive=" + expiredEmitter.getAliveCount()
                    + " batches=" + ParticleMaster.getParticles().size()
                    + " pool=" + ParticleMaster.getPooledCount());
        }
        int width = fbo.getTexWidth(), height = fbo.getTexHeight();
        byte[] pixels = new byte[width * height * 4];
        fbo.begin();
        try {
            glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels, 0);
            int error = glGetError();
            if (error != GL_NO_ERROR) throw new RuntimeException("Capture GL error: " + error);
        } finally {
            fbo.end();
        }
        // Uncompressed BGRA TGA, bottom-left origin, matching OpenGL readback.
        for (int i = 0; i < pixels.length; i += 4) {
            byte red = pixels[i];
            pixels[i] = pixels[i + 2];
            pixels[i + 2] = red;
        }
        byte[] header = new byte[18];
        header[2] = 2;
        header[12] = (byte) width;
        header[13] = (byte) (width >> 8);
        header[14] = (byte) height;
        header[15] = (byte) (height >> 8);
        header[16] = 32;
        header[17] = 8;
        File output = new File(directory, (particles ? PARTICLE_VIEWS : glow ? GLOW_VIEWS : platform ? PLATFORM_VIEWS : VIEWS)[view] + ".tga");
        try (FileOutputStream stream = new FileOutputStream(output)) {
            stream.write(header);
            stream.write(pixels);
            System.out.println("SCENE_CAPTURE " + output.getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException("Cannot write scene capture", ex);
        }
        if (view == VIEWS.length - 1) {
            if (particles) {
                EffectMaster.cleanUp();
                ParticleMaster.clear();
                ParticleMaster.setMaxParticles(savedParticleBudget);
                System.out.println("PARTICLE_CAPTURE final cleanup: live=" + ParticleMaster.getActiveCount()
                        + " batches=" + ParticleMaster.getParticles().size()
                        + " emitterLive=" + limitedEmitter.getAliveCount());
            }
            System.exit(0);
        }
    }
}
