package g3dtest.simple;

import org.mini.layout.XPanel;

public class XSimplePanel
        extends XPanel {

    static public final String XML_NAME = "g3dtest.simple.XSimplePanel";


    public XSimplePanel() {
        super();
    }

    public String getXmlTag() {
        return XML_NAME;
    }


    protected void createGui() {
        if (panel == null) {
            panel = new SimplePanel(x, y, width, height);
            panel.setName(name);
            panel.setAttachment(this);
        } else {
            panel.setLocation(x, y);
            panel.setSize(width, height);
        }
    }
}
