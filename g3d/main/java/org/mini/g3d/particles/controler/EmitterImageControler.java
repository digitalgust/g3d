package org.mini.g3d.particles.controler;

import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Effect;
import org.mini.g3d.particles.Emitter;
import org.mini.g3d.particles.EmitterControler;
import org.mini.gui.GImage;

/**
 * 粒子发射器图象控制器
 */
public class EmitterImageControler extends EmitterControler {
    String emitterName;

    GImage texture;
    int rows;
    int frameIndex;
    boolean additive;
    boolean depthTest;

    public EmitterImageControler(GImage texture, boolean additive, boolean depthTest, int rows, int frameIndex, String pnodeName) {
        this.texture = texture;
        this.rows = rows;
        this.frameIndex = frameIndex;
        this.additive = additive;
        this.depthTest = depthTest;
        emitterName = pnodeName;
    }

    public boolean canApply(Emitter emitter) {
        return emitterName == null || "*".equals(emitterName) || emitterName.equals(emitter.name);
    }

    public void onStart(Emitter emi) {
        if (texture == null) return;
        if (emi != null) {
            emi.setImageGrids(rows);
            emi.setImg(texture, additive, depthTest);
            emi.setFrameIndex(frameIndex);
        }
    }

    @Override
    public void onTeminate(Emitter emi) {

    }

    @Override
    public void onUpdate(Emitter emi) {

    }


    public GImage getTexture() {
        return texture;
    }

    public int getRows() {
        return rows;
    }

    public int getFrameIndex() {
        return frameIndex;
    }
}
