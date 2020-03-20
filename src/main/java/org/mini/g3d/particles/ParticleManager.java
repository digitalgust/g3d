package org.mini.g3d.particles;


import org.mini.g3d.core.Camera;
import org.mini.g3d.core.Loader;
import org.mini.g3d.core.vector.Matrix4f;

import java.util.*;
import java.util.Map.Entry;

public class ParticleManager {

    private static Map<ParticleTexture, List<Particle>> particles = new HashMap<ParticleTexture, List<Particle>>();
    private static ParticleRenderer renderer;

    public static void init(Loader loader, Matrix4f projectionMatrix) {
        renderer = new ParticleRenderer(loader, projectionMatrix);
    }

    public static void update(Camera camera) {
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
                    }
                }
            }
            if (!entry.getKey().usesAdditiveBlending()) {
                InsertionSort.sortHighToLow(list);
            }
        }
    }

    public static void reloadProjectionMatrix(Matrix4f projectionMatrix) {
        renderer.reloadProjectionMatrix(projectionMatrix);
    }


    public static void renderParticles(Camera camera) {
        renderer.render(particles, camera);
    }

    public static void cleanUp() {
        renderer.cleanUp();
    }

    public static void addParticle(Particle particle) {
        List<Particle> list = particles.get(particle.getTexture());
        if (list == null) {
            list = new ArrayList<Particle>();
            particles.put(particle.getTexture(), list);
        }
        list.add(particle);
    }
}
