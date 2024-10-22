package org.mini.g3d.particles;

import org.mini.g3d.particles.controler.EmitterDirectionControler;
import org.mini.g3d.particles.controler.EmitterLocationControler;
import org.mini.json.JsonParser;

/**
 * 用来控制粒子发生器,
 * 可以由用户自行扩充,并把 类名作为 controlerType
 */
public abstract class EmitterControler implements JsonParser.Polymorphic {
    String controlerType;


    /**
     * 当emitter创建时执行一次
     * @param emi
     */
    public abstract void onStart(Emitter emi);

    /**
     * 当结束时执行一次
     * @param emi
     */
    public abstract void onTeminate(Emitter emi);

    /**
     * 每次更新
     * @param emi
     */
    public abstract void onUpdate(Emitter emi);

    /**
     * 是否对emitter进行作用
     * @param emitter
     * @return
     */
    public abstract boolean canApply(Emitter emitter);

    @Override
    public Class getType() {
        switch (controlerType) {
            case "LocOffset":
                return EmitterLocationControler.class;
            case "LocRation":
                return EmitterLocationControler.class;
            case "Direction":
                return EmitterDirectionControler.class;
            default://try to parse as class name
                try {
                    return Class.forName(controlerType);
                } catch (Exception e) {
                }
        }
        throw new RuntimeException("Controler type not found:" + controlerType);
    }


    /**
     * ======================================================
     * setter   getter
     * ======================================================
     */

    public void setControlerType(String controlerType) {
        this.controlerType = controlerType;
    }

}
