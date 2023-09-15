package org.mini.g3d.skybox;


import org.mini.g3d.core.DisplayManager;

/**
 * 日夜更替系统
 */
public class DayAndNight {
    public static final int NIGHT = 0;
    public static final int NIGHT_TO_DAY = 1;
    public static final int DAY = 2;
    public static final int DAY_TO_NIGHT = 3;


    static final int SECONDS_PER_DAY_DEFAULT = 7200;//second
    int secondsPerDay;//second
    int secondsPerHour;//second
    int[][] timeSegDef;

    float time = 0f;
    int segment;
    float percentInSeg = 0f;

    public DayAndNight() {
        setSecondsPerDay(SECONDS_PER_DAY_DEFAULT);
    }

    public void setSecondsPerDay(int seconds) {
        if (seconds < 24) throw new RuntimeException("seconds must bigger than 24");
        seconds = seconds / 24 * 24;//near by 24
        secondsPerDay = seconds;
        secondsPerHour = secondsPerDay / 24;
        timeSegDef = new int[][]{
                {0 * secondsPerHour, 8 * secondsPerHour, NIGHT},//0h-5h  =0 (NIGHT)
                {8 * secondsPerHour, 11 * secondsPerHour, NIGHT_TO_DAY},
                {11 * secondsPerHour, 21 * secondsPerHour, DAY},
                {21 * secondsPerHour, 24 * secondsPerHour, DAY_TO_NIGHT},
        };
    }

    public void reset() {
        time = 0;
    }


    public void update() {
        float sec = DisplayManager.getFrameTimeSeconds();
        time += sec;
        time %= secondsPerDay;


        for (int i = 0, imax = timeSegDef.length; i < imax; i++) {
            if (time >= timeSegDef[i][0] && time < timeSegDef[i][1]) {
                segment = timeSegDef[i][2];
                percentInSeg = (time - timeSegDef[i][0]) / (timeSegDef[i][1] - timeSegDef[i][0]);
                break;
            }
        }
    }

    public int getSegment() {
        return segment;
    }

    public float getPercentInSeg() {
        return percentInSeg;
    }

    public float getTime() {
        return time;
    }
}
