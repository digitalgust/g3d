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
    private static final int MAX_TOTAL_EFFECT_PLAYING = 24;
    private static final int MAX_SAME_EFFECT_PLAYING = 6;
    private static final int MAX_POOL_PER_EFFECT = 8;

    private boolean soundOpen = true;// 控制声音的开关
    private MaEngine maEngine = new MaEngine();
    private MaSound bgm;
    String bgmName;
    private float bgmVolume = 0.3f;
    private float effectVolume = 0.7f;
    private float minDistance = 0f;
    private float maxDistance = 100.0f;//listener 和 声源 最远距离
    private static Sound instance;

    private static class PlayingItem {
        final String path;
        final MaSound sound;

        PlayingItem(String path, MaSound sound) {
            this.path = path;
            this.sound = sound;
        }
    }

    private final Map<String, byte[]> audios = new HashMap<>();
    private final Map<String, List<MaSound>> effectPool = new HashMap<>();
    private final List<PlayingItem> playing = new ArrayList<>();

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

    public synchronized void play(String audioPath) {
        if (!soundOpen) {
            return;
        }
        collectEndedSounds();
        if (playing.size() >= MAX_TOTAL_EFFECT_PLAYING || countPlayingByPath(audioPath) >= MAX_SAME_EFFECT_PLAYING) {
            return;
        }

        MaSound snd = obtainEffectSound(audioPath);
        if (snd != null) {
            snd.setSpatialization(false);
            snd.start();
            addPlayingSound(audioPath, snd);
        }
    }

    public void play(String audioPath, Vector3f pos) {
        play(audioPath, pos.x, pos.y, pos.z);
    }

    public synchronized void play(String audioPath, float x, float y, float z) {
        if (!soundOpen) {
            return;
        }
        collectEndedSounds();
        if (playing.size() >= MAX_TOTAL_EFFECT_PLAYING || countPlayingByPath(audioPath) >= MAX_SAME_EFFECT_PLAYING) {
            return;
        }

        MaSound snd = obtainEffectSound(audioPath);
        if (snd != null) {
            snd.setSpatialization(true);
            snd.setAttenuationModel(MiniAudio.ma_attenuation_model_linear);
            snd.setMinDistance(minDistance);
            snd.setMaxDistance(maxDistance);
            snd.setPosition(x, y, z);
            snd.start();
            addPlayingSound(audioPath, snd);
        }
    }

    private MaSound obtainEffectSound(String audioPath) {
        List<MaSound> pool = effectPool.get(audioPath);
        if (pool != null) {
            for (int i = 0, n = pool.size(); i < n; i++) {
                MaSound s = pool.get(i);
                if (!s.isPlaying() && s.isPlayEnd()) {
                    s.setVolume(effectVolume);
                    return s;
                }
            }
            if (pool.size() >= MAX_POOL_PER_EFFECT) {
                return null;
            }
        }

        byte[] audiobytes = audios.get(audioPath);
        if (audiobytes == null) {
            audiobytes = GToolkit.readFileFromJar(audioPath);
            if (audiobytes == null) {
                return null;
            }
            audios.put(audioPath, audiobytes);
        }

        MaDecoder decoder = new MaDecoder(audiobytes, maEngine.getFormat(), maEngine.getChannels(), maEngine.getRatio());
        MaSound snd = new MaSound(maEngine, decoder, MiniAudio.MA_SOUND_FLAG_DECODE);
        snd.setVolume(effectVolume);
        if (pool == null) {
            pool = new ArrayList<>();
            effectPool.put(audioPath, pool);
        }
        pool.add(snd);
        return snd;
    }

    private int countPlayingByPath(String audioPath) {
        int c = 0;
        for (int i = 0, n = playing.size(); i < n; i++) {
            PlayingItem item = playing.get(i);
            if (audioPath.equals(item.path)) {
                c++;
            }
        }
        return c;
    }

    private void addPlayingSound(String path, MaSound snd) {
        if (snd != null) {
            playing.add(new PlayingItem(path, snd));
        }
    }

    private void collectEndedSounds() {
        for (int i = playing.size() - 1; i >= 0; i--) {
            PlayingItem p = playing.get(i);
            if (p.sound.isPlayEnd()) {
                p.sound.stop();
                playing.remove(i);
            }
        }
    }

    private synchronized void stopAllEffect() {
        for (int i = playing.size() - 1; i >= 0; i--) {
            PlayingItem p = playing.get(i);
            p.sound.stop();
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

    public synchronized void setEffectVolume(float effectVolume) {
        this.effectVolume = effectVolume;
        for (List<MaSound> pool : effectPool.values()) {
            for (int i = 0, n = pool.size(); i < n; i++) {
                pool.get(i).setVolume(effectVolume);
            }
        }
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


    public synchronized void stopAll() {
        stopBgm();
        stopAllEffect();
    }

    public synchronized void clearCache() {
        stopAllEffect();
        effectPool.clear();
        audios.clear();
    }
}
