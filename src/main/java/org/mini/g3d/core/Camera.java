package org.mini.g3d.core;

import org.mini.g3d.entity.Entity;
import org.mini.g3d.core.vector.Vector3f;

public class Camera {

    private float distanceFromPlayer = 50;
    private float angleAroundPlayer = 0;

    private Vector3f position = new Vector3f(0, 0, 0);
    private float pitch = 0;
    private float yaw = 0;
    private float roll;

    private boolean yawFollowPlayer;//player rotate Y axis effect camera if true

    private Entity player;

    public Camera(Entity player) {
        this.player = player;
    }

    public void move() {
        calculateZoom();

        float horizontalDistance = calculateHorizontalDistance();
        float verticalDistance = calculateVerticalDistance();
        calculateCameraPosition(horizontalDistance, verticalDistance);
        this.yaw = 180 - (yawFollowPlayer ? player.getRotY() : 0f + angleAroundPlayer);
    }

    public Vector3f getPosition() {
        return position;
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

    public float getAngleAroundPlayer() {
        return angleAroundPlayer;
    }

    public void setAngleAroundPlayer(float angleAroundPlayer) {
        this.angleAroundPlayer = angleAroundPlayer;
    }

    public boolean isYawFollowPlayer() {
        return yawFollowPlayer;
    }

    public void setYawFollowPlayer(boolean follow) {
        yawFollowPlayer = follow;
    }

    private void calculateCameraPosition(float horizDistance, float verticDistance) {
        float theta = yawFollowPlayer ? player.getRotY() : 0f + angleAroundPlayer;
        float offsetX = (float) (horizDistance * Math.sin(Math.toRadians(theta)));
        float offsetZ = (float) (horizDistance * Math.cos(Math.toRadians(theta)));

        position.y = player.getPosition().y + verticDistance + 15;
        position.x = player.getPosition().x - offsetX;
        position.z = player.getPosition().z - offsetZ;
    }

    private float calculateHorizontalDistance() {
        return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
    }

    private float calculateVerticalDistance() {
        return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
    }

    public void calculatePitch(float verticalDistance) {

        //System.out.print("pitch : old = " + pitch);
        pitch =  (float) Math.toDegrees(Math.asin(verticalDistance / distanceFromPlayer));
        //System.out.println(" new = " + pitch + "  v/d = " + verticalDistance + " / " + distanceFromPlayer);
    }

    private void calculateZoom() {
//		float zoomLevel = Mouse.getDWheel() * 0.1f;
//		distanceFromPlayer -= zoomLevel;
    }

    public void calculatePitch(int x, int y) {
        float pitchChange = x * 0.1f;
        pitch -= pitchChange;
        float angleChange = y * 0.3f;
        angleAroundPlayer -= angleChange;
    }

    public String toString() {
        return position.toString();
    }
}
