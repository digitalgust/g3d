package g3dtest;

import org.mini.apploader.AppManager;
import org.mini.gui.GForm;
import org.mini.gui.GObject;
import org.mini.layout.XEventHandler;

public class GameUIEventHandle extends XEventHandler {
    GForm form;
    G3d g3d;

    public GameUIEventHandle(G3d g3d) {
        this.g3d = g3d;
    }

    public void setForm(GForm form) {
        this.form = form;
    }


    public void action(GObject gobj, String cmd) {
        String name = gobj.getName();
        if ("MI_SIMPLE".equals(name)) {
            g3d.showSimplePanel();
        } else if ("MI_GAME".equals(name)) {
            g3d.showGamePanel();
        } else if ("MI_EXIT".equals(name)) {
            g3d.exit();
            AppManager.getInstance().active();
        }
    }

    public void onStateChange(GObject gobj, String cmd) {
    }
}