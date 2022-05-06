package org.mini.g3d.shadowmap;

import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.Light;
import org.mini.g3d.core.MasterRenderer;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.core.textures.ModelTexture;
import org.mini.glwrap.GLUtil;
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
    ShadowMappingPass shadowMappingPass;


    public ShadowMappingRenderer(ShadowMappingPass shadowMappingPass) {
        this.shadowMappingPass = shadowMappingPass;
    }

    void updateDepthMVP(Light light) {
        Vector3f pos = light.getPosition();
        if (pos.x != eye[0] || pos.y != eye[1] || pos.z != eye[2]) {
            // MVP from light poisition
            GLMath.mat4x4_ortho(depthProjection.mat, -20.0f, 1080.0f, -20.0f, 1080.0f, 400f, 1000.0f);
            light.getPosition().store(eye);
            GLMath.mat4x4_look_at(depthView.mat, eye, center, up);
            GLMath.mat4x4_identity(depthModel.mat);
            GLMath.mat4x4_mul(depthPV.mat, depthProjection.mat, depthView.mat);
            GLMath.mat4x4_mul(depthMVP.mat, depthPV.mat, depthModel.mat);
            GLMath.mat4x4_mul(depthBiasMVP.mat, biasMatrix, depthMVP.mat);
        }
    }

    public Matrix4f getDepthBiasMVP() {
        return depthBiasMVP;
    }


    public void render(Light light) {
        updateDepthMVP(light);
        shadowMappingShader.start();

//        for (Terrain t : terrains) {
//            glBindVertexArray(t.getModel().getVaoID());
//            glEnableVertexAttribArray(0);
//            glEnableVertexAttribArray(1);
//            glEnableVertexAttribArray(2);
//
//            Matrix4f transformationMatrix = G3dMath.createTransformationMatrix(new Vector3f(t.getX(), 0, t.getZ()), 0, 0, 0, 1);
//            GLMath.mat4x4_mul(transformationMatrix.mat, depthPV.mat, transformationMatrix.mat);
//            shadowMappingShader.loadDepthMVP(transformationMatrix);
//            glDrawElements(GL_TRIANGLES, t.getModel().getVertexCount(), GL_UNSIGNED_INT, null, 0);//gust
//            triangles += t.getModel().getVertexCount();
//
//            glDisableVertexAttribArray(0);
//            glDisableVertexAttribArray(1);
//            glDisableVertexAttribArray(2);
//            glBindVertexArray(0);
//        }

        Map<TexturedModel, List<Entity>> entities = getEntities();
        for (TexturedModel texturedModel : entities.keySet()) {
            RawModel rawModel = texturedModel.getRawModel();
            ModelTexture texture = texturedModel.getTexture();
            if (texture.isHasTransparency()) {
                MasterRenderer.disableCulling();
            }

            glBindVertexArray(rawModel.getVaoID());
            glEnableVertexAttribArray(0);
            glEnableVertexAttribArray(1);
            glEnableVertexAttribArray(2);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, texture.getID());

            List<Entity> batch = entities.get(texturedModel);
            for (int i = 0, imax = batch.size(); i < imax; i++) {
                Entity entity = batch.get(i);

                G3dMath.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale(), transformationMatrix);
                GLMath.mat4x4_mul(transformationMatrix.mat, depthPV.mat, transformationMatrix.mat);
                shadowMappingShader.loadDepthMVP(transformationMatrix);
                glDrawElements(GL_TRIANGLES, rawModel.getVertexCount(), GL_UNSIGNED_INT, null, 0);
                triangles += rawModel.getVertexCount();
            }
            MasterRenderer.enableCulling();
            glDisableVertexAttribArray(0);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(2);
            glBindVertexArray(0);
        }
        shadowMappingShader.stop();
        clear();
    }


    public int getShadowMappingTexture() {
        return shadowMappingPass.getTexture();
    }

    public void setShadowMappingPass(ShadowMappingPass shadowMappingPass) {
        this.shadowMappingPass = shadowMappingPass;
    }
}
