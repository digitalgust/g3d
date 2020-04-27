package org.mini.g3d.animation;

import org.mini.g3d.core.Camera;
import org.mini.g3d.core.WorldCamera;
import org.mini.g3d.core.gltf2.render.Renderer;
import org.mini.g3d.core.vector.Matrix4f;


/**
 * Handles the rendering of a animated model/entity
 * The pose that the animated model will be rendered in
 * is determined by the joint transforms
 *
 * @author Glenn Arne Christensen
 */
public class AnimatedModelRenderer {


    private Renderer renderer;

    /**
     * Initializes the shader program used for rendering animated models.
     */
    public AnimatedModelRenderer(WorldCamera camera) {
        renderer = new Renderer();
    }


    /**
     * Renders the animated entity. Works the same as rendering a entity,
     * but notice with a animated model we have to enable five attributes
     * of the VAO before we render the animated entity. This is because
     * we need to have the joints and weights
     */
    public void render(Camera camera, AnimatedModel entity) {
        renderer.draw(camera, entity.getRootRenderNode(), -1);
    }


}
