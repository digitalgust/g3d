package org.mini.g3d.particles;


import org.mini.g3d.core.Camera;
import org.mini.g3d.core.EngineManager;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;

public class Particle {

    private Vector3f position;
    private Vector3f velocity;
    private float gravityEffect;
    private float lifeLength;
    private float rotation;
    private float scale;

    private ParticleTexture texture;

    private Vector2f texOffset1 = new Vector2f();
    private Vector2f texOffset2 = new Vector2f();
    private float blend;

    private float elapsedTime = 0;
    private float distance;

    private Vector3f reusableChange = new Vector3f();

    private boolean alive = false;

    public Particle() {
    }

    public void setActive(ParticleTexture texture, Vector3f position, Vector3f velocity,
                          float gravityEffect, float lifeLength, float rotation, float scale) {
        this.alive = true;
        this.position = position;
        this.velocity = velocity;
        this.gravityEffect = gravityEffect;
        this.lifeLength = lifeLength;
        this.rotation = rotation;
        this.scale = scale;
        this.texture = texture;
        ParticleManager.addParticle(this);
        // System.out.println("Particle setActive");
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

    public float getBlend() {
        return blend;
    }

    public ParticleTexture getTexture() {
        return texture;
    }

    public Vector3f getPosition() {
        return position;
    }

    public float getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    private static float GRAVITY = -20;

    protected boolean update(Camera camera) {
        velocity.y += GRAVITY * this.gravityEffect * EngineManager.getFrameTimeSeconds();
        reusableChange.set(velocity);
        reusableChange.scale(EngineManager.getFrameTimeSeconds());
        Vector3f.add(reusableChange, this.position, this.position);
        updateTextureCoordInfo();
        distance = Vector3f.sub(camera.getPosition(), position, null).lengthSquared();
        this.elapsedTime += EngineManager.getFrameTimeSeconds();
        alive = this.elapsedTime < this.lifeLength;
        return alive;
    }

    private void updateTextureCoordInfo() {
        float lifeFactor = elapsedTime / lifeLength;
        int stageCount = texture.getNumberOfRows() * texture.getNumberOfRows();
        float atlasProgression = lifeFactor * stageCount;
        int index1 = (int) Math.floor(atlasProgression);
        int index2 = index1 < stageCount - 1 ? index1 + 1 : index1;
        this.blend = atlasProgression % 1;
        setTextureOffset(texOffset1, index1);
        setTextureOffset(texOffset2, index2);
    }

    private void setTextureOffset(Vector2f offset, int index) {
        int column = index % texture.getNumberOfRows();
        int row = index / texture.getNumberOfRows();
        offset.x = (float) column / texture.getNumberOfRows();
        offset.y = (float) row / texture.getNumberOfRows();
    }
}
