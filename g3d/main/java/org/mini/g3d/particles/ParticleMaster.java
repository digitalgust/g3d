package org.mini.g3d.particles;

import org.mini.g3d.core.ICamera;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/** Owns live particles and a bounded pool. All mutations run under the same lock. */
public class ParticleMaster {
    private static final Map<ParticleTexture, List<Particle>> particles = new ConcurrentHashMap<>();
    private static final List<Predicate<Particle>> removals = new ArrayList<>();
    private static final List<Particle> pool = new ArrayList<>();
    private static final int MAX_POOLED = 4096;
    private static int maxParticles = 16000;
    private static int activeCount;

    static synchronized Particle obtain() {
        if (activeCount >= maxParticles) return null;
        Particle particle = pool.isEmpty() ? null : pool.remove(pool.size() - 1);
        if (particle == null) particle = new Particle();
        particle.pooled = true;
        return particle;
    }

    static synchronized void recycleUnused(Particle particle) {
        particle.release();
        if (particle.pooled && pool.size() < MAX_POOLED) pool.add(particle);
    }

    private static void release(Particle particle) {
        activeCount--;
        recycleUnused(particle);
    }

    public static synchronized void update(ICamera camera) {
        Iterator<Map.Entry<ParticleTexture, List<Particle>>> batches = particles.entrySet().iterator();
        while (batches.hasNext()) {
            List<Particle> list = batches.next().getValue();
            Iterator<Particle> iterator = list.iterator();
            while (iterator.hasNext()) {
                Particle particle = iterator.next();
                boolean remove = !particle.update(camera);
                for (int i = 0; !remove && i < removals.size(); i++) {
                    remove = removals.get(i).test(particle);
                }
                if (remove) {
                    iterator.remove();
                    release(particle);
                }
            }
            if (list.isEmpty()) batches.remove();
        }
        removals.clear();
    }

    /** Clear scene ownership while keeping a small reusable pool for the next map. */
    public static synchronized void clear() {
        for (List<Particle> list : particles.values()) {
            for (Particle particle : list) release(particle);
        }
        particles.clear();
        removals.clear();
    }

    public static synchronized void cleanUp() {
        clear();
        pool.clear();
    }

    public static Map<ParticleTexture, List<Particle>> getParticles() { return particles; }
    public static synchronized int getActiveCount() { return activeCount; }
    public static synchronized int getPooledCount() { return pool.size(); }
    public static synchronized int getMaxParticles() { return maxParticles; }
    public static synchronized void setMaxParticles(int max) { maxParticles = Math.max(1, max); }

    public static synchronized void addParticle(Particle particle) {
        if (particle.registered) return;
        if (particle.getTexture() == null || activeCount >= maxParticles) {
            particle.release();
            return;
        }
        List<Particle> list = particles.get(particle.getTexture());
        if (list == null) {
            list = new ArrayList<>();
            particles.put(particle.getTexture(), list);
        }
        list.add(particle);
        particle.registered = true;
        activeCount++;
    }

    public static synchronized void removeParticle(Particle particle) {
        ParticleTexture texture = particle.getTexture();
        if (texture == null) return;
        List<Particle> list = particles.get(texture);
        if (list != null && list.remove(particle)) {
            release(particle);
            if (list.isEmpty()) particles.remove(texture);
        }
    }

    public static synchronized void removeParticleIf(Predicate<Particle> predicate) {
        if (predicate != null) removals.add(predicate);
    }
}
