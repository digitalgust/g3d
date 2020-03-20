package org.mini.g3d.core.widget;

import org.mini.gui.GOpenGLPanel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class WidgetContainer extends Widget {
    GOpenGLPanel glPanel;
    List<Widget> widgets = new CopyOnWriteArrayList<>();

    public WidgetContainer(GOpenGLPanel glPanel, float x, float y, float w, float h) {
        super(x, y, w, h);
        this.glPanel = glPanel;
    }

    public void add(Widget widget) {
        widgets.add(widget);
        widget.parent = this;
    }

    public boolean update(long vg) {
        for (Widget w : widgets) {
            w.update(vg);
        }
        return true;
    }

    public void reSize(float w, float h) {
        for (Widget wg : widgets) {
            wg.reSize(w, h);
        }
    }


    public void keyEvent(int key, int scanCode, int action, int mods) {
        for (Widget w : widgets) {
            w.keyEvent(key, scanCode, action, mods);
        }
    }

    public void mouseButtonEvent(int button, boolean pressed, int x, int y) {
        for (Widget w : widgets) {
            w.mouseButtonEvent(button, pressed, x, y);
        }
    }

    public boolean dragEvent(float dx, float dy, float x, float y) {
        for (Widget w : widgets) {
            w.dragEvent(dx, dy, x, y);
        }
        return true;
    }

    public void longTouchedEvent(int x, int y) {
        for (Widget w : widgets) {
            w.longTouchedEvent(x, y);
        }
    }

    public void touchEvent(int touchid, int phase, int x, int y) {
        for (Widget w : widgets) {
            w.touchEvent(touchid, phase, x, y);
        }
    }
}
