/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;

import org.mini.g3d.core.Camera;
import org.mini.g3d.core.gltf2.loader.data.GLTFCamera;
import org.mini.g3d.core.gltf2.loader.data.GLTFPerspective;
import org.mini.g3d.core.gltf2.SimpleViewer;
import org.mini.g3d.core.vector.AABBf;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Quaternionf;
import org.mini.g3d.core.vector.Vector3f;

public class RenderCamera implements Camera {


    private float FOVY = 0.8f;
    private float Z_NEAR = 0.01f;
    private float Z_FAR = 1000f;
    private float zoomFactor = 1.04f;
    private float rotateSpeed = (float) 1 / 180;
    private boolean staticView = false;

    private float aspectRatio = ((float) SimpleViewer.WIDTH) / SimpleViewer.HEIGHT;

    private final Vector3f position = new Vector3f(0, 0, 0);
    private final Vector3f target = new Vector3f();
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Quaternionf rotation = new Quaternionf();
    private float zoom;

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

    public void setGLTFCamera(GLTFCamera camera) {
        System.out.println("Using file defined camera");
        if (camera.getType() == GLTFCamera.GLTFCameraType.PERSPECTIVE) {
            GLTFPerspective perspective = camera.getPerspective();

            //Don't set fov, without changing the window size it just looks bad
//      RenderCamera.FOVY = perspective.getYfov();
//      if (perspective.getAspectRatio() != null) {
//        aspectRatio = perspective.getAspectRatio();
//      }
            Z_NEAR = perspective.getZnear();
            Z_FAR = perspective.getZfar();
        } else {
            System.out.println("Unsupported camera type: " + camera.getType());
        }
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

    public void update() {
        if (!staticView) {
            //Calculate direction from focus to camera (assuming camera is at positive z)
            Vector3f direction = new Vector3f(0, 0, 1);
            toLocalRotation(direction);

            position.zero();
            position.translate(direction.scale(zoom));
            position.translate(target);
        }
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

    public Matrix4f getViewMatrix() {
        Matrix4f view = new Matrix4f();
        view.lookAt(getPosition(), getLookAtTarget(), up);
        return view;
    }

    public Vector3f getLookAtTarget() {
        return target;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void fitViewToScene(RenderNode rootNode) {
        AABBf sceneBounds = new AABBf();
        getSceneExtends(rootNode, sceneBounds);

        fitCameraTargetToExtends(sceneBounds);
        fitZoomToExtends(sceneBounds);
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void getSceneExtends(RenderNode rootNode, AABBf bounds) {
        for (RenderNode rn : rootNode.getChildren()) {
            bounds.union(rn.getBoundingBox());
            getSceneExtends(rn, bounds);
        }
    }

    private void fitCameraTargetToExtends(AABBf bounds) {
//    for (int i = 0; i < 3; i++) {
//      float mid = (bounds.getMax(i) + bounds.getMin(i)) / 2;
//      this.target.setComponent(i, mid);
//    }
        target.setX((bounds.getMax(0) + bounds.getMin(0)) / 2f);
        target.setY((bounds.getMax(1) + bounds.getMin(1)) / 2f);
        target.setZ((bounds.getMax(2) + bounds.getMin(2)) / 2f);
    }

    private void fitZoomToExtends(AABBf bounds) {
        float maxAxisLength = Math.max(bounds.maxX - bounds.minX, bounds.maxY - bounds.minY);

        float yfov = FOVY;
        float xfov = FOVY * this.aspectRatio;

        double yZoom = maxAxisLength / 2 / Math.tan(yfov / 2);
        double xZoom = maxAxisLength / 2 / Math.tan(xfov / 2);

        this.zoom = (float) Math.max(xZoom, yZoom);
    }

    public void setStaticView(boolean staticView) {
        this.staticView = staticView;
    }

    public void setFOVY(float fov) {
        this.FOVY = fov;
    }

}
