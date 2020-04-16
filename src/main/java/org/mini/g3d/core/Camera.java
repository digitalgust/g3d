package org.mini.g3d.core;

import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector3f;

public interface Camera {

    Vector3f getPosition();

    void update();

    Matrix4f getProjectionMatrix();

    Matrix4f getSkyBoxProjectionMatrix();

    Matrix4f getViewMatrix();
}

