package tools;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class TestBuffer {


    public static void main1(String[] args) {
        ByteBuffer data = ByteBuffer.allocate(22);
        IntBuffer ibuf = data.asIntBuffer();

        data.put((byte) 10);
        System.out.println("bbuf=" + data.position() + ",cap=" + data.capacity() + ",lim=" + data.limit());
        System.out.println("ibuf=" + ibuf.position() + ",cap=" + ibuf.capacity() + ",lim=" + ibuf.limit());
        ibuf.put(20);
        System.out.println("bbuf=" + data.position() + ",cap=" + data.capacity() + ",lim=" + data.limit());
        System.out.println("ibuf=" + ibuf.position() + ",cap=" + ibuf.capacity() + ",lim=" + ibuf.limit());
        for (int i = 0; i < data.capacity(); i++) {
            System.out.print(" " + data.get(i));
        }
        System.out.println();
    }

    public static void main(String[] args) {
//        Vector4 a = encodeFloatRGBA(1.5f);
//        Vector4 b = encodeFloatRGBA(1.3405345E-6f);
//        Vector4 c = encodeFloatRGBA(-0.061906934f);
//        Vector4 d = encodeFloatRGBA(0.016596038f);
        int debug = 1;
        new TestBuffer().main();
    }

    static class Vector4 {
        float[] data = new float[4];

        Vector4(float d0, float d1, float d2, float d3) {
            data[0] = d0;
            data[1] = d1;
            data[2] = d2;
            data[3] = d3;
        }

        Vector4() {

        }

        Vector4(float[] d) {
            for (int i = 0; i < data.length; i++) {
                data[i] = d[i];
            }
        }


        Vector4 mul(float f) {
            return new Vector4(data[0] * f, data[1] * f, data[2] * f, data[3] * f);
        }

        Vector4 sub(Vector4 v) {
            return new Vector4(data[0] - v.data[0], data[1] - v.data[1], data[2] - v.data[2], data[3] - v.data[3]);
        }

        Vector4 frac() {
            return new Vector4(data[0] - (float) Math.floor(data[0]), data[1] - (float) Math.floor(data[1]), data[2] - (float) Math.floor(data[2]), data[3] - (float) Math.floor(data[3]));
        }

        float dot(Vector4 v) {
            float p = 0.f;
            int i;
            for (i = 0; i < data.length; ++i)
                p += v.data[i] * this.data[i];
            return p;
        }

        float x() {
            return data[0];
        }

        float y() {
            return data[1];
        }

        float z() {
            return data[2];
        }

        float w() {
            return data[3];
        }
    }

    private static Vector4 encodeFloatRGBA(float v) {
        v = v * 0.01f + 0.5f;
        Vector4 kEncodeMul = new Vector4(1.0f, 255.0f, 65025.0f, 160581375.0f);
        float kEncodeBit = 1.0f / 255.0f;
        Vector4 enc = kEncodeMul.mul(v);
        for (int i = 0; i < 4; i++)
            enc.data[i] = enc.data[i] - (float) Math.floor(enc.data[i]);
        enc = enc.sub(new Vector4(enc.y(), enc.z(), enc.w(), enc.w()).mul(kEncodeBit));
        return enc;
    }

    float DecodeFloatRGBA(Vector4 enc) {
        Vector4 kDecodeDot = new Vector4(1.0f, 1 / 255.0f, 1 / 65025.0f, 1 / 160581375.0f);
        return enc.dot(kDecodeDot);
    }

    // Encoding/decoding [0..1) floats into 8 bit/channel RGBA. Note that 1.0 will not be encoded properly.
    Vector4 EncodeFloatRGBA(float v) {
        Vector4 kEncodeMul = new Vector4(1.0f, 255.0f, 65025.0f, 160581375.0f);
        float kEncodeBit = 1.0f / 255.0f;
        Vector4 enc = kEncodeMul.mul(v);
        enc = enc.frac();
        enc = enc.sub(new Vector4(enc.y(), enc.z(), enc.w(), enc.w()).mul(kEncodeBit));
        return enc;
    }

    static final float[] kEncodeMul = {1.0f, 255.0f, 65025.0f, 160581375.0f};
    static float[] encChannel = {0, 0, 0, 0};
    static float[] result = {0, 0, 0, 0};

    static float[] encodeFloatRGBA1(float v) {
        float kEncodeBit = 1.0f / 255.0f;
        for (int i = 0; i < 4; i++)
            result[i] = kEncodeMul[i] * v;
        for (int i = 0; i < 4; i++) {
            result[i] = result[i] - (float) Math.floor(result[i]);
        }
        encChannel[0] = result[1];
        encChannel[1] = result[2];
        encChannel[2] = result[3];
        encChannel[3] = result[3];
        for (int i = 0; i < 4; i++) {
            result[i] -= encChannel[i] * kEncodeBit;
        }
        return result;
    }

    int main() {
//        Vector4 in = new Vector4(0.1f, 0.2f, 0.3f, 0.4f);
//
//        float decoded = DecodeFloatRGBA(in);
//        System.out.println("decoded : " + decoded);
//
//        Vector4 encoded = EncodeFloatRGBA(decoded);
//        System.out.println("encoded : " + encoded.x() + "," + encoded.y() + "," + encoded.z() + "," + encoded.w());

        Vector4 encoded = EncodeFloatRGBA(0.061906934f);
        float[] encoded1 = encodeFloatRGBA1(0.061906934f);

        Vector4 m = new Vector4();
        for (int i = 0; i < encoded1.length; i++) {
            byte b = (byte) (encoded1[i] * 256);
            m.data[i] = (b & 0xff) / 256f;

        }
        System.out.println("decode =" + DecodeFloatRGBA(m));
        return 0;
    }
}
