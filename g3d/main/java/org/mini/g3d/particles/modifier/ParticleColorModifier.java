package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector4f;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;

public class ParticleColorModifier extends ParticleModifier {
    float startAt;
    float endAt;
    Vector4f startColor;
    Vector4f endColor;


    @Override
    public void update(Particle particle) {
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();//in second
        if (t >= startAt && t <= endAt) {
            float e = t - startAt;
            float ef = e / (endAt - startAt);
            float r, g, b, a;
            r = startColor.x + (endColor.x - startColor.x) * ef;
            g = startColor.y + (endColor.y - startColor.y) * ef;
            b = startColor.z + (endColor.z - startColor.z) * ef;
            a = startColor.w + (endColor.w - startColor.w) * ef;
            particle.getColor().set(r, g, b, a);
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


    public void setStartColor(Vector4f startColor) {
        this.startColor = startColor;
    }

    public void setEndColor(Vector4f endColor) {
        this.endColor = endColor;
    }

}
