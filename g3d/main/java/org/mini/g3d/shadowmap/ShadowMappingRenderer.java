package org.mini.g3d.shadowmap;

import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.Light;
import org.mini.g3d.core.MasterRenderer;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.terrain.Terrain;
import org.mini.gl.GLMath;

import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

public class ShadowMappingRenderer extends AbstractRenderer {

    private final ShadowMappingShader shadowMappingShader = new ShadowMappingShader();
    private final Matrix4f depthProjection = new Matrix4f();
    private final Matrix4f depthView = new Matrix4f();
    private final Matrix4f depthMVP = new Matrix4f();
    private final Matrix4f depthPV = new Matrix4f();
    private final Matrix4f depthBiasMVP = new Matrix4f();
    private final Matrix4f depthModel = new Matrix4f();
    private final Matrix4f transformationMatrix = new Matrix4f();
    float[] eye = new float[3];
    float[] center = new float[]{0.0f, 0.0f, 0.0f};
    float[] up = new float[]{0.0f, 1.0f, 0.0f};

    public static int triangles = 0;

    static final float[] biasMatrix = {
            0.5f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.5f, 0.0f,
            0.5f, 0.5f, 0.5f, 1.0f
    };
    ShadowMappingFrameBuffer shadowMappingFbo;


    public ShadowMappingRenderer(ShadowMappingFrameBuffer shadowMappingFbo) {
        this.shadowMappingFbo = shadowMappingFbo;
    }

    void updateDepthMVP(Scene scene) {
        Light light = scene.getSun();
        Vector3f pos = light.getPosition();
        if (pos.x != eye[0] || pos.y != eye[1] || pos.z != eye[2]) {
            // MVP from light poisition
            Terrain t = scene.getTerrain();
            //static public native float[] mat4x4_ortho(float[] rm, float l, float r, float b, float t, float n, float f);
            GLMath.mat4x4_ortho(depthProjection.mat, t.getMin().x - 20.0f, t.getMax().x + 20.0f, t.getMin().z - 20.0f, t.getMax().z + 20.0f, 10f, 1000.0f);
            light.getPosition().store(eye);
            GLMath.mat4x4_look_at(depthView.mat, eye, center, up);
            GLMath.mat4x4_identity(depthModel.mat);
            GLMath.mat4x4_mul(depthPV.mat, depthProjection.mat, depthView.mat);
            GLMath.mat4x4_mul(depthMVP.mat, depthPV.mat, depthModel.mat);
            GLMath.mat4x4_mul(depthBiasMVP.mat, biasMatrix, depthMVP.mat);
            shadowMappingFbo.setDepthBiasMVP(depthBiasMVP);
        }
    }

//    void updateDepthMVP(Scene scene) {
//        Light light = scene.getSun();
//        Vector3f pos = light.getPosition();
//        if (pos.x != eye[0] || pos.y != eye[1] || pos.z != eye[2]) {
//            // MVP from light poisition
//            //static public native float[] mat4x4_ortho(float[] rm, float l, float r, float b, float t, float n, float f);
//            Terrain t = scene.getTerrain();
//            float midx = (t.getMax().x - t.getMin().x) * .5f;
//            float midz = (t.getMax().z - t.getMin().z) * .5f;
////            eye[0] = midx + 1;
////            eye[2] = midz + 1;
//            float h = t.getHeightOfTerrain(midx, midz);
//            float dx = pos.x / pos.y * h;
//            float dz = pos.z / pos.y * h;
//            GLMath.mat4x4_ortho(depthProjection.mat, t.getMin().x - dx, t.getMax().x - dx, t.getMin().z - dz, t.getMax().z - dz, 10f, 1000.0f);
//            light.getPosition().store(eye);
//            GLMath.mat4x4_look_at(depthView.mat, eye, center, up);
//            GLMath.mat4x4_identity(depthModel.mat);
//            GLMath.mat4x4_mul(depthPV.mat, depthProjection.mat, depthView.mat);
//            GLMath.mat4x4_mul(depthMVP.mat, depthPV.mat, depthModel.mat);
//            GLMath.mat4x4_mul(depthBiasMVP.mat, biasMatrix, depthMVP.mat);
//            shadowMappingFbo.setDepthBiasMVP(depthBiasMVP);
//        }
//    }


    public void render(Scene scene) {
        updateDepthMVP(scene);
        shadowMappingShader.start();

//        for (Terrain t : terrains) {
//            glBindVertexArray(t.getModel().getVaoID());
//            glEnableVertexAttribArray(0);
//            glEnableVertexAttribArray(1);
//            glEnableVertexAttribArray(2);
//
//            Matrix4f transformationMatrix = G3dMath.createTransformationMatrix(new Vector3f(t.getX(), 0, t.getZ()), 0, 0, 0, 1);
//            GLUtil.mat4x4_mul(transformationMatrix.mat, depthPV.mat, transformationMatrix.mat);
//            shadowMappingShader.loadDepthMVP(transformationMatrix);
//            glDrawElements(GL_TRIANGLES, t.getModel().getVertexCount(), GL_UNSIGNED_INT, null, 0);//gust
//            triangles += t.getModel().getVertexCount();
//
//            glDisableVertexAttribArray(0);
//            glDisableVertexAttribArray(1);
//            glDisableVertexAttribArray(2);
//            glBindVertexArray(0);
//        }

        Map<TexturedModel, List<Entity>> entities = scene.getEntitieMap();
        entities.forEach((texturedModel, batch) -> {
            //multithread , if scene.clear() may batch is null
            if (texturedModel == null || batch == null) {
                return;
            }
            RawModel rawModel = texturedModel.getRawModel();
            Texture texture = texturedModel.getTexture();
            if (rawModel.isCullingBack()) {
                enableCulling();
            } else {
                disableCulling();
            }

            glBindVertexArray(rawModel.getVaoID());
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getID());

            for (int i = 0, imax = batch.size(); i < imax; i++) {
                Entity entity = batch.get(i);

                G3dUtil.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale(), transformationMatrix);
                GLMath.mat4x4_mul(transformationMatrix.mat, depthPV.mat, transformationMatrix.mat);
                shadowMappingShader.loadDepthMVP(transformationMatrix);
                glDrawElements(GL_TRIANGLES, rawModel.getVertexCount(), GL_UNSIGNED_INT, null, 0);
                triangles += rawModel.getVertexCount();
            }
            enableCulling();
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(2);
            glBindVertexArray(0);
        });
        shadowMappingShader.stop();
    }


    public void reset() {
        eye[0] = eye[1] = eye[2] = -1f;//change it
    }
}
