package org.mini.g3d.core.widget;

import org.mini.glfm.Glfm;
import org.mini.gui.GImage;
import org.mini.gui.GToolkit;

public class WButton extends Widget {

    GImage icon;


    boolean touched;


    public WButton(String iconPath, float left, float top, float w, float h) {
        super(left, top, w, h);
        icon = GImage.createImageFromJar(iconPath);
    }


    public boolean update(long vg) {
        float size = 48f;
        if (icon != null) GToolkit.drawImage(vg, icon, getX(), getY(), size, size, false, 0.7f);
        return true;
    }


    public void mouseButtonEvent(int button, boolean pressed, int x, int y) {
        if (pressed) {
            if (isInArea(x, y)) {
                touched = true;
                doAction();
            }
        } else {
            touched = false;
        }

    }

    @Override
    public void touchEvent(int touchid, int phase, int x, int y) {
        if (phase == Glfm.GLFMTouchPhaseBegan) {
            if (isInArea(x, y)) {
                touched = true;
                doAction();
            }
        } else if (phase == Glfm.GLFMTouchPhaseEnded) {
            touched = false;
        }
    }


}
