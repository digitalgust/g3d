package org.mini.g3d.particles.controler;

import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Emitter;
import org.mini.g3d.particles.EmitterControler;
import org.mini.g3d.particles.MutableLocation;

/**
 * 粒子发射器位置控制,位置是可变的，因此无法直接进行json配置
 */
public class EmitterMutableLocationControler extends EmitterControler {
    MutableLocation mutablePosition = null;
    String emitterName;


    public EmitterMutableLocationControler(MutableLocation mutablePosition, String pnodeName) {
        this.mutablePosition = mutablePosition;
        this.emitterName = pnodeName;
    }


    public boolean canApply(Emitter emitter) {
        return emitterName == null || "*".equals(emitterName) || emitterName.equals(emitter.name);
    }

    public void onStart(Emitter emi) {
        if (mutablePosition == null) return;
        //
        Vector3f loc = mutablePosition.getPosition();
        if (emi != null && loc != null) {
            emi.offsetLocation.set(loc);
        }
    }

    @Override
    public void onTeminate(Emitter emi) {

    }

    @Override
    public void onUpdate(Emitter emi) {
        onStart(emi);
    }


    public void setMutablePosition(MutableLocation mutablePosition) {
        this.mutablePosition = mutablePosition;
    }

    public MutableLocation getMutablePosition() {
        return mutablePosition;
    }

    public void setEmitterName(String emitterName) {
        this.emitterName = emitterName;
    }


}
