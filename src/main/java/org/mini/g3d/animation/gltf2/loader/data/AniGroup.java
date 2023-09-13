package org.mini.g3d.animation.gltf2.loader.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AniGroup {
    //    static final float FPT = .041666666666667f;//24fps
    public static final float FPT = .033333333333333f;//30fps

    //don't change the name , it's json map name
    List<AniClip> aniClips;
    int keyFrameMin = Integer.MAX_VALUE;
    int keyFrameMax = Integer.MIN_VALUE;
    int fullAniIndex;


    public void setAniClips(List<AniClip> aniClips) {
        this.aniClips = aniClips;

        for (int i = 0; i < aniClips.size(); i++) {
            AniClip c = aniClips.get(i);
            //pre calc
            c.beginAt = c.begin * FPT;
            c.endAt = c.end * FPT;
            if (keyFrameMin > c.begin) {
                keyFrameMin = c.begin;
            }
            if (keyFrameMax < c.end) {
                keyFrameMax = c.end;
            }
        }
        AniClip ac = new AniClip();
        ac.setClipName("_FULL_ANI");
        ac.begin = 0;//from zero
        ac.end = keyFrameMax;
        ac.beginAt = ac.begin * FPT;
        ac.endAt = ac.end * FPT;
        aniClips.add(ac);
        fullAniIndex = aniClips.indexOf(ac);
    }

    public List<AniClip> getAniClips() {
        return aniClips;
    }


    Map<GLTFAccessor, AniClip[]> indexMap = new HashMap();


//    public void setAnimation(int[] animation) {
//        if ((animation.length % 2) != 0) throw new RuntimeException("animation need pair of begin and end");
//        this.animation = animation;
//    }

    public AniClip[] getAniClips(GLTFAccessor accessor) {
        AniClip[] indices = indexMap.get(accessor);
        if (indices == null) {
            indices = parseInput(accessor);
            indexMap.put(accessor, indices);
        }
        return indices;
    }


    private AniClip[] parseInput(GLTFAccessor input) {
        float spf = input.getMax()[0] / input.getCount();

        AniClip[] aniMotions = aniClips.toArray(new AniClip[aniClips.size()]);
        for (int i = 0; i < aniMotions.length; i++) {
            aniMotions[i] = aniClips.get(i);
            //real calc
            aniMotions[i].beginAt = aniMotions[i].begin * spf;
            aniMotions[i].endAt = aniMotions[i].end * spf;
        }


        //时间为倒序排列
//        int count = input.getPrimitiveCount();
//        for (int i = count - 1; i >= 0; --i) {
//            float t = input.getFloat(i);
//            System.out.print(t);
//            System.out.print(' ');
//        }
//        System.out.println();

        return aniMotions;
    }


    public int getKeyFrameMin() {
        return keyFrameMin;
    }

    public void setKeyFrameMin(int keyFrameMin) {
        this.keyFrameMin = keyFrameMin;
    }

    public int getKeyFrameMax() {
        return keyFrameMax;
    }

    public void setKeyFrameMax(int keyFrameMax) {
        this.keyFrameMax = keyFrameMax;
    }

    public int getFullAniIndex() {
        return fullAniIndex;
    }
}
