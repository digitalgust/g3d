package org.mini.g3d.particles;


import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.vector.Vector4f;

import java.util.List;

/**
 * 粒子
 * 由Emitter 发射出来
 * 由ParticleMaster管理
 * 粒子具有时空特性
 * 粒子可渲染
 * 可被ParticleModifier改变时空特性
 */
public class Particle {

    private static float GRAVITY = -20;

    private Vector3f position;
    private Vector3f velocity;
    private float gravityEffect;
    private float lifeLength;
    private Vector3f rotation = new Vector3f();
    private float scale;

    private ParticleTexture texture;

    private Vector2f texOffset1 = new Vector2f();
    private Vector2f texOffset2 = new Vector2f();
    private float frameBlend = .0f;
    private Vector4f color = new Vector4f(1.f, 1.f, 1.f, 1.f);


    private float elapsedTime = 0;
    private float distance;

    private Vector3f reusableChange = new Vector3f();

    private boolean alive = false;
    public List<ParticleModifier> modifiers;
    float generateAt = DisplayManager.getTime();
    Emitter emitter;
    //frame modifier
    boolean frameChgByModifier = false;
    int frameIndex;
    boolean orientCamera = true;//默认朝向摄像机

    /**
     * ======================================================
     * method
     * ======================================================
     */
    public Particle() {
    }

    public void setActive(ParticleTexture texture, Vector3f position, Vector3f velocity,
                          float gravityEffect, float lifeLength, Vector3f rotation, float scale) {
        this.alive = true;
        this.position = position;
        this.velocity = velocity;
        this.gravityEffect = gravityEffect;
        this.lifeLength = lifeLength;
        if (rotation != null) this.rotation.set(rotation);
        this.scale = scale;
        this.texture = texture;
        ParticleMaster.addParticle(this);
        // System.out.println("Particle setActive");
    }


    protected boolean update(ICamera camera) {

        if (modifiers != null) {
            for (ParticleModifier pm : modifiers) {
                pm.update(this);
            }
        }
        velocity.y += GRAVITY * this.gravityEffect * DisplayManager.getFrameTimeSeconds();
        reusableChange.set(velocity);
        reusableChange.scale(DisplayManager.getFrameTimeSeconds());
        Vector3f.add(reusableChange, this.position, this.position);
        updateTextureCoordInfo();
        distance = Vector3f.sub(camera.getPosition(), position, null).lengthSquared();
        this.elapsedTime += DisplayManager.getFrameTimeSeconds();
        alive = this.elapsedTime < this.lifeLength;
        return alive;
    }

    private void updateTextureCoordInfo() {
        float lifeFactor = elapsedTime / lifeLength;
        int stageCount = texture.getNumberOfRows() * texture.getNumberOfRows();
        if (!frameChgByModifier) {//如果帧没有被别人修改
            if (emitter.fps != 0) {
                float atlasProgression = lifeFactor * emitter.fps;
                frameIndex = (int) Math.floor(atlasProgression);
                frameIndex = frameIndex % stageCount;
            } else {
                float atlasProgression = lifeFactor * stageCount;
                frameIndex = (int) Math.floor(atlasProgression);
            }
        }
        int frameIndex2 = frameIndex < stageCount - 1 ? frameIndex + 1 : 0;
        //this.frameBlend = .0f;//atlasProgression % 1;
        setTextureOffset(texOffset1, frameIndex);
        setTextureOffset(texOffset2, frameIndex2);
    }

    private void setTextureOffset(Vector2f offset, int index) {
        int column = index % texture.getNumberOfRows();
        int row = index / texture.getNumberOfRows();
        offset.x = (float) column / texture.getNumberOfRows();
        offset.y = (float) row / texture.getNumberOfRows();
    }

    public float getGravityY() {
        return GRAVITY * this.gravityEffect * (DisplayManager.getTime() - generateAt);
    }

    /**
     * ======================================================
     * getter   setter
     * ======================================================
     */


    public float getDistance() {
        return distance;
    }

    public Vector2f getTexOffset1() {
        return texOffset1;
    }

    public Vector2f getTexOffset2() {
        return texOffset2;
    }

    public float getFrameBlend() {
        return frameBlend;
    }

    public void setFrameBlend(float frameBlend) {
        this.frameBlend = frameBlend;
    }

    public float getAlpha() {
        return color.w;
    }

    public ParticleTexture getTexture() {
        return texture;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation.set(rotation);
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    public List<ParticleModifier> getModifiers() {
        return modifiers;
    }

    public void setModifiers(List<ParticleModifier> modifiers) {
        this.modifiers = modifiers;
    }

    public float getGenerateAt() {
        return generateAt;
    }

    public void setEmitter(Emitter emitter) {
        this.emitter = emitter;
    }

    public Emitter getEmitter() {
        return this.emitter;
    }

    public Vector4f getColor() {
        return color;
    }

    public void setColor(Vector4f color) {
        this.color.set(color);
    }

    public void setFrameChgByModifier(boolean frameModify) {
        this.frameChgByModifier = frameModify;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setOrientCamera(boolean orientCamera) {
        this.orientCamera = orientCamera;
    }

    public boolean isOrientCamera() {
        return orientCamera;
    }


}
