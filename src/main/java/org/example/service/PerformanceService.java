package org.example.service;

import org.example.common.PageResult;
import org.example.dto.PerformanceRequest;
import org.example.vo.PerformanceVo;

import java.util.List;

/**
 * 业绩服务。
 */
public interface PerformanceService {

    List<PerformanceVo> listMine(Long employeeId);

    PerformanceVo submit(Long employeeId, PerformanceRequest request);

    PerformanceVo update(Long employeeId, Long id, PerformanceRequest request);

    void delete(Long employeeId, Long id);

    PageResult<PerformanceVo> page(String status, int page, int size);

    void review(Long reviewerId, Long id, boolean approve, String comment);
}
