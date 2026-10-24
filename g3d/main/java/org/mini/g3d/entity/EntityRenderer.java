package org.mini.g3d.entity;

import org.mini.g3d.core.AbstractRenderer;
import org.mini.g3d.core.MainFrameBuffer;
import org.mini.g3d.core.MasterRenderer;
import org.mini.g3d.core.Scene;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.core.textures.Texture;
import org.mini.g3d.core.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.mini.gl.GL.*;

public class EntityRenderer extends AbstractRenderer {

    private EntityShader shader = new EntityShader();
    private static final int MAX_INSTANCES = 5000; // 最大实例数量
    private int instanceVBO; // 实例化顶点缓冲区对象ID
    private FloatBuffer instanceBuffer; // 实例化数据缓冲区
    private int[] vboIds = new int[1];
    private final List<Entity> transparentEntities = new ArrayList<>();
    private float[] sortView;
    private final Comparator<Entity> backToFront = new Comparator<Entity>() {
        @Override
        public int compare(Entity a, Entity b) {
            return Float.compare(viewDepth(b), viewDepth(a));
        }
    };

    private float viewDepth(Entity entity) {
        Vector3f p = entity.getPosition();
        return -(sortView[2] * p.x + sortView[6] * p.y + sortView[10] * p.z + sortView[14]);
    }

    private static boolean isTransparent(Entity entity) {
        return entity.getTransparency() > 0f && entity.getTransparency() < 1f;
    }

    public EntityRenderer() {
        // 创建实例VBO
        glGenBuffers(1, vboIds, 0);
        instanceVBO = vboIds[0];

        // 创建足够大的缓冲区来存储实例数据
        // 每个实例数据: 3(位置) + 3(旋转) + 1(缩放) + 2(纹理偏移) + 1(透明度) = 10个浮点数
        instanceBuffer = FloatBuffer.allocate(MAX_INSTANCES * 10);
    }

    /** Compatibility entry point for callers drawing entities without other scene content. */
    public void render(Scene scene) {
        renderOpaque(scene);
        renderTransparent(scene);
    }

    public void renderOpaque(Scene scene) {
        renderPass(scene, false);
    }

    public void renderTransparent(Scene scene) {
        renderPass(scene, true);
    }

    private void renderPass(Scene scene, boolean transparent) {
        shader.start();
        shader.loadSkyColour(scene.getFogColor());
        shader.loadLights(scene.getLightIterator());
        shader.loadViewMatrix(scene.getCamera());
        shader.loadProjectionMatrix(scene.getCamera().getProjectionMatrix());
        shader.loadTransparencyDistance(scene.getCamera().getDistanceFromTarget() - 1f);
        shader.loadTransparentPass(transparent);
        glEnable(GL_DEPTH_TEST);
        glDepthMask(transparent ? GL_FALSE : GL_TRUE);
        if (transparent) {
            glEnable(GL_BLEND);
            glBlendEquation(GL_FUNC_ADD);
            // Shader RGB is premultiplied and includes emission; preserve destination alpha.
            glBlendFuncSeparate(GL_ONE, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        } else {
            glDisable(GL_BLEND);
        }

        try {
            Map<TexturedModel, List<Entity>> entities = scene.getVisibleEntitieMap(scene.getCamera());
            if (transparent) {
                transparentEntities.clear();
                for (Map.Entry<TexturedModel, List<Entity>> entry : entities.entrySet()) {
                    if (entry.getKey() == null || entry.getKey().getTexture() == null || entry.getValue() == null) continue;
                    for (Entity entity : entry.getValue()) {
                        if (isTransparent(entity)) transparentEntities.add(entity);
                    }
                }
                sortView = scene.getCamera().getViewMatrix().mat;
                Collections.sort(transparentEntities, backToFront);
                // Only adjacent, same-model instances may merge after global depth sorting.
                for (int first = 0; first < transparentEntities.size();) {
                    TexturedModel model = transparentEntities.get(first).getModel();
                    int end = first + 1;
                    while (end < transparentEntities.size() && transparentEntities.get(end).getModel() == model) end++;
                    drawBatch(model, transparentEntities, first, end, true);
                    first = end;
                }
            } else {
                for (Map.Entry<TexturedModel, List<Entity>> entry : entities.entrySet()) {
                    TexturedModel model = entry.getKey();
                    List<Entity> batch = entry.getValue();
                    if (model != null && model.getTexture() != null && batch != null && !batch.isEmpty()) {
                        drawBatch(model, batch, 0, batch.size(), false);
                    }
                }
            }
        } finally {
            transparentEntities.clear();
            glDepthMask(GL_TRUE);
            glDisable(GL_BLEND);
            shader.stop();
        }
    }

    private void drawBatch(TexturedModel model, List<Entity> batch, int first, int end, boolean transparent) {
        boolean bound = false;
        while (first < end) {
            instanceBuffer.clear();
            int count = 0;
            while (first < end && count < MAX_INSTANCES) {
                Entity entity = batch.get(first++);
                if (entity.getTransparency() <= 0f || isTransparent(entity) != transparent) continue;
                Vector3f position = entity.getPosition();
                instanceBuffer.put(position.x).put(position.y).put(position.z);
                instanceBuffer.put(entity.getRotX()).put(entity.getRotY()).put(entity.getRotZ());
                instanceBuffer.put(entity.getScale());
                instanceBuffer.put(entity.getTextureXOffset()).put(entity.getTextureYOffset());
                instanceBuffer.put(entity.getTransparency());
                count++;
            }
            if (count == 0) continue;
            instanceBuffer.flip();
            if (!bound) {
                prepareTexturedModel(model);
                bound = true;
            }
            glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
            glBufferData(GL_ARRAY_BUFFER, instanceBuffer.limit() * 4, instanceBuffer.array(), 0, GL_DYNAMIC_DRAW);
            setupInstanceAttributes();
            glDrawElementsInstanced(GL_TRIANGLES, model.getRawModel().getVertexCount(), GL_UNSIGNED_INT, null, 0, count);
            MainFrameBuffer.triangles += model.getRawModel().getVertexCount() * count;
        }
        if (bound) {
            disableInstanceAttributes();
            unbindTexturedModel();
        }
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
