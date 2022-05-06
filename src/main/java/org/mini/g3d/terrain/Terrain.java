package org.mini.g3d.terrain;


import org.mini.g3d.core.EngineManager;
import org.mini.g3d.core.Loader;
import org.mini.g3d.shadowmap.ShadowMappingRenderer;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.toolbox.G3dMath;
import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.textures.TextureData;
import org.mini.glwrap.GLUtil;

public class Terrain {

    private static float mapSize;
    private static final float MAX_HEIGHT = 10;
    private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;

    private float x;
    private float z;
    private RawModel model;
    private TerrainTexturePack texturePack;
    private TerrainTexture blendMap;
    private ShadowMappingRenderer shadowMappingRenderer;

    private float[][] heights;

    public Terrain(int gridX, int gridZ, Loader loader, TerrainTexturePack texturePack, TerrainTexture blendMap, String heightMap, ShadowMappingRenderer shadowMappingRenderer) {
        this.texturePack = texturePack;
        this.blendMap = blendMap;
        long startAt = System.currentTimeMillis();
        this.model = generateTerrain(loader, heightMap);
        this.shadowMappingRenderer = shadowMappingRenderer;
        this.x = gridX * mapSize;
        this.z = gridZ * mapSize;
        System.out.println("load terrain cost (ms): " + (System.currentTimeMillis() - startAt));
    }

    public float getX() {
        return x;
    }

    public float getZ() {
        return z;
    }

    public RawModel getModel() {
        return model;
    }

    public TerrainTexturePack getTexturePack() {
        return texturePack;
    }

    public TerrainTexture getBlendMap() {
        return blendMap;
    }

    public int getShdowMappingTexture() {
        return shadowMappingRenderer.getShadowMappingTexture();
    }

    public Matrix4f getDepthBiasMVP() {
        return shadowMappingRenderer.getDepthBiasMVP();
    }

    private RawModel generateTerrain(Loader loader, String heightMap) {

//		BufferedImage image = null;
//		try {
//			image = ImageIO.read(new File(EngineManager.RES_LOC + heightMap + ".png"));
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

        int[] w_h_d = {0, 0, 0};
        byte[] filecont = EngineManager.loadFileFromJar(EngineManager.RES_LOC + heightMap + ".png");
        byte[] b = GLUtil.image_parse_from_file_content(filecont, w_h_d);

        int VERTEX_COUNT = w_h_d[1];
        mapSize = w_h_d[1] * 2;
        heights = new float[VERTEX_COUNT][VERTEX_COUNT];
        TextureData tdata = new TextureData(b, w_h_d[0], w_h_d[1], w_h_d[2]);

        int count = VERTEX_COUNT * VERTEX_COUNT;
        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];
        int[] indices = new int[6 * (VERTEX_COUNT - 1) * (VERTEX_COUNT - 1)];
        int vertexPointer = 0;
        for (int i = 0; i < VERTEX_COUNT; i++) {
            for (int j = 0; j < VERTEX_COUNT; j++) {
                vertices[vertexPointer * 3] = (float) j / ((float) VERTEX_COUNT - 1) * mapSize;
                float height = getHeight(j, i, tdata);
                heights[j][i] = height;
                vertices[vertexPointer * 3 + 1] = height;
                vertices[vertexPointer * 3 + 2] = (float) i / ((float) VERTEX_COUNT - 1) * mapSize;
                Vector3f normal = calculateNormal(j, i, tdata);
                normals[vertexPointer * 3] = normal.x;
                normals[vertexPointer * 3 + 1] = normal.y;
                normals[vertexPointer * 3 + 2] = normal.z;
                textureCoords[vertexPointer * 2] = (float) j / ((float) VERTEX_COUNT - 1);
                textureCoords[vertexPointer * 2 + 1] = (float) i / ((float) VERTEX_COUNT - 1);
                vertexPointer++;
            }
        }
        int pointer = 0;
        for (int gz = 0; gz < VERTEX_COUNT - 1; gz++) {
            for (int gx = 0; gx < VERTEX_COUNT - 1; gx++) {
                int topLeft = (gz * VERTEX_COUNT) + gx;
                int topRight = topLeft + 1;
                int bottomLeft = ((gz + 1) * VERTEX_COUNT) + gx;
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }
        return loader.loadToVAO(vertices, textureCoords, normals, indices);
    }

    public static float getMapSize() {
        return mapSize;
    }

    public float getHeightOfTerrain(float worldX, float worldZ) {
        float terrainX = worldX - this.x;
        float terrainZ = worldZ - this.z;
        float gridSquareSize = mapSize / (float) (heights.length - 1);
        int gridX = (int) Math.floor(terrainX / gridSquareSize);
        int gridZ = (int) Math.floor(terrainZ / gridSquareSize);
        if (gridX >= heights.length - 1 || gridZ >= heights.length - 1 || gridX < 0 || gridZ < 0) {
            return 0;
        }

        float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
        float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;

        float answer;
        if (xCoord <= (1 - zCoord)) {
            answer = G3dMath.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1,
                    heights[gridX + 1][gridZ], 0), new Vector3f(0,
                    heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        } else {
            answer = G3dMath.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1,
                    heights[gridX + 1][gridZ + 1], 1), new Vector3f(0,
                    heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        }

        return answer;
    }

    private Vector3f calculateNormal(int x, int y, TextureData image) {
        float heightL = getHeight(x - 1, y, image);
        float heightR = getHeight(x + 1, y, image);
        float heightD = getHeight(x, y - 1, image);
        float heightU = getHeight(x, y + 1, image);
        Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
        normal.normalise();
        return normal;
    }

    private float getHeight(int x, int z, TextureData image) {
        if (x < 0) x = 0;
        if (z < 0) z = 0;
        if (x >= image.getHeight()) {
            x = image.getHeight() - 1;
        }
        if (z >= image.getWidth()) {
            x = image.getWidth() - 1;
        }


        float height = image.getRGB(x, z);
        height += MAX_PIXEL_COLOUR / 2f;
        height /= MAX_PIXEL_COLOUR / 2f;
        height *= MAX_HEIGHT;
        return height;

    }

}
