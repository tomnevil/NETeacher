package com.neteacher.progress.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomeToday {
    private List<TodayTask> tasks;
    private HomeStats stats;
    private CheckInStatus checkIn;
    private int totalStars;

    @Data
    public static class HomeStats {
        private double speakingAvg;
        private int streakDays;
        private int totalMinutes;
        private int totalStars;
    }
}
