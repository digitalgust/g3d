package org.mini.hmi.widget;

import org.mini.g3d.core.Camera;
import org.mini.glfm.Glfm;
import org.mini.gui.GImage;
import org.mini.gui.GToolkit;

public class ViewMover extends Widget {

    GImage icon;


    int touchedId;
    Camera camera;


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
            if (isInArea(x, y) && touchedId == NO_TOUCHEDID) {
                touchedId = button;
                return false;
            }
        } else {
            if (this.touchedId == button) {
                touchedId = NO_TOUCHEDID;
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean touchEvent(int touchid, int phase, int x, int y) {
        //System.out.println("mousemover touch " + phase + "," + x + "," + y);
        if (phase == Glfm.GLFMTouchPhaseBegan) {
            if (isInArea(x, y) && touchedId == NO_TOUCHEDID) {
                this.touchedId = touchid;
                return false;
            }
        } else if (phase == Glfm.GLFMTouchPhaseEnded && this.touchedId == touchid) {
            touchedId = NO_TOUCHEDID;
            return false;
        }
        return false;
    }


    @Override
    public boolean dragEvent(int button, float dx, float dy, float x, float y) {

        if (camera != null && touchedId == button) {
            //System.out.println("mousemover drag      " + dx + " , " + dy + "          ," + x + "," + y);
            float a = camera.getAngleAroundTarget();
            float adjx = dx * 0.5f;
            camera.setAngleAroundTarget(a - adjx);
            float pitch = camera.getPitch();
            float adjy = dy * 0.3f;
            float newpitch = pitch + adjy;
            if (newpitch > 2f && newpitch < 70f) {
                camera.setPitch(newpitch);
            }
            return false;
        }
        return false;
    }

}
