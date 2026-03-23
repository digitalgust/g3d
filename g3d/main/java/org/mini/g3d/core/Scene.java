package org.mini.g3d.core;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.entity.EntityShader;
import org.mini.g3d.gui.GuiTexture;
import org.mini.g3d.particles.EffectMaster;
import org.mini.g3d.particles.ParticleMaster;
import org.mini.g3d.skybox.DayAndNight;
import org.mini.g3d.skybox.Skybox;
import org.mini.g3d.terrain.Terrain;
import org.mini.g3d.water.WaterTile;
import org.mini.util.SysLog;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 所有实体数据,需要渲染的对象,放入各个容器中
 */

public class Scene {

    final Object lock = new Object();
    public static final Vector3f SUN_COLOR_NIGHT = new Vector3f(0.4f, 0.4f, 0.6f);
    public static final Vector3f SUN_COLOR_DAY = new Vector3f(1.0f, 1.0f, 0.8f);

    public static final Vector3f FOG_COLOR_NIGHT = new Vector3f(0.25f, 0.25f, 0.4f);
    public static final Vector3f FOG_COLOR_DAY = new Vector3f(0.7f, 0.7f, 0.9f);

    List<Light> lights = new CopyOnWriteArrayList<>();
    Map<TexturedModel, List<Entity>> entitieMap = new ConcurrentHashMap<>();
    final Map<TexturedModel, List<Entity>> visibleEntitieMap = new HashMap<>();
    final Map<TexturedModel, List<Entity>> visibleBatchCache = new HashMap<>();
    final Vector3f cameraForward = new Vector3f();
    final Vector3f cameraRight = new Vector3f();
    final Vector3f cameraUp = new Vector3f();

    float tanHalfFovX = 1.0f;
    float tanHalfFovY = 1.0f;
    float nearPlane = 0.1f;
    float farPlane = 500f;

    //            Collections.synchronizedMap(new HashMap<TexturedModel, List<Entity>>() {
//                @Override
//                public Set<TexturedModel> keySet() {
//                    throw new UnsupportedOperationException("not supoort, see forEach()");
//                }
//
//                @Override
//                public Collection<List<Entity>> values() {
//                    throw new UnsupportedOperationException("not supoort, see forEach()");
//                }
//
//                @Override
//                public Set<Map.Entry<TexturedModel, List<Entity>>> entrySet() {
//                    throw new UnsupportedOperationException("not supoort, see forEach()");
//                }
//            });
    List<WaterTile> waters = new CopyOnWriteArrayList<>();
    List<GuiTexture> guis = new CopyOnWriteArrayList<>();
    List<AnimatedModel> animatedModels = new CopyOnWriteArrayList<>();

    //            Collections.synchronizedList(new ArrayList<AnimatedModel>() {
//                @Override
//                public ListIterator<AnimatedModel> iterator() {
//                    throw new UnsupportedOperationException("not supoort, see forEach()");
//                }
//
//                @Override
//                public ListIterator<AnimatedModel> listIterator() {
//                    throw new UnsupportedOperationException("not supoort, see forEach()");
//                }
//
//                @Override
//                public ListIterator<AnimatedModel> listIterator(int index) {
//                    throw new UnsupportedOperationException("not supoort, see forEach()");
//                }
//            });
    List<Entity> subsitutes = new ArrayList<>();//sprite 替身模型, 用于产生影子


    Skybox skybox;
    Vector3f fogColor = new Vector3f();
    Terrain terrain;
    Camera camera;
    public Light sun;
    DayAndNight dayAndNight;

    boolean volumetricFog = false;


    //用于标志当前是否是在进行阴影渲染
    private boolean shadowRender = false;

    /**
     *
     */
    public Scene() {
        //太阳暂时不能设置Z值,会导致阴影贴图旋转
        sun = new Light(new Vector3f(-200, 500, 0), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(0.7f, 0, 0));
        addLight(sun);
        dayAndNight = new DayAndNight();
    }

    public void update() {
        dayAndNight.update();
        EffectMaster.update();
        ParticleMaster.update(getCamera());

        camera.update();
    }

    public void setSkybox(Skybox skybox) {
        synchronized (lock) {
            this.skybox = skybox;
        }
    }

    public void setSun(Light sun) {
        synchronized (lock) {
            this.sun = sun;
        }
    }

    public void setTerrain(Terrain terrain) {
        synchronized (lock) {
            this.terrain = terrain;
        }
    }

    public void setCamera(Camera camera) {
        synchronized (lock) {
            this.camera = camera;
        }
    }

    public Skybox getSkybox() {
        return skybox;
    }

    public Light getSun() {
        return sun;
    }

    public Camera getCamera() {
        return camera;
    }

    public Iterator<Light> getLightIterator() {
        return lights.iterator();
    }

    public List<Light> getLights() {
        return lights;
    }

    public int getLightSize() {
        return lights.size();
    }

    public void addLight(Light light) {
        lights.add(light);
        if (lights.size() > EntityShader.MAX_LIGHTS) {
            SysLog.warn("G3D|max light num is " + EntityShader.MAX_LIGHTS + ", exceeded " + lights.size());
        }
    }

    public List<WaterTile> getWaters() {
        return waters;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    /**
     * =====================================================
     * AnimatedModel
     * <p>
     * 不可直接获取 容器 进行修改
     */

    public Iterator<AnimatedModel> getAnimatedModelsIterator() {
        return animatedModels.iterator();
    }

    public void addAnimatedModel(AnimatedModel model) {
        if (model == null) return;

        synchronized (lock) {
            animatedModels.add(model);
            if (model.getShadowNode() != null) {
                subsitutes.add(model.getShadowNode());
            }
        }
    }

    public void addAnimatedModels(List<? extends AnimatedModel> models) {
        if (models == null) return;
        synchronized (lock) {
            for (int i = 0, imax = models.size(); i < imax; i++) {
                AnimatedModel am = models.get(i);
                addAnimatedModel(am);
            }
        }
    }

    public void removeAnimatedMode(AnimatedModel model) {
        if (model == null) return;

        synchronized (lock) {
            animatedModels.remove(model);
            if (model.getShadowNode() != null) {
                subsitutes.remove(model.getShadowNode());
            }
        }
    }

    public void removeAnimatedModels(List<? extends AnimatedModel> models) {
        if (models == null) return;

        synchronized (lock) {
            for (int i = 0, imax = models.size(); i < imax; i++) {
                AnimatedModel am = models.get(i);
                removeAnimatedMode(am);
            }
        }
    }

    public int getAnimatedModelSize() {
        return animatedModels.size();
    }


    public void clearAnimatedModels() {
        synchronized (lock) {
            animatedModels.clear();
            subsitutes.clear();
        }
    }

    /**
     * =====================================================
     */
    public void clear() {
        synchronized (lock) {
            lights.clear();
            entitieMap.clear();
            visibleEntitieMap.clear();
            visibleBatchCache.clear();
            waters.clear();
            if (terrain != null) {
                terrain.cleanUp();
            }
            setTerrain(null);
            setSkybox(null);
            clearGuis();
            clearAnimatedModels();
            // Don't clear VBO buffers here — GLTF accessor objects are cached and reused
            // across map transitions, so their VBO buffers in accessor2GlBufferMap can be
            // reused directly, avoiding GPU memory churn from delete+recreate cycles.
            // Full cleanup (including buffers) is still done via GLDriver.cleanUp() on shutdown.

            //reset
            addLight(sun);
        }
    }

    public void addEntity(Entity entity) {
        synchronized (lock) {
            TexturedModel entityModel = entity.getModel();
            if (entityModel == null) {
                new Throwable("Entity :null TexturedModel").printStackTrace();
            }
            synchronized (entitieMap) {
                List<Entity> batch = entitieMap.get(entityModel);

                if (batch != null) {
                    batch.add(entity);
                } else {
                    List<Entity> newBatch = new ArrayList<Entity>();
                    newBatch.add(entity);
                    entitieMap.put(entityModel, newBatch);
                }
            }
        }
    }


    public void removeEntity(Entity entity) {

        synchronized (lock) {
            TexturedModel entityModel = entity.getModel();
            if (entityModel == null) {
                new Throwable("Entity :null TexturedModel").printStackTrace();
            }
            synchronized (entitieMap) {
                List<Entity> batch = entitieMap.get(entityModel);

                if (batch != null) {
                    batch.remove(entity);
                    if (batch.isEmpty()) {
                        entitieMap.remove(entityModel);
                        visibleEntitieMap.remove(entityModel);
                        visibleBatchCache.remove(entityModel);
                    }
                }
            }
        }
    }


    public Map<TexturedModel, List<Entity>> getEntitieMap() {
        return entitieMap;
    }

    public Map<TexturedModel, List<Entity>> getVisibleEntitieMap(Camera cam) {
        if (cam == null) {
            return entitieMap;
        }
        synchronized (lock) {
            prepareCameraCullingData(cam);
            Vector3f camPos = cam.getPosition();
            visibleEntitieMap.clear();

            for (Map.Entry<TexturedModel, List<Entity>> entry : entitieMap.entrySet()) {
                TexturedModel tm = entry.getKey();
                List<Entity> entities = entry.getValue();
                if (tm == null || entities == null || entities.isEmpty()) {
                    continue;
                }
                List<Entity> visibleBatch = visibleBatchCache.get(tm);
                if (visibleBatch == null) {
                    visibleBatch = new ArrayList<>(Math.min(entities.size(), 64));
                    visibleBatchCache.put(tm, visibleBatch);
                } else {
                    visibleBatch.clear();
                }

                float modelRadius = 1.0f;
                if (tm.getRawModel() != null) {
                    modelRadius = tm.getRawModel().getBoundingRadius();
                }

                for (int i = 0, imax = entities.size(); i < imax; i++) {
                    Entity e = entities.get(i);
                    if (e == null || e.getPosition() == null) {
                        continue;
                    }
                    if (isEntityVisible(e, camPos, modelRadius)) {

                        visibleBatch.add(e);
                    }
                }

                if (!visibleBatch.isEmpty()) {
                    visibleEntitieMap.put(tm, visibleBatch);
                }
            }
            return visibleEntitieMap;
        }
    }

    private void prepareCameraCullingData(Camera cam) {
        fillCameraBasis(cam, cameraForward, cameraRight, cameraUp);

        Matrix4f proj = cam.getProjectionMatrix();
        float m00 = proj.mat[Matrix4f.M00];
        float m11 = proj.mat[Matrix4f.M11];
        tanHalfFovX = m00 != 0f ? 1.0f / Math.abs(m00) : 1.0f;
        tanHalfFovY = m11 != 0f ? 1.0f / Math.abs(m11) : 1.0f;
        nearPlane = cam.getNear();
        farPlane = cam.getFar();
    }

    private boolean isEntityVisible(Entity entity, Vector3f camPos, float modelRadius) {
        Vector3f p = entity.getPosition();
        float dx = p.x - camPos.x;
        float dy = p.y - camPos.y;
        float dz = p.z - camPos.z;

        float radius = Math.abs(entity.getScale()) * modelRadius;

        float zCam = dx * cameraForward.x + dy * cameraForward.y + dz * cameraForward.z;
        if (zCam + radius < nearPlane || zCam - radius > farPlane) {
            return false;
        }

        float xCam = dx * cameraRight.x + dy * cameraRight.y + dz * cameraRight.z;
        float xLimit = zCam * tanHalfFovX + radius;
        if (xCam > xLimit || xCam < -xLimit) {
            return false;
        }

        float yCam = dx * cameraUp.x + dy * cameraUp.y + dz * cameraUp.z;
        float yLimit = zCam * tanHalfFovY + radius;
        return !(yCam > yLimit || yCam < -yLimit);
    }

    private void fillCameraBasis(Camera cam, Vector3f forward, Vector3f right, Vector3f up) {
        float pitchRad = (float) Math.toRadians(cam.getPitch());
        float yawRad = (float) Math.toRadians(cam.getYaw());
        float cosPitch = (float) Math.cos(pitchRad);

        forward.x = (float) (Math.sin(yawRad) * cosPitch);
        forward.y = (float) (-Math.sin(pitchRad));
        forward.z = (float) (-Math.cos(yawRad) * cosPitch);
        normalize(forward);

        right.x = forward.z;
        right.y = 0f;
        right.z = -forward.x;
        if (!normalize(right)) {
            right.x = 1f;
            right.y = 0f;
            right.z = 0f;
        }

        up.x = right.y * forward.z - right.z * forward.y;
        up.y = right.z * forward.x - right.x * forward.z;
        up.z = right.x * forward.y - right.y * forward.x;
        normalize(up);
    }

    private boolean normalize(Vector3f v) {
        float len2 = v.x * v.x + v.y * v.y + v.z * v.z;
        if (len2 < 1e-8f) {
            return false;
        }
        float invLen = 1.0f / (float) Math.sqrt(len2);
        v.x *= invLen;
        v.y *= invLen;
        v.z *= invLen;
        return true;
    }

    public Vector3f getFogColor() {
        return fogColor;
    }

    public void setFogColor(Vector3f fogColor) {
        this.fogColor.set(fogColor);
    }

    public void addGuiTex(GuiTexture tex) {
        synchronized (lock) {
            guis.add(tex);
        }
    }

    public void removeGuiTex(GuiTexture tex) {
        synchronized (lock) {
            guis.remove(tex);
        }
    }

    public void clearGuis() {
        synchronized (lock) {
            guis.clear();
        }
    }

    public List<GuiTexture> getGuis() {
        return guis;
    }

    public DayAndNight getDayAndNight() {
        return dayAndNight;
    }

    public void setShadowRender(boolean shadowNow) {
        synchronized (lock) {
            shadowRender = shadowNow;
            if (!subsitutes.isEmpty()) {
                TexturedModel tm = subsitutes.get(0).getModel();
                if (shadowNow) {
                    if (entitieMap.get(tm) != null) {
                        //throw new RuntimeException("Subsitue of AnimatedModel TextureModel can't used by other");
                    }
                    entitieMap.put(tm, subsitutes);
                } else {
                    entitieMap.remove(tm);
                }
            }
        }
    }

    public boolean isShadowRender() {
        return shadowRender;
    }

    public Object getLock() {
        return lock;
    }

    public boolean isVolumetricFog() {
        return volumetricFog;
    }

    public void setVolumetricFog(boolean volumetricFog) {
        this.volumetricFog = volumetricFog;
    }
}
