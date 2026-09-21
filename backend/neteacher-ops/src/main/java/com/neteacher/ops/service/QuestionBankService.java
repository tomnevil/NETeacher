package com.neteacher.ops.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neteacher.assessment.entity.Question;
import com.neteacher.assessment.repository.QuestionRepository;
import com.neteacher.ops.dto.CoverageDTO;
import com.neteacher.ops.dto.GenerateResultDTO;
import com.neteacher.ops.dto.ImportResultDTO;
import com.neteacher.ops.dto.QuestionDTO;
import com.neteacher.ops.dto.QuestionUpsertDTO;
import com.neteacher.ops.util.CsvUtil;
import com.neteacher.common.ai.QuestionDraft;
import com.neteacher.common.ai.QuestionGenRequest;
import com.neteacher.common.ai.QuestionGeneratePort;
import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class QuestionBankService {

    private static final List<String> CSV_HEADER = List.of(
            "level", "subject", "type", "knowledgePoint", "usage",
            "status", "mediaUrl", "stem", "options", "answer", "analysis", "source");

    private final QuestionRepository questionRepository;
    private final ObjectMapper objectMapper;
    private final QuestionGeneratePort questionGeneratePort;

    @Autowired
    public QuestionBankService(QuestionRepository questionRepository, ObjectMapper objectMapper,
                               QuestionGeneratePort questionGeneratePort) {
        this.questionRepository = questionRepository;
        this.objectMapper = objectMapper;
        this.questionGeneratePort = questionGeneratePort;
    }

    /** 列表查询（可选过滤 + 分页） */
    public Page<QuestionDTO> list(Integer level, String subject, String type, String status,
                                  String usage, String knowledgePoint, String keyword, int page, int size) {
        Specification<Question> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> preds = new ArrayList<>();
            if (level != null) {
                preds.add(cb.equal(root.get("level"), level));
            }
            if (subject != null && !subject.isBlank()) {
                preds.add(cb.equal(root.get("subject"), subject));
            }
            if (type != null && !type.isBlank()) {
                preds.add(cb.equal(root.get("type"), type));
            }
            if (status != null && !status.isBlank()) {
                preds.add(cb.equal(root.get("status"), status));
            }
            if (usage != null && !usage.isBlank()) {
                preds.add(cb.like(root.get("usage"), "%" + usage + "%"));
            }
            if (knowledgePoint != null && !knowledgePoint.isBlank()) {
                preds.add(cb.like(root.get("knowledgePoint"), "%" + knowledgePoint + "%"));
            }
            if (keyword != null && !keyword.isBlank()) {
                preds.add(cb.or(
                        cb.like(root.get("stem"), "%" + keyword + "%"),
                        cb.like(root.get("knowledgePoint"), "%" + keyword + "%")));
            }
            return cb.and(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size));
        return questionRepository.findAll(spec, pageable).map(this::toDto);
    }

    public QuestionDTO getById(Long id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "题目不存在: " + id));
        return toDto(q);
    }

    public QuestionDTO create(QuestionUpsertDTO dto) {
        Question q = fromUpsert(new Question(), dto);
        return toDto(questionRepository.save(q));
    }

    public QuestionDTO update(Long id, QuestionUpsertDTO dto) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "题目不存在: " + id));
        fromUpsert(q, dto);
        return toDto(questionRepository.save(q));
    }

    @Transactional
    public void delete(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new BizException(ErrorCode.NOT_FOUND, "题目不存在: " + id);
        }
        questionRepository.deleteById(id);
    }

    /** AI 出题：调用出题端口生成内容并落库为草稿（status=draft, source=ai），需人工复核后发布 */
    @Transactional
    public GenerateResultDTO generate(QuestionGenRequest req) {
        GenerateResultDTO result = new GenerateResultDTO();
        Integer level = req.getLevel() == null ? 1 : req.getLevel();
        String subject = orDefault(req.getSubject(), "word").trim();
        String usage = orDefault(req.getUsage(), "practice").trim();
        String type = orDefault(req.getType(), "mcq").trim();
        int count = req.getCount() == null ? 5 : Math.min(Math.max(req.getCount(), 1), 20);
        req.setLevel(level);
        req.setSubject(subject);
        req.setUsage(usage);
        req.setType(type);
        req.setCount(count);

        List<QuestionDraft> drafts;
        try {
            drafts = questionGeneratePort.generate(req);
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "AI 出题失败: " + e.getMessage());
        }
        result.setProvider(questionGeneratePort.provider());
        if (drafts == null || drafts.isEmpty()) {
            // generated 保持 0：模型未返回可解析内容，前端据此提示
            return result;
        }
        for (QuestionDraft d : drafts) {
            if (d.getStem() == null || d.getStem().isBlank()) {
                continue;
            }
            Question q = new Question();
            q.setLevel(level);
            q.setSubject(subject);
            q.setType(type);
            q.setStem(d.getStem());
            q.setUsage(usage);
            // 生成结果一律先落为草稿，避免未经审核的内容进入抽题与练习
            q.setStatus("draft");
            q.setSource("ai");
            q.setKnowledgePoint(orDefault(d.getKnowledgePoint(), req.getKnowledgePoint()));
            q.setAnswer(orDefault(d.getAnswer(), ""));
            q.setAnalysis(d.getAnalysis());
            q.setOptions(toOptionsJson(d.getOptions()));
            result.getQuestions().add(toDto(questionRepository.save(q)));
        }
        result.setGenerated(result.getQuestions().size());
        return result;
    }

    /** 批量导入 CSV（每行新建一条，跳过非法行并记录原因） */
    public ImportResultDTO importCsv(MultipartFile file) throws IOException {
        ImportResultDTO result = new ImportResultDTO();
        List<List<String>> rows = CsvUtil.parse(file.getInputStream());
        if (rows.isEmpty()) {
            return result;
        }
        // 解析表头 -> 列索引（去除可能的 UTF-8 BOM）
        Map<String, Integer> headerIdx = new LinkedHashMap<>();
        List<String> header = rows.get(0);
        for (int i = 0; i < header.size(); i++) {
            headerIdx.put(header.get(i).trim().replace("\uFEFF", ""), i);
        }
        for (int r = 1; r < rows.size(); r++) {
            List<String> row = rows.get(r);
            try {
                Question q = new Question();
                Integer level = cellInt(headerIdx, row, "level");
                if (level == null) {
                    skip(result, r, "缺少有效的 level");
                    continue;
                }
                String subject = cell(headerIdx, row, "subject");
                if (subject == null || subject.isBlank()) {
                    skip(result, r, "缺少 subject");
                    continue;
                }
                String stem = cell(headerIdx, row, "stem");
                if (stem == null || stem.isBlank()) {
                    skip(result, r, "缺少 stem 题干");
                    continue;
                }
                q.setLevel(level);
                q.setSubject(subject.trim());
                q.setStem(stem);
                q.setType(orDefault(cell(headerIdx, row, "type"), "mcq"));
                q.setKnowledgePoint(cell(headerIdx, row, "knowledgePoint"));
                q.setUsage(cell(headerIdx, row, "usage"));
                q.setStatus(orDefault(cell(headerIdx, row, "status"), "published"));
                q.setMediaUrl(cell(headerIdx, row, "mediaUrl"));
                q.setAnswer(orDefault(cell(headerIdx, row, "answer"), ""));
                q.setAnalysis(cell(headerIdx, row, "analysis"));
                q.setSource(orDefault(cell(headerIdx, row, "source"), "imported"));
                // options：竖线分隔 -> JSON
                String optCell = cell(headerIdx, row, "options");
                List<String> opts = new ArrayList<>();
                if (optCell != null && !optCell.isBlank()) {
                    for (String o : optCell.split("\\|")) {
                        if (!o.isBlank()) {
                            opts.add(o.trim());
                        }
                    }
                }
                q.setOptions(objectMapper.writeValueAsString(opts));
                questionRepository.save(q);
                result.setImported(result.getImported() + 1);
            } catch (Exception e) {
                skip(result, r, "解析失败: " + e.getMessage());
            }
        }
        return result;
    }

    /** 导出 CSV（带 BOM，Excel 直接打开） */
    public String exportCsv() {
        List<Question> all = questionRepository.findAll();
        List<List<String>> rows = all.stream().map(q -> {
            List<String> row = new ArrayList<>();
            row.add(q.getLevel() == null ? "" : String.valueOf(q.getLevel()));
            row.add(nullToEmpty(q.getSubject()));
            row.add(nullToEmpty(q.getType()));
            row.add(nullToEmpty(q.getKnowledgePoint()));
            row.add(nullToEmpty(q.getUsage()));
            row.add(nullToEmpty(q.getStatus()));
            row.add(nullToEmpty(q.getMediaUrl()));
            row.add(nullToEmpty(q.getStem()));
            row.add(optionsToCell(q.getOptions()));
            row.add(nullToEmpty(q.getAnswer()));
            row.add(nullToEmpty(q.getAnalysis()));
            row.add(nullToEmpty(q.getSource()));
            return row;
        }).collect(Collectors.toList());
        return CsvUtil.toCsv(CSV_HEADER, rows);
    }

    /**
     * 覆盖度统计。默认「等级 × 学科」；by=knowledgePoint 时按「等级 × 知识点」聚合。
     */
    public List<CoverageDTO> coverage(String by) {
        boolean byKnowledge = "knowledgePoint".equalsIgnoreCase(by);
        List<Question> all = questionRepository.findAll();
        Map<String, CoverageDTO> map = new LinkedHashMap<>();
        for (Question q : all) {
            Integer level = q.getLevel() == null ? 0 : q.getLevel();
            String dim;
            if (byKnowledge) {
                dim = orDefault(q.getKnowledgePoint(), "未标注");
            } else {
                dim = orDefault(q.getSubject(), "?");
            }
            String key = level + "|" + dim;
            CoverageDTO dto = map.computeIfAbsent(key, k -> {
                CoverageDTO c = new CoverageDTO();
                c.setLevel(level);
                if (byKnowledge) {
                    c.setKnowledgePoint(dim);
                } else {
                    c.setSubject(dim);
                }
                c.setTotal(0);
                c.setPublished(0);
                c.setDraft(0);
                return c;
            });
            dto.setTotal(dto.getTotal() + 1);
            if ("published".equals(q.getStatus())) {
                dto.setPublished(dto.getPublished() + 1);
            } else if ("draft".equals(q.getStatus())) {
                dto.setDraft(dto.getDraft() + 1);
            }
        }
        List<CoverageDTO> out = new ArrayList<>(map.values());
        // 稳定排序：等级升序 + 维度名升序，避免插入顺序导致的抖动
        out.sort(Comparator.comparing(CoverageDTO::getLevel)
                .thenComparing(c -> c.getSubject() != null ? c.getSubject() : c.getKnowledgePoint()));
        return out;
    }

    // ---------- 内部工具 ----------

    private QuestionDTO toDto(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setLevel(q.getLevel());
        dto.setSubject(q.getSubject());
        dto.setType(q.getType());
        dto.setStem(q.getStem());
        dto.setOptions(optionsFromJson(q.getOptions()));
        dto.setAnswer(q.getAnswer());
        dto.setAnalysis(q.getAnalysis());
        dto.setKnowledgePoint(q.getKnowledgePoint());
        dto.setMediaUrl(q.getMediaUrl());
        dto.setUsage(q.getUsage());
        dto.setStatus(q.getStatus());
        dto.setSource(q.getSource());
        dto.setCreatedAt(q.getCreatedAt());
        dto.setUpdatedAt(q.getUpdatedAt());
        return dto;
    }

    private Question fromUpsert(Question q, QuestionUpsertDTO dto) {
        q.setLevel(dto.getLevel());
        q.setSubject(dto.getSubject());
        q.setType(dto.getType() == null ? "mcq" : dto.getType());
        q.setStem(dto.getStem());
        q.setKnowledgePoint(dto.getKnowledgePoint());
        q.setMediaUrl(dto.getMediaUrl());
        q.setUsage(dto.getUsage());
        q.setStatus(dto.getStatus() == null ? "published" : dto.getStatus());
        q.setSource(dto.getSource() == null ? "teacher" : dto.getSource());
        q.setAnswer(dto.getAnswer());
        q.setAnalysis(dto.getAnalysis());
        try {
            q.setOptions(objectMapper.writeValueAsString(dto.getOptions() == null ? List.of() : dto.getOptions()));
        } catch (IOException e) {
            q.setOptions("[]");
        }
        return q;
    }

    private List<String> optionsFromJson(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private String optionsToCell(String json) {
        return optionsFromJson(json).stream().collect(Collectors.joining(" | "));
    }

    private String toOptionsJson(List<String> opts) {
        try {
            return objectMapper.writeValueAsString(opts == null ? List.of() : opts);
        } catch (IOException e) {
            return "[]";
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private String orDefault(String s, String def) {
        return (s == null || s.isBlank()) ? def : s;
    }

    private String cell(Map<String, Integer> idx, List<String> row, String name) {
        Integer i = idx.get(name);
        if (i == null || i >= row.size()) {
            return null;
        }
        return row.get(i);
    }

    private Integer cellInt(Map<String, Integer> idx, List<String> row, String name) {
        String v = cell(idx, row, name);
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void skip(ImportResultDTO result, int rowNum, String reason) {
        result.setSkipped(result.getSkipped() + 1);
        result.getErrors().add("第 " + (rowNum + 1) + " 行: " + reason);
    }
}
