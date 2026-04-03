package org.mini.g3d.particles;


import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.util.G3dUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class ParticleMaster {

    private static Map<ParticleTexture, List<Particle>> particles = new ConcurrentHashMap<ParticleTexture, List<Particle>>();

    private static List<Predicate> predicateRemoveList = new ArrayList<Predicate>();

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
                    } else if (predicateRemoveList.size() > 0) {
                        for (int i = 0; i < predicateRemoveList.size(); i++) {
                            Predicate predicate = predicateRemoveList.get(i);
                            if (predicate.test(p)) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                }
                if (!entry.getKey().usesAdditiveBlending()) {
                    InsertionSort.sortHighToLow(list);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        predicateRemoveList.clear();
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

    public static void removeParticle(Particle particle) {
        List<Particle> list = particles.get(particle.getTexture());
        if (list != null) {
            list.remove(particle);
            if (list.isEmpty()) {
                particles.remove(particle.getTexture());
                G3dUtil.putCachedList(list);
            }
        }
    }

    public static void removeParticleIf(Predicate<Particle> predicate) {
        if (predicate != null) {
            predicateRemoveList.add(predicate);
        }
    }
}
