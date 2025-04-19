package org.mini.g3d.particles;


import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.util.G3dUtil;

import java.util.*;
import java.util.Map.Entry;

public class ParticleMaster {

    private static Map<ParticleTexture, List<Particle>> particles = new HashMap<ParticleTexture, List<Particle>>();


    public static void update(ICamera camera) {
        //update particle
        try {

            Iterator<Entry<ParticleTexture, List<Particle>>> mapIterator = particles.entrySet().iterator();
            while (mapIterator.hasNext()) {
                Entry<ParticleTexture, List<Particle>> entry = mapIterator.next();
                List<Particle> list = entry.getValue();
                Iterator<Particle> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Particle p = iterator.next();
                    boolean stillAlive = p.update(camera);
                    if (!stillAlive) {
                        iterator.remove();
                        if (list.isEmpty()) {
                            mapIterator.remove();
                            G3dUtil.putCachedList(list);
                        }
                    }
                }
                if (!entry.getKey().usesAdditiveBlending()) {
                    InsertionSort.sortHighToLow(list);
                }
            }
        } catch (Exception e) {
        }
    }

    public static void cleanUp() {

    }

    public static Map<ParticleTexture, List<Particle>> getParticles() {
        return particles;
    }

    public static void addParticle(Particle particle) {
        List<Particle> list = particles.get(particle.getTexture());
        if (list == null) {
            list = G3dUtil.getCachedList();
            particles.put(particle.getTexture(), list);
        }
        list.add(particle);
    }
}
