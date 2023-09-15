package org.mini.g3d.core.models;

import org.mini.g3d.core.textures.Texture;

public class TexturedModel {
	
	private RawModel rawModel;
	private Texture texture;
	
	public TexturedModel(RawModel model, Texture texture) {
		this.rawModel = model;
		this.texture = texture;
	}

	public RawModel getRawModel() {
		return rawModel;
	}

	public Texture getTexture() {
		return texture;
	}
	
}
