package org.mini.g3d.animation.gltf2.loader.data;

public class AniClip {
    public String clipName;
    public float endAt;
    public float beginAt;
    public int begin;
    public int end;

    public String getClipName() {
        return clipName;
    }

    public void setClipName(String clipName) {
        this.clipName = clipName;
    }

    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
        }
        return null;
    }
}
