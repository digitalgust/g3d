package org.mini.g3d.entity;

import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.MainFrameBuffer;
import org.mini.g3d.core.MasterRenderer;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

public class EntityRenderer extends AbstractRenderer {

    private EntityShader shader = new EntityShader();
    private static final int MAX_INSTANCES = 5000; // 最大实例数量
    private int instanceVBO; // 实例化顶点缓冲区对象ID
    private FloatBuffer instanceBuffer; // 实例化数据缓冲区
    private int[] vboIds = new int[1]; // 用于存储VBO ID

    public EntityRenderer() {
        // 创建实例VBO
        glGenBuffers(1, vboIds, 0);
        instanceVBO = vboIds[0];

        // 创建足够大的缓冲区来存储实例数据
        // 每个实例数据: 3(位置) + 3(旋转) + 1(缩放) + 2(纹理偏移) + 1(透明度) = 10个浮点数
        instanceBuffer = FloatBuffer.allocate(MAX_INSTANCES * 10);
    }

    public void render(Scene scene) {
        shader.start();
        shader.loadSkyColour(scene.getFogColor());
        shader.loadLights(scene.getLightIterator());
        shader.loadViewMatrix(scene.getCamera());
        Matrix4f projectionMatrix = scene.getCamera().getProjectionMatrix();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.loadTransparencyDistance(scene.getCamera().getDistanceFromTarget() - 1f);

        Map<TexturedModel, List<Entity>> entities = scene.getEntitieMap();
        entities.forEach((texturedModel, batch) -> {
            //multithread , if scene.clear() may batch is null
            if (texturedModel == null || batch == null) {
                return;
            }
            if (texturedModel.getTexture() != null) {
                prepareTexturedModel(texturedModel);

                // 准备实例数据
                int instanceCount = prepareInstancesData(batch);

                // 绑定实例VBO并上传数据
                glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
                // 上传实例数据到GPU
                instanceBuffer.position(0);
                glBufferData(GL_ARRAY_BUFFER, instanceBuffer.limit() * 4, instanceBuffer.array(), 0, GL_DYNAMIC_DRAW);

                // 设置实例属性
                setupInstanceAttributes();

                // 使用实例化渲染
                glDrawElementsInstanced(GL_TRIANGLES, texturedModel.getRawModel().getVertexCount(),
                        GL_UNSIGNED_INT, null, 0, instanceCount);
                MainFrameBuffer.triangles += texturedModel.getRawModel().getVertexCount() * instanceCount;

                // 解绑实例属性
                disableInstanceAttributes();
                unbindTexturedModel();
            }
        });
        shader.stop();
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

    private void prepareTexturedModel(TexturedModel model) {
        RawModel rawModel = model.getRawModel();
        Texture texture = model.getTexture();
        if (rawModel.isCullingBack()) {
            MasterRenderer.enableCulling();
        } else {
            MasterRenderer.disableCulling();
        }

        glBindVertexArray(rawModel.getVaoID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        shader.loadNumberOfRows(texture.getNumberOfRows());
        shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());
        shader.setLightning(texture.isUseFakeLightning());
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getID());
    }

    private void unbindTexturedModel() {
        enableCulling();
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }
}
