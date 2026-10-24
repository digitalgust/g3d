package org.mini.g3d.particles;

import org.mini.gui.GImage;

public class ParticleTexture {

    private final GImage img;
    private final int numberOfRows;
    private final boolean additive;
    private final boolean depthTest;

    public ParticleTexture(GImage pimg, int numberOfRows, boolean additive, boolean depthTest) {
        this.img = pimg;
        this.numberOfRows = Math.max(1, numberOfRows);
        this.additive = additive;
        this.depthTest = depthTest;
    }

    protected boolean usesAdditiveBlending() {
        return additive;
    }

    public int getTextureID() {
        return img.getGLTextureId();//
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }

    public boolean isDepthTest() {
        return depthTest;
    }

    public GImage getImage() {
        return img;
    }

    public int hashCode() {
        int result = img.hashCode();
        result = 31 * result + numberOfRows;
        result = 31 * result + (additive ? 1 : 0);
        return 31 * result + (depthTest ? 1 : 0);
    }

    public boolean equals(Object obj) {
        if (obj instanceof ParticleTexture) {
            ParticleTexture other = (ParticleTexture) obj;
            return img.equals(other.img) && numberOfRows == other.numberOfRows
                    && additive == other.additive && depthTest == other.depthTest;
        }
        return false;
    }
}
