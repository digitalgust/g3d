package org.mini.g3d.particles.controler;

import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Emitter;
import org.mini.g3d.particles.EmitterControler;

/**
 * 粒子发射器位置控制,叠加在json配置上的位置
 */
public class EmitterLocationControler extends EmitterControler {
    Vector3f location = new Vector3f();
    String emitterName;

    public EmitterLocationControler(Vector3f location, String pnodeName) {
        this.location.set(location);
        this.emitterName = pnodeName;
    }


    public boolean canApply(Emitter emitter) {
        return emitterName == null || "*".equals(emitterName) || emitterName.equals(emitter.name);
    }

    public void onStart(Emitter emi) {
        if (location == null) return;
        //
        if (emi != null) {
            emi.offsetLocation.set(location);
        }
    }

    @Override
    public void onTeminate(Emitter emi) {

    }

    @Override
    public void onUpdate(Emitter emi) {
        onStart(emi);
    }


    public void setLocation(Vector3f location) {
        this.location = location;
    }

    public void setEmitterName(String emitterName) {
        this.emitterName = emitterName;
    }


}
