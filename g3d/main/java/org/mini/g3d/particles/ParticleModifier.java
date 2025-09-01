package org.mini.g3d.particles;

import org.mini.g3d.particles.modifier.*;
import org.mini.json.JsonParser;

/**
 * 用来修改已生成的粒子特性
 * 可以由用户自行扩充,并把 类名作为 modifierType
 */
public class ParticleModifier implements JsonParser.Polymorphic {
    String modifierType;

    public void update(Particle particle) {
    }


    @Override
    public Class getType() {
        switch (modifierType) {
            case "Fade":
                return ParticleFadeModifier.class;
            case "Color"://change coler or alpha
                return ParticleColorModifier.class;
            case "Circle":
                return ParticleCircleModifier.class;
            case "Frame":
                return ParticleFrameModifier.class;
            case "Scale":
                return ParticleScaleModifier.class;
            case "Rotation":
                return ParticleRotationModifier.class;
            case "WeaponAura":
                return ParticleWeaponAuraModifier.class;
            default://try to parse as class name
                try {
                    return Class.forName(modifierType);
                } catch (Exception e) {
                }
        }
        throw new RuntimeException("Modifier type not found:" + modifierType);
    }

    /**
     * ======================================================
     * getter
     * ======================================================
     */


    /**
     * ======================================================
     * setter
     * ======================================================
     */

    public void setModifierType(String modifierType) {
        this.modifierType = modifierType;
    }

}
