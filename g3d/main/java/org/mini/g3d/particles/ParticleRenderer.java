package org.mini.g3d.particles;


import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.util.Loader;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

public class ParticleRenderer extends AbstractRenderer {

    private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
    private static final int MAX_INSTANCES = 2000;
    float[] vboData = new float[MAX_INSTANCES * INSTANCE_DATA_LENGTH];
    private static final int INSTANCE_DATA_LENGTH = 21;
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

    public void render(Map<ParticleTexture, List<Particle>> particles, Scene scene) {
        prepare();
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();
        Matrix4f projectionMatrix = scene.getCamera().getProjectionMatrix();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.loadViewMatrix(viewMatrix);
        int depthTestState = glIsEnabled(GL_DEPTH_TEST);

        for (ParticleTexture texture : particles.keySet()) {
            if (texture.isDepthTest()) {
                glEnable(GL_DEPTH_TEST);
            } else {
                glDisable(GL_DEPTH_TEST);
            }
            bindTexture(texture);
            List<Particle> particleList = particles.get(texture);
            for (int i = 0; i < particleList.size(); i++) {
                Particle particle = particleList.get(i);
                updateModelData(particle, vboData, i * INSTANCE_DATA_LENGTH);
            }
            loader.updateVbo(vbo, vboData);
            glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, quad.getVertexCount(), particleList.size());
        }
        if (depthTestState == GL_TRUE) {
            glEnable(GL_DEPTH_TEST);
        } else {
            glDisable(GL_DEPTH_TEST);
        }
        finish();
    }

    public void cleanUp() {
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
            glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        } else {
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
