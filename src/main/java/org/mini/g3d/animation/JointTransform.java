package org.mini.g3d.animation;

import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.vector.Quaternion;


/**
 * Has the position and the rotation of the joint, relative to its parent
 * The root joint is relative to the models origin
 * 
 * @author Glenn Arne Christensen
 *
 */

public class JointTransform {

	private final Vector3f position;
	private final Quaternion rotation;


	public JointTransform(Vector3f position, Quaternion rotation) {
		this.position = position;
		this.rotation = rotation;
	}

	/**
	 * Creates a transformation matrix by translating the position of the joint
	 * and then rotating it, by converting quaternion to a 4x4 rotation matrix. As 
	 * it makes it easier as it gets multiplied with other 4x4 matrices.
	 * This is then multiplied with the transformation matrix
	 */
	protected Matrix4f getLocalTransform() {
		Matrix4f matrix = new Matrix4f();
		matrix.translate(position);
		Matrix4f.mul(matrix, rotation.toRotationMatrix(), matrix);
		return matrix;
	}

	/**
	 * 
	 * Interpolates between two joint transforms, based on a progression value. 
	 * And returns the interpolated joint transform.
	 * Progression is a float value that is between 0 and 1, that indicates
	 * how far from the two frames (frame A and frame B) it should interpolate
	 * 
	 * If the progression value is 0, it will return a transform that is equal to frame A.
	 * If it is 1, it will return a transform equal to frame B
	 */
	protected static JointTransform interpolate(JointTransform frameA, JointTransform frameB, float progression) {
		Vector3f pos = interpolate(frameA.position, frameB.position, progression);
		Quaternion rot = Quaternion.interpolate(frameA.rotation, frameB.rotation, progression); // Here "nlerp" is used and not "slerp"!
		return new JointTransform(pos, rot);
	}

	/**
	 * Linearly interpolates between two points, based on the progression value
	 */
	private static Vector3f interpolate(Vector3f start, Vector3f end, float progression) {
		float x = start.x + (end.x - start.x) * progression;
		float y = start.y + (end.y - start.y) * progression;
		float z = start.z + (end.z - start.z) * progression;
		return new Vector3f(x, y, z);
	}

}
