package org.mini.hmi.widget;

abstract public class Widget {
    static final int NO_TOUCHEDID = -1;

    public float left, top, w, h;
    WidgetContainer parent;
    float imgAlpha = 1.f;

    int priority = 5;//default range 1-10 ,the value is more bigger is more Priority

    WidgetListener listener;
    WidgetPaintAgent widgetPaintAgent;

    boolean enable = true;

    public Widget(float left, float top, float w, float h) {
        this.left = left;
        this.top = top;
        this.w = w;
        this.h = h;
    }

    final boolean repaint(long vg) {
        paint(vg);
        if (widgetPaintAgent != null) {
            widgetPaintAgent.paint(vg, this);
        }
        return true;
    }

    abstract boolean paint(long vg);

    public boolean isInArea(float x, float y) {
        if (x >= getX() && y >= getY() && x < getX() + w && y < getY() + h) {
            return true;
        }
        return false;
    }

    public static boolean isInArea(float x, float y, float rect_x, float rect_y, float rect_w, float rect_h) {
        if (x >= rect_x && y >= rect_y && x < rect_x + rect_w && y < rect_y + rect_h) {
            return true;
        }
        return false;
    }

    public void keyEvent(int key, int scanCode, int action, int mods) {
    }

    public boolean mouseButtonEvent(int button, boolean pressed, int x, int y) {
        return false;
    }

    public boolean scrollEvent(float scrollX, float scrollY, float x, float y) {
        return false;
    }

    public boolean dragEvent(int button, float dx, float dy, float x, float y) {
        return false;
    }

    public boolean longTouchedEvent(int x, int y) {
        return false;
    }

    public boolean touchEvent(int touchid, int phase, int x, int y) {
        return false;
    }

    public void setLocation(float left, float top) {
        this.left = left;
        this.top = top;
    }

    public void setSize(float w, float h) {
        this.w = w;
        this.h = h;
    }

    public float getX() {
        return parent == null ? left : parent.getX() + left;
    }

    public float getY() {
        return parent == null ? top : parent.getY() + top;
    }

    public float getW() {
        return w;
    }

    public float getH() {
        return h;
    }

    protected void doAction() {
        if (listener != null) {
            listener.action(this);
        }
    }

    public WidgetListener getListener() {
        return listener;
    }

    public void setListener(WidgetListener listener) {
        this.listener = listener;
    }

    public void reSize(float w, float h) {

    }

    public int getPriority() {
        return priority;
    }

    public void setImgAlpha(float imgAlpha) {
        this.imgAlpha = imgAlpha;
    }

    public float getImgAlpha() {
        return imgAlpha;
    }

    public WidgetPaintAgent getWidgetPaintAgent() {
        return widgetPaintAgent;
    }

    public void setWidgetPaintAgent(WidgetPaintAgent widgetPaintAgent) {
        this.widgetPaintAgent = widgetPaintAgent;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
