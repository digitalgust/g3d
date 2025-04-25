package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Emitter;
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
    Vector3f diff = new Vector3f();

    long frameCount;
    //

    @Override
    public void update(Particle particle) {
        if (mutableLocation == null) return;

        // 每帧都调用一次onNewFrame
        if (frameCount != DisplayManager.getFrameCount()) {
            frameCount = DisplayManager.getFrameCount();
            onNewFrame(particle.getEmitter());
        }
        // 计算出相对时间差
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();//in second
        if (t >= startAt && t <= endAt) {
            Vector3f temp = particle.getPosition();
            temp.x += diff.x;
            temp.y += diff.y;
            temp.z += diff.z;
        }
    }

    private void onNewFrame(Emitter emitter) {
        // 当新的一帧来临时，计算出相对位置差
        Vector3f newPos = mutableLocation.getPosition();
        if (oldPos == null) {
            oldPos = new Vector3f(newPos);
        }
        diff.set(newPos.x - oldPos.x, newPos.y - oldPos.y, newPos.z - oldPos.z);
        this.oldPos.set(newPos);
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
