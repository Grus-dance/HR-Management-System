package org.example.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 首页概览：统计卡 + 可选的部门分布 / 最近记录。
 */
@Data
public class DashboardVo {

    private List<StatCardVo> cards;
    private List<NamedValueVo> departmentDistribution;
    private List<PerformanceVo> recentPerformances;
    private List<RedPacketVo> recentRedPackets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatCardVo {
        private String key;
        private String label;
        private String value;
        private String unit;
    }
}
