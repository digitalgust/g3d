package org.mini.hmi.widget;

import org.mini.g3d.core.Camera;
import org.mini.glfm.Glfm;
import org.mini.gui.GImage;
import org.mini.gui.GToolkit;

/**
 * 拖动相机
 * 一个手指拖动，旋转相机
 * 两个手指拖动，缩放相机
 */
public class ViewMover extends Widget {

    GImage icon;


    int touchedId1 = NO_TOUCHEDID, touchedId2 = NO_TOUCHEDID;
    float touchedX1, touchedX2;
    float touchedY1, touchedY2;
    int zoomDirection = 0;
    Camera camera;

    float cameraDistanceFar = 30;
    float cameraDistanceNear = 5;


    public ViewMover(String iconPath, float left, float top, float w, float h) {
        super(left, top, w, h);
        icon = GToolkit.getCachedImageFromJar(iconPath);
        priority = 1;
    }


    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    @Override
    public boolean paint(long vg) {
//        float size = 48f;
//        if (icon != null) GToolkit.drawImage(vg, icon, left, top, size, size, false, 0.7f);
        return true;
    }


    @Override
    public boolean mouseButtonEvent(int button, boolean pressed, int x, int y) {
        if (pressed) {
            if (isInArea(x, y) && touchedId1 == NO_TOUCHEDID) {
                touchedId1 = button;
                return false; //可以穿透这个板到达其他层
            }
        } else {
            if (this.touchedId1 == button) {
                touchedId1 = NO_TOUCHEDID;
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean scrollEvent(float scrollX, float scrollY, float x, float y) {
        Camera cam = camera;
        if (cam == null) return false;

        float dis2tgt = cam.getDistanceFromTarget() - (scrollY * .2f);
        //SysLog.info("G3D|dis3tgt = " + dis2tgt + "  /  " + cam.getDistanceFromTarget());
        setCameraDistance(dis2tgt);
        return super.scrollEvent(scrollX, scrollY, x, y);
    }

    @Override
    public boolean touchEvent(int touchid, int phase, int x, int y) {
        //SysLog.info("G3D|mousemover touch " + phase + "," + x + "," + y);
        if (phase == Glfm.GLFMTouchPhaseBegan) {
            if (isInArea(x, y)) {
                if (touchedId1 == NO_TOUCHEDID) {
                    this.touchedId1 = touchid;
                    touchedX1 = x;
                    touchedY1 = y;
                    return false;
                } else if (touchedId2 == NO_TOUCHEDID) {
                    this.touchedId2 = touchid;
                    touchedX2 = x;
                    touchedY2 = y;
                    return false;
                }
            }
        } else if (phase == Glfm.GLFMTouchPhaseEnded) {
            if (this.touchedId1 == touchid) {
                touchedId1 = NO_TOUCHEDID;
            }
            if (this.touchedId2 == touchid) {
                touchedId2 = NO_TOUCHEDID;
            }
            return false;
        }
        return false;
    }


    @Override
    public boolean dragEvent(int button, float dx, float dy, float x, float y) {

        if (camera != null) {
            if (touchedId1 != NO_TOUCHEDID && touchedId2 != NO_TOUCHEDID) {
                //zoom
                //判断这个触点离另一个触点是在变远还是变近
                float distanceOld = (float) Math.sqrt((touchedX1 - touchedX2) * (touchedX1 - touchedX2) + (touchedY1 - touchedY2) * (touchedY1 - touchedY2));
                if (button == touchedId1) {
                    touchedX1 = x;
                    touchedY1 = y;
                } else if (button == touchedId2) {
                    touchedX2 = x;
                    touchedY2 = y;
                }
                float distanceNew = (float) Math.sqrt((touchedX1 - touchedX2) * (touchedX1 - touchedX2) + (touchedY1 - touchedY2) * (touchedY1 - touchedY2));
                //在双指缩放时，避免抖动
                if (distanceNew > distanceOld) {
                    zoomDirection++;
                    if (zoomDirection > 2) {
                        zoomDirection = 2;
                    }
                }
                if (distanceNew < distanceOld) {
                    zoomDirection--;
                    if (zoomDirection < -2) {
                        zoomDirection = -2;
                    }
                }

                if (zoomDirection > 0) {//变近
                    setCameraDistance(camera.getDistanceFromTarget() * 0.98f);
                }
                if (zoomDirection < 0) {//变远
                    setCameraDistance(camera.getDistanceFromTarget() * 1.02f);
                }
            } else {
                //rotate
                if (touchedId1 == button && touchedId2 == NO_TOUCHEDID) {
                    //SysLog.info("G3D|mousemover drag      " + dx + " , " + dy + "          ," + x + "," + y);
                    float a = camera.getAngleAroundTarget();
                    float adjx = dx * 0.5f;
                    camera.setAngleAroundTarget(a - adjx);
                    float pitch = camera.getPitch();
                    float adjy = dy * 0.3f;
                    float newpitch = pitch + adjy;
                    if (newpitch > 2f && newpitch < 70f) {
                        camera.setPitch(newpitch);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void setCameraNearFar(float cameraDistanceNear, float cameraDistanceFar) {
        this.cameraDistanceFar = cameraDistanceFar;
        this.cameraDistanceNear = cameraDistanceNear;
    }

    private void setCameraDistance(float distance) {
        if (camera != null) {
            if (distance > cameraDistanceFar) {
                distance = cameraDistanceFar;
            }
            if (distance < cameraDistanceNear) {
                distance = cameraDistanceNear;
            }
            camera.setDistanceFromTarget(distance);
        }
    }

    public float getCameraDistanceNear() {
        return cameraDistanceNear;
    }

    public float getCameraDistanceFar() {
        return cameraDistanceFar;
    }
}
