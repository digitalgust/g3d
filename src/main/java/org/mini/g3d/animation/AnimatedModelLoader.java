package org.mini.g3d.animation;

import org.mini.g3d.core.collada.*;
import org.mini.g3d.core.Loader;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.toolbox.MyFile;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Quaternion;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.textures.ModelTexture;

import java.util.HashMap;
import java.util.Map;

public class AnimatedModelLoader {	

	/**
	 * Creates an AnimatedEntity from the data in an entity file. It loads up
	 * the collada model data, stores the extracted data in a VAO, sets up the
	 * joint heirarchy, and loads up the entity's texture.
	 */
	public AnimatedModel loadAnimatedModel(String modelFile, String textureFile, Vector3f position, float rotX, float rotY, float rotZ, float scale, Loader loader) {
		MyFile modelfile = new MyFile("/res", modelFile +".dae");
		
		AnimatedModelData entityData = ColladaLoader.loadColladaModel(modelfile, 3);
		MeshData modelMesh = entityData.getMeshData();
		RawModel model = loader.loadAnimatedModelToVAO(modelMesh.getVertices(), modelMesh.getTextureCoords(), modelMesh.getNormals(), modelMesh.getIndices(), modelMesh.getJointIds(), modelMesh.getVertexWeights());
		ModelTexture texture = new ModelTexture(loader.loadTexture(textureFile)); // Set texture
		SkeletonData skeletonData = entityData.getJointsData();
		Joint headJoint = createJoints(skeletonData.headJoint);
		AnimatedModel animatedModel = new AnimatedModel(model, texture, headJoint, skeletonData.jointCount, position, rotX, rotY, rotZ, scale);
		return animatedModel;
	}
	
	/**
	 * Loads up the animation. Finds the keyframes in the collada file, 
	 * creates the key frames, and initializes a new animation
	 * @param file
	 * @return
	 */
	public Animation loadAnimation(String file) {
		MyFile colladaFile = new MyFile("/res", file + ".dae");
		
		AnimationData animationData = ColladaLoader.loadColladaAnimation(colladaFile);
		KeyFrame[] frames = new KeyFrame[animationData.keyFrames.length];
		for (int i = 0; i < frames.length; i++) {
			frames[i] = createKeyFrame(animationData.keyFrames[i]);
		}
		return new Animation(animationData.lengthSeconds, frames);
	}
	
	/**
	 * Creates a keyframe from the data extracted from the collada file.
	 * 
	 * @param data
	 *            - the data about the keyframe that was extracted from the
	 *            collada file.
	 * @return The keyframe.
	 */
	private static KeyFrame createKeyFrame(KeyFrameData data) {
		Map<String, JointTransform> map = new HashMap<String, JointTransform>();
		for (JointTransformData jointData : data.jointTransforms) {
			JointTransform jointTransform = createTransform(jointData);
			map.put(jointData.jointNameId, jointTransform);
		}
		return new KeyFrame(data.time, map);
	}
	
	
	/**
	 * Creates a joint transform from the data extracted from the collada file.
	 * 
	 * @param data
	 *            - the data from the collada file.
	 * @return The joint transform.
	 */
	private static JointTransform createTransform(JointTransformData data) {
		Matrix4f matrix = data.jointLocalTransform;
		Quaternion rotation =new Quaternion();
                rotation.setFromMatrix(matrix);
		Vector3f translation = new Vector3f(matrix.mat[Matrix4f.M30], matrix.mat[Matrix4f.M31], matrix.mat[Matrix4f.M32]);
		return new JointTransform(translation, rotation);
	}
	
	/**
	 * Organizes the hierarchy of the joint skeleton, 
	 * from the data received fromt the collada file
	 * 
	 * @param data
	 *            - the joints data from the collada file for the head joint.
	 * @return The created joint, with all its descendants added.
	 */
	private static Joint createJoints(JointData data) {
		Joint joint = new Joint(data.index, data.nameId, data.bindLocalTransform);
		for (JointData child : data.children) {
			joint.addChild(createJoints(child));
		}
		return joint;
	}

}
