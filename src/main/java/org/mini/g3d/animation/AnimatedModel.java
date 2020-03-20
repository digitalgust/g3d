package org.mini.g3d.animation;

import org.mini.g3d.core.models.TexturedModel;
import org.mini.g3d.entity.Entity;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.textures.ModelTexture;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;


/**
 * A entity that can be animated
 *
 * @author Glenn Arne Christensen
 */
public class AnimatedModel extends Entity {

    // Skin of the animated model
//	private final RawModel rawModel;
//	private final ModelTexture modelTexture;

    // Skeleton of the animated model
    private final Joint rootJoint; // Only needed to have a reference to the root joint, as it is structured in a hierarchy
    private final int jointCount; // Holds the amount of joints in the skeleton, so we know how big we have to create the matrix

    /**
     * AnimatedModel needs its own animator, as this will do all the work,
     * on animating the model and having it in the correct poses at the giving times
     */
    private final Animator animator;

//	private Vector3f position;
//	private float rotX, rotY, rotZ, scale;


    /**
     * Constructor to create a animated model.
     * Has the same values as creating a entity, only difference here
     * is that we need to send in the root joint, as well as
     * the amount of joints that are in the skeleton
     */
    public AnimatedModel(RawModel rawModel, ModelTexture modelTexture, Joint rootJoint, int jointCount, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
        super(new TexturedModel(rawModel, modelTexture), position, rotX, rotY, rotZ, scale);
        this.rootJoint = rootJoint;
        this.jointCount = jointCount;
        System.out.println("jointCount: " + jointCount);
        this.animator = new Animator(this);
        rootJoint.calculateInverseBindTransform(new Matrix4f()); // This function takes in a parent bind transform, but as we use the root joint, we just send in a matrix
    }

    /**
     * Makes so we can move the animated model
     */
    public void increasePosition(float dx, float dy, float dz) {
        this.position.x += dx;
        this.position.y += dy;
        this.position.z += dz;
    }

    /**
     * Change rotation of the entity
     */
    public void increaseRotation(float dx, float dy, float dz) {
        this.rotX += dx;
        this.rotY += dy;
        this.rotZ += dz;
    }

    /**
     * Tells the animator of this animated model to do the animation.
     */
    public void doAnimation(Animation animation) {
        animator.doAnimation(animation);
    }

    /**
     * Updates the animator for this entity, basically updating the animated
     * pose of the entity. Must be called every frame.
     */
    public void update() {
        animator.update();
    }


    /**
     * Adds the current joint as well as their children to the matrix array
     * containing the joint transforms
     */
    private void addJointsToMatrixArray(Joint currentJoint, Matrix4f[] jointMatrices) {
        jointMatrices[currentJoint.jointID] = currentJoint.getAnimatedTransform();
        for (Joint childJoint : currentJoint.children) {
            addJointsToMatrixArray(childJoint, jointMatrices);
        }
    }

    // Getters and setters

    /**
     * Creates a matrix, the size dependent of the amounts of joints in the skeleton.
     * The way the joints are ordered is dependent of the index of the joint.
     * And returns the transforms of the joints in the current pose of the animation
     */
    public Matrix4f[] getJointTransforms() {
        Matrix4f[] jointMatrices = new Matrix4f[jointCount];
        addJointsToMatrixArray(rootJoint, jointMatrices);
        return jointMatrices;
    }


    public Joint getRootJoint() {
        return rootJoint;
    }

    public int getJointCount() {
        return jointCount;
    }


    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public float getRotX() {
        return rotX;
    }

    public void setRotX(float rotX) {
        this.rotX = rotX;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getRotZ() {
        return rotZ;
    }

    public void setRotZ(float rotZ) {
        this.rotZ = rotZ;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

}
