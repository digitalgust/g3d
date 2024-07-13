package org.mini.g3d.particles;

import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.Sound;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.vector.Vector4f;
import org.mini.gui.GImage;
import org.mini.gui.GToolkit;

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
    public int max;// max particles
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
    public Vector3f offsetLocation;
    public Vector3f rotation = new Vector3f();
    public Vector3f runtimeLocation = new Vector3f();
    public ParticleTexture texture;
    public int generatedCount = 0;
    static Vector3f zAxis = new Vector3f(0, 0, 1);
    static Matrix4f localRotationMatrix = new Matrix4f();


    public void update() {
        if (startuped && !terminated) {
            generateParticles(getEmitLocation());
        }
    }

    public void startup() {
        if (!startuped) {
            startuped = true;
            GImage img = GToolkit.getCachedImageFromJar(imagePath);
            if (img != null) {
                setImg(img, additive, depthTest);
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
        runtimeLocation.x += (random.nextFloat() - 0.5f) * 2f * locationError.x;
        runtimeLocation.y += (random.nextFloat() - 0.5f) * 2f * locationError.y;
        runtimeLocation.z += (random.nextFloat() - 0.5f) * 2f * locationError.z;
        return runtimeLocation;
    }

    public void addModifier(ParticleModifier modifier) {
        if (modifiers == null) {
            modifiers = new ArrayList<>();
        }
        modifiers.add(modifier);
    }


    /**
     * 每秒调用一次
     */
    public void onSecondOver() {
        generatedCount = 0;
    }

    public void onEmitterTerminate(Vector3f systemCenter) {
        for (int i = generatedCount; i < getMin(); i++) {
            emitParticle(systemCenter);
        }
    }

    public void generateParticles(Vector3f systemCenter) {
        float delta = DisplayManager.getFrameTimeSeconds();
        float particlesToCreate = pps * delta;
        int count = (int) Math.floor(particlesToCreate);
        //不足1的部分按概率发射
        float partialParticle = particlesToCreate % 1;
        if (random.nextFloat() < partialParticle) {
            count++;
        }
        if (getMax() > 0) {
            if (generatedCount + count > getMax()) {
                count = getMax() - generatedCount;
            }
        }

        for (int i = 0; i < count; i++) {
            emitParticle(systemCenter);
        }
        generatedCount += count;

    }

    private void emitParticle(Vector3f center) {
        Vector3f velocity;
        if (direction != null) {
            velocity = generateRandomUnitVectorWithinCone(direction, (float) (directionDeviation * Math.PI));
        } else {
            velocity = generateRandomUnitVector();
        }
        velocity.normalise();
        velocity.scale(generateValue(speed, speedError));
        float scaletmp = generateValue(scale, scaleError);
        float lifeLength = generateValue(particleLife, particleLifeError);
        Particle p = new Particle();
        p.setActive(texture, new Vector3f(center), velocity, gravity, lifeLength, generateRotation(), scaletmp);
        p.setColor(color);
        if (frameIndex >= 0) {
            p.setFrameChgByModifier(true);
            p.setFrameIndex(frameIndex);
        }
        p.setModifiers(modifiers);
        p.setEmitter(this);
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

        Vector4f direction = new Vector4f(x, y, z, 1);
        if (coneDirection.x != 0 || coneDirection.y != 0 || (coneDirection.z != 0 && coneDirection.z != 1 && coneDirection.z != -1)) {
            Vector3f rotateAxis = Vector3f.cross(coneDirection, zAxis, null);
            rotateAxis.normalise();
            float rotateAngle = (float) Math.acos(Vector3f.dot(coneDirection, zAxis));
            Matrix4f rotationMatrix = localRotationMatrix;
            rotationMatrix.identity();
            rotationMatrix.rotate(-rotateAngle, rotateAxis);
            Matrix4f.transform(rotationMatrix, direction, direction);
        } else if (coneDirection.z == -1) {
            direction.z *= -1;
        }
        return new Vector3f(direction);
    }


    private Vector3f generateRandomUnitVector() {
        float theta = (float) (random.nextFloat() * 2f * Math.PI);
        float z = (random.nextFloat() * 2) - 1;
        float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
        float x = (float) (rootOneMinusZSquared * Math.cos(theta));
        float y = (float) (rootOneMinusZSquared * Math.sin(theta));
        return new Vector3f(x, y, z);
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

    public void setImg(GImage img, boolean additivePara, boolean depthTestPara) {
        this.img = img;
        if (img != null) {
            texture = new ParticleTexture(img.getGLTextureId(), imageGrids, additivePara, depthTestPara);
        } else {
            System.out.println("[G3D][ERROR]load image error " + imagePath);
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
