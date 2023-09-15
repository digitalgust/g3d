/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mini.g3d.skybox;

import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.util.Loader;

/**
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

    RawModel cube;
    int texture;
    int nightTexture;

    public Skybox(Loader loader, String[] dayTexturePath, String[] nightTexturePath) {
        cube = loader.loadToVAO(VERTICES, 3);
        texture = loader.loadCubeMap(dayTexturePath);
        nightTexture = loader.loadCubeMap(nightTexturePath);
    }

    public int getVaoID() {
        return cube.getVaoID();
    }

    public int getVertexCount() {
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
