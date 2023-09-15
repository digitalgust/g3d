package org.mini.hmi.widget;

import org.mini.glfm.Glfm;
import org.mini.gui.GImage;
import org.mini.gui.GToolkit;
import org.mini.nanovg.Nanovg;

public class WButton extends Widget {

    static final float[] RED = {1.f, 0.1f, 0.1f, .8f};

    GImage icon;
    String text;
    float[] color = new float[]{1.f, 1.f, .8f, .6f};

    int touchedId = NO_TOUCHEDID;
    int redPoint = 0;
    boolean border = false;


    public WButton(String iconPath, String text, float left, float top, float w, float h) {
        super(left, top, w, h);
        icon = GToolkit.getCachedImageFromJar(iconPath);
        this.text = text;
        this.imgAlpha = .7f;
    }

    public void setImage(String path) {
        icon = GToolkit.getCachedImageFromJar(path);
    }


    @Override
    public boolean paint(long vg) {
        if (icon != null) {
            GToolkit.drawImage(vg, icon, getX(), getY(), w, h, isBorder(), imgAlpha);
        }
        if (text != null) {
            GToolkit.drawTextLineWithShadow(vg, getX() + w / 2, getY() + h, text, 14, color, Nanovg.NVG_ALIGN_CENTER | Nanovg.NVG_ALIGN_TOP, GToolkit.getStyle().getTextShadowColor(), 3f);
        }
        if (redPoint != 0) {
            GToolkit.drawCircle(vg, getX() + w - 5, getY() + 5, 3, RED, true);
        }
        return true;
    }


    @Override
    public boolean isInArea(float x, float y) {
        if (text != null) {
            if (x >= getX() && y >= getY() + getH() && x < getX() + w && y < getY() + h + 14) {
                return true;
            }
        }
        return super.isInArea(x, y);
    }


    @Override
    public boolean mouseButtonEvent(int button, boolean pressed, int x, int y) {
        if (pressed) {
            if (isInArea(x, y) && touchedId == NO_TOUCHEDID) {
                touchedId = button;
                doAction();
                return true;
            }
        } else {
            if (touchedId == button) {
                touchedId = NO_TOUCHEDID;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean touchEvent(int touchid, int phase, int x, int y) {
        if (phase == Glfm.GLFMTouchPhaseBegan) {
            if (isInArea(x, y) && touchedId == NO_TOUCHEDID) {
                touchedId = touchid;
                doAction();
                return true;
            }
        } else if (phase == Glfm.GLFMTouchPhaseEnded) {
            if (touchedId == touchid) {
                touchedId = NO_TOUCHEDID;
                return true;
            }
        }
        return false;
    }

    public void setTextColor(float r, float g, float b, float a) {
        color[0] = r;
        color[1] = g;
        color[2] = b;
        color[3] = a;
    }

    public float[] getTextColor() {
        return color;
    }

    public void setRedPoint(int i) {
        redPoint = i;
    }

    public int getRedPoint() {
        return redPoint;
    }

    public boolean isBorder() {
        return border;
    }

    public void setBorder(boolean border) {
        this.border = border;
    }


}
