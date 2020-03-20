package org.mini.g3d.particles;


import org.mini.g3d.core.Camera;
import org.mini.g3d.core.Loader;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

public class ParticleRenderer {

    private static final float[] VERTICES = {-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f, -0.5f};
    private static final int MAX_INSTANCES = 1000;
    private static final int INSTANCE_DATA_LENGTH = 21;

//    private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_INSTANCES * INSTANCE_DATA_LENGTH);

    private RawModel quad;
    private ParticleShader shader;

    private Loader loader;
    private int vbo;
    private int pointer = 0;

    protected ParticleRenderer(Loader loader, Matrix4f projectionMatrix) {
        this.loader = loader;
        this.vbo = loader.createEmptyFloatVbo(INSTANCE_DATA_LENGTH * MAX_INSTANCES);
        this.quad = loader.loadToVAO(this.VERTICES, 2);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 1, 4, INSTANCE_DATA_LENGTH, 0);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 2, 4, INSTANCE_DATA_LENGTH, 4);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 3, 4, INSTANCE_DATA_LENGTH, 8);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 4, 4, INSTANCE_DATA_LENGTH, 12);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 5, 4, INSTANCE_DATA_LENGTH, 16);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 6, 1, INSTANCE_DATA_LENGTH, 20);
        shader = new ParticleShader();
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    protected void render(Map<ParticleTexture, List<Particle>> particles, Camera camera) {
        Matrix4f viewMatrix = G3dMath.createViewMatrix(camera);
        prepare();
        for (ParticleTexture texture : particles.keySet()) {
            bindTexture(texture);
            List<Particle> particleList = particles.get(texture);
            pointer = 0;
            float[] vboData = new float[particleList.size() * INSTANCE_DATA_LENGTH];
            for (Particle particle : particleList) {
                updateModelViewMatrix(particle.getPosition(), particle.getRotation(),
                        particle.getScale(), viewMatrix, vboData);
                updateTexCoordInfo(particle, vboData);
            }
            loader.updateVbo(vbo, vboData);
            glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, quad.getVertexCount(), particleList.size());
        }
        finishRendering();
    }

    public void reloadProjectionMatrix(Matrix4f projectionMatrix) {
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }


    protected void cleanUp() {
        shader.cleanUp();
    }

    private void updateTexCoordInfo(Particle particle, float[] data) {
        data[pointer++] = particle.getTexOffset1().x;
        data[pointer++] = particle.getTexOffset1().y;
        data[pointer++] = particle.getTexOffset2().x;
        data[pointer++] = particle.getTexOffset2().y;
        data[pointer++] = particle.getBlend();
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

    private void updateModelViewMatrix(Vector3f position, float rotation, float scale,
                                       Matrix4f viewMatrix, float[] vboData) {
        Matrix4f modelMatrix = new Matrix4f();
        Matrix4f.translate(position, modelMatrix, modelMatrix);
        modelMatrix.mat[Matrix4f.M00] = viewMatrix.mat[Matrix4f.M00];
        modelMatrix.mat[Matrix4f.M01] = viewMatrix.mat[Matrix4f.M10];
        modelMatrix.mat[Matrix4f.M02] = viewMatrix.mat[Matrix4f.M20];
        modelMatrix.mat[Matrix4f.M10] = viewMatrix.mat[Matrix4f.M01];
        modelMatrix.mat[Matrix4f.M11] = viewMatrix.mat[Matrix4f.M11];
        modelMatrix.mat[Matrix4f.M12] = viewMatrix.mat[Matrix4f.M21];
        modelMatrix.mat[Matrix4f.M20] = viewMatrix.mat[Matrix4f.M02];
        modelMatrix.mat[Matrix4f.M21] = viewMatrix.mat[Matrix4f.M12];
        modelMatrix.mat[Matrix4f.M22] = viewMatrix.mat[Matrix4f.M22];
        Matrix4f modelViewMatrix = Matrix4f.mul(viewMatrix, modelMatrix, null);
        Matrix4f.rotate((float) Math.toRadians(rotation), new Vector3f(0, 0, 1), modelViewMatrix, modelViewMatrix);
        Matrix4f.scale(new Vector3f(scale, scale, scale), modelViewMatrix, modelViewMatrix);
        modelViewMatrix.store(vboData);
        System.arraycopy(modelViewMatrix.mat, 0, vboData, pointer, 16);
        pointer += 16;
    }

    private void prepare() {
        shader.start();
        glBindVertexArray(quad.getVaoID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glEnableVertexAttribArray(5);
        glEnableVertexAttribArray(6);
        glEnable(GL_BLEND);
        // glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(GL_FALSE);
    }

    private void finishRendering() {
        glDepthMask(GL_TRUE);
        glDisable(GL_BLEND);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glDisableVertexAttribArray(5);
        glDisableVertexAttribArray(6);
        glBindVertexArray(0);
        shader.stop();
    }
}
