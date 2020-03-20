package org.mini.g3d.particles;

public class ParticleTexture {

	private int textureID;
	private int numberOfRows;
	private boolean additive;

	public ParticleTexture(int textureID, int numberOfRows, boolean additive) {
		this.textureID = textureID;
		this.numberOfRows = numberOfRows;
		this.additive = additive;
	}

	protected boolean usesAdditiveBlending() {
		return additive;
	}

	public int getTextureID() {
		return textureID;
	}

	public int getNumberOfRows() {
		return numberOfRows;
	}
}
