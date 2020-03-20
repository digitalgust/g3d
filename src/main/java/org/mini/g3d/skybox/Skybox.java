/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mini.g3d.skybox;

import org.mini.g3d.core.Loader;
import org.mini.g3d.core.models.RawModel;

/**
 *
 * @author Gust
 */
public class Skybox {
    
    private static final float SIZE = 1000f;

    private static final float[] VERTICES = {
        -SIZE, SIZE, -SIZE,
        -SIZE, -SIZE, -SIZE,
        SIZE, -SIZE, -SIZE,
        SIZE, -SIZE, -SIZE,
        SIZE, SIZE, -SIZE,
        -SIZE, SIZE, -SIZE,
        -SIZE, -SIZE, SIZE,
        -SIZE, -SIZE, -SIZE,
        -SIZE, SIZE, -SIZE,
        -SIZE, SIZE, -SIZE,
        -SIZE, SIZE, SIZE,
        -SIZE, -SIZE, SIZE,
        SIZE, -SIZE, -SIZE,
        SIZE, -SIZE, SIZE,
        SIZE, SIZE, SIZE,
        SIZE, SIZE, SIZE,
        SIZE, SIZE, -SIZE,
        SIZE, -SIZE, -SIZE,
        -SIZE, -SIZE, SIZE,
        -SIZE, SIZE, SIZE,
        SIZE, SIZE, SIZE,
        SIZE, SIZE, SIZE,
        SIZE, -SIZE, SIZE,
        -SIZE, -SIZE, SIZE,
        -SIZE, SIZE, -SIZE,
        SIZE, SIZE, -SIZE,
        SIZE, SIZE, SIZE,
        SIZE, SIZE, SIZE,
        -SIZE, SIZE, SIZE,
        -SIZE, SIZE, -SIZE,
        -SIZE, -SIZE, -SIZE,
        -SIZE, -SIZE, SIZE,
        SIZE, -SIZE, -SIZE,
        SIZE, -SIZE, -SIZE,
        -SIZE, -SIZE, SIZE,
        SIZE, -SIZE, SIZE
    };

    private static final String[] TEXTURE_FILES = {"textures/skybox/right", "textures/skybox/left", "textures/skybox/top", "textures/skybox/bottom", "textures/skybox/back", "textures/skybox/front"};
    private static final String[] NIGHT_TEXTURE_FILES = {"textures/skybox/nightRight", "textures/skybox/nightLeft", "textures/skybox/nightTop", "textures/skybox/nightBottom", "textures/skybox/nightBack", "textures/skybox/nightFront"};

    RawModel cube;
    int texture;
    int nightTexture;

    public Skybox(Loader loader) {
        cube = loader.loadToVAO(VERTICES, 3);
        texture = loader.loadCubeMap(TEXTURE_FILES);
        nightTexture = loader.loadCubeMap(NIGHT_TEXTURE_FILES);
        
    }
    
    public int getVaoID(){
        return cube.getVaoID();
    }
    
    public int getVertexCount(){
        return cube.getVertexCount();
    }

    /**
     * @return the cube
     */
    public RawModel getCube() {
        return cube;
    }

    /**
     * @return the texture
     */
    public int getTexture() {
        return texture;
    }

    /**
     * @return the nightTexture
     */
    public int getNightTexture() {
        return nightTexture;
    }
    
   
}
