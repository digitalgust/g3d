package org.mini.g3d.core;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.core.models.TexturedModel;
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
            SysLog.info("G3D|max light num is " + EntityShader.MAX_LIGHTS + ", exceeded " + lights.size());
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
            waters.clear();
            setTerrain(null);
            setSkybox(null);
            clearGuis();
            clearAnimatedModels();

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
                }
            }
        }
    }


    public Map<TexturedModel, List<Entity>> getEntitieMap() {
        return entitieMap;
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
