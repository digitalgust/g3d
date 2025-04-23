package org.mini.g3d.skybox;


import org.mini.g3d.core.DisplayManager;

import java.util.Calendar;

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

    Calendar calendar = Calendar.getInstance();
    float time = 0f;
    float diff = 0;
    int segment;
    float percentInSeg = 0f;


    public DayAndNight() {
        setSecondsPerDay(SECONDS_PER_DAY_DEFAULT);
        diff = 0f;

    }

    public void setSecondsPerDay(int seconds) {
        if (seconds < 24) throw new RuntimeException("seconds must bigger than 24");
        seconds = seconds / 24 * 24;//near by 24
        secondsPerDay = seconds;
        secondsPerHour = secondsPerDay / 24;
        timeSegDef = new int[][]{
                {0 * secondsPerHour, 5 * secondsPerHour, NIGHT},//0h-5h  =0 (NIGHT)
                {5 * secondsPerHour, 9 * secondsPerHour, NIGHT_TO_DAY},
                {9 * secondsPerHour, 21 * secondsPerHour, DAY},
                {21 * secondsPerHour, 24 * secondsPerHour, DAY_TO_NIGHT},
        };
    }

    public void reset() {
        diff = 0;
    }


    public void update() {
        calendar.setTimeInMillis(DisplayManager.getCurrentTime());
        time = calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);

        float t = getTime();

        for (int i = 0, imax = timeSegDef.length; i < imax; i++) {
            if (t >= timeSegDef[i][0] && t < timeSegDef[i][1]) {
                segment = timeSegDef[i][2];
                percentInSeg = (t - timeSegDef[i][0]) / (timeSegDef[i][1] - timeSegDef[i][0]);
                break;
            }
        }
    }

    public int getSecondsPerDay() {
        return secondsPerDay;
    }

    public int getSegment() {
        return segment;
    }

    public float getPercentInSeg() {
        return percentInSeg;
    }

    public float getTime() {
        return (time + diff) % secondsPerDay;
    }

    public void setTime(float ptime) {
        float t = getTime();
        this.diff = ptime - t;
    }
}
