package org.mini.g3d.core;

import static org.mini.gl.GL.*;
import static org.mini.gl.GL.GL_BACK;

abstract public class AbstractRenderer {

    public static void enableCulling() {
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    public static void disableCulling() {
        glDisable(GL_CULL_FACE);
    }

    void prepare() {
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0, 0, 0, 1);
    }

    void finish() {

    }

    public void cleanUp() {
    }

}
