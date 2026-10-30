package org.mini.g3d.particles.modifier;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.particles.Particle;
import org.mini.g3d.particles.ParticleModifier;

import java.util.List;

public class ParticleFrameModifier extends ParticleModifier {
    //List<Integer> frameIndex_and_millSecond;
    int[] frameIndese;//帧索引
    int[] timeInMs;//每帧停留累加毫秒数
    float endAt;
    float startAt;

    @Override
    public void update(Particle particle) {
        float t = particle.getElapsedTime();
        if (t >= startAt && t <= endAt && timeInMs != null && timeInMs.length > 0) {
            int tms = (int) ((t - startAt) * 1000) % timeInMs[timeInMs.length - 1];
            int index = 0;
            while (index < timeInMs.length - 1 && tms >= timeInMs[index]) index++;
            particle.setFrameChgByModifier(true);
            particle.setFrameIndex(frameIndese[index]);
        } else {
            particle.setFrameChgByModifier(false);
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

    public void setFrameInfo(List<Integer> frameInfo) {
        if (frameInfo == null || frameInfo.isEmpty() || frameInfo.size() % 2 != 0) {
            throw new IllegalArgumentException("frameInfo requires frame/duration pairs");
        }
        int len = frameInfo.size() / 2;
        int[] frames = new int[len];
        int[] times = new int[len];
        long sum = 0;
        for (int i = 0; i < len; i++) {
            Integer frame = frameInfo.get(i * 2), duration = frameInfo.get(i * 2 + 1);
            if (frame == null || duration == null || frame < 0 || duration <= 0) {
                throw new IllegalArgumentException("Frames must be nonnegative and durations positive");
            }
            sum += duration;
            if (sum > Integer.MAX_VALUE) throw new IllegalArgumentException("Frame duration overflow");
            frames[i] = frame;
            times[i] = (int) sum;
        }
        frameIndese = frames;
        timeInMs = times;
    }

    @Override
    public ParticleModifier deepClone() {
        ParticleFrameModifier cloned = new ParticleFrameModifier();
        cloned.frameIndese = frameIndese == null ? null : frameIndese.clone();
        cloned.timeInMs = timeInMs == null ? null : timeInMs.clone();
        cloned.endAt = endAt;
        cloned.startAt = startAt;
        copyTo(cloned);
        return cloned;
    }

}
