package org.mini.g3d.shadowmap;

import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.Light;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.terrain.Terrain;
import org.mini.gl.GLMath;

import java.nio.FloatBuffer;
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
    
    // 实例化渲染相关
    private static final int MAX_INSTANCES = 5000; // 最大实例数量
    private int instanceVBO; // 实例化顶点缓冲区对象ID
    private FloatBuffer instanceBuffer; // 实例化数据缓冲区
    private int[] vboIds = new int[1]; // 用于存储VBO ID
    
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
        
        // 创建实例VBO
        glGenBuffers(1, vboIds, 0);
        instanceVBO = vboIds[0];
        
        // 创建足够大的缓冲区来存储实例数据
        // 每个实例数据: 3(位置) + 3(旋转) + 1(缩放) + 2(纹理偏移) + 1(透明度) = 10个浮点数
        instanceBuffer = FloatBuffer.allocate(MAX_INSTANCES * 10);
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

    private int prepareInstancesData(List<Entity> entities) {
        instanceBuffer.clear();
        int count = Math.min(entities.size(), MAX_INSTANCES);
        
        for (int i = 0; i < count; i++) {
            Entity entity = entities.get(i);
            
            // 添加位置
            Vector3f position = entity.getPosition();
            instanceBuffer.put(position.x);
            instanceBuffer.put(position.y);
            instanceBuffer.put(position.z);
            
            // 添加旋转
            instanceBuffer.put(entity.getRotX());
            instanceBuffer.put(entity.getRotY());
            instanceBuffer.put(entity.getRotZ());
            
            // 添加缩放
            instanceBuffer.put(entity.getScale());
            
            // 添加纹理偏移
            instanceBuffer.put(entity.getTextureXOffset());
            instanceBuffer.put(entity.getTextureYOffset());
            
            // 添加透明度
            instanceBuffer.put(entity.getTransparency());
        }
        
        instanceBuffer.flip();
        return count;
    }
    
    private void setupInstanceAttributes() {
        // 使用顶点属性3开始，因为0,1,2已被使用
        // 实例位置 (vec3) - 属性索引3
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 3, GL_FLOAT, GL_FALSE, 10 * 4, null, 0);
        glVertexAttribDivisor(3, 1);
        
        // 实例旋转 (vec3) - 属性索引4
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 3, GL_FLOAT, GL_FALSE, 10 * 4, null, 3 * 4);
        glVertexAttribDivisor(4, 1);
        
        // 实例缩放 (float) - 属性索引5
        glEnableVertexAttribArray(5);
        glVertexAttribPointer(5, 1, GL_FLOAT, GL_FALSE, 10 * 4, null, 6 * 4);
        glVertexAttribDivisor(5, 1);
        
        // 实例纹理偏移 (vec2) - 属性索引6
        glEnableVertexAttribArray(6);
        glVertexAttribPointer(6, 2, GL_FLOAT, GL_FALSE, 10 * 4, null, 7 * 4);
        glVertexAttribDivisor(6, 1);
        
        // 实例透明度 (float) - 属性索引7
        glEnableVertexAttribArray(7);
        glVertexAttribPointer(7, 1, GL_FLOAT, GL_FALSE, 10 * 4, null, 9 * 4);
        glVertexAttribDivisor(7, 1);
    }
    
    private void disableInstanceAttributes() {
        glVertexAttribDivisor(3, 0);
        glVertexAttribDivisor(4, 0);
        glVertexAttribDivisor(5, 0);
        glVertexAttribDivisor(6, 0);
        glVertexAttribDivisor(7, 0);
        
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);
        glDisableVertexAttribArray(5);
        glDisableVertexAttribArray(6);
        glDisableVertexAttribArray(7);
    }

    public void render(Scene scene) {
        updateDepthMVP(scene);
        shadowMappingShader.start();
        
        // 加载深度MVP矩阵，这个现在只需要加载一次
        shadowMappingShader.loadDepthMVP(depthMVP);

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

            // 准备实例数据
            int instanceCount = prepareInstancesData(batch);
            
            // 绑定实例VBO并上传数据
            glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
            instanceBuffer.position(0);
            glBufferData(GL_ARRAY_BUFFER, instanceBuffer.limit() * 4, instanceBuffer.array(), 0, GL_DYNAMIC_DRAW);
            
            // 设置实例属性
            setupInstanceAttributes();
            
            // 使用实例化渲染
            glDrawElementsInstanced(GL_TRIANGLES, rawModel.getVertexCount(), GL_UNSIGNED_INT, null, 0, instanceCount);
            triangles += rawModel.getVertexCount() * instanceCount;
            
            // 解绑实例属性
            disableInstanceAttributes();
            
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
