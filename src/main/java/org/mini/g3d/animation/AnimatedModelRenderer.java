package org.mini.g3d.animation;

import org.mini.g3d.core.Camera;
import org.mini.g3d.core.MasterRenderer;
import org.mini.g3d.core.WorldCamera;
import org.mini.g3d.core.gltf2.render.Renderer;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.textures.ModelTexture;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.nanovg.Gutil;

import static org.mini.gl.GL.*;


/**
 * Handles the rendering of a animated model/entity
 * The pose that the animated model will be rendered in
 * is determined by the joint transforms
 *
 * @author Glenn Arne Christensen
 */
public class AnimatedModelRenderer {

    private AnimatedModelShader shader;

    private Renderer renderer;

    /**
     * Initializes the shader program used for rendering animated models.
     */
    public AnimatedModelRenderer(AnimatedModelShader shader, WorldCamera camera) {
        camera.getProjectionDispatcher().register(new Runnable() {
            @Override
            public void run() {
                // Loads the shader, only has to be done once
                shader.start();
                Matrix4f projectionMatrix = camera.getProjectionMatrix();
                shader.loadProjectionMatrix(projectionMatrix);
                shader.start();
            }
        });
        this.shader = shader;


        renderer = new Renderer();
    }


    public void reloadProjectionMatrix(Matrix4f projectionMatrix) {
        shader.start();
        shader.loadProjectionMatrix(projectionMatrix);
        shader.stop();
    }

    /**
     * Renders the animated entity. Works the same as rendering a entity,
     * but notice with a animated model we have to enable five attributes
     * of the VAO before we render the animated entity. This is because
     * we need to have the joints and weights
     */
    public void render(Camera camera, AnimatedModel entity) {
        Gutil.checkGlError(this.getClass().getCanonicalName() + "render 0");
        glBindVertexArray(entity.getModel().getRawModel().getVaoID());
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);
        glClearDepth(1.0);

        glHint(GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL_NICEST); //Use a nicer calculation in fragment shaders
        Gutil.checkGlError(this.getClass().getCanonicalName() + "render 1");
        renderer.draw(camera, entity.getRootRenderNode(), -1);
//        prepareTexturedModel(entity);
//        prepareInstance(entity);
//
//        // Have to get the joint transforms of the animated model before we render it
////        shader.loadJointTransforms(entity.getJointTransforms());
//
//
//        glDrawElements(GL_TRIANGLES, entity.getModel().getRawModel().getVertexCount(), GL_UNSIGNED_INT, null, 0);
//        unbindTexturedModel(); // Unbinds all textures after its done
    }

    /**
     * Prepares the animated model by enabling five attributes of the VAO.
     * We also load up damper and reflectivity onto texture, before binding it.
     */
    public void prepareTexturedModel(AnimatedModel entity) {
        RawModel rawModel = entity.getModel().getRawModel();

        // Binds the VAO we want to use
        glBindVertexArray(rawModel.getVaoID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);

        // Load up the texture and apply the shinedamper and reflectivity on it
        ModelTexture texture = entity.getModel().getTexture();
        shader.loadNumberOfRows(texture.getNumberOfRows());
        shader.loadShineVariables(texture.getShineDamper(), texture.getReflectivity());

        // Tells OpenGL which texture we want to render
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, entity.getModel().getTexture().getID());
    }

    /**
     * Creates a transformationMatrix from the entities position
     */
    private void prepareInstance(AnimatedModel entity) {
        Matrix4f transfomationMatrix = G3dMath.createTransformationMatrix(entity.getPosition(), entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
        shader.loadTransformationMatrix(transfomationMatrix);
        shader.loadOffset(0, 0);

    }

    /**
     * Disable the attribute list when everything is finished.
     */
    private void unbindTexturedModel() {
        MasterRenderer.enableCulling();
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glDisableVertexAttribArray(3);
        glDisableVertexAttribArray(4);

        // Also have to unbind the VAO by putting in a 0
        glBindVertexArray(0);

    }

}
