package org.mini.hmi.widget;

import org.mini.gui.GOpenGLPanel;
import org.mini.glfm.Glfm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class WidgetContainer extends Widget {
    GOpenGLPanel glPanel;
    List<Widget> widgets = new ArrayList<>();
    private final Map<Integer, TouchCapture> touchCaptures = new HashMap<>();

    private static class TouchCapture {
        final List<Widget> owners = new ArrayList<>();
        boolean consumed;
    }

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
        for (TouchCapture capture : touchCaptures.values()) {
            capture.owners.remove(widget);
        }
        widget.resetInput();
    }

    public void clear() {
        resetInput();
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

    public boolean keyEvent(int key, int scanCode, int action, int mods) {
        if (!isEnable()) return false;

        for (Widget w : widgets) {
            if (!w.isEnable()) continue;
            if (w.keyEvent(key, scanCode, action, mods)) {
                return true;
            }
        }
        return super.keyEvent(key, scanCode, action, mods);
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

        TouchCapture capture = touchCaptures.get(button);
        if (capture != null) {
            for (Widget w : new ArrayList<>(capture.owners)) {
                if (w.isEnable()) w.dragEvent(button, dx, dy, x, y);
            }
            return true;
        }

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

        if (phase == Glfm.GLFMTouchPhaseBegan) {
            TouchCapture previous = touchCaptures.remove(touchid);
            if (previous != null) {
                for (Widget w : previous.owners) {
                    w.touchEvent(touchid, Glfm.GLFMTouchPhaseCancelled, x, y);
                }
            }
            TouchCapture capture = new TouchCapture();
            touchCaptures.put(touchid, capture);
            // 按下时只命中一次；之后拖出控件也仍由原控件接收。
            for (Widget w : new ArrayList<>(widgets)) {
                if (!w.isEnable()) continue;
                boolean consumed = w.touchEvent(touchid, phase, x, y);
                // 按钮回调可能关闭、重建或禁用界面。
                if (touchCaptures.get(touchid) != capture || !widgets.contains(w) || !w.isEnable()) {
                    w.touchEvent(touchid, Glfm.GLFMTouchPhaseCancelled, x, y);
                    return true;
                }
                if (consumed || w.isTouchedBy(touchid)) capture.owners.add(w);
                if (consumed) {
                    capture.consumed = true;
                    return true;
                }
            }
            // 镜头观察触点，但仍允许场景处理轻点。
            return false;
        }

        TouchCapture capture = touchCaptures.get(touchid);
        if (capture == null) return false;
        if (phase == Glfm.GLFMTouchPhaseEnded || phase == Glfm.GLFMTouchPhaseCancelled) {
            touchCaptures.remove(touchid);
        }
        for (Widget w : new ArrayList<>(capture.owners)) {
            if (w.isEnable()) w.touchEvent(touchid, phase, x, y);
        }
        return capture.consumed;
    }

    @Override
    public void resetInput() {
        touchCaptures.clear();
        for (Widget w : new ArrayList<>(widgets)) {
            w.resetInput();
        }
    }
}
