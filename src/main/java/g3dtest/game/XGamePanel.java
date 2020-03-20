package g3dtest.game;

import org.mini.layout.XPanel;

public class XGamePanel
        extends XPanel {

    static public final String XML_NAME = "g3dtest.game.XGamePanel";


    public XGamePanel() {
        super();
    }

    public String getXmlTag() {
        return XML_NAME;
    }

    protected void createGui() {
        if (panel == null) {
            panel = new GamePanel(x, y, width, height);
            panel.setName(name);
            panel.setAttachment(this);
        } else {
            panel.setLocation(x, y);
            panel.setSize(width, height);
        }
    }
}
