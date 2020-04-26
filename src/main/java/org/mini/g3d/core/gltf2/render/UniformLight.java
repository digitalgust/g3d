/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.render;


import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;

/**
 * Mirrors the Light struct in gltfFragment.shader Defaults from
 * glTF-Sample-Viewer/src/light.js
 */
public class UniformLight {

  public Vector3f direction = new Vector3f(-0.7399f, -0.6428f, -0.1983f);
  public float range = -1;
  public Vector3f color = new Vector3f(1, 1, 1);
  public float intensity = 1;
  public Vector3f position = new Vector3f(0, 0, 0);
  public float innerConeCos = 0;
  public float outerConeCos = (float) (Math.PI / 4);
  public int type = 0;
  public Vector2f padding = new Vector2f();
}
