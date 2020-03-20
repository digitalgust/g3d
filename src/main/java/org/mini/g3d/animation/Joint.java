package org.mini.g3d.animation;

import java.util.ArrayList;
import java.util.List;
import org.mini.g3d.core.vector.Matrix4f;


/**
 * 
 * Joint is a part of in the skeleton.
 * 
 * @author Glenn Arne Christensen
 *
 */
public class Joint {

	public final int jointID;
	
	 // The name of the joint, that is found in the collada file
	public final String name;
	
	// List of all the children of the joint. Not needed to know about the joints parent.
	public final List<Joint> children = new ArrayList<Joint>(); 

	// The current position and rotation of the joint, so changing this will make us able to set our model in different poses
	private Matrix4f animatedTransform = new Matrix4f(); 
	
	// Is the original transform of the joint, in the relation of its parent joint, this is before the animation is applied.
	private final Matrix4f localBindTransform;
	
	/**
	 * Original position and rotation of the joint, in model space. Model space is the models origin. In blender that would be were the 3d cursor is located.
	 * Its inverse as well, which means that it is the joint pointing towards the model space, and not the other way around.
	 */
	private Matrix4f inverseBindTransform = new Matrix4f();

	/**
	 * Joint constructor
	 */
	public Joint(int jointID, String name, Matrix4f bindLocalTransform) {
		this.jointID = jointID;
		this.name = name;
		this.localBindTransform = bindLocalTransform;
	}

	/**
	 * Adds a child to the current joint. Used to create the
	 * joint hierarchy.
	 */
	public void addChild(Joint child) {
		this.children.add(child);
	}
	
	/**
	 * Calculates the inverse bind transform.
	 * 
	 * This is calculated by multiplying the parent of the joints bind transform, and the original transform of the joint, together 
	 * 
	 * First the bindtransform has to be calculated which is done by
	 * multiplying the parent of the joints bind transform, and the original transform of the joint.
	 * By multiplying them we get original position of the joint, in relation of the origin of the model
	 * And then we inverse it, so the joint points towards the origin of the model.
	 * It will also calculate for the joints children, if it has it.
	 * 
	 * Wanted to write it like this 
	 * "Matrix4f invertedBindTransform = Matrix4f.invert(Matrix4f.mul(parentBindTransform, localBindTransform, null), inverseBindTransform);"
	 * But then the model got stretched and animation not getting applied as intended
	 */
	protected void calculateInverseBindTransform(Matrix4f parentBindTransform) {
		Matrix4f bindTransform = Matrix4f.mul(parentBindTransform, localBindTransform, null);
		Matrix4f.invert(bindTransform, inverseBindTransform);
		
		// If the joint has no children, no need to do the loop.
		if(children.isEmpty()) {}
		else {
			for (Joint jointChild : children) {
				jointChild.calculateInverseBindTransform(bindTransform);
			}
		}
		
	}
	
	// Getters and setters

	public Matrix4f getAnimatedTransform() {
		return animatedTransform;
	}

	/**
	 * Used by the animator class, by using this setter it can put the 
	 * animated model in a pose. 
	 */
	public void setAnimationTransform(Matrix4f animationTransform) {
		this.animatedTransform = animationTransform;
	}

	
	public Matrix4f getInverseBindTransform() {
		return inverseBindTransform;
	}

	

}
