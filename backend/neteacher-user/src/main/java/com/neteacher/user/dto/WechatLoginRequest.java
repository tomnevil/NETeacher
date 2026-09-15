package com.neteacher.user.dto;

import lombok.Data;

/**
 * 微信登录请求。code 由前端从微信 OAuth 回调获取；未配置 appid/secret 时降级为演示登录。
 */
@Data
public class WechatLoginRequest {
    private String code;
}
