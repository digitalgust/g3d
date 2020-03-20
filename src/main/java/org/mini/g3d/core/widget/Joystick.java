package org.mini.g3d.core.widget;

import org.mini.glfm.Glfm;
import org.mini.glfw.Glfw;
import org.mini.gui.GImage;
import org.mini.gui.GToolkit;

import static org.mini.glfw.Glfw.*;

public class Joystick extends Widget {

    GImage imgSlot, imgStick;


    boolean touched;
    int startX, startY;
    float curX, curY;


    float dirDegree;

    public Joystick(String slotPath, String stickPath, float left, float top, float w, float h) {
        super(left, top, w, h);
        imgSlot = GImage.createImageFromJar(slotPath);
        imgStick = GImage.createImageFromJar(stickPath);
    }


    public boolean update(long vg) {
        float bigSize = 96f;
        float smallSize = 48f;
        float bigX = getX() + w * .5f - bigSize * .5f;
        float bigY = getY() + h * .5f - bigSize * .5f;
        float smallX = getX() + w * .5f - smallSize * .5f;
        float smallY = getY() + h * .5f - smallSize * .5f;
        if (touched) {
            bigX = startX - bigSize * .5f;
            bigY = startY - bigSize * .5f;
            smallX = curX - smallSize * .5f;
            smallY = curY - smallSize * .5f;
        }
        if (imgSlot != null) {
            GToolkit.drawImage(vg, imgSlot, bigX, bigY, bigSize, bigSize, false, 0.3f);
        }
        if (imgStick != null) {
            GToolkit.drawImage(vg, imgStick, smallX, smallY, smallSize, smallSize, false, 0.3f);
        }

        return true;
    }

    public float getDirection() {
        return dirDegree;
    }

    public boolean isTouched() {
        return touched;
    }

    public boolean dragEvent(float dx, float dy, float x, float y) {
        if (y == startY || !isInArea(x, y) || !isInArea(startX, startY)) {
            return true;
        }
        touched = true;
        curX = x;
        curY = y;
        dirDegree = (float) Math.toDegrees(Math.atan((startY - y) / (x - startX)));
        if (x - startX < 0) {
            dirDegree += 180f;
        }
        //System.out.println("dir:" + dirDegree);
        return true;
    }


    public void keyEvent(int key, int scanCode, int action, int mods) {
        //System.out.println("key:" + key + " doAction:" + doAction);
        if (key == GLFW_KEY_W && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            dirDegree = 90f;
            touched = true;
        } else if (key == GLFW_KEY_S && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            dirDegree = 270f;
            touched = true;
        } else if (key == GLFW_KEY_D && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            dirDegree = 0f;
            touched = true;
        } else if (key == GLFW_KEY_A && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            dirDegree = 180f;
            touched = true;
        } else {
            touched = false;
        }
        if (touched) {
            curX = (getX() + w * .5f);
            startX = (int) curX;
            curY = (getY() + h * .5f);
            startY = (int) curY;
        }
    }


    public void mouseButtonEvent(int button, boolean pressed, int x, int y) {
        if (!pressed) {
            startX = startY = 0;
            touched = false;
            dirDegree = 0f;
        } else {
            curX = startX = x;
            curY = startY = y;
        }

    }

    public void touchEvent(int touchid, int phase, int x, int y) {
        if (phase == Glfm.GLFMTouchPhaseBegan) {
            curX = startX = x;
            curY = startY = y;

        } else if (phase == Glfm.GLFMTouchPhaseEnded) {
            touched = false;

            startX = startY = 0;
            dirDegree = 0f;
        }
    }


}
