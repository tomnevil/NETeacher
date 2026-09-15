package com.neteacher.progress.dto;

import lombok.Data;

@Data
public class TodayTask {
    private String id;
    private String title;
    private String type;
    private Integer level;
    private int progress;
    private int starsReward;
    private String category;
    private String to;
}
