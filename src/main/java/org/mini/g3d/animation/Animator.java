package org.mini.g3d.animation;

import java.util.HashMap;
import java.util.Map;
import org.mini.g3d.core.EngineManager;
import org.mini.g3d.core.vector.Matrix4f;


/**
 * This class has the required methods to be able to apply a animation to a animated entity
 * The animator needs to be called every frame, as it needs to update the pose, at it can change
 * at any time.
 * 
 * @author Glenn Arne Christensen
 *
 */
public class Animator {

	// The animated model it is going to animate
	private final AnimatedModel entity;

	// The animation that is going to be applied to the model
	private Animation currentAnimation;
	
	// Stores the time/progression it is in the animation
	private float animationTime = 0;


	public Animator(AnimatedModel entity) {
		this.entity = entity;
	}

	/**
	 * Starts the current animation from the start
	 */
	public void doAnimation(Animation animation) {
		this.currentAnimation = animation;
		this.animationTime = 0;
	}

	/**
	 * This method should be called each frame to update the animation currently
	 * being played. This increases the animation time (and loops it back to
	 * zero if necessary), finds the pose that the entity should be in at that
	 * time of the animation, and then applies that pose to all the model's
	 * joints by setting the joint transforms.
	 */
	public void update() {
		// If there is an animation, update it
		if (currentAnimation != null) {
			increaseAnimationTime();
			Map<String, Matrix4f> currentPose = calculateCurrentAnimationPose(); // Calculates the pose that the model should be in
			applyPoseToJoints(currentPose, entity.getRootJoint(), new Matrix4f()); // Applies the pose to the models joints
		}
		
	}

	/**
	 * Increases the time of the animated that is current. If the animated time is
	 * longer than the length of the animation, it will then start over again, so 
	 * the animation loops.
	 */
	private void increaseAnimationTime() {
		animationTime += EngineManager.getFrameTimeSeconds();
		if (animationTime > currentAnimation.getLength()) {
			this.animationTime = 0; // Sets it back to zero, so the animation time is reset
		}
	}

	/**     
	 * Calculates the current pose of the animation, by finding the previous
	 * and the next frames which are dependent of the time the animation is in.         
	 * Then finds the progression by calculation where the timer is between
	 * frame A and frame B. At the end it interpolates between the two poses
	 * dependent on the value of "progression"                 
	 */
	private Map<String, Matrix4f> calculateCurrentAnimationPose() {
		KeyFrame[] frames = getPreviousAndNextFrames();
		float progression = calculateProgression(frames[0], frames[1]);
		return interpolatePoses(frames[0], frames[1], progression);
	}

	/**            
	 * Applies the pose to the joints and all of its children. 
	 * It gets the local transform (The original transform of the joint), from the hash map 
	 * and calculate the current transform by multiplying the parent of the 
	 * joint transform, with the original transform of the joint. By
	 * multiplying them we get the original position of the joint, in relation of the 
	 * origin of the model
	 * 
	 * Its the same as done in the "Joint" class, in the "calculateInverseBindTransform"
	 * where we get the bind transform.
	 * 
	 * Then we apply the pose to the joints of the model until there is no more.
	 * 
	 * And at the end, we calculate the joint transform.
	 * Explaination on this can you find on the report, in chapter XX
	 */
	private void applyPoseToJoints(Map<String, Matrix4f> currentPose, Joint joint, Matrix4f parentTransform) {
		Matrix4f currentLocalTransform = currentPose.get(joint.name);
		Matrix4f currentTransform = Matrix4f.mul(parentTransform, currentLocalTransform, null);
		for (Joint childJoint : joint.children) {
			applyPoseToJoints(currentPose, childJoint, currentTransform);
		}
		Matrix4f.mul(currentTransform, joint.getInverseBindTransform(), currentTransform);
		joint.setAnimationTransform(currentTransform);
	}

	/**        
	 * Finds the previous and next keyframe in the animation. 
	 * In the case were there is no previous frame, the
	 * first keyframe is used for previous and next.                
	 */
	private KeyFrame[] getPreviousAndNextFrames() {
		KeyFrame[] allFrames = currentAnimation.getKeyFrames();
		KeyFrame previousFrame = allFrames[0];
		KeyFrame nextFrame = allFrames[0];
		for (int i = 1; i < allFrames.length; i++) {
			nextFrame = allFrames[i];
			if (nextFrame.getTimeStamp() > animationTime) {
				break;
			}
			previousFrame = allFrames[i];
		}
		return new KeyFrame[] { previousFrame, nextFrame };
	}

	/**
	 * Calculates how far it is between the previous and next keyframe.
	 * By finding the total time between the two keyframes, then the current time
	 * it is in. And dividing those two values.
	 */
	private float calculateProgression(KeyFrame previousFrame, KeyFrame nextFrame) {
		float totalTime = nextFrame.getTimeStamp() - previousFrame.getTimeStamp();
		float currentTime = animationTime - previousFrame.getTimeStamp();
		return currentTime / totalTime;
	}

	/**        
	 *  Transforms the current pose of the model.
	 *  Goes through all the joints, on each iteration it gets their transform in the previous frame,
	 *  and getting the transform for the next frame.
	 *  It then interpolates the two transforms, 
	 *  and adds them to hash map that has the values of the current pose, 
	 *  as well as the name of the current joint.
	 */
	private Map<String, Matrix4f> interpolatePoses(KeyFrame previousFrame, KeyFrame nextFrame, float progression) {
		Map<String, Matrix4f> currentPose = new HashMap<String, Matrix4f>();
		for (String jointName : previousFrame.getJointKeyFrames().keySet()) {
			JointTransform previousTransform = previousFrame.getJointKeyFrames().get(jointName);
			JointTransform nextTransform = nextFrame.getJointKeyFrames().get(jointName);
			JointTransform currentTransform = JointTransform.interpolate(previousTransform, nextTransform, progression);
			currentPose.put(jointName, currentTransform.getLocalTransform());
		}
		return currentPose;
	}

}
