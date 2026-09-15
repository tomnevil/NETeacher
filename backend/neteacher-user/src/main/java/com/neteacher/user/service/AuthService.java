package com.neteacher.user.service;

import com.neteacher.common.exception.BizException;
import com.neteacher.common.exception.ErrorCode;
import com.neteacher.common.util.JwtUtil;
import com.neteacher.common.util.PasswordUtil;
import com.neteacher.user.dto.LoginRequest;
import com.neteacher.user.dto.LoginResponse;
import com.neteacher.user.dto.RegisterRequest;
import com.neteacher.user.dto.UserInfo;
import com.neteacher.user.dto.WechatLoginRequest;
import com.neteacher.user.entity.UserAccount;
import com.neteacher.user.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 账号与认证服务（DE-001 / DE-002 / DE-003）。
 * 当前采用手机号 + 密码；微信登录、家长绑定留待后续迭代。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userRepo;
    private final JwtUtil jwtUtil;

    @Value("${wx.appid:}")
    private String wxAppid;

    @Value("${wx.secret:}")
    private String wxSecret;

    public LoginResponse login(LoginRequest req) {
        UserAccount u = userRepo.findByPhone(req.getPhone())
                .orElseThrow(() -> new BizException(ErrorCode.BIZ_ERROR, "账号不存在"));
        if (u.getStatus() != null && u.getStatus() == 9) {
            throw new BizException(ErrorCode.FORBIDDEN, "账号已注销");
        }
        if (!PasswordUtil.matches(req.getPassword(), u.getPassword())) {
            throw new BizException(ErrorCode.BIZ_ERROR, "手机号或密码错误");
        }
        return issue(u);
    }

    public UserInfo register(RegisterRequest req) {
        if (userRepo.findByPhone(req.getPhone()).isPresent()) {
            throw new BizException(ErrorCode.BIZ_ERROR, "该手机号已注册");
        }
        UserAccount u = new UserAccount();
        u.setPhone(req.getPhone());
        u.setPassword(PasswordUtil.hash(req.getPassword()));
        u.setNickname(Objects.requireNonNullElse(req.getNickname(),
                "学员" + req.getPhone().substring(req.getPhone().length() - 4)));
        u.setRole(Objects.requireNonNullElse(req.getRole(), "STUDENT"));
        u.setGrade(req.getGrade());
        u.setStatus(2);
        // 学生绑定家长（可选）：若填了家长手机号且已存在，则建立 parentId 关联
        if (req.getParentPhone() != null && !req.getParentPhone().isBlank()) {
            userRepo.findByPhone(req.getParentPhone()).ifPresent(p -> u.setParentId(p.getId()));
        }
        return toInfo(userRepo.save(u));
    }

    public UserInfo profile(Long uid) {
        return toInfo(load(uid));
    }

    public UserInfo updateProfile(Long uid, String nickname, String avatar) {
        UserAccount u = load(uid);
        if (nickname != null) {
            u.setNickname(nickname);
        }
        if (avatar != null) {
            u.setAvatar(avatar);
        }
        return toInfo(userRepo.save(u));
    }

    private UserAccount load(Long uid) {
        return userRepo.findById(uid)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "用户不存在"));
    }

    private LoginResponse issue(UserAccount u) {
        Map<String, Object> claims = new HashMap<>(2);
        claims.put("uid", u.getId());
        claims.put("role", Objects.requireNonNullElse(u.getRole(), "STUDENT"));
        LoginResponse resp = new LoginResponse();
        resp.setToken(jwtUtil.generateToken(u.getPhone(), claims));
        resp.setUid(u.getId());
        resp.setNickname(u.getNickname());
        resp.setRole(u.getRole());
        resp.setGrade(u.getGrade());
        return resp;
    }

    public LoginResponse wechatLogin(WechatLoginRequest req) {
        if (req.getCode() == null || req.getCode().isBlank()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "微信 code 不能为空");
        }
        String openid;
        if (wxAppid != null && !wxAppid.isBlank() && wxSecret != null && !wxSecret.isBlank()) {
            openid = fetchOpenid(req.getCode());
        } else {
            // 未配置微信凭证：降级为基于 code 的演示账号，便于本地联调
            openid = "mock_" + req.getCode();
        }
        UserAccount u = userRepo.findByWxOpenid(openid).orElseGet(() -> {
            UserAccount n = new UserAccount();
            n.setWxOpenid(openid);
            String phone = "wx" + Math.abs(openid.hashCode() % 1_000_000_000L);
            if (userRepo.findByPhone(phone).isEmpty()) {
                n.setPhone(phone);
            } else {
                n.setPhone(phone + (openid.hashCode() & 0xff));
            }
            n.setNickname("微信用户" + phone.substring(2));
            n.setRole("STUDENT");
            n.setStatus(2);
            return userRepo.save(n);
        });
        return issue(u);
    }

    private String fetchOpenid(String code) {
        try {
            String url = String.format(
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    wxAppid, wxSecret, code);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            String body = resp.body();
            int i = body.indexOf("\"openid\"");
            if (i < 0) return "mock_" + code;
            int start = body.indexOf('"', i + 9) + 1;
            int end = body.indexOf('"', start);
            return body.substring(start, end);
        } catch (Exception e) {
            return "mock_" + code;
        }
    }

    public Integer gradeOf(Long uid) {
        UserAccount u = userRepo.findById(uid).orElse(null);
        return u == null ? null : u.getGrade();
    }

    private UserInfo toInfo(UserAccount u) {
        UserInfo info = new UserInfo();
        info.setUid(u.getId());
        info.setPhone(u.getPhone());
        info.setNickname(u.getNickname());
        info.setAvatar(u.getAvatar());
        info.setGrade(u.getGrade());
        info.setRole(u.getRole());
        info.setStatus(u.getStatus());
        return info;
    }
}
