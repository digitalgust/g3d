package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;

import java.util.Random;

public class ParticleWanderModifier extends ParticleModifier {
    float startAt;
    float endAt;
    float fluctuation = 0.5f;
    float damping = 0.5f; // default damping
    static Random random = new Random();

    @Override
    public void update(Particle particle) {
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();//in second
        if (t >= startAt && t <= endAt) {
            float dt = DisplayManager.getFrameTimeSeconds();
            float f = fluctuation * dt;
            Vector3f vel = particle.getVelocity();
            vel.x += (random.nextFloat() - 0.5f) * f;
            vel.y += (random.nextFloat() - 0.5f) * f;
            vel.z += (random.nextFloat() - 0.5f) * f;
            
            // apply damping
            if (damping > 0) {
                float d = 1.0f - damping * dt;
                if (d < 0) d = 0;
                vel.scale(d);
            }
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

    public void setFluctuation(float fluctuation) {
        this.fluctuation = fluctuation;
    }

    public void setDamping(float damping) {
        this.damping = damping;
    }

}