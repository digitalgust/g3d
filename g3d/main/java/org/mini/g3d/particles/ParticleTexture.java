package org.mini.g3d.particles;

public class ParticleTexture {

    private int textureID;
    private int numberOfRows;
    private boolean additive;
    private boolean depthTest;

    public ParticleTexture(int textureID, int numberOfRows, boolean additive, boolean depthTest) {
        this.textureID = textureID;
        this.numberOfRows = numberOfRows;
        this.additive = additive;
        this.depthTest = depthTest;
    }

    protected boolean usesAdditiveBlending() {
        return additive;
    }

    public int getTextureID() {
        return textureID;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public boolean isDepthTest() {
        return depthTest;
    }
}
