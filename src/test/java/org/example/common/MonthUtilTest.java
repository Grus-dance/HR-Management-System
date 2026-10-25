package org.example.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonthUtilTest {

    @Test
    void lastMonthsShouldContainRequestedCount() {
        List<String> months = MonthUtil.lastMonths(6);
        assertEquals(6, months.size());
        for (int i = 1; i < months.size(); i++) {
            assertTrue(months.get(i - 1).compareTo(months.get(i)) < 0);
        }
        assertEquals(MonthUtil.current(), months.get(months.size() - 1));
    }

    @Test
    void shiftShouldCrossYearBoundary() {
        assertEquals("2025-12", MonthUtil.shift("2026-01", -1));
        assertEquals("2027-01", MonthUtil.shift("2026-12", 1));
    }

    @Test
    void validationShouldRejectBadMonth() {
        assertTrue(MonthUtil.isValid("2026-10"));
        assertFalse(MonthUtil.isValid("2026-13"));
        assertFalse(MonthUtil.isValid("2026/10"));
    }
}
