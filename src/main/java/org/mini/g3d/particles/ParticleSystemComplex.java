package org.mini.g3d.particles;


import org.mini.g3d.core.EngineManager;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.vector.Vector4f;

import java.util.Random;

public class ParticleSystemComplex  implements ParticleSystem {

    private float pps, averageSpeed, gravityComplient, averageLifeLength, averageScale;

    private float speedError, lifeError, scaleError = 0;
    private boolean randomRotation = false;
    private Vector3f direction;
    private float directionDeviation = 0;

    private ParticleTexture texture;

    private Random random = new Random();

    public ParticleSystemComplex(ParticleTexture texture, float pps, float speed, float gravityComplient, float lifeLength, float scale) {
        this.texture = texture;
        this.pps = pps;
        this.averageSpeed = speed;
        this.gravityComplient = gravityComplient;
        this.averageLifeLength = lifeLength;
        this.averageScale = scale;
    }

    /**
     * @param direction - The average direction in which particles are emitted.
     * @param deviation - A value between 0 and 1 indicating how far from the chosen direction particles can deviate.
     */
    public void setDirection(Vector3f direction, float deviation) {
        this.direction = new Vector3f(direction);
        this.directionDeviation = (float) (deviation * Math.PI);
    }

    public ParticleSystemComplex randomizeRotation() {
        randomRotation = true;
        return this;
    }

    /**
     * @param error - A number between 0 and 1, where 0 means no error margin.
     */
    public ParticleSystemComplex setSpeedError(float error) {
        this.speedError = error * averageSpeed;
        return this;
    }

    /**
     * @param error - A number between 0 and 1, where 0 means no error margin.
     */
    public ParticleSystemComplex setLifeError(float error) {
        this.lifeError = error * averageLifeLength;
        return this;
    }

    /**
     * @param error - A number between 0 and 1, where 0 means no error margin.
     */
    public ParticleSystemComplex setScaleError(float error) {
        this.scaleError = error * averageScale;
        return this;
    }

    public void generateParticles(Vector3f systemCenter) {
        float delta = EngineManager.getFrameTimeSeconds();
        float particlesToCreate = pps * delta;
        int count = (int) Math.floor(particlesToCreate);
        float partialParticle = particlesToCreate % 1;
        for (int i = 0; i < count; i++) {
            emitParticle(systemCenter);
        }
        if (random.nextFloat() < partialParticle) {
            emitParticle(systemCenter);
        }
    }

    private void emitParticle(Vector3f center) {
        Vector3f velocity = null;
        if (direction != null) {
            velocity = generateRandomUnitVectorWithinCone(direction, directionDeviation);
        } else {
            velocity = generateRandomUnitVector();
        }
        velocity.normalise();
        velocity.scale(generateValue(averageSpeed, speedError));
        float scale = generateValue(averageScale, scaleError);
        float lifeLength = generateValue(averageLifeLength, lifeError);
        new Particle().setActive(this.texture, new Vector3f(center), velocity, gravityComplient, lifeLength, generateRotation(), scale);
    }

    private float generateValue(float average, float errorMargin) {
        float offset = (random.nextFloat() - 0.5f) * 2f * errorMargin;
        return average + offset;
    }

    private float generateRotation() {
        if (randomRotation) {
            return random.nextFloat() * 360f;
        } else {
            return 0;
        }
    }

    private static Vector3f generateRandomUnitVectorWithinCone(Vector3f coneDirection, float angle) {
        float cosAngle = (float) Math.cos(angle);
        Random random = new Random();
        float theta = (float) (random.nextFloat() * 2f * Math.PI);
        float z = cosAngle + (random.nextFloat() * (1 - cosAngle));
        float rootOneMinusZSquared = (float) Math.sqrt(1 - z * z);
        float x = (float) (rootOneMinusZSquared * Math.cos(theta));
        float y = (float) (rootOneMinusZSquared * Math.sin(theta));

        Vector4f direction = new Vector4f(x, y, z, 1);
        if (coneDirection.x != 0 || coneDirection.y != 0 ||
                (coneDirection.z != 0 && coneDirection.z != 1 && coneDirection.z != -1)) {
            Vector3f rotateAxis = Vector3f.cross(coneDirection, new Vector3f(0, 0, 1), null);
            rotateAxis.normalise();
            float rotateAngle = (float) Math.acos(Vector3f.dot(coneDirection, new Vector3f(0, 0, 1)));
            Matrix4f rotationMatrix = new Matrix4f();
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
}
