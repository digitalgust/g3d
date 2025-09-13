package org.mini.g3d.terrain;


import org.mini.g3d.core.util.G3dUtil;
import org.mini.g3d.core.util.Loader;
import org.mini.g3d.core.models.RawModel;
import org.mini.g3d.core.textures.TextureData;
import org.mini.g3d.core.vector.Vector2f;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.glwrap.GLUtil;
import org.mini.util.SysLog;

public class Terrain {
    public static final float DEFAULT_MAP_SCALE = 2;

    private static final float MAX_HEIGHT = 10;
    private static final float MAX_PIXEL_COLOUR = 256 * 256 * 256;

    static final int SIDE_FACE_GRIDS = 2;//地图边缘下垂两格,两侧各下垂此格数

    private int cols;
    private int rows;
    private RawModel model;
    private TerrainTexturePack texturePack;
    private TerrainTexture blendMap;
    private float mapScale = DEFAULT_MAP_SCALE;

    private float[][] heights;

    private Vector3f min, max;
    Loader loader;

    /**
     * @param cols
     * @param rows
     * @param texturePack
     * @param blendMap
     * @param heightMap
     */

    public Terrain(int cols, int rows, TerrainTexturePack texturePack, TerrainTexture blendMap, String heightMap) {
        this(cols, rows, texturePack, blendMap, heightMap, DEFAULT_MAP_SCALE);
    }

    /**
     * @param cols
     * @param rows
     * @param texturePack
     * @param blendMap
     * @param heightMap
     * @param mapScale
     */
    public Terrain(int cols, int rows, TerrainTexturePack texturePack, TerrainTexture blendMap, String heightMap, float mapScale) {
        this.cols = cols;
        this.rows = rows;
        this.texturePack = texturePack;
        this.blendMap = blendMap;
        this.mapScale = mapScale;
        long startAt = System.currentTimeMillis();
        loader = new Loader();
        this.model = generateTerrain(heightMap);
        SysLog.info("G3D|load terrain cost (ms): " + (System.currentTimeMillis() - startAt));
    }

    public float getCols() {
        return cols;
    }

    public float getRows() {
        return rows;
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


    /**
     * 根据地图数据,生成一个网格地图
     * 其中比较关键的是,在生成网格时,三条线只能生成两行,因些地图顶点数会多出一行和一列
     * <p>
     * 另一个是生成了侧面网络,使地图看起来并不单薄,同样要注意生成时,三条线才能生成两行
     *
     * @param heightMap
     * @return
     */
    private RawModel generateTerrain(String heightMap) {

//		BufferedImage image = null;
//		try {
//			image = ImageIO.read(new File(EngineManager.RES_LOC + heightMap + ".png"));
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

        int[] w_h_d = {0, 0, 0};
        byte[] filecont = G3dUtil.loadFileFromJar(heightMap);
        byte[] b = GLUtil.image_parse_from_file_content(filecont, w_h_d);

        heights = new float[cols + 1][rows + 1];
        TextureData tdata = new TextureData(b, w_h_d[0], w_h_d[1], w_h_d[2]);

        int[][] extEastWest = {{0, 0}, {0, -1}, {0, -2}, {cols, -2}, {cols, -1}, {cols, 0}};
        int[][] extNorthSouth = {{0, -0}, {0, -1}, {0, -2}, {rows, -2}, {rows, -1}, {rows, 0}};

        //地图栅格数
        int count = (cols + 1) * (rows + 1);
        //加上四面侧边
        count += (rows + 1) * extEastWest.length + (cols + 1) * extNorthSouth.length;

        float[] vertices = new float[count * 3];
        float[] normals = new float[count * 3];
        float[] textureCoords = new float[count * 2];
        int[] indices = new int[6 * (cols) * (rows) + 6 * (cols + rows) * 4];

        int vertexPointer = 0;
        for (int i = 0; i <= rows; i++) {
            for (int j = 0; j <= cols; j++) {
                vertices[vertexPointer * 3] = j * mapScale;
                float h = getHeight(j, i, tdata);
                heights[j][i] = h;
                vertices[vertexPointer * 3 + 1] = h;
                vertices[vertexPointer * 3 + 2] = i * mapScale;
                Vector3f normal = calculateNormal(j, i, tdata);
                normals[vertexPointer * 3] = normal.x;
                normals[vertexPointer * 3 + 1] = normal.y;
                normals[vertexPointer * 3 + 2] = normal.z;
                textureCoords[vertexPointer * 2] = (float) j / ((float) cols);
                textureCoords[vertexPointer * 2 + 1] = (float) i / ((float) rows);
                vertexPointer++;
            }
        }
        int vertCnt1 = vertexPointer;

        //SysLog.info("G3D|======================" + vertexPointer);
        //添加地图东西两个侧面
        for (int k = 0; k < extEastWest.length; k++) {
            int xIdx = extEastWest[k][0];
            int yIdx = extEastWest[k][1];
            for (int zIdx = 0; zIdx <= rows; zIdx++) {
                float h = getHeight(xIdx, zIdx, tdata);

                //vertices
                vertices[vertexPointer * 3] = xIdx * mapScale;
                vertices[vertexPointer * 3 + 1] = h + yIdx * mapScale * 8;
                vertices[vertexPointer * 3 + 2] = zIdx * mapScale;
                //System.out.print(" " + vertices[vertexPointer * 3] + "/" + vertices[vertexPointer * 3 + 1] + "/" + vertices[vertexPointer * 3 + 2]);
                //normals
                normals[vertexPointer * 3] = xIdx == 0 ? -1 : 1;
                normals[vertexPointer * 3 + 1] = 0;
                normals[vertexPointer * 3 + 2] = 0;
                //uv
                textureCoords[vertexPointer * 2] = (float) (k) / ((float) cols);
                textureCoords[vertexPointer * 2 + 1] = (float) zIdx / ((float) rows);
                vertexPointer++;
            }
            //SysLog.info("G3D|" + vertexPointer);
        }
        int vectCnt2 = vertexPointer;
        //SysLog.info("G3D|======================" + vertexPointer);

        //添加南北两个侧面, 数组第二维为{z,y}
        for (int k = 0; k < extNorthSouth.length; k++) {
            int zIdx = extNorthSouth[k][0];
            int yIdx = extNorthSouth[k][1];
            for (int xIdx = 0; xIdx <= cols; xIdx++) {
                float h = getHeight(xIdx, zIdx, tdata);

                //vertices
                vertices[vertexPointer * 3] = xIdx * mapScale;
                vertices[vertexPointer * 3 + 1] = h + yIdx * mapScale * 8;
                vertices[vertexPointer * 3 + 2] = zIdx * mapScale;
                //System.out.print(" " + vertices[vertexPointer * 3] + "/" + vertices[vertexPointer * 3 + 1] + "/" + vertices[vertexPointer * 3 + 2]);
                //normals
                normals[vertexPointer * 3] = 0;
                normals[vertexPointer * 3 + 1] = 0;
                normals[vertexPointer * 3 + 2] = zIdx == 0 ? -1 : 1;
                //uv
                textureCoords[vertexPointer * 2] = (float) (xIdx) / ((float) cols);
                textureCoords[vertexPointer * 2 + 1] = (float) k / ((float) rows);
                vertexPointer++;
            }
            //SysLog.info("G3D|" + vertexPointer);
        }
        //SysLog.info("G3D|======================");

        int pointer = 0;
        for (int gz = 0; gz < rows; gz++) {
            for (int gx = 0; gx < cols; gx++) {
                int topLeft = ((gz * (cols + 1)) + gx);
                int topRight = topLeft + 1;
                int bottomLeft = (((gz + 1) * (cols + 1)) + gx);
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = topLeft;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = topRight;
                indices[pointer++] = topRight;
                indices[pointer++] = bottomLeft;
                indices[pointer++] = bottomRight;
            }
        }

        int[] eastAndWest = {0, 1, 3, 4};//见前面注释
        for (int k = 0; k < eastAndWest.length; k++) {
            int idx = eastAndWest[k];
            for (int zIdx = 0; zIdx < rows; zIdx++) {
                int topLeft = (((idx + 1) * (rows + 1)) + zIdx);
                int bottomLeft = topLeft + 1;
                int topRight = (((idx) * (rows + 1)) + zIdx);
                int bottomRight = topRight + 1;
                indices[pointer++] = vertCnt1 + topLeft;
                indices[pointer++] = vertCnt1 + bottomLeft;
                indices[pointer++] = vertCnt1 + topRight;
                indices[pointer++] = vertCnt1 + topRight;
                indices[pointer++] = vertCnt1 + bottomLeft;
                indices[pointer++] = vertCnt1 + bottomRight;
//                Vector3f p1 = new Vector3f(vertices[(vertCnt1 + topLeft) * 3], vertices[(vertCnt1 + topLeft) * 3 + 1], vertices[(vertCnt1 + topLeft) * 3 + 2]);
//                Vector3f p2 = new Vector3f(vertices[(vertCnt1 + bottomLeft) * 3], vertices[(vertCnt1 + bottomLeft) * 3 + 1], vertices[(vertCnt1 + bottomLeft) * 3 + 2]);
//                Vector3f p3 = new Vector3f(vertices[(vertCnt1 + topRight) * 3], vertices[(vertCnt1 + topRight) * 3 + 1], vertices[(vertCnt1 + topRight) * 3 + 2]);
//                Vector3f p4 = new Vector3f(vertices[(vertCnt1 + bottomRight) * 3], vertices[(vertCnt1 + bottomRight) * 3 + 1], vertices[(vertCnt1 + bottomRight) * 3 + 2]);
//                SysLog.info("G3D|" + (vertCnt1 + topLeft) + "      " + p1 + " , " + p2 + " , " + p3 + "                     " + p3 + " , " + p2 + " , " + p4);
            }
            //SysLog.info("G3D|" + );
        }
        //SysLog.info("G3D|======================");

        int[] northAndSouth = {0, 1, 3, 4};//因为两行格子需要三条线,所以跳过边上那条
        for (int k = 0; k < northAndSouth.length; k++) {
            int idx = northAndSouth[k];
            for (int xIdx = 0; xIdx < cols; xIdx++) {
                int topLeft = (((idx + 1) * (cols + 1)) + xIdx);
                int topRight = topLeft + 1;
                int bottomLeft = (((idx) * (cols + 1)) + xIdx);
                int bottomRight = bottomLeft + 1;
                indices[pointer++] = vectCnt2 + topLeft;
                indices[pointer++] = vectCnt2 + bottomLeft;
                indices[pointer++] = vectCnt2 + topRight;
                indices[pointer++] = vectCnt2 + topRight;
                indices[pointer++] = vectCnt2 + bottomLeft;
                indices[pointer++] = vectCnt2 + bottomRight;
//                Vector3f p1 = new Vector3f(vertices[(vertCnt1 + topLeft) * 3], vertices[(vertCnt1 + topLeft) * 3 + 1], vertices[(vertCnt1 + topLeft) * 3 + 2]);
//                Vector3f p2 = new Vector3f(vertices[(vertCnt1 + bottomLeft) * 3], vertices[(vertCnt1 + bottomLeft) * 3 + 1], vertices[(vertCnt1 + bottomLeft) * 3 + 2]);
//                Vector3f p3 = new Vector3f(vertices[(vertCnt1 + topRight) * 3], vertices[(vertCnt1 + topRight) * 3 + 1], vertices[(vertCnt1 + topRight) * 3 + 2]);
//                Vector3f p4 = new Vector3f(vertices[(vertCnt1 + bottomRight) * 3], vertices[(vertCnt1 + bottomRight) * 3 + 1], vertices[(vertCnt1 + bottomRight) * 3 + 2]);
//                SysLog.info("G3D|" + (vectCnt2 + topLeft) + "      " + p1 + " , " + p2 + " , " + p3 + "                     " + p3 + " , " + p2 + " , " + p4);

            }
            //SysLog.info("G3D|" + );
        }

        min = new Vector3f(0.f, getHeightOfTerrain(0, 0), 0.f);

        float maxX = cols * mapScale;
        float maxZ = rows * mapScale;
        float maxY = getHeightOfTerrain(maxX, maxZ);
        max = new Vector3f(maxX, maxY, maxZ);

        return loader.loadToVAO(vertices, textureCoords, normals, indices);
    }


    public float getHeightOfTerrain(float worldX, float worldZ) {
        float terrainX = worldX;
        float terrainZ = worldZ;
        int gridX = (int) Math.floor(terrainX / mapScale);
        int gridZ = (int) Math.floor(terrainZ / mapScale);
        if (gridX < 0) gridX = 0;
        if (gridZ < 0) gridZ = 0;
        if (gridX >= cols) gridX = cols - 1;
        if (gridZ >= rows) gridZ = rows - 1;

        float xCoord = (terrainX % mapScale) / mapScale;
        float zCoord = (terrainZ % mapScale) / mapScale;

        float answer;
        if (xCoord <= (1 - zCoord)) {
            answer = G3dUtil.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0), new Vector3f(1,
                    heights[gridX + 1][gridZ], 0), new Vector3f(0,
                    heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        } else {
            answer = G3dUtil.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0), new Vector3f(1,
                    heights[gridX + 1][gridZ + 1], 1), new Vector3f(0,
                    heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
        }

        return answer;
    }


    public float getAbsHeightOfTerrain(float worldX, float worldZ) {
        float terrainX = worldX;
        float terrainZ = worldZ;
        int gridX = (int) Math.floor(terrainX / mapScale);
        int gridZ = (int) Math.floor(terrainZ / mapScale);
        if (gridX < 0) gridX = 0;
        if (gridZ < 0) gridZ = 0;
        if (gridX >= cols) gridX = cols - 1;
        if (gridZ >= rows) gridZ = rows - 1;

        float xCoord = (terrainX % mapScale) / mapScale;
        float zCoord = (terrainZ % mapScale) / mapScale;

        return heights[gridX][gridZ];
    }

    private Vector3f calculateNormal(int x, int z, TextureData image) {
        float heightL = getHeight(x - 1, z, image);
        float heightR = getHeight(x + 1, z, image);
        float heightD = getHeight(x, z - 1, image);
        float heightU = getHeight(x, z + 1, image);
        Vector3f normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
        normal.normalise();
        return normal;
    }

    private float getHeight(int x, int z, TextureData image) {
        if (x < 0) x = 0;
        if (z < 0) z = 0;
        if (x >= image.getWidth()) {
            x = image.getWidth() - 1;
        }
        if (z >= image.getHeight()) {
            z = image.getHeight() - 1;
        }


        float height = image.getRGB(x, z);
        height += MAX_PIXEL_COLOUR / 2f;
        height /= MAX_PIXEL_COLOUR / 2f;
        height *= MAX_HEIGHT;
        return height;

    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }

    public float getMapScale() {
        return mapScale;
    }

    public void setMapScale(float mapScale) {
        this.mapScale = mapScale;
    }

}
