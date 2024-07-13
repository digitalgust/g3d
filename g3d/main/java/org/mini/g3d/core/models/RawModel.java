package org.mini.g3d.core.models;

public class RawModel {

    private int vaoID;
    private int vertexCount;
    private boolean cullingBack = true;

    public RawModel(int vaoID, int vertexCount) {
        this.vaoID = vaoID;
        this.vertexCount = vertexCount;
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


}
