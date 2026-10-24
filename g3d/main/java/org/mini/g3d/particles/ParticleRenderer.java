package org.mini.g3d.particles;


import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.util.Loader;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import static org.mini.gl.GL.*;

public class ParticleRenderer extends AbstractRenderer {

    private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
    private static final int MAX_INSTANCES = 2000;
    float[] vboData = new float[MAX_INSTANCES * INSTANCE_DATA_LENGTH];
    private static final int INSTANCE_DATA_LENGTH = 17;
    Loader loader = new Loader();

    private RawModel quad;
    private ParticleShader shader;

    private int vbo;

    public ParticleRenderer() {

        this.vbo = loader.createEmptyFloatVbo(INSTANCE_DATA_LENGTH * MAX_INSTANCES);
        this.quad = loader.loadToVAO(this.VERTICES, 2);

        loader.addInstancedAttribute(quad.getVaoID(), vbo, 1, 3, INSTANCE_DATA_LENGTH, 0);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 2, 3, INSTANCE_DATA_LENGTH, 3);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 3, 1, INSTANCE_DATA_LENGTH, 6);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 4, 1, INSTANCE_DATA_LENGTH, 7);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 5, 4, INSTANCE_DATA_LENGTH, 8);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 6, 1, INSTANCE_DATA_LENGTH, 12);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 7, 4, INSTANCE_DATA_LENGTH, 13);
        shader = new ParticleShader();
    }

    private final List<Particle> ordered = new ArrayList<>();
    private final Comparator<Particle> backToFront = new Comparator<Particle>() {
        public int compare(Particle a, Particle b) {
            return Float.compare(viewDepth(b), viewDepth(a));
        }
    };
    private Matrix4f view;
    private boolean cleaned;
    private final int[] depthMask = new int[1];

    private float viewDepth(Particle particle) {
        Vector3f p = particle.getPosition();
        float[] m = view.mat;
        return -(m[2] * p.x + m[6] * p.y + m[10] * p.z + m[14]);
    }

    public void render(Map<ParticleTexture, List<Particle>> particles, Scene scene) {
        if (particles.isEmpty()) return;
        int depthTestState = glIsEnabled(GL_DEPTH_TEST);
        int blendState = glIsEnabled(GL_BLEND);
        glGetBooleanv(GL_DEPTH_WRITEMASK, depthMask, 0);
        prepare();
        try {
            view = scene.getCamera().getViewMatrix();
            shader.loadProjectionMatrix(scene.getCamera().getProjectionMatrix());
            shader.loadViewMatrix(view);
            // World particles first; intentional depth-disabled overlays form the last layer.
            for (int layer = 0; layer < 2; layer++) {
                boolean depthTest = layer == 0;
                boolean needsSorting = false;
                ordered.clear();
                for (Map.Entry<ParticleTexture, List<Particle>> entry : particles.entrySet()) {
                    if (entry.getKey().isDepthTest() == depthTest) {
                        ordered.addAll(entry.getValue());
                        needsSorting |= !entry.getKey().usesAdditiveBlending();
                    }
                }
                // Additive-only batches commute. With smoke/alpha, sort across ALL textures.
                if (needsSorting) Collections.sort(ordered, backToFront);
                if (depthTest) glEnable(GL_DEPTH_TEST); else glDisable(GL_DEPTH_TEST);
                int first = 0;
                while (first < ordered.size()) {
                    ParticleTexture texture = ordered.get(first).getTexture();
                    int end = first + 1;
                    while (end < ordered.size() && texture.equals(ordered.get(end).getTexture())) end++;
                    bindTexture(texture);
                    for (int base = first; base < end; base += MAX_INSTANCES) {
                        int count = Math.min(MAX_INSTANCES, end - base);
                        for (int i = 0; i < count; i++) {
                            updateModelData(ordered.get(base + i), vboData, i * INSTANCE_DATA_LENGTH);
                        }
                        loader.updateVbo(vbo, vboData, count * INSTANCE_DATA_LENGTH);
                        glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, quad.getVertexCount(), count);
                    }
                    first = end;
                }
            }
        } finally {
            ordered.clear();
            finish();
            glDepthMask(depthMask[0] != 0 ? GL_TRUE : GL_FALSE);
            if (depthTestState == GL_TRUE) glEnable(GL_DEPTH_TEST); else glDisable(GL_DEPTH_TEST);
            if (blendState == GL_TRUE) glEnable(GL_BLEND); else glDisable(GL_BLEND);
        }
    }

    public void cleanUp() {
        if (cleaned) return;
        cleaned = true;
        ordered.clear();
        shader.cleanUp();
        loader.cleanUp();
    }

    private void updateModelData(Particle particle, float[] data, int index) {
        Vector3f position = particle.getPosition();
        data[index++] = position.x;
        data[index++] = position.y;
        data[index++] = position.z;

        Vector3f rotation = particle.getRotation();
        data[index++] = rotation.x;
        data[index++] = rotation.y;
        data[index++] = rotation.z;

        data[index++] = particle.getScale();

        data[index++] = particle.isOrientCamera() ? 1.0f : 0.0f;

        data[index++] = particle.getTexOffset1().x;
        data[index++] = particle.getTexOffset1().y;
        data[index++] = particle.getTexOffset2().x;
        data[index++] = particle.getTexOffset2().y;
        data[index++] = particle.getFrameBlend();
        
        data[index++] = particle.getColor().x;
        data[index++] = particle.getColor().y;
        data[index++] = particle.getColor().z;
        data[index++] = particle.getColor().w;
    }

    private void bindTexture(ParticleTexture texture) {
        if (texture.usesAdditiveBlending()) {
            glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ZERO, GL_ONE);
        } else {
            glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        }
        // bind texture
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getTextureID());
        shader.loadNumberOfRows(texture.getNumberOfRows());
    }

    void prepare() {
        shader.start();
        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glEnableVertexAttribArray(5);
        glEnableVertexAttribArray(6);
        glEnableVertexAttribArray(7);
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glDepthMask(GL_FALSE);
    }

    void finish() {
        glDepthMask(GL_TRUE);
        glDisable(GL_BLEND);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glDisableVertexAttribArray(5);
        glDisableVertexAttribArray(6);
        glDisableVertexAttribArray(7);
        glBindVertexArray(0);
        shader.stop();
    }
}
