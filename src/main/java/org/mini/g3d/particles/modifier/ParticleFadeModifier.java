package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;

public class ParticleFadeModifier extends ParticleModifier {
    float startAt;
    float endAt;
    float startAlpha;
    float endAlpha;

    @Override
    public void update(Particle particle) {
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();//in second
        if (t >= startAt && t <= endAt) {
            float e = t - startAt;
            float ef = e / (endAt - startAt);
            float al = startAlpha + (endAlpha - startAlpha) * ef;
            particle.getColor().setW(al);
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

    public void setStartAlpha(float startAlpha) {
        this.startAlpha = startAlpha;
    }

    public void setEndAlpha(float endAlpha) {
        this.endAlpha = endAlpha;
    }

}
