/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.render;

import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.vector.AABBf;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Quaternionf;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.gl.GLMath;

public class RenderCamera implements ICamera {


    float FOVY = 45f;
    float Z_NEAR = 0.01f;
    float Z_FAR = 1000f;
    float zoomFactor = 1.04f;
    float rotateSpeed = (float) 1 / 180;
    boolean staticView = false;

    float aspectRatio = ((float) 800) / 600f;

    final Vector3f position = new Vector3f(0, 0, 0);
    final Vector3f target = new Vector3f();
    final Vector3f up = new Vector3f(0, 1, 0);
    final Quaternionf rotation = new Quaternionf();
    float zoom;

    public RenderCamera() {
    }

    public void reset() {
        FOVY = 45f;
        Z_NEAR = 0.001f;
        Z_FAR = 100f;
        position.zero();
        target.zero();
        rotation.identity();
        zoom = 0;
    }


    public void fitZoomToExtends(AABBf bounds) {
        float maxAxisLength = Math.max(bounds.maxX - bounds.minX, bounds.maxY - bounds.minY);

        float yfov = FOVY;
        float xfov = FOVY * this.aspectRatio;

        double yZoom = maxAxisLength / 2 / Math.tan(yfov / 2);
        double xZoom = maxAxisLength / 2 / Math.tan(xfov / 2);

        this.zoom = (float) Math.max(xZoom, yZoom);
    }

    public void zoom(float direction) {
        if (direction < 0) {
            this.zoom *= this.zoomFactor;
        } else {
            this.zoom /= this.zoomFactor;
        }
    }

    public void rotate(float x, float y) {
        this.rotation.x += (x * this.rotateSpeed);
        this.rotation.y += (y * this.rotateSpeed);
        float yMax = (float) (Math.PI / 2 - 0.01);
        this.rotation.y = Math.min(this.rotation.y, yMax);
        this.rotation.y = Math.max(this.rotation.y, -yMax);
    }

    float rot = 0;

    public void update() {
        //Calculate direction from focus to camera (assuming camera is at positive z)
//        Vector3f direction = new Vector3f(0, 0, 1);
//        toLocalRotation(direction);
//
//        position.zero();
//        position.translate(direction.scale(zoom));
        position.translate(target.x, target.y, target.z - 5);

    }

    private void toLocalRotation(Vector3f direction) {
        //Rotate x by y and y by x. Not sure why.
        direction.rotateX(-rotation.y);
        direction.rotateY(-rotation.x);
    }

    public Matrix4f getProjectionMatrix() {
        Matrix4f projection = new Matrix4f();

        projection.perspective(FOVY, aspectRatio, Z_NEAR, Z_FAR);

        return projection;
    }

    @Override
    public Matrix4f getSkyBoxProjectionMatrix() {
        return getProjectionMatrix();
    }

    @Override
    public void reflect(float height) {

    }

    public Matrix4f getViewMatrix() {
        Matrix4f view = new Matrix4f();
        view.lookAt(getPosition(), getLookAtTarget(), up);
        return view;
    }

    public void getViewMatrix(Matrix4f result) {
        Matrix4f view = getViewMatrix();
        GLMath.mat4x4_dup(result.mat, view.mat);
    }

    public Vector3f getLookAtTarget() {
        return target;
    }

    public void setLookAtTarget(Vector3f target) {
        this.target.set(target);
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }


    public void setStaticView(boolean staticView) {
        this.staticView = staticView;
    }

    public void setFOVY(float fov) {
        this.FOVY = fov;
    }

}
