package org.mini.hmi.widget;

public interface JoystickListener {
    public static final int OPERATION_PRESS = 0;
    public static final int OPERATION_MOVE = 1;
    public static final int OPERATION_RELEASE = 2;

    void onMove(int operation, float x, float y, float degree);
}
