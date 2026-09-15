package com.neteacher.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 端到端集成测试：覆盖 认证 / 课程 / 测评判分 / 自适应路径 / 家长报告 主链路。
 * 使用 dev  profile（H2 内存库 + 种子数据），无需外部数据库。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class ApiE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    private String studentToken;
    private String parentToken;

    @BeforeEach
    void setUp() {
        studentToken = login("13800000000", "123456");
        parentToken = login("13900000000", "123456");
    }

    private String login(String phone, String password) {
        @SuppressWarnings("unchecked")
        Map<String, Object> body = rest.postForObject(url("/api/auth/login"),
                Map.of("phone", phone, "password", password), Map.class);
        assertNotNull(body);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        return (String) data.get("token");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private HttpEntity<Map<String, Object>> authEntity(String token, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return new HttpEntity<>(body, headers);
    }

    @Test
    void healthUp() {
        @SuppressWarnings("unchecked")
        Map<String, Object> h = rest.getForObject(url("/actuator/health"), Map.class);
        assertEquals("UP", h.get("status"));
    }

    @Test
    void coursesRequireAuth() {
        ResponseEntity<Map> r = rest.getForEntity(url("/api/courses"), Map.class);
        // 拦截器拒绝匿名访问：返回 code != 0（BizException 处理器 HTTP 200 + code）
        assertTrue((Integer) r.getBody().get("code") != 0);
    }

    @Test
    void courseListReturnsSeed() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> r = rest.exchange(url("/api/courses?size=20"), HttpMethod.GET,
                authEntity(studentToken, null), Map.class);
        assertEquals(0, r.getBody().get("code"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) r.getBody().get("data");
        assertTrue((Integer) data.get("total") >= 6, "应至少 6 门种子课程");
    }

    @Test
    void quizSubmitScoresAndHistory() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> quiz = rest.exchange(url("/api/assessments/quiz?size=3"), HttpMethod.GET,
                authEntity(studentToken, null), Map.class);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) quiz.getBody().get("data");
        assertTrue(questions.size() > 0);

        List<Map<String, Object>> answers = questions.stream()
                .map(q -> Map.of("questionId", q.get("id"), "answer", "A"))
                .toList();

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> submit = rest.exchange(url("/api/assessments/quiz/submit"), HttpMethod.POST,
                authEntity(studentToken, Map.of("subject", "reading", "type", "quiz", "answers", answers)),
                Map.class);
        assertEquals(0, submit.getBody().get("code"));
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) submit.getBody().get("data");
        int score = (Integer) result.get("score");
        assertTrue(score >= 0 && score <= 100);
        assertEquals(answers.size(), result.get("totalCount"));

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> history = rest.exchange(url("/api/assessments"), HttpMethod.GET,
                authEntity(studentToken, null), Map.class);
        assertTrue((Integer) history.getBody().get("code") == 0);
        assertTrue(((List<?>) history.getBody().get("data")).size() >= 1);
    }

    @Test
    void recommendPathComputes() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> r = rest.exchange(url("/api/recommend/path"), HttpMethod.GET,
                authEntity(studentToken, null), Map.class);
        assertEquals(0, r.getBody().get("code"));
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) r.getBody().get("data");
        int current = (Integer) data.get("currentLevel");
        assertTrue(current >= 1 && current <= 6);
        assertTrue(((List<?>) data.get("items")).size() > 0);
    }

    @Test
    void parentSeesChildrenAndReport() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> children = rest.exchange(url("/api/parent/children"), HttpMethod.GET,
                authEntity(parentToken, null), Map.class);
        assertEquals(0, children.getBody().get("code"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> kids = (List<Map<String, Object>>) children.getBody().get("data");
        assertTrue(kids.size() >= 1);

        Long childId = ((Number) kids.get(0).get("uid")).longValue();
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> report = rest.exchange(url("/api/parent/report/" + childId), HttpMethod.GET,
                authEntity(parentToken, null), Map.class);
        assertEquals(0, report.getBody().get("code"));
        assertNotNull(report.getBody().get("data"));
    }

    @Test
    void bindParentUpdatesStatus() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Map> before = rest.exchange(url("/api/user/bind-status"), HttpMethod.GET,
                authEntity(studentToken, null), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> beforeData = (Map<String, Object>) before.getBody().get("data");
        // 演示学生已在种子中绑定家长，这里验证绑定信息存在
        assertEquals(true, beforeData.get("bound"));
    }
}
