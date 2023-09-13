package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;


/**
 * 改变粒子的朝向
 * 如果粒子设置了此modifier,
 * 则其randomRotation失效,粒子也不再朝向摄像机,而是按设定的方向进行朝向.
 */
public class ParticleRotationModifier extends ParticleModifier {
    float startAt;
    float endAt;
    Vector3f startPitchYawRoll;
    Vector3f endPitchYawRoll;

    //
    Vector3f rotation = new Vector3f();

    @Override
    public void update(Particle particle) {
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();//in second
        if (t >= startAt && t <= endAt) {
            float e = t - startAt;
            float ef = e / (endAt - startAt);
            rotation.x = startPitchYawRoll.x + (endPitchYawRoll.x - startPitchYawRoll.x) * ef;
            rotation.y = startPitchYawRoll.y + (endPitchYawRoll.y - startPitchYawRoll.y) * ef;
            rotation.z = startPitchYawRoll.z + (endPitchYawRoll.z - startPitchYawRoll.z) * ef;
            particle.setRotation(rotation);
            particle.setOrientCamera(false);
        }
    }


    /**
     * ======================================================
     * setter
     * ======================================================
     */

    public void setStartAt(float startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(float endAt) {
        this.endAt = endAt;
    }

    public void setStartPitchYawRoll(Vector3f startPitchYawRoll) {
        this.startPitchYawRoll = startPitchYawRoll;
    }

    public void setEndPitchYawRoll(Vector3f endPitchYawRoll) {
        this.endPitchYawRoll = endPitchYawRoll;
    }

}
