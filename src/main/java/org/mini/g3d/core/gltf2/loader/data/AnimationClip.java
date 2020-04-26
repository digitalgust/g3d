package org.mini.g3d.core.gltf2.loader.data;

import java.util.HashMap;
import java.util.Map;

public class AnimationClip {
    static final float FPT = 0.0333333f;

    public static class Ani {
        public String clipName;
        public float endAt;
        public float beginAt;
        public int begin;
        public int end;
    }

    String[] clipName;
    int[] animation;

    Map<GLTFAccessor, Ani[]> indexMap = new HashMap();

    public String[] getClipName() {
        return clipName;
    }

    public void setClipName(String[] clipName) {
        this.clipName = clipName;
    }


    public void setAnimation(int[] animation) {
        if ((animation.length % 2) != 0) throw new RuntimeException("animation need pair of begin and end");
        this.animation = animation;
    }

    public Ani[] getClips(GLTFAccessor accessor) {
        Ani[] indices = indexMap.get(accessor);
        if (indices == null) {
            indices = parseInput(accessor);
            indexMap.put(accessor, indices);
        }
        return indices;
    }

    private Ani[] parseInput(GLTFAccessor input) {

        Ani[] anis = new Ani[clipName.length];
        for (int i = 0; i < anis.length; i++) {
            Ani c = new Ani();
            anis[i] = c;
            c.clipName = clipName[i];
            c.beginAt = animation[i * 2] * FPT;
            c.endAt = animation[i * 2 + 1] * FPT;
        }


        int count = input.getPrimitiveCount();
        int j = 0;
        for (int i = 0; i < count; i++) {
            float t = input.getFloat(i);
            if (t >= anis[j].beginAt - FPT / 2f) {
                anis[j].begin = i;
                anis[j].beginAt = t;
                j++;
                if (j >= anis.length) break;
            }
        }
        j = anis.length - 1;
        for (int i = count - 1; i >= 0; --i) {
            float t = input.getFloat(i);
            if (t <= anis[j].endAt + FPT / 2f) {
                anis[j].end = i;
                anis[j].endAt = t;
                --j;
                if (j < 0) break;
            }
        }

        return anis;
    }

}
