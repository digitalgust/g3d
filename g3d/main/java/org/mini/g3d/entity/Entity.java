package org.mini.g3d.entity;

import org.mini.g3d.core.vector.Vector3f;

import org.mini.g3d.core.models.TexturedModel;

public class Entity {

    protected Object attachment;
    protected TexturedModel model;
    protected Vector3f position = new Vector3f();
    protected float rotX, rotY, rotZ;
    protected float scale = 1.0f;
    protected float transparency = 1.0f; //采用网络透，性能高，目前仅支付半透明，1不透明，0透明，0.5半透明

    protected int textureIndex = 0;

    protected Entity() {
    }

    public Entity(TexturedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        this.model = model;
        this.position = position;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scale = scale;
    }

    public Entity(TexturedModel model, int index, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        this.model = model;
        this.textureIndex = index;
        this.position = position;
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.scale = scale;
    }

    public int getTextureIndex() {
        return textureIndex;
    }

    public void setTextureIndex(int textureIndex) {
        this.textureIndex = textureIndex;
    }

    public float getTextureXOffset() {
        int column = textureIndex % model.getTexture().getNumberOfRows();
        return (float) column / (float) model.getTexture().getNumberOfRows();
    }

    public float getTextureYOffset() {
        int row = textureIndex / model.getTexture().getNumberOfRows();
        return (float) row / (float) model.getTexture().getNumberOfRows();
    }

    public void increasePosition(float dx, float dy, float dz) {
        if (Float.isNaN(dx) || Float.isNaN(dy) || Float.isNaN(dz)) {
            int debug = 1;
        }
        this.position.x += dx;
        this.position.y += dy;
        this.position.z += dz;
    }

    public void increaseRotation(float dx, float dy, float dz) {
        rotX += dx;
        rotY += dy;
        rotZ += dz;
    }

    public TexturedModel getModel() {
        return model;
    }

    public void setModel(TexturedModel model) {
        this.model = model;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public float getScale() {
        return scale;
    }

    public void setTransparency(float transparency) {
        this.transparency = transparency;
    }

    public float getTransparency() {
        return transparency;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }


    public String toString() {
        return position.toString();
    }

    public Object getAttachment() {
        return attachment;
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }


}
