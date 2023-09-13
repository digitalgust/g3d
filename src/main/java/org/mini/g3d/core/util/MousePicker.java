package org.mini.g3d.core.util;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.vector.Vector4f;
import org.mini.g3d.terrain.Terrain;

public class MousePicker {

    private static final int RECURSION_COUNT = 200;
    private static final float RAY_RANGE = 600;


    private Matrix4f projectionMatrix;
    private Matrix4f viewMatrix = new Matrix4f();

    private Scene scene;
    private Vector3f currentTerrainPoint;

    public MousePicker(Scene scene) {
        this.scene = scene;
    }

    public Vector3f getCurrentTerrainPoint() {
        return currentTerrainPoint;
    }


//    public void update() {
//
//        ;
//        if (intersectionInRange(0, RAY_RANGE, currentRay)) {
//            currentTerrainPoint = binarySearch(0, 0, RAY_RANGE, currentRay);
//        } else {
//            currentTerrainPoint = null;
//        }
//    }

    public Vector3f calculateMouseRay(int mouseX, int mouseY) {
        projectionMatrix = scene.getCamera().getProjectionMatrix();
        scene.getCamera().getViewMatrix(viewMatrix);
//		float mouseX = Mouse.getX();
//		float mouseY = Mouse.getY();
        Vector2f normalizedCoords = getNormalisedDeviceCoordinates(mouseX, mouseY);
        Vector4f clipCoords = new Vector4f(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
        Vector4f eyeCoords = toEyeCoords(clipCoords);
        Vector3f worldRay = toWorldCoords(eyeCoords);
        return worldRay;
    }

    private Vector3f toWorldCoords(Vector4f eyeCoords) {
        Matrix4f invertedView = Matrix4f.invert(viewMatrix, null);
        Vector4f rayWorld = Matrix4f.transform(invertedView, eyeCoords, null);
        Vector3f mouseRay = new Vector3f(rayWorld.x, rayWorld.y, rayWorld.z);
        mouseRay.normalise();
        return mouseRay;
    }

    private Vector4f toEyeCoords(Vector4f clipCoords) {
        Matrix4f invertedProjection = Matrix4f.invert(projectionMatrix, null);
        Vector4f eyeCoords = Matrix4f.transform(invertedProjection, clipCoords, null);
        return new Vector4f(eyeCoords.x, eyeCoords.y, -1f, 0f);
    }

    private Vector2f getNormalisedDeviceCoordinates(float mouseX, float mouseY) {
        float x = (2.0f * mouseX) / DisplayManager.getWidth() - 1f;
        float y = (2.0f * mouseY) / DisplayManager.getHeight() - 1f;
        return new Vector2f(x, y);
    }

    //**********************************************************
    private Vector3f getPointOnRay(Vector3f ray, float distance) {
        Vector3f camPos = scene.getCamera().getPosition();
        Vector3f start = new Vector3f(camPos.x, camPos.y, camPos.z);
        Vector3f scaledRay = new Vector3f(ray.x * distance, ray.y * distance, ray.z * distance);
        return Vector3f.add(start, scaledRay, null);
    }

    private Vector3f binarySearch(int count, float start, float finish, Vector3f ray) {
        float half = start + ((finish - start) / 2f);
        if (count >= RECURSION_COUNT) {
            Vector3f endPoint = getPointOnRay(ray, half);
            Terrain terrain = getTerrain(endPoint.getX(), endPoint.getZ());
            if (terrain != null) {
                return endPoint;
            } else {
                return null;
            }
        }
        if (intersectionInRange(start, half, ray)) {
            return binarySearch(count + 1, start, half, ray);
        } else {
            return binarySearch(count + 1, half, finish, ray);
        }
    }

    private boolean intersectionInRange(float start, float finish, Vector3f ray) {
        Vector3f startPoint = getPointOnRay(ray, start);
        Vector3f endPoint = getPointOnRay(ray, finish);
        if (!isUnderGround(startPoint) && isUnderGround(endPoint)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isUnderGround(Vector3f testPoint) {
        Terrain terrain = getTerrain(testPoint.getX(), testPoint.getZ());
        float height = 0;
        if (terrain != null) {
            height = terrain.getHeightOfTerrain(testPoint.getX(), testPoint.getZ());
        }
        if (testPoint.y < height) {
            return true;
        } else {
            return false;
        }
    }

    private Terrain getTerrain(float worldX, float worldZ) {
        return scene.getTerrain();
    }

    float mouseX, mouseY;

    public void cursorPos(int x, int y) {

        mouseX = x;
        mouseY = y;

        calculateMouseRay(x, y);
    }

}
