package org.mini.g3d.core.textures;

public class ModelTexture {

	private int textureID;
	
	private float shineDamper = 1;
	private float reflectivity = 0;
	
	private boolean hasTransparency = false;
	private boolean useFakeLightning = false;
	
	private int numberOfROws = 1;
	
	public ModelTexture(int id) {
		this.textureID = id;
	}
	
	public int getNumberOfRows() {
		return numberOfROws;
	}



	public void setNumberOfRows(int numberOfROws) {
		this.numberOfROws = numberOfROws;
	}



	public boolean isHasTransparency() {
		return hasTransparency;
	}

	public void setHasTransparency(boolean hasTransparency) {
		this.hasTransparency = hasTransparency;
	}

	public float isUseFakeLightning() {
		
		float useFakeLightningReturn = 0;
		if (useFakeLightning) {
			useFakeLightningReturn = 1;
		}
		
		return useFakeLightningReturn;
	}

	public void setUseFakeLighting(boolean useFakeLightning) {
		this.useFakeLightning = useFakeLightning;
	}

	public int getID() {
		return this.textureID;
	}

	public int getTextureID() {
		return textureID;
	}

	public void setTextureID(int textureID) {
		this.textureID = textureID;
	}

	public float getShineDamper() {
		return shineDamper;
	}

	public void setShineDamper(float shineDamper) {
		this.shineDamper = shineDamper;
	}

	public float getReflectivity() {
		return reflectivity;
	}

	public void setReflectivity(float reflectivity) {
		this.reflectivity = reflectivity;
	}
	
}
