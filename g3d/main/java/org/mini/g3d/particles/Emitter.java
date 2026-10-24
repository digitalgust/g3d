package org.mini.g3d.particles;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.Sound;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.vector.Vector4f;
import org.mini.gui.GImage;
import org.mini.gui.GToolkit;
import org.mini.util.SysLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 粒子发射器
 * 用于发射粒子,
 * 提供发身粒子的控制数据,
 * 为粒子提供各种粒子修改器
 * 其尽可能用json来配置其数据
 * 可被EmitterControler来改变发射属性
 */
public class Emitter {
    static Random random = new Random();
    //configuration
    public float startAt; //in second
    public float emitterLife; //in second
    public String name;
    public String imagePath;
    public int imageGrids;
    public String audioPath;
    public boolean additive = true;
    private boolean depthTest = true;
    public Vector3f location;
    public Vector3f locationError = new Vector3f(0, 0, 0);
    public Vector3f direction = new Vector3f(0, 0, 0);
    public float directionDeviation = 0.001f;
    public float pps; //particle per second
    public int min;// min particles
    public int max;// max births per second (legacy semantics)
    public int maxAlive;// concurrent particles; 0 uses the global budget
    public boolean legacyFps;// compatibility for old lifetime-normalized fps
    public boolean interpolatePosition;
    public float maxInterpolationDistance = 10f;
    public int fps;//
    public float speed;
    public float speedError;
    public float gravity;
    public float particleLife; //in second
    public float particleLifeError;
    public float scale;
    public float scaleError;
    public Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    public boolean randomRotation;  //粒子自转一个初始角度
    public int frameIndex = -1;  //粒子自转一个初始角度
    public List<ParticleModifier> modifiers;

    // runtime
    private boolean startuped = false, terminated = false;
    GImage img;
    public Vector3f offsetLocation = new Vector3f();
    public Vector3f rotation = new Vector3f();
    public Vector3f runtimeLocation = new Vector3f();
    public ParticleTexture texture;
    public int generatedCount = 0;
    static Vector3f zAxis = new Vector3f(0, 0, 1);
    private final Matrix4f localRotationMatrix = new Matrix4f();
    private final Vector3f emitPosition = new Vector3f();
    private final Vector3f emitVelocity = new Vector3f();
    private final Vector3f coneAxis = new Vector3f();
    private final Vector3f coneUnit = new Vector3f();
    private final Vector4f coneSample = new Vector4f();
    private final Vector3f previousCenter = new Vector3f();
    private boolean hasPreviousCenter;
    private int aliveCount;

    void particleAdded() { aliveCount++; }
    void particleReleased() { aliveCount = Math.max(0, aliveCount - 1); }
    public int getAliveCount() { return aliveCount; }
    public void setMaxAlive(int maxAlive) { this.maxAlive = Math.max(0, maxAlive); }
    public void setLegacyFps(boolean legacyFps) { this.legacyFps = legacyFps; }
    public void setInterpolatePosition(boolean value) { interpolatePosition = value; }
    public void setMaxInterpolationDistance(float value) { maxInterpolationDistance = Math.max(0f, value); }
    public void resetEmissionPath() { hasPreviousCenter = false; }
    /** Cancellation does not emit the normal end-of-effect minimum burst. */
    public void cancel() { terminated = true; resetEmissionPath(); }



    public void update() {
        if (startuped && !terminated) {
            generateParticles(getEmitLocation());
        }
    }

    public void startup() {
        if (!startuped) {
            startuped = true;
            if (imagePath != null && !imagePath.isEmpty()) {
                GImage image = GToolkit.getCachedImageFromJar(imagePath);
                if (image != null) setImg(image, imageGrids, additive, depthTest);
            }
            if (audioPath != null && !"".equals(audioPath)) {
                Sound.getInstance().play(audioPath, getEmitLocation());
            }
        }
    }

    public void terminate() {
        if (!terminated) {
            onEmitterTerminate(getEmitLocation());
            terminated = true;
        }
    }

    public boolean isStartuped() {
        return startuped;
    }

    public boolean isTerminated() {
        return terminated;
    }

    public Vector3f getEmitLocation() {
        runtimeLocation.zero();
        if (location != null) {
            runtimeLocation.translate(location);
        }
        if (offsetLocation != null) {
            runtimeLocation.translate(offsetLocation);
        }
        return runtimeLocation;
    }

    public void addModifier(ParticleModifier modifier) {
        if (modifiers == null) {
            modifiers = new ArrayList<>();
        }
        modifiers.add(modifier);
    }

    public Emitter deepClone() {
        Emitter cloned = new Emitter();
        cloned.startAt = startAt;
        cloned.emitterLife = emitterLife;
        cloned.name = name;
        cloned.imagePath = imagePath;
        cloned.imageGrids = imageGrids;
        cloned.audioPath = audioPath;
        cloned.additive = additive;
        cloned.depthTest = depthTest;
        cloned.location = location == null ? null : new Vector3f(location);
        cloned.locationError = locationError == null ? null : new Vector3f(locationError);
        cloned.direction = direction == null ? null : new Vector3f(direction);
        cloned.directionDeviation = directionDeviation;
        cloned.pps = pps;
        cloned.min = min;
        cloned.max = max;
        cloned.maxAlive = maxAlive;
        cloned.legacyFps = legacyFps;
        cloned.interpolatePosition = interpolatePosition;
        cloned.maxInterpolationDistance = maxInterpolationDistance;
        cloned.fps = fps;
        cloned.speed = speed;
        cloned.speedError = speedError;
        cloned.gravity = gravity;
        cloned.particleLife = particleLife;
        cloned.particleLifeError = particleLifeError;
        cloned.scale = scale;
        cloned.scaleError = scaleError;
        cloned.color = color == null ? null : new Vector4f(color);
        cloned.randomRotation = randomRotation;
        cloned.frameIndex = frameIndex;
        if (img != null) cloned.setImg(img, imageGrids, additive, depthTest);
        if (modifiers != null) {
            List<ParticleModifier> modifierCopies = new ArrayList<>(modifiers.size());
            for (int i = 0; i < modifiers.size(); i++) {
                ParticleModifier modifier = modifiers.get(i);
                modifierCopies.add(modifier == null ? null : modifier.deepClone());
            }
            cloned.modifiers = modifierCopies;
        }
        return cloned;
    }


    /**
     * 每秒调用一次
     */
    public void onSecondOver() {
        generatedCount = 0;
    }

    public void onEmitterTerminate(Vector3f systemCenter) {
        int missing = Math.max(0, getMin() - generatedCount);
        if (getMax() > 0) missing = Math.min(missing, Math.max(0, getMax() - generatedCount));
        for (int i = 0; i < missing && emitParticle(systemCenter); i++) generatedCount++;
    }

    public void generateParticles(Vector3f systemCenter) {
        if (texture == null || terminated) return;
        float particlesToCreate = Math.max(0f, pps) * Math.max(0f, DisplayManager.getFrameTimeSeconds());
        int count = (int) Math.floor(particlesToCreate);
        if (random.nextFloat() < particlesToCreate - count) count++;
        if (getMax() > 0) count = Math.min(count, Math.max(0, getMax() - generatedCount));
        if (maxAlive > 0) count = Math.min(count, Math.max(0, maxAlive - aliveCount));
        count = Math.min(count, Math.max(0, ParticleMaster.getMaxParticles() - ParticleMaster.getActiveCount()));
        Vector3f.sub(systemCenter, previousCenter, emitPosition);
        boolean interpolate = interpolatePosition && hasPreviousCenter
                && emitPosition.lengthSquared() <= maxInterpolationDistance * maxInterpolationDistance;
        for (int i = 0; i < count; i++) {
            if (interpolate) {
                float t = (i + 1f) / count;
                emitPosition.set(previousCenter.x + (systemCenter.x - previousCenter.x) * t,
                        previousCenter.y + (systemCenter.y - previousCenter.y) * t,
                        previousCenter.z + (systemCenter.z - previousCenter.z) * t);
            } else {
                emitPosition.set(systemCenter);
            }
            if (!emitParticle(emitPosition)) break;
            generatedCount++;
        }
        previousCenter.set(systemCenter);
        hasPreviousCenter = true;
    }

    private boolean emitParticle(Vector3f center) {
        if (texture == null || (maxAlive > 0 && aliveCount >= maxAlive)) return false;
        Particle p = ParticleMaster.obtain();
        if (p == null) return false;
        Vector3f velocity;
        if (direction != null && direction.lengthSquared() > .000001f) {
            velocity = generateRandomUnitVectorWithinCone(direction, (float) (Math.max(0f, Math.min(1f, directionDeviation)) * Math.PI));
        } else {
            velocity = generateRandomUnitVector();
        }
        velocity.scale(generateValue(speed, speedError));
        // Jitter belongs to a birth, never to a getter queried by aura modifiers.
        emitPosition.set(center);
        if (locationError != null) {
            emitPosition.x += generateValue(0f, locationError.x);
            emitPosition.y += generateValue(0f, locationError.y);
            emitPosition.z += generateValue(0f, locationError.z);
        }
        p.setActive(texture, emitPosition, velocity, gravity, generateValue(particleLife, particleLifeError),
                generateRotation(), generateValue(scale, scaleError));
        if (!p.isAlive()) {
            ParticleMaster.recycleUnused(p);
            return false;
        }
        if (color != null) p.setColor(color);
        if (frameIndex >= 0) {
            p.setFrameChgByModifier(true);
            p.setFrameIndex(frameIndex);
        }
        p.setModifiers(modifiers);
        p.setEmitter(this);
        return true;
    }

    private float generateValue(float average, float errorMargin) {
        float offset = (random.nextFloat() - 0.5f) * 2f * errorMargin;
        return average + offset;
    }

    private Vector3f generateRotation() {
        if (randomRotation) {
            rotation.x = 0;
            rotation.y = 0;
            rotation.z = random.nextFloat() * 360f;
            return rotation;
        } else {
            return null;
        }
    }

    private Vector3f generateRandomUnitVectorWithinCone(Vector3f coneDirection, float angle) {
        float cosAngle = (float) Math.cos(angle);

        float theta = (float) (random.nextFloat() * 2f * Math.PI);
        float z = cosAngle + (random.nextFloat() * (1 - cosAngle));
        float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
        float x = (float) (rootOneMinusZSquared * Math.cos(theta));
        float y = (float) (rootOneMinusZSquared * Math.sin(theta));

        coneUnit.set(coneDirection);
        coneUnit.normalise();
        coneDirection = coneUnit;
        Vector4f direction = coneSample;
        direction.set(x, y, z, 0);
        if (coneDirection.x != 0 || coneDirection.y != 0 || (coneDirection.z != 0 && coneDirection.z != 1 && coneDirection.z != -1)) {
            Vector3f rotateAxis = Vector3f.cross(coneDirection, zAxis, coneAxis);
            rotateAxis.normalise();
            float rotateAngle = (float) Math.acos(Math.max(-1f, Math.min(1f, Vector3f.dot(coneDirection, zAxis))));
            Matrix4f rotationMatrix = localRotationMatrix;
            rotationMatrix.identity();
            rotationMatrix.rotate(-rotateAngle, rotateAxis);
            Matrix4f.transform(rotationMatrix, direction, direction);
        } else if (coneDirection.z == -1) {
            direction.z *= -1;
        }
        emitVelocity.set(direction.x, direction.y, direction.z);
        return emitVelocity;
    }


    private Vector3f generateRandomUnitVector() {
        float theta = (float) (random.nextFloat() * 2f * Math.PI);
        float z = (random.nextFloat() * 2) - 1;
        float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
        float x = (float) (rootOneMinusZSquared * Math.cos(theta));
        float y = (float) (rootOneMinusZSquared * Math.sin(theta));
        emitVelocity.set(x, y, z);
        return emitVelocity;
    }

    /**
     * ======================================================
     * setter
     * ======================================================
     */

    public float getStartAt() {
        return startAt;
    }

    public void setStartAt(float startAt) {
        this.startAt = startAt;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setImageGrids(int imageGrids) {
        this.imageGrids = imageGrids;
    }

    public void setLocation(Vector3f location) {
        this.location = location;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public void setDirectionDeviation(float directionDeviation) {
        this.directionDeviation = directionDeviation;
    }

    public void setEmitterLife(float emitterLife) {
        this.emitterLife = emitterLife;
    }

    public void setPps(float pps) {
        this.pps = pps;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public void setParticleLife(float particleLife) {
        this.particleLife = particleLife;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setSpeedError(float speedError) {
        this.speedError = speedError;
    }

    public void setParticleLifeError(float particleLifeError) {
        this.particleLifeError = particleLifeError;
    }

    public void setScaleError(float scaleError) {
        this.scaleError = scaleError;
    }

    public void setRandomRotation(boolean randomRotation) {
        this.randomRotation = randomRotation;
    }

    public void setModifiers(List<ParticleModifier> modifiers) {
        this.modifiers = modifiers;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public float getDirectionDeviation() {
        return directionDeviation;
    }

    public float getPps() {
        return pps;
    }

    public float getSpeed() {
        return speed;
    }

    public Vector4f getColor() {
        return color;
    }

    public float getSpeedError() {
        return speedError;
    }

    public float getGravity() {
        return gravity;
    }

    public float getParticleLife() {
        return particleLife;
    }

    public float getParticleLifeError() {
        return particleLifeError;
    }

    public float getScale() {
        return scale;
    }

    public float getScaleError() {
        return scaleError;
    }

    public boolean isRandomRotation() {
        return randomRotation;
    }

    public List<ParticleModifier> getModifiers() {
        return modifiers;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public Vector3f getLocation() {
        return location;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public float getAlpha() {
        return color.w;
    }

    public void setAlpha(float alpha) {
        this.color.w = alpha;
    }

    public void setColor(Vector4f color) {
        this.color = color;
    }

    public void setFps(int fps) {
        this.fps = fps;
    }

    public void setLocationError(Vector3f locationError) {
        this.locationError = locationError;
    }

    public void setImg(GImage img, int rows, boolean additivePara, boolean depthTestPara) {
        this.img = img;
        imageGrids = Math.max(1, rows);
        additive = additivePara;
        depthTest = depthTestPara;
        if (img != null) {
            texture = new ParticleTexture(img, imageGrids, additivePara, depthTestPara);
        } else {
            SysLog.error("G3D|load image error " + imagePath);
        }
    }

    public GImage getImg() {
        return img;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public boolean isAdditive() {
        return additive;
    }

    public void setAdditive(boolean additive) {
        this.additive = additive;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }


    public boolean isDepthTest() {
        return depthTest;
    }

    public void setDepthTest(boolean depthTest) {
        this.depthTest = depthTest;
    }
}
