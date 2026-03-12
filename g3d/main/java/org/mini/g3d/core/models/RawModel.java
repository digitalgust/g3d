package org.mini.g3d.core.models;

public class RawModel {

    private int vaoID;
    private int vertexCount;
    private boolean cullingBack = true;
    private float boundingRadius = 1.0f;

    public RawModel(int vaoID, int vertexCount) {
        this.vaoID = vaoID;
        this.vertexCount = vertexCount;
    }

    public RawModel(int vaoID, int vertexCount, float boundingRadius) {
        this.vaoID = vaoID;
        this.vertexCount = vertexCount;
        this.boundingRadius = boundingRadius;
    }

    public int getVaoID() {
        return vaoID;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void setCullingBack(boolean cullingBack) {
        this.cullingBack = cullingBack;
    }

    public boolean isCullingBack() {
        return cullingBack;
    }

    public float getBoundingRadius() {
        return boundingRadius;
    }

    public void setBoundingRadius(float boundingRadius) {
        this.boundingRadius = boundingRadius;
    }

}

