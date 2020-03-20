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
        int pos = (y * width + x) * channels;
        if (pos + 3 < buffer.length) {
            int v = (buffer[pos+3] << 24)&0xff000000;
            v |= (buffer[pos+2] << 16)&0x00ff0000;
            v |= (buffer[pos+1] << 8)&0x0000ff00;
            //v |= buffer[pos+0] << 0;
            return v;
        }
        return 0;
    }
}
