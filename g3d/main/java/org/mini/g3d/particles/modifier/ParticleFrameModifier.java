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

    //
    int curIndex = 0;

    @Override
    public void update(Particle particle) {
        float cur = DisplayManager.getTime();
        float t = cur - particle.getGenerateAt();//in second
        if (t >= startAt && t <= endAt) {
            float e = t - startAt;

            int tms = (int) (e * 1000);
            tms = tms % timeInMs[timeInMs.length - 1];
            if (tms < timeInMs[0]) {
                curIndex = frameIndese[0];
            } else {
                for (int i = 0, imax = timeInMs.length; i < imax - 1; i++) {
                    if (tms > timeInMs[i] && tms < timeInMs[i + 1]) {
                        curIndex = frameIndese[i + 1];
                        break;
                    }
                }
            }
            particle.setFrameChgByModifier(true);
            particle.setFrameIndex(curIndex);
            //SysLog.info("G3D|===="+curIndex);
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
        //this.frameIndex_and_millSecond = frameIndex_and_millSecond;
        int size = frameInfo.size();
        if (size % 2 != 0) {
            System.err.println("[G3D][ERROR]frameModifier need pair of frameIndex and timeLenth");
            return;
        }
        int len = size / 2;
        frameIndese = new int[len];
        timeInMs = new int[len];
        int sum = 0;
        for (int i = 0; i < len; i++) {
            frameIndese[i] = frameInfo.get(i * 2);
            sum += frameInfo.get(i * 2 + 1);
            timeInMs[i] = sum;
        }
    }

}
