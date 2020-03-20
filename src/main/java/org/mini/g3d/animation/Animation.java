package org.mini.g3d.animation;

/**
 * A animation that can be applied on a AnimatedModel
 * 
 * @author Glenn Arne Christensen
 */
public class Animation {

	// Is the length of the whole animation
	private final float length; // Is defined in seconds
	
	// Holds all the keyframes in the animation, and is also order correctly
	private final KeyFrame[] keyFrames;

	/**
	 * Constructor for a Animation.
	 */
	public Animation(float lengthInSeconds, KeyFrame[] frames) {
		this.length = lengthInSeconds;
		this.keyFrames = frames;
	}

	// Getters
	
	public float getLength() {
		return length;
	}


	public KeyFrame[] getKeyFrames() {
		return keyFrames;
	}

}
