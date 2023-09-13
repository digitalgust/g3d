package org.mini.g3d.particles.controler;

import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Effect;
import org.mini.g3d.particles.Emitter;
import org.mini.g3d.particles.EmitterControler;

/**
 * 粒子发射器位置控制,叠加在json配置上的位置
 */
public class EmitterDirectionControler extends EmitterControler {
    Vector3f direction = new Vector3f();
    String emitterName;

    public EmitterDirectionControler(Vector3f direction, String pnodeName) {
        this.direction.set(direction);
        this.direction.normalise();
        this.emitterName = pnodeName;
    }

    public boolean canApply(Emitter emitter) {
        return emitterName == null || "*".equals(emitterName) || emitterName.equals(emitter.name);
    }

    public void onStart(Emitter emi) {
        if (direction == null) return;
        if (emi != null) {
            emi.direction.set(direction);
        }
    }

    @Override
    public void onTeminate(Emitter emi) {

    }

    @Override
    public void onUpdate(Emitter emi) {

    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

}
