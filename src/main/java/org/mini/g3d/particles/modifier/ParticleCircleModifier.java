package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;

public class ParticleCircleModifier extends ParticleModifier {

    static Vector3f vecI = new Vector3f(0, 1, 0);
    static Vector3f vecJ = new Vector3f(1, 0, 0);

    float startAt;
    float endAt;
    float radius;//半径
    float radiusChange;//半径变化
    Vector3f normal;//圆平面过圆心法线
    float angularVelocity;//一圈每秒  2PI/S=6.28/S
    float initAngular; //粒子初始角度

    @Override
    public void update(Particle particle) {
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();
        if (t >= startAt && t <= endAt) {
            float e = t - startAt;
            float theta = (float) (angularVelocity * (e * 2 * Math.PI) + initAngular);
            Vector3f center = particle.getEmitter().getEmitLocation();//圆心
            Vector3f pos = calcPos(center, theta, e);
            pos.y += particle.getGravityY();
            particle.getPosition().set(pos);
        }
    }


    /**
     * @param center 圆心
     * @param theta  转动的弧度
     * @return
     */
    Vector3f calcPos(Vector3f center, float theta, float time) {
        Vector3f a = Vector3f.cross(normal, vecI, null);
        if (a.x == 0 || a.y == 0 || a.z == 0) {
            a = Vector3f.cross(normal, vecJ, null);
        }

        Vector3f b = Vector3f.cross(normal, a, null);
        a = a.normalise(null);
        b = b.normalise(null);


//        for (int i = 0; i < 100; i++) { //转360度,圆圈上各点的位置
//            float theta = (float) (i * (2 * Math.PI / 100));

        float c1 = center.x;
        float c2 = center.y;
        float c3 = center.z;

        float r = radius + time * radiusChange;

        float x = (float) (c1 + r * a.x * Math.cos(theta) + r * b.x * Math.sin(theta));
        float y = (float) (c2 + r * a.y * Math.cos(theta) + r * b.y * Math.sin(theta));
        float z = (float) (c3 + r * a.z * Math.cos(theta) + r * b.z * Math.sin(theta));

//            System.out.println(x + "\t" + y + "\t" + z);
//        }
        return new Vector3f(x, y, z);
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

    public void setNormal(Vector3f normal) {
        this.normal = normal;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public void setInitAngular(float initAngular) {
        this.initAngular = initAngular;
    }

    public void setRadiusChange(float radiusChange) {
        this.radiusChange = radiusChange;
    }


}
