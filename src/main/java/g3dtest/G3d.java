package g3dtest;

import g3dtest.game.GamePanel;
import g3dtest.simple.SimplePanel;
import org.mini.gui.*;
import org.mini.apploader.GApplication;
import org.mini.gui.event.GSizeChangeListener;
import org.mini.layout.UITemplate;
import org.mini.layout.XContainer;
import org.mini.layout.XForm;
import org.mini.layout.XmlExtAssist;

/**
 * @author gust
 */
public class G3d extends GApplication {

    GForm form;
    GMenu menu;
    SimplePanel simplePanel;
    GamePanel gamePanel;
    GameUIEventHandle eventHandle;

    @Override
    public GForm getForm() {
        if (form != null) {
            return form;
        }
        GLanguage.setCurLang(GLanguage.ID_CHN);

        eventHandle = new GameUIEventHandle(this);
        XmlExtAssist assist = new XmlExtAssist();

        assist.registerGUI("g3dtest.simple.XSimplePanel");
        assist.registerGUI("g3dtest.game.XGamePanel");
        String xmlStr = GToolkit.readFileFromJarAsString("/res/ui/G3dForm.xml", "utf-8");
        UITemplate uit = new UITemplate(xmlStr);
        for (String key : uit.getVariable()) {
            uit.setVar(key, GLanguage.getString(key));
        }
        XForm xform = (XForm) XContainer.parseXml(uit.parse(), assist);
        xform.build(GCallBack.getInstance().getDeviceWidth(), GCallBack.getInstance().getDeviceHeight(), eventHandle);
        form = (GForm) xform.getGui();
        eventHandle.setForm(form);

        simplePanel = (SimplePanel) form.findByName("GLP_SIMPLE");
        form.remove(simplePanel);
        gamePanel = (GamePanel) form.findByName("GLP_GAME");
        form.remove(gamePanel);
        menu = (GMenu) form.findByName("MENU_MAIN");
        form.setSizeChangeListener(new GSizeChangeListener() {
            @Override
            public void onSizeChange(int width, int height) {
                ((XContainer) form.getAttachment()).reSize(width, height);
                simplePanel.reSize();
                gamePanel.reSize();
            }
        });
        showGamePanel();
        return form;


    }

    void showGamePanel() {
        form.remove(simplePanel);
        gamePanel.setLocation(0, (int) (form.getH() * .1));
        form.add(gamePanel);
        gamePanel.reSize();
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
}
