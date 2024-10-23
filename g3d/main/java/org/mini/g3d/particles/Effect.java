package org.mini.g3d.particles;

import org.mini.g3d.core.DisplayManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * an effect define json
 * json解析而来的特效,
 * 特效具有时空特性,
 * 他主要由 emitter 和改变emitter的控制器EmittrControler构成
 * 特效由 EffectMaster 来解析并管理
 */

public class Effect {
    //config
    public List<Emitter> emitters;

    private float firstRun;
    private float secondRun;
    boolean exit = false;


    List<EmitterControler> controlers;

    public Effect() {
    }


    public void update() {

        float now = DisplayManager.getTime();
        if (firstRun == 0) {
            firstRun = now;
            secondRun = now;
        }
        boolean onSeconds = false;
        if (now - secondRun > 1.f) {
            secondRun = now;
            onSeconds = true;
        }

        boolean over = true;
        for (int i = 0; i < emitters.size(); i++) {
            Emitter emi = emitters.get(i);
            if (now - firstRun >= emi.startAt) {
                emi.startup();
                for (int j = 0; j < controlers.size(); j++) {
                    EmitterControler c = controlers.get(j);
                    if (c.canApply(emi)) {
                        c.onStart(emi);
                    }
                }
            }
            for (int j = 0; j < controlers.size(); j++) {
                EmitterControler c = controlers.get(j);
                if (c.canApply(emi)) {
                    c.onUpdate(emi);
                }
            }
            //
            emi.update();
            // 每秒调用一次
            if (onSeconds) {
                emi.onSecondOver();
            }
            if (emi.emitterLife > 0.f && now - firstRun - emi.startAt > emi.emitterLife) {
                for (int j = 0; j < controlers.size(); j++) {
                    EmitterControler c = controlers.get(j);
                    if (c.canApply(emi)) {
                        c.onTeminate(emi);
                    }
                }
                emi.terminate();
            }
            over = over && emi.isTerminated();
        }
        exit = over;

    }

    public void addControler(EmitterControler modifier) {
        if (controlers == null) controlers = new ArrayList<>();
        controlers.add(modifier);
    }

    public boolean isTerminated() {
        return exit;
    }


    public Emitter get(String pnodeName) {
        if (emitters != null) {
            for (int i = 0; i < emitters.size(); i++) {
                Emitter emi = emitters.get(i);
                if (emi.name.equals(pnodeName)) {
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
