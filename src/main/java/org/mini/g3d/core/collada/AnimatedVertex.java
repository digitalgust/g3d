package org.mini.g3d.core.collada;

import java.util.ArrayList;
import java.util.List;
import org.mini.g3d.core.vector.Vector3f;

public class AnimatedVertex extends org.mini.g3d.core.converter.Vertex {

    private List<Vector3f> tangents;
    private final Vector3f averagedTangent = new Vector3f(0, 0, 0);

    private VertexSkinData weightsData;

    public AnimatedVertex(int index, Vector3f position, VertexSkinData weightsData) {
        super(index, position);
        this.weightsData = weightsData;
    }

    public VertexSkinData getWeightsData() {
        return weightsData;
    }

    public void addTangent(Vector3f tangent) {
        if (tangents == null) {
            tangents = new ArrayList<Vector3f>();
        }
        tangents.add(tangent);
    }

    public void averageTangents() {
        if (tangents == null || tangents.isEmpty()) {
            return;
        }
        for (Vector3f tangent : tangents) {
            Vector3f.add(averagedTangent, tangent, averagedTangent);
        }
        averagedTangent.normalise();
    }

    public Vector3f getAverageTangent() {
        return averagedTangent;
    }

}
