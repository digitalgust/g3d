package org.mini.g3d.animation.gltf2.render;

import org.mini.g3d.animation.gltf2.loader.data.GLTFCamera;
import org.mini.g3d.animation.gltf2.loader.data.GLTFPerspective;
import org.mini.g3d.core.vector.AABBf;
import org.mini.util.SysLog;

public class GLTFRenderCamera extends RenderCamera {


    public void setGLTFCamera(GLTFCamera camera) {
        SysLog.info("G3D|Using file defined camera");
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
            SysLog.warn("G3D|Unsupported camera type: " + camera.getType());
        }
    }


    public void fitViewToScene(RenderNode rootNode) {
        AABBf sceneBounds = new AABBf();
        getSceneExtends(rootNode, sceneBounds);

        fitCameraTargetToExtends(sceneBounds);
        fitZoomToExtends(sceneBounds);
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


}
