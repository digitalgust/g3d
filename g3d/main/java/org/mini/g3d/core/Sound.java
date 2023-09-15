package org.mini.g3d.core;


import org.mini.g3d.core.vector.Vector3f;
import org.mini.gui.GToolkit;
import org.mini.media.MaDecoder;
import org.mini.media.MiniAudio;
import org.mini.media.engine.MaEngine;
import org.mini.media.engine.MaSound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Sound {
    private boolean soundOpen = true;// 控制声音的开关
    private MaEngine maEngine = new MaEngine();
    private MaSound bgm;
    String bgmName;
    private float bgmVolume = 0.3f;
    private float effectVolume = 0.7f;
    private float minDistance = 0f;
    private float maxDistance = 100.0f;//listener 和 声源 最远距离
    private static Sound instance;

    Map<String, byte[]> audios = new HashMap<>();
    List<MaSound> playing = new ArrayList<>();

    /**
     * @return
     */
    public static synchronized Sound getInstance() {
        if (instance == null) {
            instance = new Sound();
        }
        return instance;
    }

    public void startBgm(String pathInJar) {
        if (pathInJar.equals(bgmName)) {
            return;
        }
        byte[] audio = GToolkit.readFileFromJar(pathInJar);
        if (audio != null) {
            stopBgm();
            bgmName = pathInJar;

            MaDecoder decoder = new MaDecoder(audio);
            bgm = new MaSound(maEngine, decoder, MiniAudio.MA_SOUND_FLAG_STREAM | MiniAudio.MA_SOUND_FLAG_ASYNC);
            bgm.setVolume(bgmVolume);
            bgm.setSpatialization(false);
            bgm.setLooping(true);
            bgm.setFadeIn(1000, bgmVolume);
            bgm.start();
        }
    }

    public void stopBgm() {
        if (bgm != null) {
            bgm.stop();
            bgm = null;
            bgmName = null;
        }
    }


    public float getBgmVolume() {
        return bgmVolume;
    }

    public void setBgmVolume(float bgmVolume) {
        this.bgmVolume = bgmVolume;
        if (bgm != null) {
            bgm.setVolume(this.bgmVolume);
        }
    }

    public void play(String audioPath) {
        if (!soundOpen) {
            return;
        }
        MaSound snd = getMaSound(audioPath);
        if (snd != null) {
            snd.setSpatialization(false);
            snd.start();
            addPlayingSound(snd);
        }
    }

    public void play(String audioPath, Vector3f pos) {
        play(audioPath, pos.x, pos.y, pos.z);
    }

    public void play(String audioPath, float x, float y, float z) {
        if (!soundOpen) {
            return;
        }
        MaSound snd = getMaSound(audioPath);
        if (snd != null) {
            snd.setSpatialization(true);
            snd.setAttenuationModel(MiniAudio.ma_attenuation_model_linear);
            snd.setMinDistance(minDistance);
            snd.setMaxDistance(maxDistance);
            snd.setPosition(x, y, z);
            snd.start();
            addPlayingSound(snd);
        }
    }

    private MaSound getMaSound(String audioPath) {
        byte[] audiobytes = audios.get(audioPath);
        if (audiobytes == null) {
            audiobytes = GToolkit.readFileFromJar(audioPath);
            if (audiobytes != null) {
                audios.put(audioPath, audiobytes);
            }
        }
        if (audiobytes != null) {
            MaDecoder decoder = new MaDecoder(audiobytes, maEngine.getFormat(), maEngine.getChannels(), maEngine.getRatio());
            MaSound snd = new MaSound(maEngine, decoder, MiniAudio.MA_SOUND_FLAG_DECODE | MiniAudio.MA_SOUND_FLAG_ASYNC);
            snd.setVolume(effectVolume);
            return snd;
        }
        return null;
    }

    private synchronized void addPlayingSound(MaSound snd) {
        if (snd != null) {
            playing.add(snd);
        }
        for (int i = playing.size() - 1; i >= 0; i--) {
            MaSound p = playing.get(i);
            if (p.isPlayEnd()) {
                p.stop();
                playing.remove(i);
            }
        }
    }

    private synchronized void stopAllEffect() {
        for (int i = playing.size() - 1; i >= 0; i--) {
            MaSound p = playing.get(i);
            p.stop();
        }
        playing.clear();
    }


    public void setSoundOpen(boolean soundOpen) {
        this.soundOpen = soundOpen;
        if (this.soundOpen) {
            maEngine.setVolume(1.0f);
        } else {
            maEngine.setVolume(0.0f);
        }
    }

    public boolean isSoundOpen() {
        return soundOpen;
    }

    public float getEffectVolume() {
        return effectVolume;
    }

    public void setEffectVolume(float effectVolume) {
        this.effectVolume = effectVolume;
    }

    public void setListenerPosition(int listenerIdx, Vector3f pos) {
        if (pos == null) return;
        maEngine.setListenerPosition(listenerIdx, pos.x, pos.y, pos.z);
    }


    public void setListenerPosition(int listenerIdx, float x, float y, float z) {
        maEngine.setListenerPosition(listenerIdx, x, y, z);
    }

    public void setListenerDirection(int listenerIdx, float x, float y, float z) {
        maEngine.setListenerDirection(listenerIdx, x, y, z);
    }


    public float getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(float minDistance) {
        this.minDistance = minDistance;
    }

    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }


    public float getMaxDistance() {
        return maxDistance;
    }


    public void stopAll() {
        stopBgm();
        stopAllEffect();
    }

    public void clearCache() {
        audios.clear();
    }
}
