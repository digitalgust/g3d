package org.mini.g3d.gui;

import org.mini.g3d.core.vector.Vector2f;
import org.mini.gui.GImage;
import org.mini.gui.GToolkit;

import java.util.List;

/**
 * 生成一个字符串图片
 * 原理:
 * 用guirenderer往fbo上画一串数字
 * 再把fbo贴到一个particle上
 */
public class BitmapFont {

    static final int GUI_FBO_W = 512, GUI_FBO_H = 512;
    static final int ROWS_OF_GUI_FBO = 8;

    GImage numberImg;
    int rowsOfNumberTex;
    int assciiStartPos;
    static final int MAX_DIGIT_OF_NUMBER = 8;
    //means row*row grid per fbo(1.0f), 8 digit per grid
    static final float DIGIT_WIDTH = 1.0f / ROWS_OF_GUI_FBO / MAX_DIGIT_OF_NUMBER;
    static final float OFFSET_X_GRID = 1.0f / ROWS_OF_GUI_FBO / 2;
    static final float OFFSET_Y_GRID = 1.0f / ROWS_OF_GUI_FBO / 2;
    static final Vector2f scale = new Vector2f(DIGIT_WIDTH * 1.5f, DIGIT_WIDTH * 2);

    DynTextureGenerator dynTexture;

    /**
     * @param fontImgPath
     * @param rowsOfFontImage
     */
    public BitmapFont(String fontImgPath, int rowsOfFontImage, int assciiStartPos) {
        numberImg = GToolkit.getCachedImageFromJar(fontImgPath);
        this.rowsOfNumberTex = rowsOfFontImage;
        this.assciiStartPos = assciiStartPos;

    }

    public GImage getFboImage() {
        return dynTexture.getTextureImage();
    }

    public int getRowsOfFbo() {
        return ROWS_OF_GUI_FBO;
    }


    public int printString(String s, float[] color, long timeOut) {
        int slot = dynTexture.findSlot();
        if (slot >= 0) {
            dynTexture.setSlotTimeOut(slot, timeOut);

            List<GuiTexture> list = dynTexture.getSlot(slot);

            float posOfFboX = (slot % ROWS_OF_GUI_FBO) / (float) ROWS_OF_GUI_FBO;
            float posOfFboY = (slot / ROWS_OF_GUI_FBO) / (float) ROWS_OF_GUI_FBO;
            posOfFboY += OFFSET_Y_GRID;


            GuiTexture t;
            int frameIndex;
            Vector2f pos;

            float leftSpace = (MAX_DIGIT_OF_NUMBER - s.length()) / 2 * DIGIT_WIDTH;

            //add digit
            for (int i = 0, imax = s.length(); i < imax; i++) {
                char ch = s.charAt(i);
                pos = new Vector2f();
                pos.x = (posOfFboX + leftSpace + i * DIGIT_WIDTH) * 2 - 1f;
                pos.y = (posOfFboY) * 2 - 1f;
                frameIndex = ch - assciiStartPos;
                t = new GuiTexture(numberImg.getGLTextureId(), rowsOfNumberTex, frameIndex, pos, scale);
                list.add(t);
            }
        }
        return slot;
    }


    public void gl_init() {

        dynTexture = new DynTextureGenerator(GUI_FBO_W, GUI_FBO_H, ROWS_OF_GUI_FBO);
        dynTexture.gl_init();
    }


    public void gl_paint() {
        dynTexture.renderToFbo();
    }
}
