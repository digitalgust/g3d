package org.mini.g3d.core.objmodel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * PLY (Polygon File Format) loader.
 * Supports ASCII and binary (little-endian / big-endian) formats.
 * Reads vertex positions, normals, and texture coordinates into ModelData.
 * Minimizes object allocation: pre-sized primitive arrays, reusable buffers,
 * manual int parsing for face data.
 */
public class PlyFileLoader {

    private static final int FORMAT_ASCII = 0;
    private static final int FORMAT_BINARY_LE = 1;
    private static final int FORMAT_BINARY_BE = 2;
    private static final int MAX_PROPS = 32;

    public static ModelData loadPLY(String plyFileName) {
        InputStream is = PlyFileLoader.class.getResourceAsStream(plyFileName);
        if (is == null) {
            System.err.println("[G3D][ERROR]PLY file not found: " + plyFileName);
            return null;
        }

        BufferedInputStream bis = new BufferedInputStream(is, 65536);
        StringBuilder lineBuf = new StringBuilder(256);
        try {
            // ========== Parse header ==========
            int format = FORMAT_ASCII;
            int vertexCount = 0;
            int faceCount = 0;

            int[] propSize = new int[MAX_PROPS];
            boolean[] propIsFloat = new boolean[MAX_PROPS];
            boolean[] propIsSigned = new boolean[MAX_PROPS];
            int propCount = 0;

            int colX = -1, colY = -1, colZ = -1;
            int colNx = -1, colNy = -1, colNz = -1;
            int colS = -1, colT = -1;

            int faceCountTypeSize = 1;  // default: uchar
            int faceIndexTypeSize = 4;  // default: int
            int currentElement = 0;     // 0=none, 1=vertex, 2=face, 3=other

            String line;
            while ((line = readLine(bis, lineBuf)) != null) {
                line = line.trim();
                if (line.length() == 0) continue;
                if ("end_header".equals(line)) break;

                if (line.startsWith("format ")) {
                    if (line.indexOf("binary_little_endian") >= 0) format = FORMAT_BINARY_LE;
                    else if (line.indexOf("binary_big_endian") >= 0) format = FORMAT_BINARY_BE;
                } else if (line.startsWith("element ")) {
                    if (line.startsWith("element vertex")) {
                        vertexCount = parseTrailingInt(line);
                        currentElement = 1;
                    } else if (line.startsWith("element face")) {
                        faceCount = parseTrailingInt(line);
                        currentElement = 2;
                    } else {
                        currentElement = 3;
                    }
                } else if (line.startsWith("property ")) {
                    if (currentElement == 1 && !line.startsWith("property list")) {
                        // Vertex scalar property: "property <type> <name>"
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 3 && propCount < MAX_PROPS) {
                            String type = parts[1];
                            String name = parts[2];
                            propSize[propCount] = typeByteSize(type);
                            propIsFloat[propCount] = isFloatType(type);
                            propIsSigned[propCount] = isSignedIntType(type);

                            switch (name) {
                                case "x":
                                    colX = propCount;
                                    break;
                                case "y":
                                    colY = propCount;
                                    break;
                                case "z":
                                    colZ = propCount;
                                    break;
                                case "nx":
                                    colNx = propCount;
                                    break;
                                case "ny":
                                    colNy = propCount;
                                    break;
                                case "nz":
                                    colNz = propCount;
                                    break;
                                case "s":
                                case "u":
                                case "texture_u":
                                    colS = propCount;
                                    break;
                                case "t":
                                case "v":
                                case "texture_v":
                                    colT = propCount;
                                    break;
                            }
                            propCount++;
                        }
                    } else if (currentElement == 2 && line.startsWith("property list")) {
                        // "property list uchar int vertex_indices"
                        String[] parts = line.split("\\s+");
                        if (parts.length >= 5) {
                            faceCountTypeSize = typeByteSize(parts[2]);
                            faceIndexTypeSize = typeByteSize(parts[3]);
                        }
                    }
                }
            }

            if (vertexCount <= 0) {
                System.err.println("[G3D][ERROR]PLY has no vertices: " + plyFileName);
                bis.close();
                return null;
            }

            // ========== Allocate output arrays ==========
            boolean hasNormals = colNx >= 0 && colNy >= 0 && colNz >= 0;
            boolean hasTexCoords = colS >= 0 && colT >= 0;
            float[] verts = new float[vertexCount * 3];
            float[] texCs = new float[vertexCount * 2];
            float[] norms = new float[vertexCount * 3];
            float furthest2 = 0; // squared furthest distance

            // ========== Read vertices ==========
            if (format == FORMAT_ASCII) {
                for (int i = 0; i < vertexCount; i++) {
                    line = readLine(bis, lineBuf);
                    if (line == null) break;
                    String[] vals = line.trim().split("\\s+");
                    float x = colX >= 0 && colX < vals.length ? Float.parseFloat(vals[colX]) : 0;
                    float y = colY >= 0 && colY < vals.length ? Float.parseFloat(vals[colY]) : 0;
                    float z = colZ >= 0 && colZ < vals.length ? Float.parseFloat(vals[colZ]) : 0;
                    int i3 = i * 3;
                    verts[i3] = x;
                    verts[i3 + 1] = y;
                    verts[i3 + 2] = z;
                    float d2 = x * x + y * y + z * z;
                    if (d2 > furthest2) furthest2 = d2;
                    if (hasNormals) {
                        norms[i3] = Float.parseFloat(vals[colNx]);
                        norms[i3 + 1] = Float.parseFloat(vals[colNy]);
                        norms[i3 + 2] = Float.parseFloat(vals[colNz]);
                    }
                    if (hasTexCoords) {
                        int i2 = i * 2;
                        texCs[i2] = Float.parseFloat(vals[colS]);
                        texCs[i2 + 1] = 1.0f - Float.parseFloat(vals[colT]);
                    }
                }
            } else {
                // Binary vertex reading
                int[] propOff = new int[propCount];
                int stride = 0;
                for (int p = 0; p < propCount; p++) {
                    propOff[p] = stride;
                    stride += propSize[p];
                }
                boolean be = (format == FORMAT_BINARY_BE);
                byte[] buf = new byte[stride];

                for (int i = 0; i < vertexCount; i++) {
                    int r = readFully(bis, buf, stride);
                    if(r < stride) break;

                    float x = colX >= 0 ? readFloatProp(buf, propOff[colX], propSize[colX], propIsFloat[colX], propIsSigned[colX], be) : 0;
                    float y = colY >= 0 ? readFloatProp(buf, propOff[colY], propSize[colY], propIsFloat[colY], propIsSigned[colY], be) : 0;
                    float z = colZ >= 0 ? readFloatProp(buf, propOff[colZ], propSize[colZ], propIsFloat[colZ], propIsSigned[colZ], be) : 0;
                    int i3 = i * 3;
                    verts[i3] = x;
                    verts[i3 + 1] = y;
                    verts[i3 + 2] = z;
                    float d2 = x * x + y * y + z * z;
                    if (d2 > furthest2) furthest2 = d2;
                    if (hasNormals) {
                        norms[i3] = readFloatProp(buf, propOff[colNx], propSize[colNx], propIsFloat[colNx], propIsSigned[colNx], be);
                        norms[i3 + 1] = readFloatProp(buf, propOff[colNy], propSize[colNy], propIsFloat[colNy], propIsSigned[colNy], be);
                        norms[i3 + 2] = readFloatProp(buf, propOff[colNz], propSize[colNz], propIsFloat[colNz], propIsSigned[colNz], be);
                    }
                    if (hasTexCoords) {
                        int i2 = i * 2;
                        texCs[i2] = readFloatProp(buf, propOff[colS], propSize[colS], propIsFloat[colS], propIsSigned[colS], be);
                        texCs[i2 + 1] = 1.0f - readFloatProp(buf, propOff[colT], propSize[colT], propIsFloat[colT], propIsSigned[colT], be);
                    }
                }
            }

            float furthest = (float) Math.sqrt(furthest2);

            // ========== Read faces (fan-triangulated) ==========
            int triCap = Math.max(faceCount * 3, 64);
            int[] indices = new int[triCap];
            int idxCount = 0;
            int[] faceV = new int[16]; // reusable per-face vertex buffer

            if (faceCount > 0 && format == FORMAT_ASCII) {
                int[] pos = {0}; // reusable parse cursor
                for (int i = 0; i < faceCount; i++) {
                    line = readLine(bis, lineBuf);
                    if (line == null) break;
                    line = line.trim();
                    if(line.length() == 0) {
                        i--;
                        continue;
                    }

                    pos[0] = 0;
                    int n = nextInt(line, pos);
                    if (n < 3) continue;
                    if (n > faceV.length) faceV = new int[n];
                    for (int j = 0; j < n; j++) {
                        faceV[j] = nextInt(line, pos);
                    }
                    // Fan triangulation: (v0,v1,v2), (v0,v2,v3), ...
                    for (int j = 1; j < n - 1; j++) {
                        if (idxCount + 3 > indices.length) indices = grow(indices);
                        indices[idxCount++] = faceV[0];
                        indices[idxCount++] = faceV[j];
                        indices[idxCount++] = faceV[j + 1];
                    }
                }
            } else if (faceCount > 0) {
                // Binary face reading
                boolean be = (format == FORMAT_BINARY_BE);
                byte[] cntBuf = new byte[faceCountTypeSize];
                byte[] idxBuf = new byte[16 * faceIndexTypeSize];

                for (int i = 0; i < faceCount; i++) {
                    int r = readFully(bis, cntBuf, faceCountTypeSize);
                    if(r < faceCountTypeSize) break;

                    int n = readUInt(cntBuf, 0, faceCountTypeSize, be);
                    if (n < 3) {
                        // skip degenerate face data
                        skipFully(bis, n * faceIndexTypeSize);
                        continue;
                    }
                    int bytesNeeded = n * faceIndexTypeSize;
                    if (bytesNeeded > idxBuf.length) idxBuf = new byte[bytesNeeded];
                    if (n > faceV.length) faceV = new int[n];
                    r = readFully(bis, idxBuf, bytesNeeded);
                    if(r < bytesNeeded) break;

                    for (int j = 0; j < n; j++) {
                        faceV[j] = readUInt(idxBuf, j * faceIndexTypeSize, faceIndexTypeSize, be);
                    }
                    for (int j = 1; j < n - 1; j++) {
                        if (idxCount + 3 > indices.length) indices = grow(indices);
                        indices[idxCount++] = faceV[0];
                        indices[idxCount++] = faceV[j];
                        indices[idxCount++] = faceV[j + 1];
                    }
                }
            }

            // Trim indices array to actual size
            if (idxCount != indices.length) {
                int[] trimmed = new int[idxCount];
                System.arraycopy(indices, 0, trimmed, 0, idxCount);
                indices = trimmed;
            }

            bis.close();
            return new ModelData(verts, texCs, norms, indices, furthest);
        } catch (Exception e) {
            System.err.println("[G3D][ERROR]Error reading PLY: " + plyFileName);
            e.printStackTrace();
            try {
                bis.close();
            } catch (IOException ignored) {
            }
            return null;
        }
    }

    // ==================== Utility methods ====================

    /**
     * Read one line from the stream using a reusable StringBuilder.
     */
    private static String readLine(InputStream in, StringBuilder sb) throws IOException {
        sb.setLength(0);
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\n') return sb.toString();
            if (c != '\r') sb.append((char) c);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Parse trailing integer from line, e.g. "element vertex 1024".
     */
    private static int parseTrailingInt(String s) {
        int end = s.length() - 1;
        while (end >= 0 && (s.charAt(end) < '0' || s.charAt(end) > '9')) end--;
        if (end < 0) return 0;
        int start = end;
        while (start >= 0 && s.charAt(start) >= '0' && s.charAt(start) <= '9') start--;
        int val = 0;
        for (int i = start + 1; i <= end; i++) {
            val = val * 10 + (s.charAt(i) - '0');
        }
        return val;
    }

    /**
     * Parse next integer from string, advancing the cursor pos[0]. No allocation.
     */
    private static int nextInt(String s, int[] pos) {
        int i = pos[0];
        int len = s.length();
        while (i < len) {
            char ch = s.charAt(i);
            if (ch != ' ' && ch != '\t') break;
            i++;
        }
        int sign = 1;
        if (i < len && s.charAt(i) == '-') {
            sign = -1;
            i++;
        }
        int val = 0;
        while (i < len) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') break;
            val = val * 10 + (c - '0');
            i++;
        }
        pos[0] = i;
        return sign * val;
    }


    private static boolean isFloatType(String type) {
        return "float".equals(type) || "double".equals(type)
                || "float32".equals(type) || "float64".equals(type);
    }

    private static boolean isSignedIntType(String type) {
        return "char".equals(type) || "int8".equals(type)
                || "short".equals(type) || "int16".equals(type)
                || "int".equals(type) || "int32".equals(type)
                || "int64".equals(type);
    }

    private static int typeByteSize(String type) {
        switch (type) {
            case "char":
            case "uchar":
            case "uint8":
            case "int8":
                return 1;
            case "short":
            case "ushort":
            case "uint16":
            case "int16":
                return 2;
            case "int":
            case "uint":
            case "float":
            case "int32":
            case "uint32":
            case "float32":
                return 4;
            case "double":
            case "float64":
            case "int64":
            case "uint64":
                return 8;
            default:
                return 4;
        }
    }

    private static int readFully(InputStream in, byte[] buf, int len) throws IOException {
        int off = 0;
        while (off < len) {
            int n = in.read(buf, off, len - off);
            if (n < 0) {
               System.err.println("[G3D][ERROR]Unexpected end of PLY stream");
               break;
            }
            off += n;
        }
        return off;
    }

    private static void skipFully(InputStream in, int len) throws IOException {
        int left = len;
        while (left > 0) {
            long skipped = in.skip(left);
            if (skipped <= 0) {
                // 如果 skip 返回 0 或负数，可能是因为流已经读完了，或者无法跳过
                // 尝试读取一个字节
                int b = in.read();
                if (b < 0) {
                   System.err.println("[G3D][ERROR]Unexpected end of PLY stream");
                   // 如果读到流的末尾，直接返回
                   break;
                }
                left--;
            } else {
                left -= (int) skipped;
            }
        }
    }

    /**
     * Read a vertex property value as float from a binary buffer.
     */
    private static float readFloatProp(byte[] buf, int off, int size, boolean isFloat, boolean isSigned, boolean be) {
        if (isFloat && size == 4) {
            return Float.intBitsToFloat(readInt32(buf, off, be));
        } else if (isFloat && size == 8) {
            return (float) Double.longBitsToDouble(readInt64(buf, off, be));
        } else if (size == 1) {
            return isSigned ? (float) buf[off] : (float) (buf[off] & 0xFF);
        } else if (size == 2) {
            int v = be
                    ? ((buf[off] & 0xFF) << 8) | (buf[off + 1] & 0xFF)
                    : (buf[off] & 0xFF) | ((buf[off + 1] & 0xFF) << 8);
            if (isSigned && (v & 0x8000) != 0) v |= 0xFFFF0000;
            return (float) v;
        } else {
            return (float) readInt32(buf, off, be);
        }
    }

    /**
     * Read an unsigned integer of given byte size from a binary buffer.
     */
    private static int readUInt(byte[] buf, int off, int size, boolean be) {
        if (size == 1) return buf[off] & 0xFF;
        if (size == 2) return be
                ? ((buf[off] & 0xFF) << 8) | (buf[off + 1] & 0xFF)
                : (buf[off] & 0xFF) | ((buf[off + 1] & 0xFF) << 8);
        return readInt32(buf, off, be);
    }

    private static int readInt32(byte[] buf, int off, boolean be) {
        return be
                ? ((buf[off] & 0xFF) << 24) | ((buf[off + 1] & 0xFF) << 16) | ((buf[off + 2] & 0xFF) << 8) | (buf[off + 3] & 0xFF)
                : (buf[off] & 0xFF) | ((buf[off + 1] & 0xFF) << 8) | ((buf[off + 2] & 0xFF) << 16) | ((buf[off + 3] & 0xFF) << 24);
    }

    private static long readInt64(byte[] buf, int off, boolean be) {
        if (be) {
            return ((buf[off] & 0xFFL) << 56) | ((buf[off + 1] & 0xFFL) << 48)
                    | ((buf[off + 2] & 0xFFL) << 40) | ((buf[off + 3] & 0xFFL) << 32)
                    | ((buf[off + 4] & 0xFFL) << 24) | ((buf[off + 5] & 0xFFL) << 16)
                    | ((buf[off + 6] & 0xFFL) << 8) | (buf[off + 7] & 0xFFL);
        } else {
            return (buf[off] & 0xFFL) | ((buf[off + 1] & 0xFFL) << 8)
                    | ((buf[off + 2] & 0xFFL) << 16) | ((buf[off + 3] & 0xFFL) << 24)
                    | ((buf[off + 4] & 0xFFL) << 32) | ((buf[off + 5] & 0xFFL) << 40)
                    | ((buf[off + 6] & 0xFFL) << 48) | ((buf[off + 7] & 0xFFL) << 56);
        }
    }

    /**
     * Grow an int array by 50%.
     */
    private static int[] grow(int[] arr) {
        int[] bigger = new int[arr.length + (arr.length >> 1)];
        System.arraycopy(arr, 0, bigger, 0, arr.length);
        return bigger;
    }
}
