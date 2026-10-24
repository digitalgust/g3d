package org.mini.g3d.particles;

import org.mini.json.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *  Effect 管理器
 *  Effect 是组合 emitter 和 particle 的效果
 *
 */
public class EffectMaster {
    private static final List<Effect> effects = new ArrayList<>();
    private static final List<Effect> pending = new ArrayList<>();

    public static synchronized void add(Effect effect) {
        if (effect != null && !effect.isTerminated() && !pending.contains(effect) && !effects.contains(effect)) {
            pending.add(effect);
        }
    }

    public static synchronized void update() {
        effects.addAll(pending);
        pending.clear();
        for (ListIterator<Effect> it = effects.listIterator(); it.hasNext();) {
            Effect effect = it.next();
            effect.update();
            if (effect.isTerminated()) it.remove();
        }
    }

    public static synchronized void cleanUp() {
        for (Effect effect : effects) effect.terminate();
        for (int i = 0; i < pending.size(); i++) pending.get(i).terminate();
        effects.clear();
        pending.clear();
    }

    public static Effect parseEffect(String effectJsonStr) {
        JsonParser<Effect> parser = new JsonParser();
        return parser.deserial(effectJsonStr, Effect.class);
    }

    public static Effect cloneEffect(Effect template) {
        if (template == null) return null;
        return template.deepClone();
    }


//    public static void main(String[] args) {
//        String s = "" ;
//        Effect effect = parseEffect(s);
//        SysLog.info("G3D|" + effect);
//        int debug = 1;
//
//    }

}
