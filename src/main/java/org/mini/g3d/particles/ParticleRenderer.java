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
    private static final int MAX_INSTANCES = 1000;
    private static final int INSTANCE_DATA_LENGTH = 25;
    Loader loader = new Loader();

//    private static final FloatBuffer buffer = BufferUtils.createFloatBuffer(MAX_INSTANCES * INSTANCE_DATA_LENGTH);

    private RawModel quad;
    private ParticleShader shader;

    private int vbo;
    private int pointer = 0;

    //缓存 局部变量 不每次都 new
    Matrix4f modelMatrix = new Matrix4f();
    Matrix4f modelViewMatrix = new Matrix4f();
    Vector3f scaler = new Vector3f();


    public ParticleRenderer() {

        this.vbo = loader.createEmptyFloatVbo(INSTANCE_DATA_LENGTH * MAX_INSTANCES);
        this.quad = loader.loadToVAO(this.VERTICES, 2);

        loader.addInstancedAttribute(quad.getVaoID(), vbo, 1, 4, INSTANCE_DATA_LENGTH, 0);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 2, 4, INSTANCE_DATA_LENGTH, 4);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 3, 4, INSTANCE_DATA_LENGTH, 8);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 4, 4, INSTANCE_DATA_LENGTH, 12);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 5, 4, INSTANCE_DATA_LENGTH, 16);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 6, 1, INSTANCE_DATA_LENGTH, 20);
        loader.addInstancedAttribute(quad.getVaoID(), vbo, 7, 4, INSTANCE_DATA_LENGTH, 21);
        shader = new ParticleShader();
    }

    public void render(Map<ParticleTexture, List<Particle>> particles, Scene scene) {
        prepare();
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();
        Matrix4f projectionMatrix = scene.getCamera().getProjectionMatrix();
        shader.loadProjectionMatrix(projectionMatrix);

        for (ParticleTexture texture : particles.keySet()) {
            bindTexture(texture);
            List<Particle> particleList = particles.get(texture);
            pointer = 0;
            float[] vboData = new float[particleList.size() * INSTANCE_DATA_LENGTH];
            for (Particle particle : particleList) {
                updateModelViewMatrix(particle, viewMatrix, vboData);
                updateTexCoordInfo(particle, vboData);
            }
            loader.updateVbo(vbo, vboData);
            glDrawArraysInstanced(GL_TRIANGLE_STRIP, 0, quad.getVertexCount(), particleList.size());
        }
        finish();
    }

    public void cleanUp() {
    }

    private void updateTexCoordInfo(Particle particle, float[] data) {
        data[pointer++] = particle.getTexOffset1().x;
        data[pointer++] = particle.getTexOffset1().y;
        data[pointer++] = particle.getTexOffset2().x;
        data[pointer++] = particle.getTexOffset2().y;
        data[pointer++] = particle.getFrameBlend();
        data[pointer++] = particle.getColor().x;
        data[pointer++] = particle.getColor().y;
        data[pointer++] = particle.getColor().z;
        data[pointer++] = particle.getColor().w;
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

    private void updateModelViewMatrix(Particle particle,
                                       Matrix4f viewMatrix, float[] vboData) {

        Vector3f position = particle.getPosition();
        Vector3f rotation = particle.getRotation();
        float scale = particle.getScale();
        modelMatrix.identity();
        Matrix4f.translate(position, modelMatrix, modelMatrix);


        //如果面朝报像机,则进行转置,使朝向Z轴
        if (particle.isOrientCamera()) {
            //此处非mat4.transpose, 只转置了3x3 非4x4
            modelMatrix.mat[Matrix4f.M00] = viewMatrix.mat[Matrix4f.M00];
            modelMatrix.mat[Matrix4f.M01] = viewMatrix.mat[Matrix4f.M10];
            modelMatrix.mat[Matrix4f.M02] = viewMatrix.mat[Matrix4f.M20];
            modelMatrix.mat[Matrix4f.M10] = viewMatrix.mat[Matrix4f.M01];
            modelMatrix.mat[Matrix4f.M11] = viewMatrix.mat[Matrix4f.M11];
            modelMatrix.mat[Matrix4f.M12] = viewMatrix.mat[Matrix4f.M21];
            modelMatrix.mat[Matrix4f.M20] = viewMatrix.mat[Matrix4f.M02];
            modelMatrix.mat[Matrix4f.M21] = viewMatrix.mat[Matrix4f.M12];
            modelMatrix.mat[Matrix4f.M22] = viewMatrix.mat[Matrix4f.M22];
        }

        Matrix4f.mul(viewMatrix, modelMatrix, modelViewMatrix);
        Matrix4f.rotate((float) Math.toRadians(rotation.x), 1, 0, 0, modelViewMatrix, modelViewMatrix);
        Matrix4f.rotate((float) Math.toRadians(rotation.y), 0, 1, 0, modelViewMatrix, modelViewMatrix);
        Matrix4f.rotate((float) Math.toRadians(rotation.z), 0, 0, 1, modelViewMatrix, modelViewMatrix);
        //Matrix4f.rotate((float) Math.toRadians(rotation), 0, 0, 1, modelViewMatrix, modelViewMatrix);
        scaler.set(scale, scale, scale);
        Matrix4f.scale(scaler, modelViewMatrix, modelViewMatrix);
        modelViewMatrix.store(vboData);
        System.arraycopy(modelViewMatrix.mat, 0, vboData, pointer, 16);
        pointer += 16;
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
        // glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
