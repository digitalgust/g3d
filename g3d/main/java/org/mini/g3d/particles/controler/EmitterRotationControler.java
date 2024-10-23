package org.mini.g3d.particles.controler;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Emitter;
import org.mini.g3d.particles.EmitterControler;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;


/**
 * 粒子发射器位置控制,叠加在json配置
 * 旋转广告板的方向
 */
public class EmitterRotationControler extends EmitterControler {
    Vector3f startPitchYawRoll;
    Vector3f endPitchYawRoll;

    String emitterName;
    //
    Vector3f rotation = new Vector3f();


    public boolean canApply(Emitter emitter) {
        return emitterName == null || "*".equals(emitterName) || emitterName.equals(emitter.name);
    }

    public void onStart(Emitter emi) {
        //todo
    }

    @Override
    public void onTeminate(Emitter emi) {

    }

    @Override
    public void onUpdate(Emitter emi) {
        onStart(emi);
    }


    /**
     * ======================================================
     * setter
     * ======================================================
     */


    public void setEmitterName(String emitterName) {
        this.emitterName = emitterName;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public void setStartPitchYawRoll(Vector3f startPitchYawRoll) {
        this.startPitchYawRoll = startPitchYawRoll;
    }

    public void setEndPitchYawRoll(Vector3f endPitchYawRoll) {
        this.endPitchYawRoll = endPitchYawRoll;
    }

}
