package org.mini.g3d.particles;

import org.mini.gui.GImage;

public class ParticleTexture {

    private final GImage img;
    private int numberOfRows;
    private boolean additive;
    private boolean depthTest;

    public ParticleTexture(GImage pimg, int numberOfRows, boolean additive, boolean depthTest) {
        this.img = pimg;
        this.numberOfRows = numberOfRows;
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
        return img.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof ParticleTexture) {
            ParticleTexture other = (ParticleTexture) obj;
            return img.equals(other.img);
        }
        return false;
    }
}
