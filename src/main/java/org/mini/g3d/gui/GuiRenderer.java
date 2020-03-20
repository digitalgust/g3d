package org.mini.g3d.gui;

import org.mini.g3d.core.Loader;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;

import java.util.List;

import static org.mini.gl.GL.*;

public class GuiRenderer {

	private final RawModel quad;
	
	private GuiShader shader;
	
	public GuiRenderer(Loader loader) {
		float[] positions = {-1,1,-1,-1,1,1,1,-1};
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
		for (GuiTexture gui: guis) {
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, gui.getTexture());
			Matrix4f matrix = G3dMath.createTransformationMatrix(gui.getPosition(), gui.getScale());
			shader.loadTransformation(matrix);
			glDrawArrays(GL_TRIANGLE_STRIP, 0, quad.getVertexCount());
		}
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_BLEND);
		glDisableVertexAttribArray(0);
		glBindVertexArray(0);		
		shader.stop();
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}
	
}
