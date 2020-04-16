package org.mini.g3d.core.widget;

import org.mini.g3d.core.WorldCamera;
import org.mini.glfm.Glfm;
import org.mini.gui.GImage;

public class ViewMover extends Widget {

    GImage icon;


    boolean touched;
    WorldCamera camera;


    public ViewMover(String iconPath, float left, float top, float w, float h) {
        super(left, top, w, h);
        icon = GImage.createImageFromJar(iconPath);
    }


    public void setCamera(WorldCamera camera) {
        this.camera = camera;
    }

    public boolean update(long vg) {
//        float size = 48f;
//        if (icon != null) GToolkit.drawImage(vg, icon, left, top, size, size, false, 0.7f);
        return true;
    }


    public void mouseButtonEvent(int button, boolean pressed, int x, int y) {
        if (pressed) {
            if (isInArea(x, y)) touched = true;
        } else {
            touched = false;
        }

    }

    @Override
    public void touchEvent(int touchid, int phase, int x, int y) {
        if (phase == Glfm.GLFMTouchPhaseBegan) {
            if (isInArea(x, y)) touched = true;
        } else if (phase == Glfm.GLFMTouchPhaseEnded) {
            touched = false;
        }
    }


    public boolean dragEvent(float dx, float dy, float x, float y) {

        if (camera != null && touched) {
            float a = camera.getAngleAroundMaster();
            float adjx = dx * 0.5f;
            camera.setAngleAroundMaster(a - adjx);
            float pitch = camera.getPitch();
            float adjy = dy * 0.3f;
            if (pitch + adjy > 2f && pitch + adjy < 70f) {
                camera.setPitch(pitch + adjy);
            }
        }
        return true;
    }


}
