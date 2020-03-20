package org.mini.g3d.skybox;

import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

import org.mini.g3d.core.Camera;
import org.mini.g3d.core.EngineManager;
import org.mini.g3d.core.ShaderProgram;
import org.mini.g3d.core.toolbox.G3dMath;

public class SkyboxShader extends ShaderProgram{

	private static final String VERTEX_FILE = "/res/shader/skyboxVertex.shader";
	private static final String FRAGMENT_FILE = "/res/shader/skyboxFragment.shader";
	
	private int location_projectionMatrix;
	private int location_viewMatrix;
	private int location_fogColour;
	private int location_cubeMap;
	private int location_cubeMap2;
	private int location_blendFactor;
	
	private static final float ROTATE_SPEED = 1f;
	
	private float rotation = 0;
	
	public SkyboxShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}
	
	public void loadProjectionMatrix(Matrix4f matrix){
		super.loadMatrix(location_projectionMatrix, matrix);
	}

	public void loadViewMatrix(Camera camera){
		Matrix4f matrix = G3dMath.createViewMatrix(camera);
		matrix.mat[Matrix4f.M30] = 0;
		matrix.mat[Matrix4f.M31] = 0;
		matrix.mat[Matrix4f.M32] = 0;
		rotation += ROTATE_SPEED * EngineManager.getFrameTimeSeconds();
		Matrix4f.rotate((float) Math.toRadians(rotation), new Vector3f(0,1,0), matrix, matrix);
		super.loadMatrix(location_viewMatrix, matrix);
	}
	
	@Override
	protected void getAllUniformLocations() {
		location_projectionMatrix = super.getUniformLocation("projectionMatrix");
		location_viewMatrix = super.getUniformLocation("viewMatrix");
		location_fogColour = super.getUniformLocation("fogColour");
		location_cubeMap = super.getUniformLocation("cubeMap");
		location_cubeMap2 = super.getUniformLocation("cubeMap2");
		location_blendFactor = super.getUniformLocation("blendFactor");
	}
	
	public void loadBlendFactor(float blend) {
		super.loadFloat(location_blendFactor, blend);
	}
	
	public void connectTextureUnits() {
		super.loadInt(location_cubeMap, 0);
		super.loadInt(location_cubeMap2, 1);
	}
	
	protected void loadFogColour(float r, float g, float b) {
		super.loadVector(location_fogColour, new Vector3f(r,g,b));
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "position");
	}

}
