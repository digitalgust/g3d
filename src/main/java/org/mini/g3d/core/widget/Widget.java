package org.mini.g3d.core.widget;

abstract public class Widget {
    public float left, top, w, h;
    WidgetContainer parent;

    WidgetListener listener;

    public Widget(float left, float top, float w, float h) {
        this.left = left;
        this.top = top;
        this.w = w;
        this.h = h;
    }

    abstract boolean update(long vg);

    public boolean isInArea(float x, float y) {
        if (x >= getX() && y >= getY() && x < getX() + w && y < getY() + h) {
            return true;
        }
        return false;
    }

    public void keyEvent(int key, int scanCode, int action, int mods) {
    }

    public void mouseButtonEvent(int button, boolean pressed, int x, int y) {
    }

    public boolean dragEvent(float dx, float dy, float x, float y) {
        return false;
    }

    public void longTouchedEvent(int x, int y) {
    }

    public void touchEvent(int touchid, int phase, int x, int y) {
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

}
