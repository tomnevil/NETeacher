package com.neteacher.ops.controller;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.result.Result;
import com.neteacher.ops.dto.CoverageDTO;
import com.neteacher.ops.dto.ImportResultDTO;
import com.neteacher.ops.dto.QuestionDTO;
import com.neteacher.ops.dto.QuestionUpsertDTO;
import com.neteacher.ops.service.QuestionBankService;
import com.neteacher.ops.util.CsvUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/ops/questions")
public class QuestionBankController {

    private final QuestionBankService service;

    @Autowired
    public QuestionBankController(QuestionBankService service) {
        this.service = service;
    }

    private void requireRole(HttpServletRequest req, String... allowed) {
        Object roleAttr = req.getAttribute("role");
        String role = roleAttr == null ? null : String.valueOf(roleAttr);
        for (String a : allowed) {
            if (a.equalsIgnoreCase(role)) {
                return;
            }
        }
        throw new BizException(ErrorCode.FORBIDDEN, "需要 " + String.join("/", allowed) + " 角色");
    }

    /** 列表（可选过滤 + 分页） */
    @GetMapping
    public Result<Page<QuestionDTO>> list(HttpServletRequest req,
                                          @RequestParam(required = false) Integer level,
                                          @RequestParam(required = false) String subject,
                                          @RequestParam(required = false) String type,
                                          @RequestParam(required = false) String status,
                                          @RequestParam(required = false) String usage,
                                          @RequestParam(required = false) String knowledgePoint,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        requireRole(req, "ADMIN", "TEACHER");
        return Result.success(service.list(level, subject, type, status, usage, knowledgePoint, keyword, page, size));
    }

    /** 详情 */
    @GetMapping("/{id}")
    public Result<QuestionDTO> get(HttpServletRequest req, @PathVariable Long id) {
        requireRole(req, "ADMIN", "TEACHER");
        return Result.success(service.getById(id));
    }

    /** 新增 */
    @PostMapping
    public Result<QuestionDTO> create(HttpServletRequest req, @RequestBody QuestionUpsertDTO dto) {
        requireRole(req, "ADMIN", "TEACHER");
        return Result.success(service.create(dto));
    }

    /** 修改 */
    @PutMapping("/{id}")
    public Result<QuestionDTO> update(HttpServletRequest req, @PathVariable Long id, @RequestBody QuestionUpsertDTO dto) {
        requireRole(req, "ADMIN", "TEACHER");
        return Result.success(service.update(id, dto));
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(HttpServletRequest req, @PathVariable Long id) {
        requireRole(req, "ADMIN", "TEACHER");
        service.delete(id);
        return Result.success(null);
    }

    /** 批量导入 CSV */
    @PostMapping("/import")
    public Result<ImportResultDTO> importCsv(HttpServletRequest req, @RequestParam("file") MultipartFile file) {
        requireRole(req, "ADMIN", "TEACHER");
        try {
            return Result.success(service.importCsv(file));
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "导入失败: " + e.getMessage());
        }
    }

    /** 导出 CSV */
    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> export(HttpServletRequest req) {
        requireRole(req, "ADMIN", "TEACHER");
        String csv = service.exportCsv();
        ByteArrayResource resource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"question-bank.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    /** 下载空白导入模板（仅表头） */
    @GetMapping("/template")
    public ResponseEntity<ByteArrayResource> template(HttpServletRequest req) {
        requireRole(req, "ADMIN", "TEACHER");
        String csv = CsvUtil.toCsv(List.of(
                "level", "subject", "type", "knowledgePoint", "usage",
                "status", "mediaUrl", "stem", "options", "answer", "analysis", "source"), List.of());
        ByteArrayResource resource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"question-bank-template.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=utf-8"))
                .contentLength(resource.contentLength())
                .body(resource);
    }

    /** 覆盖度统计 */
    @GetMapping("/coverage")
    public Result<List<CoverageDTO>> coverage(HttpServletRequest req) {
        requireRole(req, "ADMIN", "TEACHER");
        return Result.success(service.coverage());
    }
}
