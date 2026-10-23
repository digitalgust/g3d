package g3dtest.game;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.core.Light;
import org.mini.g3d.core.MainFrameBuffer;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.gui.callback.GCallBack;
import org.mini.g3d.entity.Entity;

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
    private final File directory;
    private final boolean platform;
    private float originalX, originalZ;
    private int frame;

    private SceneCapture(String path, boolean platform) {
        directory = new File(path);
        directory.mkdirs();
        this.platform = platform;
    }

    static SceneCapture fromSystemProperty() {
        String path = System.getProperty("g3d.captureDir");
        boolean platform = false;
        File config = new File(GCallBack.getInstance().getApplication().getSaveRoot(), "scene-capture.properties");
        if (path == null && config.isFile()) {
            try (FileInputStream input = new FileInputStream(config)) {
                Properties properties = new Properties();
                properties.load(input);
                path = properties.getProperty("capture.dir");
                platform = "platform".equals(properties.getProperty("capture.scene"));
            } catch (Exception ex) {
                throw new RuntimeException("Cannot read capture configuration", ex);
            }
        }
        return path == null || path.length() == 0 ? null : new SceneCapture(path, platform);
    }

    String heightMap() {
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
        boolean night = !platform && view == 2;
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
        for (Iterator<AnimatedModel> it = scene.getAnimatedModelsIterator(); it.hasNext();) {
            it.next().setAnimationTimeScale(0f);
        }
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
            // Both old and new engines have a known one-time error on their first draw.
            // Continue the warmup only; persistent or later errors invalidate the capture.
            if (frame > 0) throw new RuntimeException("Render GL error: " + renderError);
        }
        if (++frame % 4 != 0) return;
        int view = frame / 4 - 1;
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
        File output = new File(directory, (platform ? PLATFORM_VIEWS : VIEWS)[view] + ".tga");
        try (FileOutputStream stream = new FileOutputStream(output)) {
            stream.write(header);
            stream.write(pixels);
            System.out.println("SCENE_CAPTURE " + output.getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException("Cannot write scene capture", ex);
        }
        if (view == VIEWS.length - 1) System.exit(0);
    }
}
