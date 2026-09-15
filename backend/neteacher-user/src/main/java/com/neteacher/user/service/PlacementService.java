package com.neteacher.user.service;

import com.neteacher.user.dto.*;
import com.neteacher.user.entity.StudentProfile;
import com.neteacher.user.repository.StudentProfileRepository;
import com.neteacher.user.repository.UserAccountRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 入学测评定级（API-CRS-001）：降级模式下仅通过词汇 + 听力理解题确定初始级别 L1-L6。
 * 题目为内置分级题库（mock 环境，无真实音频，听力题以音标/注音呈现）。
 */
@Service
public class PlacementService {

    private final StudentProfileRepository profileRepo;
    private final UserAccountRepository userRepo;

    public PlacementService(StudentProfileRepository profileRepo, UserAccountRepository userRepo) {
        this.profileRepo = profileRepo;
        this.userRepo = userRepo;
    }

    @Data
    private static class Q {
        String id;
        String type;
        String prompt;
        String audioHint;
        List<String> options;
        int level;
        int answer; // 正确选项下标
    }

    private static Q q(String id, String type, String prompt, String audio, List<String> options, int level, int answer) {
        Q x = new Q();
        x.id = id;
        x.type = type;
        x.prompt = prompt;
        x.audioHint = audio;
        x.options = options;
        x.level = level;
        x.answer = answer;
        return x;
    }

    /** 内置分级题库：L1-L6 各 2 题（词汇 + 听力） */
    private static final List<Q> BANK = List.of(
            q("q1", "VOCAB", "选出「苹果」的英文", null, List.of("apple", "banana", "orange"), 1, 0),
            q("q2", "LISTENING", "听音选词：/ˈæp.əl/ (a-pul)", "/ˈæp.əl/", List.of("apple", "elephant", "ice"), 1, 0),
            q("q3", "VOCAB", "选出「学校」的英文", null, List.of("school", "park", "home"), 2, 0),
            q("q4", "LISTENING", "听音选词：/skuːl/", "/skuːl/", List.of("school", "spoon", "star"), 2, 0),
            q("q5", "VOCAB", "选出「图书馆」的英文", null, List.of("library", "garden", "kitchen"), 3, 0),
            q("q6", "LISTENING", "听音选词：/ˈlaɪ.brər.i/", "/ˈlaɪ.brər.i/", List.of("library", "river", "rabbit"), 3, 0),
            q("q7", "VOCAB", "选出「鼓励」的英文", null, List.of("encourage", "enjoy", "enter"), 4, 0),
            q("q8", "LISTENING", "听音选词：/ɪnˈkʌr.ɪdʒ/", "/ɪnˈkʌr.ɪdʒ/", List.of("encourage", "courage", "connect"), 4, 0),
            q("q9", "VOCAB", "选出「environment」的中文", null, List.of("环境", "装备", "入口"), 5, 0),
            q("q10", "LISTENING", "听音选词：/ɪnˈvaɪ.rən.mənt/", "/ɪnˈvaɪ.rən.mənt/", List.of("environment", "experiment", "employ"), 5, 0),
            q("q11", "VOCAB", "选出「sustainable」的中文", null, List.of("可持续的", "稳定的", "充分的"), 6, 0),
            q("q12", "LISTENING", "听音选词：/səˈsteɪ.nə.bəl/", "/səˈsteɪ.nə.bəl/", List.of("sustainable", "substantial", "sufficient"), 6, 0)
    );

    public List<PlacementQuestionDTO> getQuestions() {
        List<PlacementQuestionDTO> list = new ArrayList<>();
        for (Q x : BANK) {
            PlacementQuestionDTO d = new PlacementQuestionDTO();
            d.setId(x.id);
            d.setType(x.type);
            d.setPrompt(x.prompt);
            d.setAudioHint(x.audioHint);
            d.setOptions(x.options);
            d.setLevel(x.level);
            list.add(d);
        }
        return list;
    }

    public PlacementResultDTO evaluate(Long studentId, List<PlacementAnswerDTO> answers) {
        Map<String, Integer> correctMap = new LinkedHashMap<>();
        for (Q x : BANK) correctMap.put(x.id, x.answer);

        int total = BANK.size();
        int correct = 0;
        if (answers != null) {
            for (PlacementAnswerDTO a : answers) {
                Integer right = correctMap.get(a.getQuestionId());
                if (right != null && right.equals(a.getSelected())) correct++;
            }
        }
        int score = (int) Math.round((double) correct / total * 100);
        int initLevel = levelFromRatio((double) correct / total);

        StudentProfile profile = profileRepo.findByStudentId(studentId).orElseGet(() -> {
            StudentProfile p = new StudentProfile();
            p.setStudentId(studentId);
            userRepo.findById(studentId).ifPresent(u -> p.setGrade(u.getGrade()));
            return p;
        });
        profile.setInitLevel(initLevel);
        profileRepo.save(profile);

        PlacementResultDTO r = new PlacementResultDTO();
        r.setInitLevel(initLevel);
        r.setScore(score);
        r.setTotal(total);
        r.setCorrect(correct);
        r.setBand(bandName(initLevel));
        return r;
    }

    private int levelFromRatio(double ratio) {
        if (ratio >= 0.85) return 6;
        if (ratio >= 0.70) return 5;
        if (ratio >= 0.55) return 4;
        if (ratio >= 0.40) return 3;
        if (ratio >= 0.25) return 2;
        return 1;
    }

    private String bandName(int level) {
        return switch (level) {
            case 1 -> "启蒙级 (L1)";
            case 2 -> "基础级 (L2)";
            case 3 -> "进阶级 (L3)";
            case 4 -> "提高级 (L4)";
            case 5 -> "熟练级 (L5)";
            default -> "精通级 (L6)";
        };
    }
}
