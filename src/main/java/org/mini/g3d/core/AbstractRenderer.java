package org.mini.g3d.core;

import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.terrain.Terrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract public class AbstractRenderer {


    Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
    List<Terrain> terrains = new ArrayList<Terrain>();


    public void processTerrain(Terrain terrain) {
        terrains.add(terrain);
    }

    public void processEntity(Entity entity) {
        TexturedModel entityModel = entity.getModel();
        if (entityModel == null) {
            new Throwable().printStackTrace();
        }
        List<Entity> batch = entities.get(entityModel);

        if (batch != null) {
            batch.add(entity);
        } else {
            List<Entity> newBatch = new ArrayList<Entity>();
            newBatch.add(entity);
            entities.put(entityModel, newBatch);
        }

    }

    public List<Terrain> getTerrains() {
        return terrains;
    }

    public Map<TexturedModel, List<Entity>> getEntities() {
        return entities;
    }

    public void clear() {
        terrains.clear();
        entities.clear();
    }

}
