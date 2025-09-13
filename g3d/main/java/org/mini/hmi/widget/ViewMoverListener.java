package org.mini.hmi.widget;

public interface ViewMoverListener {
    /**
     * Called when the distance between the camera and the target has changed.
     *
     * @param distance
     */
    void onDistanceChanged(float distance);

    /**
     * Called when the angle between the camera and the target has changed.
     *
     * @param angle
     */
    void onAngleChanged(float angle);

    /**
     * Called when the pitch of the camera has changed.
     *
     * @param pitch
     */
    void onPitchChanged(float pitch);

    /**
     * Called when the touch has ended. now can save the current state
     */
    void onChangeEnded();
}
