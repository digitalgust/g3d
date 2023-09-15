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
    static List<Effect> effects = new ArrayList<>();


    public static void add(Effect effect) {
        effects.add(effect);
    }

    public static void update() {
        for (ListIterator<Effect> it = effects.listIterator(); it.hasNext(); ) {
            Effect effect = it.next();
            effect.update();
            if (effect.isTerminated()) {
                it.remove();
            }
        }
    }

    public static Effect parseEffect(String effectJsonStr) {
        JsonParser<Effect> parser = new JsonParser();
        return parser.deserial(effectJsonStr, Effect.class);
    }


//    public static void main(String[] args) {
//        String s = "" ;
//        Effect effect = parseEffect(s);
//        System.out.println(effect);
//        int debug = 1;
//
//    }

}
