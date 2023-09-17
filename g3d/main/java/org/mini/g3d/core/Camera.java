package org.mini.g3d.core;

import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.entity.Entity;
import org.mini.gl.GLMath;

public class Camera implements ICamera {
    public static final float FOV = 70;
    public static final float NEAR_PLANE = 0.1f;
    public static final float FAR_PLANE = 500;

    private float distanceFromTarget = 10; //摄像机和目标的距离
    private float angleAroundTarget = 0;
    private float heightOfLand = 0;//调整摄像机对准人之后，垂直位移


    private final Vector3f up = new Vector3f(0, 1, 0);
    private Vector3f position = new Vector3f(0, 0, 0);
    private Vector3f negativePosition = new Vector3f(0, 0, 0);
    private float pitch = 25f; //调整俯仰角度
    private float yaw = 0;
    private float roll = 0;

    private float viewW, viewH;
    private float fov, near, far;

    //防止屏幕抖动,取最近n次的主角位置,然后求平均值,可防止画面抖动,n越大抖动越小,但反应越迟滞
    //屏幕变得丝般顺滑
    static final int MAX_POS_BUF = 8;
    float[][] camPosBuf = new float[MAX_POS_BUF][3];//x,y,z
    int camPosIndx = 0;


    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f skyBoxProjectionMatrix = new Matrix4f();

    private boolean yawFollowTarget;//target rotate Y axis effect camera if true


    private Entity target;
    private Vector3f offsetToTarget = new Vector3f();

    public Camera(float width, float height, float fov, float near, float far) {
        this.viewW = width;
        this.viewH = height;
        this.fov = fov;
        this.near = near;
        this.far = far;

        createProjectionMatrix();
        createSkyboxProjectionMatrix();
    }

    public void setOffsetToTarget(float offsetX, float offsetY, float offsetZ) {
        offsetToTarget.x = offsetX;
        offsetToTarget.y = offsetY;
        offsetToTarget.z = offsetZ;
    }

    public void setOffsetToTarget(Vector3f offset) {
        setOffsetToTarget(offset.x, offset.y, offset.z);
    }

    public void setLookatTarget(Entity target) {
        this.target = target;
        //初始化镜头位置 缓冲数组
        for (int i = 0; i < MAX_POS_BUF; i++) {
            camPosBuf[i][0] = target.getPosition().x;
            camPosBuf[i][1] = target.getPosition().y;
            camPosBuf[i][2] = target.getPosition().z;
        }
    }

    public Entity getLookatTarget() {
        return target;
    }

    public void update() {

        //
        if (target != null) {
//            if (Float.isNaN(pitch) || pitch < -10) {
//                pitch = -10f;
//            }
            calculateZoom();

            float horizontalDistance = calculateHorizontalDistance();
            float verticalDistance = calculateVerticalDistance();
            calculateCameraPosition(horizontalDistance, verticalDistance);
            this.yaw = 180 - (yawFollowTarget ? target.getRotY() : 0f + angleAroundTarget);
        }

        //calc view matrix
        synchronized (viewMatrix) {
            updateViewMatrix();
        }

        //为算屏幕坐标而用
        synchronized (proj_view) {
            GLMath.mat4x4_mul(proj_view.mat, projectionMatrix.mat, viewMatrix.mat);
        }
    }


    private void calculateCameraPosition(float horizDistance, float verticDistance) {
        float theta = yawFollowTarget ? target.getRotY() : 0f + angleAroundTarget;
        float offsetX = (float) (horizDistance * Math.sin(Math.toRadians(theta)));
        float offsetZ = (float) (horizDistance * Math.cos(Math.toRadians(theta)));

        //求target位置,最近n次的平均值
        camPosBuf[camPosIndx][0] = target.getPosition().x + offsetToTarget.x;
        camPosBuf[camPosIndx][1] = target.getPosition().y + offsetToTarget.y;
        camPosBuf[camPosIndx][2] = target.getPosition().z + offsetToTarget.z;
        float dx = 0, dy = 0, dz = 0;
        for (int i = 0; i < MAX_POS_BUF; i++) {
            dx += camPosBuf[i][0];
            dy += camPosBuf[i][1];
            dz += camPosBuf[i][2];
        }
        dx /= MAX_POS_BUF;
        dy /= MAX_POS_BUF;
        dz /= MAX_POS_BUF;
        camPosIndx++;
        if (camPosIndx >= MAX_POS_BUF) camPosIndx = 0;

        //
        position.y = dy + verticDistance + heightOfLand;
        position.x = dx - offsetX;
        position.z = dz - offsetZ;
        negativePosition.x = -position.x;
        negativePosition.y = -position.y;
        negativePosition.z = -position.z;
    }

    private float calculateHorizontalDistance() {
        return (float) (distanceFromTarget * Math.cos(Math.toRadians(pitch)));
    }

    private float calculateVerticalDistance() {
        return (float) (distanceFromTarget * Math.sin(Math.toRadians(pitch)));
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getNegativePosition() {
        return negativePosition;
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

    public float getAngleAroundTarget() {
        return angleAroundTarget;
    }

    public void setAngleAroundTarget(float angleAroundTarget) {
        this.angleAroundTarget = angleAroundTarget;
    }

    public boolean isYawFollowTarget() {
        return yawFollowTarget;
    }

    public void setYawFollowTarget(boolean follow) {
        yawFollowTarget = follow;
    }


    public void calculatePitch(float verticalDistance) {

        //System.out.print("pitch : old = " + pitch);
        float v = verticalDistance / distanceFromTarget;
        v = v < 0f ? 0.1f : v;
        v = v > 1f ? 0.99f : v;
        float oldpitch = pitch;
        pitch = (float) Math.toDegrees(Math.asin(v));
        //System.out.println("pitch old: " + oldpitch + "  new: " + verticalDistance + "/" + distanceFromTarget + "=" + pitch);
    }


    public void calculatePitch(int x, int y) {
        float pitchChange = x * 0.1f;
        pitch -= pitchChange;
        float angleChange = y * 0.3f;
        angleAroundTarget -= angleChange;
    }

    public String toString() {
        return position.toString();
    }


    public void createProjectionMatrix() {
        //计算projection过程
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
        GLMath.mat4x4_perspective(projectionMatrix.mat, fov, aspectRatio, near, far);
//        GLUtil.mat4x4_ortho(projectionMatrix.mat, -1500.0f, 1500.0f, -1500.0f, 1500.0f, 0.1f, 3000.0f);
    }


    public void createSkyboxProjectionMatrix() {
        float aspectRatio = viewW / viewH;
        GLMath.mat4x4_perspective(skyBoxProjectionMatrix.mat, fov, aspectRatio, near, far * 8);
    }

    public Matrix4f getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4f getSkyBoxProjectionMatrix() {
        return skyBoxProjectionMatrix;
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

    public void setDistanceFromTarget(float distanceFromTarget) {
        this.distanceFromTarget = distanceFromTarget;
    }

    public float getDistanceFromTarget() {
        return distanceFromTarget;
    }

    public float getHeightOfLand() {
        return heightOfLand;
    }

    public void setHeightOfLand(float heightOfLand) {
        this.heightOfLand = heightOfLand;
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

    @Override
    public void reflect(float height) {
        this.pitch = -pitch;
        this.position.y = position.y - 2 * (position.y - height);
        updateViewMatrix();
    }


    private void updateViewMatrix() {
        viewMatrix.identity();
        Matrix4f.rotate((float) Math.toRadians(pitch), 1, 0, 0, viewMatrix, viewMatrix);
        Matrix4f.rotate((float) Math.toRadians(yaw), 0, 1, 0, viewMatrix, viewMatrix);
        Matrix4f.rotate((float) Math.toRadians(roll), 0, 0, 1, viewMatrix, viewMatrix);
        Matrix4f.translate(negativePosition, viewMatrix, viewMatrix);
    }

    /**
     * 不要修改此viewMatrix, 会造成其他使用此矩阵程序错误,
     * 如果需要修改viewMatrix, 使用 getViewMatrix(Matrix4f result);
     *
     * @return
     */
    public Matrix4f getViewMatrix() {
        synchronized (viewMatrix) {
            return viewMatrix;
        }
    }

    public void getViewMatrix(Matrix4f result) {
        if (result == null) return;
        GLMath.mat4x4_dup(result.mat, viewMatrix.mat);
    }


    /**
     * ===========================================
     * 计算3D到2D
     */

    Matrix4f proj_view = new Matrix4f();
    float[] _result = {0, 0, 0, 1};
    float[] _result1 = {0, 0, 0, 1};
    float[] _pos = {0, 0, 0, 1};


    public Vector2f world2screen(Vector3f pos3d, Vector2f pos2d) {
        return world2screen(pos3d.x, pos3d.y, pos3d.z, pos2d);
    }

    public Vector2f world2screen(float posX, float posY, float posZ, Vector2f pos2d) {
        if (pos2d == null) {
            pos2d = new Vector2f();
        }
        synchronized (proj_view) {
            _pos[0] = posX;
            _pos[1] = posY;
            _pos[2] = posZ;
            _pos[3] = 1.f;
            GLMath.mat4x4_mul_vec4(_result, proj_view.mat, _pos);
            GLMath.vec_normal(_result1, _result);

            //之前未除以w，导致其坐标不正确
            if (_result1[3] < 0) {
                pos2d.x = -10000f;
                pos2d.y = -10000f;
            } else {
                pos2d.x = (_result1[0] / _result1[3] + 1.f) / 2.f * viewW;
                pos2d.y = -1 * (_result1[1] / _result1[3] - 1.f) / 2 * viewH;
            }
        }
        return pos2d;
    }
}