package org.mini.g3d.animation;

import java.util.Map;

/**
 * 
 * A keyframe of an animation
 * 
 * @author Glenn Arne Christensen
 *
 */
public class KeyFrame {

	// The time that the current keyframe occurs in the animation
	private final float timeStamp; 
	
	/**
	 * The transforms for all the joins at the current keyframe (the pose), 
	 * holds the name and the transform
	 * 
	 * Example: 
	 * <"Chest", JointTransform X>
	 */
	private final Map<String, JointTransform> pose; 

	/**
	 * Holds the current pose at the given time of the animation
	 */
	public KeyFrame(float timeStamp, Map<String, JointTransform> jointKeyFrames) {
		this.timeStamp = timeStamp;
		this.pose = jointKeyFrames;
	}

	// Getters
	
	protected float getTimeStamp() {
		return timeStamp;
	}


	protected Map<String, JointTransform> getJointKeyFrames() {
		return pose;
	}

}
