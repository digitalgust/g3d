package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.MutableLocation;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;


/**
 * 改变粒子的位置
 * 如果粒子设置了此modifier,粒子位置会跟随mutableLocation而进行相对变化
 */
public class ParticleLocationModifier extends ParticleModifier {
    float startAt;
    float endAt;
    MutableLocation mutableLocation;
    Vector3f oldPos = null;

    //

    @Override
    public void update(Particle particle) {
        if (mutableLocation == null) return;

        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();//in second
        if (t >= startAt && t <= endAt) {
            if (oldPos == null) {
                oldPos = new Vector3f(mutableLocation.getPosition());
            }
            Vector3f temp = particle.getPosition();
            Vector3f newPos = mutableLocation.getPosition();
            temp.x += newPos.x - oldPos.x;
            temp.y += newPos.y - oldPos.y;
            temp.z += newPos.z - oldPos.z;
            this.oldPos.set(newPos);
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


    public void setMutableLocation(MutableLocation mutableLocation) {
        this.mutableLocation = mutableLocation;
    }
}
