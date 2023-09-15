package org.mini.g3d.water;

import org.mini.g3d.core.vector.Vector3f;

public class WaterTile {

    public static final float TILE_SIZE = 20;

    private float tileSize;
    private float height;
    private float x, z;
    Vector3f waterColor = new Vector3f(0.0f, 0.3f, 0.5f);//蓝水

    public WaterTile(float centerX, float centerZ, float height, float tileSize) {
        this(centerX, centerZ, height, tileSize, null);
    }

    public WaterTile(float centerX, float centerZ, float height, float tileSize, Vector3f waterColor) {
        this.x = centerX;
        this.z = centerZ;
        this.height = height;
        this.tileSize = tileSize;
        if (waterColor != null) {
            this.waterColor = waterColor;
        }
    }

    public Vector3f getWaterColor() {
        return waterColor;
    }

    public float getHeight() {
        return height;
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public float getTileSize() {
        return tileSize;
    }

}
