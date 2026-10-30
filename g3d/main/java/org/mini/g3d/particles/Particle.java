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

    private final Vector3f position = new Vector3f();
    private final Vector3f velocity = new Vector3f();
    private float gravityEffect;
    private float lifeLength;
    private Vector3f rotation = new Vector3f();
    private float scale;
    private float initialScale;

    private ParticleTexture texture;

    private Vector2f texOffset1 = new Vector2f();
    private Vector2f texOffset2 = new Vector2f();
    private float frameBlend = .0f;
    private Vector4f color = new Vector4f(1.f, 1.f, 1.f, 1.f);


    private float elapsedTime = 0;
    private float distance;

    private Vector3f reusableChange = new Vector3f();

    private boolean alive = false;
    boolean registered;
    boolean pooled;
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
        if (registered) {
            boolean reusable = pooled;
            pooled = false; // Reactivation must not leave this live object in the pool.
            ParticleMaster.removeParticle(this);
            pooled = reusable;
        }
        this.position.set(position);
        this.velocity.set(velocity);
        this.gravityEffect = gravityEffect;
        this.lifeLength = Math.max(.001f, lifeLength);
        this.rotation.zero();
        if (rotation != null) this.rotation.set(rotation);
        this.scale = scale;
        this.initialScale = scale;
        this.texture = texture;
        elapsedTime = 0f;
        generateAt = DisplayManager.getTime();
        distance = 0f;
        frameIndex = 0;
        frameBlend = 0f;
        frameChgByModifier = false;
        orientCamera = true;
        texOffset1.set(0f, 0f);
        texOffset2.set(0f, 0f);
        color.set(1f, 1f, 1f, 1f);
        modifiers = null;
        emitter = null;
        alive = texture != null;
        if (alive) ParticleMaster.addParticle(this);
    }

    public boolean isAlive() {
        return alive;
    }

    void release() {
        if (registered && emitter != null) emitter.particleReleased();
        registered = false;
        alive = false;
        emitter = null;
        modifiers = null;
        texture = null;
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
        Vector3f.sub(camera.getPosition(), position, reusableChange);
        distance = reusableChange.lengthSquared();
        this.elapsedTime += DisplayManager.getFrameTimeSeconds();
        alive = this.elapsedTime < this.lifeLength;
        return alive;
    }

    private void updateTextureCoordInfo() {
        int stageCount = texture.getNumberOfRows() * texture.getNumberOfRows();
        boolean loop = emitter != null && emitter.fps > 0;
        if (!frameChgByModifier) {
            float progression = loop ? elapsedTime * emitter.fps : elapsedTime / lifeLength * stageCount;
            if (loop && emitter.legacyFps) progression /= lifeLength;
            int index = (int) Math.floor(progression);
            frameIndex = loop ? index % stageCount : Math.min(index, stageCount - 1);
            frameBlend = progression - index;
        } else {
            frameBlend = 0f;
        }
        frameIndex = Math.max(0, Math.min(frameIndex, stageCount - 1));
        int next = frameIndex + 1;
        if (next >= stageCount) next = loop ? 0 : stageCount - 1;
        setTextureOffset(texOffset1, frameIndex);
        setTextureOffset(texOffset2, next);
    }

    private void setTextureOffset(Vector2f offset, int index) {
        int column = index % texture.getNumberOfRows();
        int row = index / texture.getNumberOfRows();
        offset.x = (float) column / texture.getNumberOfRows();
        offset.y = (float) row / texture.getNumberOfRows();
    }

    public float getElapsedTime() { return elapsedTime; }
    public float getLifeLength() { return lifeLength; }
    public float getInitialScale() { return initialScale; }

    public float getGravityY() {
        return GRAVITY * this.gravityEffect * elapsedTime;
    }

    /**
     * ======================================================
     * getter   setter
     * ======================================================
     */

    public void setVelocity(Vector3f velocity) {
        this.velocity.set(velocity);
    }

    public Vector3f getVelocity() {
        return velocity;
    }

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
        if (this.emitter == emitter) return;
        if (registered && this.emitter != null) this.emitter.particleReleased();
        this.emitter = emitter;
        if (registered && emitter != null) emitter.particleAdded();
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
