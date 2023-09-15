package org.mini.g3d.core;

import org.mini.g3d.core.vector.Vector3f;

public class Light {

    private Vector3f position = new Vector3f();
    private Vector3f colour = new Vector3f();
    private Vector3f attenuation = new Vector3f();

    Vector3f sunDirection = new Vector3f();


    /**
     * 创建一个灯
     * 比如太阳position(0,500,0) colour(1.0,1.0,1.0) attenuation(1.0,0,0), 可以照亮(0,0,0)的物体
     * 比如电灯position(0,5,0) colour(1.0,1.0,1.0) attenuation(0,0,1.0), 可以照亮(0,0,0)的物体
     *
     * @param position    位置
     * @param colour      灯的颜色
     * @param attenuation 衰减程度,x为1可以照亮很远的物体,x=0 y=0 z=1 只能照亮近处的物体,
     */
    public Light(Vector3f position, Vector3f colour, Vector3f attenuation) {
        this.position.set(position);
        this.colour.set(colour);
        this.attenuation.set(attenuation);
    }

    public Vector3f getAttentuation() {
        return attenuation;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
        calcDirection();
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        calcDirection();
    }


    public Vector3f getColour() {
        return colour;
    }

    public void setColor(Vector3f colour) {
        this.colour.set(colour);
    }

    public Vector3f getDirection() {
        return sunDirection;
    }

    private void calcDirection() {
        sunDirection.set(-getPosition().x, -getPosition().y, -getPosition().z);
        sunDirection.normalise();
    }
}
