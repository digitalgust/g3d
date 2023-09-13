package org.mini.g3d.gui;

import org.mini.g3d.core.vector.Vector2f;

public class GuiTexture {


    private int texture;
    private int numberOfROws = 1;
    private int frameIndex = 0;
    private Vector2f position;
    private Vector2f scale;

    private Vector2f texOffsets = new Vector2f();

    /**
     *
     */
    public GuiTexture() {
    }

    public GuiTexture(int texture, int rows, int frameIndex, Vector2f position, Vector2f scale) {
        super();
        this.texture = texture;
        this.numberOfROws = rows;
        this.frameIndex = frameIndex;
        this.position = position;
        this.scale = scale;
    }


    public float getTextureXOffset() {
        int column = frameIndex % getNumberOfRows();
        return (float) column / (float) getNumberOfRows();
    }

    public float getTextureYOffset() {
        int row = frameIndex / getNumberOfRows();
        return (float) row / (float) getNumberOfRows();
    }

    public Vector2f getTextureOffset() {
        texOffsets.x = getTextureXOffset();
        texOffsets.y = getTextureYOffset();
        return texOffsets;
    }

    /**
     * ========================================
     * getter setter
     */
    public int getTexture() {
        return texture;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getScale() {
        return scale;
    }

    public int getNumberOfRows() {
        return numberOfROws;
    }

    public void setNumberOfRows(int numberOfROws) {
        this.numberOfROws = numberOfROws;
    }

    public int getNumberOfROws() {
        return numberOfROws;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setTexture(int texture) {
        this.texture = texture;
    }

    public void setNumberOfROws(int numberOfROws) {
        this.numberOfROws = numberOfROws;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public void setPosition(Vector2f position) {
        this.position = position;
    }

    public void setScale(Vector2f scale) {
        this.scale = scale;
    }

}
