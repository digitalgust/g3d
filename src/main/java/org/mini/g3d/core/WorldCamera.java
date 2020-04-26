package org.mini.g3d.core;

import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.entity.Entity;
import org.mini.nanovg.Gutil;

public class WorldCamera implements Camera {

    private float distanceFromMaster = 15;
    private float angleAroundMaster = 0;


    private final Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f position = new Vector3f(0, 0, 0);
    private float pitch = 0.1f;
    private float yaw = 0;
    private float roll;

    private float viewW, viewH;
    private float fov, near, far;


    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f skyBoxProjectionMatrix = new Matrix4f();

    private boolean yawFollowPlayer;//target rotate Y axis effect camera if true

    EventDispatcher projectionDispatcher = new EventDispatcher();

    private Entity target;

    public WorldCamera(int width, int height, float fov, float near, float far) {
        this.viewW = width;
        this.viewH = height;
        this.fov = fov;
        this.near = near;
        this.far = far;

        createProjectionMatrix();
        createSkyboxProjectionMatrix();
    }

    public void setLookatTarget(Entity target) {
        this.target = target;
    }

    public Entity getLookatTarget() {
        return target;
    }

    public void update() {
        if (target != null) {
            calculateZoom();

            float horizontalDistance = calculateHorizontalDistance();
            float verticalDistance = calculateVerticalDistance();
            calculateCameraPosition(horizontalDistance, verticalDistance);
            this.yaw = 180 - (yawFollowPlayer ? target.getRotY() : 0f + angleAroundMaster);
        }
    }

    private void calculateCameraPosition(float horizDistance, float verticDistance) {
        float theta = yawFollowPlayer ? target.getRotY() : 0f + angleAroundMaster;
        float offsetX = (float) (horizDistance * Math.sin(Math.toRadians(theta)));
        float offsetZ = (float) (horizDistance * Math.cos(Math.toRadians(theta)));

        position.y = target.getPosition().y + verticDistance + 5;
        position.x = target.getPosition().x - offsetX;
        position.z = target.getPosition().z - offsetZ;
    }

    private float calculateHorizontalDistance() {
        return (float) (distanceFromMaster * Math.cos(Math.toRadians(pitch)));
    }

    private float calculateVerticalDistance() {
        return (float) (distanceFromMaster * Math.sin(Math.toRadians(pitch)));
    }

    public Vector3f getPosition() {
        return position;
    }

    private void calculateZoom() {
//		float zoomLevel = Mouse.getDWheel() * 0.1f;
//		distanceFromMaster -= zoomLevel;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getAngleAroundMaster() {
        return angleAroundMaster;
    }

    public void setAngleAroundMaster(float angleAroundMaster) {
        this.angleAroundMaster = angleAroundMaster;
    }

    public boolean isYawFollowPlayer() {
        return yawFollowPlayer;
    }

    public void setYawFollowPlayer(boolean follow) {
        yawFollowPlayer = follow;
    }


    public void calculatePitch(float verticalDistance) {

        //System.out.print("pitch : old = " + pitch);
        pitch = (float) Math.toDegrees(Math.asin(verticalDistance / distanceFromMaster));
        //System.out.println(" new = " + pitch + "  v/d = " + verticalDistance + " / " + distanceFromMaster);
    }


    public void calculatePitch(int x, int y) {
        float pitchChange = x * 0.1f;
        pitch -= pitchChange;
        float angleChange = y * 0.3f;
        angleAroundMaster -= angleChange;
    }

    public String toString() {
        return position.toString();
    }


    public void createProjectionMatrix() {
//        float aspectRatio = (float) EngineManager.getWidth() / (float) EngineManager.getHeight();
//        float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))) * aspectRatio);
//        float x_scale = y_scale / aspectRatio;
//        float frustum_length = FAR_PLANE - NEAR_PLANE;
//        projectionMatrix = new Matrix4f();
//        projectionMatrix.mat[Matrix4f.M00] = x_scale;
//        projectionMatrix.mat[Matrix4f.M11] = y_scale;
//        projectionMatrix.mat[Matrix4f.M22] = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
//        projectionMatrix.mat[Matrix4f.M23] = -1;
//        projectionMatrix.mat[Matrix4f.M32] = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
//        projectionMatrix.mat[Matrix4f.M33] = 0;

        float aspectRatio = viewW / viewH;
        Gutil.mat4x4_perspective(projectionMatrix.mat, fov, aspectRatio, near, far);
//        Gutil.mat4x4_ortho(projectionMatrix.mat, -1500.0f, 1500.0f, -1500.0f, 1500.0f, 0.1f, 3000.0f);
    }


    public void createSkyboxProjectionMatrix() {
        float aspectRatio = viewW / viewH;
        Gutil.mat4x4_perspective(skyBoxProjectionMatrix.mat, fov, aspectRatio, near, far * 8);
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4f getSkyBoxProjectionMatrix() {
        return skyBoxProjectionMatrix;
    }


    public EventDispatcher getProjectionDispatcher() {
        return projectionDispatcher;
    }

    public void setView(float w, float h) {
        this.viewW = w;
        this.viewH = h;
        createProjectionMatrix();
        createSkyboxProjectionMatrix();
    }


    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
        createProjectionMatrix();
        createSkyboxProjectionMatrix();
    }

    public float getNear() {
        return near;
    }

    public float getDistanceFromMaster() {
        return distanceFromMaster;
    }

    public float getFar() {
        return far;
    }

    public void setNearFar(float near, float far) {
        this.near = near;
        this.far = far;
        createProjectionMatrix();
        createSkyboxProjectionMatrix();
    }



    public Matrix4f getViewMatrix() {

        viewMatrix.identity();
        Matrix4f.rotate((float) Math.toRadians(pitch), 1, 0, 0, viewMatrix, viewMatrix);
        Matrix4f.rotate((float) Math.toRadians(yaw), 0, 1, 0, viewMatrix, viewMatrix);
        Vector3f cameraPos = getPosition();
        Vector3f negativeCameraPos = new Vector3f(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f.translate(negativeCameraPos, viewMatrix, viewMatrix);
        return viewMatrix;
    }
}