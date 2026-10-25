package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.common.BusinessException;
import org.example.common.MonthUtil;
import org.example.common.PageResult;
import org.example.dto.PerformanceRequest;
import org.example.entity.Employee;
import org.example.entity.PerformanceRecord;
import org.example.mapper.EmployeeMapper;
import org.example.mapper.PerformanceMapper;
import org.example.service.PerformanceService;
import org.example.vo.PerformanceVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 业绩服务实现。
 */
@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceMapper performanceMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    public List<PerformanceVo> listMine(Long employeeId) {
        return performanceMapper.selectByEmployee(employeeId);
    }

    @Override
    @Transactional
    public PerformanceVo submit(Long employeeId, PerformanceRequest request) {
        Employee employee = employeeMapper.findById(employeeId);
        if (employee == null) {
            throw BusinessException.notFound("员工不存在");
        }
        if (!"ACTIVE".equals(employee.getStatus())) {
            throw BusinessException.badRequest("离职员工不能提交业绩");
        }
        String period = request.getPeriod();
        if (MonthUtil.isFuture(period)) {
            throw BusinessException.badRequest("不能提交未来月份的业绩");
        }
        if (period.compareTo(employee.getHireDate().toString().substring(0, 7)) < 0) {
            throw BusinessException.badRequest("业绩月份不能早于入职月份");
        }

        PerformanceRecord existing = performanceMapper.findByEmployeeAndPeriod(employeeId, period);
        if (existing != null) {
            if (!"REJECTED".equals(existing.getStatus())) {
                throw BusinessException.conflict("该月业绩已提交，请勿重复提交");
            }
            int rows = performanceMapper.resubmit(existing.getId(), employeeId,
                    request.getScore(), request.getSelfDesc());
            if (rows == 0) {
                throw BusinessException.conflict("业绩状态已变化，请刷新后重试");
            }
        } else {
            PerformanceRecord record = new PerformanceRecord();
            record.setEmployeeId(employeeId);
            record.setPeriod(period);
            record.setScore(request.getScore());
            record.setSelfDesc(request.getSelfDesc());
            record.setStatus("PENDING");
            performanceMapper.insert(record);
        }
        return findByPeriod(employeeId, period);
    }

    @Override
    @Transactional
    public PerformanceVo update(Long employeeId, Long id, PerformanceRequest request) {
        PerformanceRecord record = requireOwnRecord(employeeId, id);
        if (!"PENDING".equals(record.getStatus())) {
            throw BusinessException.conflict("仅待审核的业绩可以修改");
        }
        if (!record.getPeriod().equals(request.getPeriod())) {
            throw BusinessException.badRequest("不能修改业绩月份，请删除后重新提交");
        }
        int rows = performanceMapper.updateSelf(id, employeeId, request.getScore(), request.getSelfDesc());
        if (rows == 0) {
            throw BusinessException.conflict("业绩状态已变化，请刷新后重试");
        }
        return findByPeriod(employeeId, record.getPeriod());
    }

    @Override
    @Transactional
    public void delete(Long employeeId, Long id) {
        requireOwnRecord(employeeId, id);
        int rows = performanceMapper.deletePending(id, employeeId);
        if (rows == 0) {
            throw BusinessException.conflict("仅待审核的业绩可以删除");
        }
    }

    @Override
    public PageResult<PerformanceVo> page(String status, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        int offset = (safePage - 1) * safeSize;
        List<PerformanceVo> list = performanceMapper.selectPage(status, offset, safeSize);
        long total = performanceMapper.countPage(status);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    @Override
    @Transactional
    public void review(Long reviewerId, Long id, boolean approve, String comment) {
        PerformanceRecord record = performanceMapper.findById(id);
        if (record == null) {
            throw BusinessException.notFound("业绩记录不存在");
        }
        if (!"PENDING".equals(record.getStatus())) {
            throw BusinessException.conflict("该业绩已审核，请刷新后重试");
        }
        if (!approve && (comment == null || comment.isBlank())) {
            throw BusinessException.badRequest("驳回时请填写审核意见");
        }
        int rows = performanceMapper.review(id, approve ? "APPROVED" : "REJECTED", comment, reviewerId);
        if (rows == 0) {
            throw BusinessException.conflict("该业绩已审核，请刷新后重试");
        }
    }

    private PerformanceRecord requireOwnRecord(Long employeeId, Long id) {
        PerformanceRecord record = performanceMapper.findById(id);
        if (record == null || !record.getEmployeeId().equals(employeeId)) {
            throw BusinessException.notFound("业绩记录不存在");
        }
        return record;
    }

    private PerformanceVo findByPeriod(Long employeeId, String period) {
        return performanceMapper.selectByEmployee(employeeId).stream()
                .filter(item -> period.equals(item.getPeriod()))
                .findFirst()
                .orElseThrow(() -> BusinessException.notFound("业绩记录不存在"));
    }
}
