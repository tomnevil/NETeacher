package com.neteacher.ops.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 轻量 CSV 工具（RFC 4180 子集）：
 * - 字段若含逗号/引号/换行则用双引号包裹，内部引号转义为两个引号
 * - 支持被引号包裹的字段跨行
 * 生成的文件带 UTF-8 BOM，Excel 可直接打开中文不乱码。
 */
public final class CsvUtil {

    private CsvUtil() {
    }

    /** 生成带 BOM 的 CSV 文本（首行表头） */
    public static String toCsv(List<String> header, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append('\uFEFF'); // UTF-8 BOM
        sb.append(escapeRow(header)).append("\r\n");
        for (List<String> row : rows) {
            sb.append(escapeRow(row)).append("\r\n");
        }
        return sb.toString();
    }

    private static String escapeRow(List<String> row) {
        List<String> escaped = new ArrayList<>(row.size());
        for (String v : row) {
            escaped.add(escape(v));
        }
        return String.join(",", escaped);
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /** 解析 CSV 输入流，返回行列表（每行是字段列表，首行为表头） */
    public static List<List<String>> parse(InputStream in) throws IOException {
        List<List<String>> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            List<String> current = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean inQuotes = false;
            boolean started = false; // 是否已开始一个字段（用于区分空行）
            int ch;
            while ((ch = reader.read()) != -1) {
                char c = (char) ch;
                if (inQuotes) {
                    if (c == '"') {
                        int next = reader.read();
                        if (next == '"') {
                            field.append('"');
                        } else {
                            inQuotes = false;
                            if (next != -1) {
                                // 把非引号字符交回主循环继续处理
                                if (next == '\r' || next == '\n') {
                                    c = (char) next;
                                    // fall through to line handling below
                                } else {
                                    // 普通字符（理论上引号后不应有普通字符，但容错）
                                    field.append((char) next);
                                    continue;
                                }
                            }
                        }
                    } else {
                        field.append(c);
                    }
                }
                if (!inQuotes) {
                    if (c == '"') {
                        inQuotes = true;
                        started = true;
                    } else if (c == ',') {
                        current.add(field.toString());
                        field.setLength(0);
                        started = true;
                    } else if (c == '\r') {
                        // 可能后跟 \n
                        int next = reader.read();
                        if (next != '\n' && next != -1) {
                            // 把下一个字符放回由下轮处理（简单处理：仅当不是引号/逗号）
                        }
                        finishField(current, field, started);
                        if (!current.isEmpty()) {
                            result.add(current);
                        }
                        current = new ArrayList<>();
                        field.setLength(0);
                        started = false;
                    } else if (c == '\n') {
                        finishField(current, field, started);
                        if (!current.isEmpty()) {
                            result.add(current);
                        }
                        current = new ArrayList<>();
                        field.setLength(0);
                        started = false;
                    } else {
                        field.append(c);
                        started = true;
                    }
                }
            }
            // 处理最后一行（无结尾换行）
            if (started || !current.isEmpty() || field.length() > 0) {
                finishField(current, field, started);
                if (!current.isEmpty()) {
                    result.add(current);
                }
            }
        }
        return result;
    }

    private static void finishField(List<String> current, StringBuilder field, boolean started) {
        if (started || field.length() > 0) {
            current.add(field.toString());
        } else {
            current.add("");
        }
        field.setLength(0);
    }
}
