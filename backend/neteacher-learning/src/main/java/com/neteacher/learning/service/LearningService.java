package com.neteacher.learning.service;

import com.neteacher.common.result.PageResult;
import com.neteacher.learning.dto.LearningRecordRequest;
import com.neteacher.learning.entity.LearningRecord;
import com.neteacher.learning.repository.LearningRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 学习记录服务（M3）。
 */
@Service
@RequiredArgsConstructor
public class LearningService {

    private final LearningRecordRepository recordRepo;

    public LearningRecord create(Long userId, LearningRecordRequest req) {
        LearningRecord r = new LearningRecord();
        r.setUserId(userId);
        r.setCourseId(req.getCourseId());
        r.setLessonId(req.getLessonId());
        r.setModule(req.getModule());
        r.setScore(req.getScore());
        r.setDurationSec(req.getDurationSec());
        r.setFinished(req.getFinished());
        r.setDetail(req.getDetail());
        return recordRepo.save(r);
    }

    public PageResult<LearningRecord> listMine(Long userId, int page, int size) {
        Page<LearningRecord> pg = recordRepo.findByUserId(userId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
        return PageResult.of(pg.getContent(), pg.getTotalElements(), pg.getNumber(), pg.getSize());
    }
}
