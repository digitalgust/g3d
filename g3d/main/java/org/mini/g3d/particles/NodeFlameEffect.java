package org.mini.g3d.particles;

import org.mini.g3d.animation.AnimatedModel;
import org.mini.g3d.animation.gltf2.loader.data.GLTFAccessor;
import org.mini.g3d.animation.gltf2.render.RenderMeshPrimitive;
import org.mini.g3d.animation.gltf2.render.RenderNode;
import org.mini.g3d.core.DisplayManager;
import org.mini.g3d.core.ICamera;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.vector.Vector4f;
import org.mini.gui.GImage;

/** Bounded, instance-local wisps and sparks sampled from a rigid equipment mesh. */
public final class NodeFlameEffect {
    private static final float SIZE_MULTIPLIER = 1.3f;
    private static final float BRIGHTNESS_MULTIPLIER = 1.25f;
    private final AnimatedModel model;
    private final RenderNode slot;
    private final FlameEmitter wisps;
    private final FlameEmitter sparks;
    private final Matrix4f world = new Matrix4f();
    private final Matrix4f viewProjection = new Matrix4f();
    private final Vector3f center = new Vector3f();
    private RenderNode visibleSlot;
    private RenderMeshPrimitive primitive;
    private float[] minimum, maximum;
    private int axis;
    private float localLength, worldLength;
    private float red, green, blue, strength = 1f;
    private long lastFrame = -1;

    public NodeFlameEffect(AnimatedModel model, RenderNode slot, GImage texture) {
        this.model = model;
        this.slot = slot;
        wisps = new FlameEmitter(texture, false);
        sparks = new FlameEmitter(texture, true);
    }

    public RenderNode getSlot() { return slot; }

    public void setColor(float red, float green, float blue, float strength) {
        this.red = Math.max(0f, Math.min(1f, red));
        this.green = Math.max(0f, Math.min(1f, green));
        this.blue = Math.max(0f, Math.min(1f, blue));
        this.strength = Math.max(0f, Math.min(2f, strength));
    }

    public void clear() {
        wisps.cancel();
        sparks.cancel();
        ParticleMaster.removeParticleIf(p -> p.getEmitter() == wisps || p.getEmitter() == sparks);
    }

    public void update(ICamera camera) {
        long frame = DisplayManager.getFrameCount();
        if (frame == lastFrame) return;
        lastFrame = frame;
        Vector3f eye = camera.getPosition(), position = model.getPosition();
        float dx = eye.x - position.x, dy = eye.y - position.y, dz = eye.z - position.z;
        float distance2 = dx * dx + dy * dy + dz * dz;
        if (distance2 > 65f * 65f) return;

        RenderNode visible = slot.getReplacer() == null ? slot : slot.getReplacer();
        if (visible != visibleSlot) {
            visibleSlot = visible;
            primitive = null;
            localLength = 0f;
            findPrimitive(visible);
        }
        if (primitive == null) return;
        // Mirror AnimatedModelRenderer's cached-current-frame matrix selection exactly.
        Matrix4f[] cached = primitive.getModelMatrics();
        if (cached != null) {
            int keyFrame = primitive.getAnimatedModel().getCurKeyFrame();
            if (keyFrame >= 0 && keyFrame < cached.length) {
                Matrix4f.mul(primitive.getAnimatedModel().getTransform(), cached[keyFrame], world);
            } else {
                world.load(primitive.getWorldTransform());
            }
        } else {
            world.load(primitive.getWorldTransform());
        }
        float[] m = world.mat;
        int column = axis * 4;
        worldLength = localLength * (float) Math.sqrt(m[column] * m[column]
                + m[column + 1] * m[column + 1] + m[column + 2] * m[column + 2]);
        if (!(worldLength > .01f && worldLength < 20f)) return;
        transformPoint((minimum[0] + maximum[0]) * .5f,
                (minimum[1] + maximum[1]) * .5f, (minimum[2] + maximum[2]) * .5f, center);
        if (!inView(camera)) return;
        float lod = distance2 < 22f * 22f ? 1f : distance2 < 45f * 45f ? .6f : .25f;
        wisps.pps = 32f * strength * lod;
        wisps.scale = Math.min(.7f, worldLength * .17f) * SIZE_MULTIPLIER;
        sparks.pps = 12f * strength * lod;
        sparks.scale = Math.min(.16f, worldLength * .035f) * SIZE_MULTIPLIER;
        wisps.generateParticles(center);
        sparks.generateParticles(center);
    }

    private boolean inView(ICamera camera) {
        Matrix4f.mul(camera.getProjectionMatrix(), camera.getViewMatrix(), viewProjection);
        float[] m = viewProjection.mat;
        // Conservative sphere includes the blade, wisps and outgoing sparks near screen edges.
        float radius = worldLength * 1.5f + .5f;
        for (int row = 0; row < 3; row++) {
            for (int sign = -1; sign <= 1; sign += 2) {
                float a = m[3] + sign * m[row];
                float b = m[7] + sign * m[4 + row];
                float c = m[11] + sign * m[8 + row];
                float d = m[15] + sign * m[12 + row];
                if (a * center.x + b * center.y + c * center.z + d
                        < -radius * (float) Math.sqrt(a * a + b * b + c * c)) return false;
            }
        }
        return true;
    }

    private void findPrimitive(RenderNode node) {
        if (node instanceof RenderMeshPrimitive) {
            RenderMeshPrimitive candidate = (RenderMeshPrimitive) node;
            // This path handles rigid replacements, not vertices needing CPU skinning.
            if (candidate.getMesh().getSkin() == null && !candidate.isSkip()) {
                GLTFAccessor positions = candidate.getGlAttributes().get("a_Position");
                float[] min = positions.getMin(), max = positions.getMax();
                if (min != null && max != null && min.length >= 3 && max.length >= 3) {
                    for (int i = 0; i < 3; i++) {
                        float length = max[i] - min[i];
                        if (length > localLength) {
                            primitive = candidate;
                            minimum = min;
                            maximum = max;
                            axis = i;
                            localLength = length;
                        }
                    }
                }
            }
        }
        for (RenderNode child : node.getChildren()) {
            findPrimitive(child.getReplacer() == null ? child : child.getReplacer());
        }
    }

    private void transformPoint(float x, float y, float z, Vector3f result) {
        float[] m = world.mat;
        result.set(m[0] * x + m[4] * y + m[8] * z + m[12],
                m[1] * x + m[5] * y + m[9] * z + m[13],
                m[2] * x + m[6] * y + m[10] * z + m[14]);
    }

    private static void setComponent(Vector3f vector, int axis, float value) {
        if (axis == 0) vector.x = value;
        else if (axis == 1) vector.y = value;
        else vector.z = value;
    }

    private final class FlameEmitter extends Emitter {
        private final boolean spark;
        private final Vector3f localPosition = new Vector3f();
        private final Vector3f radial = new Vector3f();

        FlameEmitter(GImage texture, boolean spark) {
            this.spark = spark;
            setImg(texture, 1, true, true);
            particleLife = spark ? .65f : .52f;
            particleLifeError = spark ? .18f : .12f;
            maxAlive = spark ? 12 : 28;
            randomRotation = true;
            color.set(1f, 1f, 1f, .2f);
            addModifier(new ParticleModifier() {
                @Override
                public void update(Particle particle) {
                    float age = particle.getElapsedTime();
                    float t = Math.min(1f, age / particle.getLifeLength());
                    float heat = 1f - t;
                    updateColor(particle, heat);
                    particle.setScale(particle.getInitialScale()
                            * (spark ? .35f + .65f * heat : .55f + .55f * (float) Math.sin(t * Math.PI)));
                    // Gentle curl and upward drift; old particles stay in world space.
                    float phase = (particle.hashCode() & 1023) * .017f;
                    float curl = worldLength * (spark ? .35f : .55f) * DisplayManager.getFrameTimeSeconds();
                    particle.getVelocity().x += (float) Math.sin(age * 9f + phase) * curl;
                    particle.getVelocity().z += (float) Math.cos(age * 7f + phase) * curl;
                }
            });
        }

        private void updateColor(Particle particle, float heat) {
            // Start with a bright VIP tint, then recover the base color while fading out.
            // Additive blending uses alpha as light intensity; keep the texture's soft edge.
            float white = (spark ? .55f : .35f) * heat;
            particle.getColor().set(red + (1f - red) * white,
                    green + (1f - green) * white, blue + (1f - blue) * white,
                    Math.min(1f, (spark ? .95f : .65f) * BRIGHTNESS_MULTIPLIER * heat * heat));
        }

        @Override
        protected void onParticleCreated(Particle particle) {
            localPosition.set((minimum[0] + maximum[0]) * .5f,
                    (minimum[1] + maximum[1]) * .5f, (minimum[2] + maximum[2]) * .5f);
            setComponent(localPosition, axis, minimum[axis] + localLength * (.22f + .76f * random.nextFloat()));
            int a = (axis + 1) % 3, b = (axis + 2) % 3;
            float angle = random.nextFloat() * (float) (Math.PI * 2f);
            float cosine = (float) Math.cos(angle), sine = (float) Math.sin(angle);
            float radius = localLength * (spark ? .025f : .035f) * random.nextFloat();
            radial.zero();
            setComponent(radial, a, cosine * radius);
            setComponent(radial, b, sine * radius);
            localPosition.translate(radial);
            transformPoint(localPosition.x, localPosition.y, localPosition.z, particle.getPosition());
            radial.zero();
            setComponent(radial, a, cosine);
            setComponent(radial, b, sine);
            float[] m = world.mat;
            Vector3f velocity = particle.getVelocity();
            velocity.set(m[0] * radial.x + m[4] * radial.y + m[8] * radial.z,
                    m[1] * radial.x + m[5] * radial.y + m[9] * radial.z,
                    m[2] * radial.x + m[6] * radial.y + m[10] * radial.z);
            if (velocity.lengthSquared() > .000001f) velocity.normalise();
            velocity.scale(worldLength * (spark ? .8f : .32f) * (.65f + .7f * random.nextFloat()));
            velocity.y += worldLength * .28f;
            updateColor(particle, 1f);
            particle.setScale(particle.getInitialScale() * .6f);
        }
    }
}
