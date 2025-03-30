package org.mini.g3d.animation.gltf2.loader.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AniGroup {
    static final float DEFAUT_FPT = .033333333333333f;//30fps

    //don't change the name , it's json map name
    List<AniClip> aniClips;
    int fps;
    //runtime vars
    int keyFrameMin;
    int keyFrameMax;
    int fullAniIndex = -1;

    public AniGroup() {
        fps = Math.round(1.f / DEFAUT_FPT);
    }

    private void init() {
        if (aniClips == null) return;
        keyFrameMin = Integer.MAX_VALUE;
        keyFrameMax = Integer.MIN_VALUE;

        float fpt = 1f / fps;
        //SysLog.info("G3D|setAniClips fps:" + fps);
        if (fullAniIndex >= 0) {
            aniClips.remove(fullAniIndex);
        }

        for (int i = 0; i < aniClips.size(); i++) {
            AniClip c = aniClips.get(i);
            //pre calc
            c.beginAt = c.begin * fpt;
            c.endAt = c.end * fpt;
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
        ac.beginAt = ac.begin * fpt;
        ac.endAt = ac.end * fpt;
        aniClips.add(ac);
        fullAniIndex = aniClips.indexOf(ac);
    }

    public void setAniClips(List<AniClip> aniClips) {
        this.aniClips = aniClips;
        init();
    }

    public List<AniClip> getAniClips() {
        return aniClips;
    }

    public int getSize() {
        return aniClips.size();
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
//        SysLog.info("G3D|" );

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

    public void setFps(int fps) {
        this.fps = fps;
        init();
    }

    public int getFps() {
        return fps;
    }

    public float getFpt() {
        return 1f / fps;
    }
}
