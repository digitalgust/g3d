package g3dtest;

import g3dtest.game.GamePanel;
import g3dtest.simple.SimplePanel;
import org.mini.apploader.GApplication;
import org.mini.gui.*;
import org.mini.gui.callback.GCallBack;
import org.mini.gui.event.GSizeChangeListener;
import org.mini.layout.loader.UITemplate;
import org.mini.layout.XContainer;
import org.mini.layout.XForm;
import org.mini.layout.loader.XmlExtAssist;
import org.mini.layout.loader.XuiAppHolder;


/**
 * @author gust
 */
public class G3d extends GApplication  implements XuiAppHolder {

    GForm form;
    GMenu menu;
    SimplePanel simplePanel;
    GamePanel gamePanel;
    GameUIEventHandle eventHandle;

    @Override
    public void onInit() {

        GLanguage.setCurLang(GLanguage.ID_CHN);

        eventHandle = new GameUIEventHandle(this);

        XmlExtAssist xmlExtAssist = new XmlExtAssist(this);
        xmlExtAssist.registerGUI("g3dtest.simple.XSimplePanel");
        xmlExtAssist.registerGUI("g3dtest.game.XGamePanel");
        String xmlStr = GToolkit.readFileFromJarAsString("/res/ui/G3dForm.xml", "utf-8");
        UITemplate uit = new UITemplate(xmlStr);
        for (String key : uit.getVariable()) {
            uit.setVar(key, getString(key));
        }
        XForm xform = (XForm) XContainer.parseXml(uit.parse(), xmlExtAssist);
        xform.build(GCallBack.getInstance().getDeviceWidth(), GCallBack.getInstance().getDeviceHeight(), eventHandle);
        form = (GForm) xform.getGui();
        setForm(form);
        eventHandle.setForm(form);

        simplePanel = (SimplePanel) form.findByName("GLP_SIMPLE");
        form.remove(simplePanel);
        gamePanel = (GamePanel) form.findByName("GLP_GAME");
        form.remove(gamePanel);
        menu = (GMenu) form.findByName("MENU_MAIN");
        form.setSizeChangeListener(new GSizeChangeListener() {
            @Override
            public void onSizeChange(int width, int height) {
                ((XContainer) form.getLayout()).reSize(width, height);
                simplePanel.reSize();
            }
        });
        showGamePanel();


    }

    void showGamePanel() {
        form.remove(simplePanel);
        gamePanel.setLocation(0, (int) (form.getH() * .1));
        form.add(gamePanel);
    }

    void showSimplePanel() {
        form.remove(gamePanel);
        simplePanel.setLocation(0, (int) (form.getH() * .1));
        form.add(simplePanel);
        simplePanel.reSize();
    }

    void exit() {
        if (simplePanel != null) {

        }
        if (gamePanel != null) {
            gamePanel.exit();
        }
    }

    @Override
    public GApplication getApp() {
        return this;
    }

    @Override
    public GContainer getWebView() {
        return null;
    }
}
