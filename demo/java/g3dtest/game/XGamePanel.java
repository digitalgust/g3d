package g3dtest.game;

import g3dtest.simple.SimplePanel;
import org.mini.gui.GObject;
import org.mini.layout.XContainer;
import org.mini.layout.XPanel;

public class XGamePanel
        extends XPanel {

    static public final String XML_NAME = "g3dtest.game.XGamePanel";


    public XGamePanel(XContainer xc) {
        super(xc);
    }

    public String getXmlTag() {
        return XML_NAME;
    }


    protected GObject createGuiImpl() {
        return new GamePanel(getAssist().getForm(), x, y, width, height);
    }
}
