package org.mini.hmi.widget;

import org.mini.glfm.Glfm;
import org.mini.glfw.Glfw;
import org.mini.gui.GImage;
import org.mini.gui.GToolkit;

import static org.mini.glfw.Glfw.*;

public class Joystick extends Widget {
//    static int LEFT = 150, TOP = 150;

    float bigSize;
    float smallSize;

    GImage imgSlot, imgStick;

    int touchedId = NO_TOUCHEDID;
    //int startX, startY;
    float curX, curY;
    float bigX, bigY;
    JoystickListener joystickListener;


    float dirDegree;

    public Joystick(String slotPath, String stickPath, float left, float top, float w, float h) {
        super(left, top, w, h);
        imgSlot = GToolkit.getCachedImageFromJar(slotPath);
        imgStick = GToolkit.getCachedImageFromJar(stickPath);
        priority = 10;
        imgAlpha = .3f;

        if (w < h) {
            bigSize = w * .65f;
            smallSize = bigSize / 2;
        } else {
            bigSize = h * .65f;
            smallSize = bigSize / 2;
        }
        bigX = getX() + w * .5f;
        bigY = getY() + h * .5f;
    }


    @Override
    public boolean paint(long vg) {
        float bigX = getX() + w * .5f - bigSize * .5f;
        float bigY = getY() + h * .5f - bigSize * .5f;
        float smallX, smallY;
        if (touchedId != NO_TOUCHEDID) {
            smallX = curX - smallSize * .5f;
            smallY = curY - smallSize * .5f;
        } else {
            smallX = getX() + w * .5f - smallSize * .5f;
            smallY = getY() + h * .5f - smallSize * .5f;
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
        return touchedId != NO_TOUCHEDID;
    }

    @Override
    public boolean dragEvent(int button, float dx, float dy, float x, float y) {
        if (button == this.touchedId) {
            curX = x;
            curY = y;
            calcDegree();
            //System.out.println("joystick:" + ((int) dx) + "," + (int) dy + "," + (int) x + "," + (int) y);
            callListener(JoystickListener.OPERATION_MOVE, x, y, dirDegree);
            return true;
        }
        return false;
    }

    private void calcDegree() {
        if (curX == bigX) return;
        dirDegree = (float) Math.toDegrees(Math.atan((bigY - curY) / (curX - bigX)));
        if (curX - bigX < 0) {
            dirDegree += 180f;
        }
    }

    private void callListener(int operation, float x, float y, float degree) {
        if (joystickListener != null) joystickListener.onMove(operation, x, y, degree);
    }


    @Override
    public void keyEvent(int key, int scanCode, int action, int mods) {
        //System.out.println("key:" + key + " doAction:" + doAction);
        float dx = 0, dy = 0;
        if (key == GLFW_KEY_W && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            dirDegree = 90f;
            dy = -50;
            touchedId = GLFW_MOUSE_BUTTON_4;
        } else if (key == GLFW_KEY_S && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            dirDegree = 270f;
            dy = 50;
            touchedId = GLFW_MOUSE_BUTTON_4;
        } else if (key == GLFW_KEY_D && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            dirDegree = 0f;
            dx = 50;
            touchedId = GLFW_MOUSE_BUTTON_4;
        } else if (key == GLFW_KEY_A && (action == GLFW_PRESS || action == Glfw.GLFW_REPEAT)) {
            dirDegree = 180f;
            dx = -50;
            touchedId = GLFW_MOUSE_BUTTON_4;
        } else {
            dirDegree = 0f;
            touchedId = NO_TOUCHEDID;
        }
        if (touchedId == GLFW_MOUSE_BUTTON_4) {
            float bigX = getX() + w * .5f;
            float bigY = getY() + h * .5f;
            curX = (bigX + dx);
            curY = (bigY + dy);
        }
        if (action == GLFW_PRESS) {
            callListener(JoystickListener.OPERATION_PRESS, curX, curY, dirDegree);
        } else if (action == Glfw.GLFW_REPEAT) {
            callListener(JoystickListener.OPERATION_MOVE, curX, curY, dirDegree);
        } else {
            callListener(JoystickListener.OPERATION_RELEASE, curX, curY, dirDegree);
        }
    }


    @Override
    public boolean mouseButtonEvent(int button, boolean pressed, int x, int y) {
        if (pressed) {
            if (isInFeelingArea(x, y) && this.touchedId == NO_TOUCHEDID) {
                curX = x;
                curY = y;
                this.touchedId = button;
                calcDegree();
                callListener(JoystickListener.OPERATION_PRESS, x, y, dirDegree);
                return true;
            }
        } else {
            if (this.touchedId == button) {
                touchedId = NO_TOUCHEDID;
                dirDegree = 0f;
                callListener(JoystickListener.OPERATION_RELEASE, x, y, dirDegree);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchEvent(int touchid, int phase, int x, int y) {
        if (phase == Glfm.GLFMTouchPhaseBegan) {
            if (isInFeelingArea(x, y) && touchedId == NO_TOUCHEDID) {
                curX = x;
                curY = y;
                this.touchedId = touchid;
                calcDegree();
                callListener(JoystickListener.OPERATION_PRESS, x, y, dirDegree);
                return true;
            }
        } else if (phase == Glfm.GLFMTouchPhaseEnded) {
            if (touchedId == touchid) {
                touchedId = NO_TOUCHEDID;
                dirDegree = 0f;
                callListener(JoystickListener.OPERATION_RELEASE, x, y, dirDegree);
                return true;
            }
        }
        return false;
    }

    private boolean isInFeelingArea(float x, float y) {
        if (isInArea(x, y, getX(), getY(), w, h)) {
            return true;
        }
        return false;
    }

    public void reset() {
        touchedId = NO_TOUCHEDID;
    }


    public JoystickListener getJoystickListener() {
        return joystickListener;
    }

    public void setJoystickListener(JoystickListener joystickListener) {
        this.joystickListener = joystickListener;
    }

}
