package com.neteacher.progress.dto;

import lombok.Data;

import java.util.List;

@Data
public class CheckInStatus {
    private boolean checkedToday;
    private int streakDays;
    private int totalDays;
    private List<Boolean> week;
}
