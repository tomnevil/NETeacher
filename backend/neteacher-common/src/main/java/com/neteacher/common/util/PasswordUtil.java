package com.neteacher.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * 轻量密码哈希工具：SHA-256 + 随机盐，避免引入额外安全依赖。
 * 存储格式为 {@code <salt-hex>:<hash-hex>}。生产环境可替换为 BCrypt/Argon2。
 */
public final class PasswordUtil {

    private static final int SALT_BYTES = 16;

    private PasswordUtil() {
    }

    public static String hash(String plain) {
        if (plain == null) {
            return null;
        }
        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = digest(salt, plain);
        return HexFormat.of().formatHex(salt) + ":" + HexFormat.of().formatHex(hash);
    }

    public static boolean matches(String plain, String stored) {
        if (plain == null || stored == null || !stored.contains(":")) {
            return false;
        }
        String[] parts = stored.split(":", 2);
        byte[] salt = HexFormat.of().parseHex(parts[0]);
        byte[] expected = HexFormat.of().parseHex(parts[1]);
        byte[] actual = digest(salt, plain);
        return MessageDigest.isEqual(expected, actual);
    }

    private static byte[] digest(byte[] salt, String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(plain.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new IllegalStateException("password hash failed", e);
        }
    }
}
