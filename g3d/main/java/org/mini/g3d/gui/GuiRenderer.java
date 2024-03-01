package org.mini.g3d.gui;

import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.util.Loader;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.vector.Matrix4f;

import java.util.List;

import static org.mini.gl.GL.*;

public class GuiRenderer extends AbstractRenderer {
    static final float[] positions = {-1, 1, -1, -1, 1, 1, 1, -1};

    private final RawModel quad;

    private GuiShader shader;
    private final Matrix4f matrix = new Matrix4f();
    Loader loader = new Loader();

    public GuiRenderer() {
        quad = loader.loadToVAO(positions, 2);
        shader = new GuiShader();
    }

    public void render(List<GuiTexture> guis) {
        shader.start();
        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        //render
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        synchronized (guis) {
            for (GuiTexture gui : guis) {
                glActiveTexture(GL_TEXTURE0);
                glBindTexture(GL_TEXTURE_2D, gui.getTexture());
                G3dUtil.createTransformationMatrix(gui.getPosition(), gui.getScale(), matrix);
                shader.loadTransformation(matrix);
                shader.loadNumberOfRows(gui.getNumberOfRows());
                shader.loadTexOffsets(gui.getTextureOffset());
                glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
            }
        }
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
        shader.stop();
    }

    public void cleanUp() {
    }

}
