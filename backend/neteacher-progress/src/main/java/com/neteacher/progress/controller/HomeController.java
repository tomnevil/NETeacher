package com.neteacher.progress.controller;

import com.neteacher.common.result.Result;
import com.neteacher.progress.dto.CheckInStatus;
import com.neteacher.progress.dto.HomeToday;
import com.neteacher.progress.dto.LevelInfo;
import com.neteacher.progress.service.GamificationService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {

    private final GamificationService gamificationService;

    public HomeController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/today")
    public Result<HomeToday> today(HttpServletRequest request) {
        return Result.success(gamificationService.getHome(request));
    }

    @GetMapping("/map")
    public Result<List<LevelInfo>> map(HttpServletRequest request) {
        return Result.success(gamificationService.getLevels(request));
    }

    @GetMapping("/checkin")
    public Result<CheckInStatus> checkin(HttpServletRequest request) {
        return Result.success(gamificationService.getCheckInStatus(request));
    }

    @PostMapping("/checkin")
    public Result<CheckInStatus> doCheckin(HttpServletRequest request) {
        return Result.success(gamificationService.doCheckIn(request));
    }
}
