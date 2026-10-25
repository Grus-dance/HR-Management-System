package org.example.common;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 月份工具：统一使用 Asia/Shanghai 与 yyyy-MM 格式。
 */
public final class MonthUtil {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private MonthUtil() {
    }

    public static String current() {
        return YearMonth.now(ZONE).format(FORMATTER);
    }

    public static String shift(String month, int delta) {
        return YearMonth.parse(month, FORMATTER).plusMonths(delta).format(FORMATTER);
    }

    public static List<String> lastMonths(int count) {
        List<String> months = new ArrayList<>(count);
        YearMonth now = YearMonth.now(ZONE);
        for (int i = count - 1; i >= 0; i--) {
            months.add(now.minusMonths(i).format(FORMATTER));
        }
        return months;
    }

    public static boolean isValid(String month) {
        try {
            YearMonth.parse(month, FORMATTER);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public static boolean isFuture(String month) {
        return month.compareTo(current()) > 0;
    }
}
