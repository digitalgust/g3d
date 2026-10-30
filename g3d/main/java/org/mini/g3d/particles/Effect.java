package org.mini.g3d.particles;

import org.mini.g3d.core.DisplayManager;

import java.util.ArrayList;
import java.util.List;


/**
 * an effect define json
 * json解析而来的特效,
 * 特效具有时空特性,
 * 他主要由 emitter 和改变emitter的控制器EmittrControler构成
 * 特效由 EffectMaster 来解析并管理
 */

public class Effect {
    //config
    public List<Emitter> emitters = new ArrayList<>();

    private double firstRun;
    private double secondRun;
    boolean exit = false;


    List<EmitterControler> controlers = new ArrayList<>();
    private boolean started;

    public Effect() {
    }


    public void update() {
        if (exit) return;
        double now = DisplayManager.getTimeSeconds();
        if (!started) {
            started = true;
            firstRun = secondRun = now;
        }
        boolean onSeconds = now - secondRun >= 1f;
        if (onSeconds) secondRun = now;
        boolean over = true;
        if (emitters != null) for (Emitter emi : emitters) {
            if (emi == null || emi.isTerminated()) continue;
            over = false;
            double elapsed = now - firstRun - emi.startAt;
            if (elapsed < 0f) continue;
            if (!emi.isStartuped()) {
                emi.startup();
                notifyControlers(emi, 0);
            }
            if (onSeconds) emi.onSecondOver();
            if (emi.emitterLife > 0f && elapsed >= emi.emitterLife) {
                notifyControlers(emi, 2);
                emi.terminate();
                continue;
            }
            notifyControlers(emi, 1);
            emi.update();
        }
        // Last emitter termination can finish the effect in the same frame.
        if (!over && emitters != null) {
            over = true;
            for (Emitter emi : emitters) if (emi != null && !emi.isTerminated()) over = false;
        }
        exit = over;
    }

    private void notifyControlers(Emitter emi, int event) {
        if (controlers == null) return;
        for (EmitterControler c : controlers) {
            if (c == null || !c.canApply(emi)) continue;
            if (event == 0) c.onStart(emi);
            else if (event == 1) c.onUpdate(emi);
            else c.onTeminate(emi);
        }
    }

    /** Stop scene-bound effects without producing an end-of-life burst. */
    public void terminate() {
        if (exit) return;
        exit = true;
        if (emitters != null) for (Emitter emi : emitters) {
            if (emi != null && !emi.isTerminated()) {
                notifyControlers(emi, 2);
                emi.cancel();
            }
        }
    }

    public void addControler(EmitterControler modifier) {
        if (controlers == null) controlers = new ArrayList<>();
        controlers.add(modifier);
    }

    public Effect deepClone() {
        Effect cloned = new Effect();
        if (emitters != null) {
            List<Emitter> emitterCopies = new ArrayList<>(emitters.size());
            for (int i = 0; i < emitters.size(); i++) {
                Emitter emitter = emitters.get(i);
                emitterCopies.add(emitter == null ? null : emitter.deepClone());
            }
            cloned.setEmitters(emitterCopies);
        }
        if (controlers != null) {
            List<EmitterControler> controlerCopies = new ArrayList<>(controlers.size());
            for (int i = 0; i < controlers.size(); i++) {
                EmitterControler controler = controlers.get(i);
                controlerCopies.add(controler == null ? null : controler.deepClone());
            }
            cloned.setControlers(controlerCopies);
        }
        return cloned;
    }

    public boolean isTerminated() {
        return exit;
    }


    public Emitter get(String pnodeName) {
        if (emitters != null) {
            for (int i = 0; i < emitters.size(); i++) {
                Emitter emi = emitters.get(i);
                if (emi != null && pnodeName != null && pnodeName.equals(emi.name)) {
                    return emi;
                }
            }
        }
        return null;
    }


    /**
     * ======================================================
     * setter
     * ======================================================
     */

    public void setEmitters(List<Emitter> emitters) {
        this.emitters = emitters;
    }


    public void setControlers(List<EmitterControler> controlers) {
        this.controlers = controlers;
    }
}
