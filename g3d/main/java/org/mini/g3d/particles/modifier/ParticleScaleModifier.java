package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;

public class ParticleScaleModifier extends ParticleModifier {
    float startAt;
    float endAt;
    float startScale;
    float endScale;

    @Override
    public void update(Particle particle) {
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();//in second
        if (t >= startAt && t <= endAt) {
            float e = t - startAt;
            float ef = e / (endAt - startAt);
            float al = startScale + (endScale - startScale) * ef;
            particle.setScale(al);
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

    public void setStartScale(float startScale) {
        this.startScale = startScale;
    }

    public void setEndScale(float endScale) {
        this.endScale = endScale;
    }

}
