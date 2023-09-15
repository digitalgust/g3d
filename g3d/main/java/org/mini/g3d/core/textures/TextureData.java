package org.mini.g3d.core.textures;

public class TextureData {

    private int width;
    private int height;
    private int channels;
    private byte[] buffer;

    public TextureData(byte[] buffer, int width, int height, int channels) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
        this.channels = channels;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getChannels() {
        return channels;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getRGB(int x, int y) {
        if (x >= width) x = width - 1;
        if (y >= height) y = height - 1;
        int pos = (y * width + x) * channels;
        if (pos + 3 < buffer.length) {
            int v = 0;//(buffer[pos + 3] & 0xff) << 24;
            v |= (buffer[pos + 2] & 0xff) << 16;
            v |= (buffer[pos + 1] & 0xff) << 8;
            v |= (buffer[pos + 0] & 0xff) << 0;
            return v;
        }
        return 0;
    }
}
