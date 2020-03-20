package g3dtest;

import g3dtest.game.GamePanel;
import g3dtest.simple.SimplePanel;
import org.mini.gui.*;
import org.mini.gui.event.GSizeChangeListener;
import org.mini.layout.UITemplate;
import org.mini.layout.XContainer;
import org.mini.layout.XForm;


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

        XContainer.registerGUI("g3dtest.simple.XSimplePanel");
        XContainer.registerGUI("g3dtest.game.XGamePanel");
        String xmlStr = GToolkit.readFileFromJarAsString("/res/ui/G3dForm.xml", "utf-8");
        UITemplate uit = new UITemplate(xmlStr);
        for (String key : uit.getVariable()) {
            uit.setVar(key, GLanguage.getString(key));
        }
        XContainer xc = new XForm(null);
        xc.parseXml(uit.parse());
        xc.build((int) GCallBack.getInstance().getDeviceWidth(), (int) GCallBack.getInstance().getDeviceHeight(), eventHandle);
        form = (GForm) xc.getGui();
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
        return form;


//        if (form != null) {
//            return form;
//        }
//        GLanguage.setCurLang(GLanguage.ID_CHN);
//        form = new GForm();
//
//        GCallBack.getInstance().setFps(30f);
//
//        int menuH = 80;
//        GImage img = GImage.createImageFromJar("/res/hello.png");
//        menu = new GMenu(0, form.getDeviceHeight() - menuH, form.getDeviceWidth(), menuH);
//        menu.setFixed(true);
//        GMenuItem item = menu.addItem("Test", img);
//        item.setActionListener(new GActionListener() {
//            @Override
//            public void action(GObject gobj) {
//                if (game != null) form.remove(game);
//                if (test == null) test = new SimplePanel(0, 0, form.getDeviceWidth(), form.getDeviceHeight() - menuH);
//                form.add(test);
//            }
//        });
//        GMenuItem item1 = menu.addItem("GamePanel", img);
//        item1.setActionListener(new GActionListener() {
//            @Override
//            public void action(GObject gobj) {
//                if (test != null) form.remove(test);
//                if (game == null) game = new GamePanel(0, 0, form.getDeviceWidth(), form.getDeviceHeight() - menuH);
//                form.add(game);
//            }
//        });
//
//        img = GImage.createImageFromJar("/res/appmgr.png");
//        item = menu.addItem("Exit", img);
//        item.setActionListener(new GActionListener() {
//            @Override
//            public void action(GObject gobj) {
//                AppManager.getInstance().active();
//            }
//        });
//
//        form.add(menu);
//        return form;
    }

    void showGamePanel() {
        form.remove(simplePanel);
        gamePanel.setLocation(0, (int)(form.getH()*.1));
        form.add(gamePanel);
        gamePanel.reSize();
    }

    void showSimplePanel() {
        form.remove(gamePanel);
        simplePanel.setLocation(0, (int)(form.getH()*.1));
        form.add(simplePanel);
        simplePanel.reSize();
    }
}
