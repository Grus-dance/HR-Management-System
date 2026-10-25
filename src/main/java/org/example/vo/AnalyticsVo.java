package org.example.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理分析数据：薪资比例 + 薪资曲线 + 业绩曲线。
 */
@Data
public class AnalyticsVo {

    private List<String> months;
    private List<NamedValueVo> salaryShare;
    private List<ChartSeriesVo> salarySeries;
    private List<ChartSeriesVo> performanceSeries;
}
