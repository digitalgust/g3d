package org.mini.hmi.widget;

import org.mini.gui.GOpenGLPanel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WidgetContainer extends Widget {
    GOpenGLPanel glPanel;
    List<Widget> widgets = new ArrayList<>();

    public WidgetContainer(GOpenGLPanel glPanel, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.glPanel = glPanel;
    }

    public void add(Widget widget) {
        widgets.add(widget);
        widget.parent = this;

        Collections.sort(widgets, new Comparator<Widget>() {
            @Override
            public int compare(Widget w1, Widget w2) {
                return w2.getPriority() - w1.getPriority();
            }
        });
    }

    public void remove(Widget widget) {
        widgets.remove(widget);
    }

    public void clear() {
        widgets.clear();
    }

    public List<Widget> getWidgets() {
        return widgets;
    }

    public boolean paint(long vg) {
        for (Widget w : widgets) {
            w.repaint(vg);
        }
        return true;
    }

    public void reSize(float w, float h) {
        for (Widget wg : widgets) {
            wg.reSize(w, h);
        }
    }

    public float getX() {
        return glPanel.getX() + left;
    }

    public float getY() {
        return glPanel.getY() + top;
    }

    public void keyEvent(int key, int scanCode, int action, int mods) {
        if (!isEnable()) return;

        for (Widget w : widgets) {
            if (!w.isEnable()) continue;
            w.keyEvent(key, scanCode, action, mods);
        }
    }

    public boolean mouseButtonEvent(int button, boolean pressed, int x, int y) {
        if (!isEnable()) return false;

        for (Widget w : widgets) {
            if (!w.isEnable()) continue;
            if (w.mouseButtonEvent(button, pressed, x, y)) {
                return true;
            }
        }
        return false;
    }

    public boolean scrollEvent(float scrollX, float scrollY, float x, float y) {
        if (!isEnable()) return false;

        for (Widget w : widgets) {
            if (!w.isEnable()) continue;
            if (w.scrollEvent(scrollX, scrollY, x, y)) {
                return true;
            }
        }
        return false;
    }

    public boolean dragEvent(int button, float dx, float dy, float x, float y) {
        if (!isEnable()) return false;

        for (Widget w : widgets) {
            if (!w.isEnable()) continue;
            if (w.dragEvent(button, dx, dy, x, y)) return true;
        }
        return true;
    }

    public boolean longTouchedEvent(int x, int y) {
        if (!isEnable()) return false;

        for (Widget w : widgets) {
            if (!w.isEnable()) continue;
            if (w.longTouchedEvent(x, y)) return true;
        }
        return false;
    }

    public boolean touchEvent(int touchid, int phase, int x, int y) {
        if (!isEnable()) return false;

        for (Widget w : widgets) {
            if (!w.isEnable()) continue;
            if (w.touchEvent(touchid, phase, x, y)) return true;
        }
        return false;
    }
}
