package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.common.BusinessException;
import org.example.dto.RedPacketRequest;
import org.example.entity.Employee;
import org.example.entity.RedPacket;
import org.example.mapper.EmployeeMapper;
import org.example.mapper.RedPacketMapper;
import org.example.service.BonusService;
import org.example.vo.RedPacketVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 私发红包服务实现。
 */
@Service
@RequiredArgsConstructor
public class BonusServiceImpl implements BonusService {

    private final RedPacketMapper redPacketMapper;
    private final EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public RedPacketVo send(Long managerId, RedPacketRequest request) {
        Employee target = employeeMapper.findById(request.getEmployeeId());
        if (target == null) {
            throw BusinessException.notFound("员工不存在");
        }
        if (!"ACTIVE".equals(target.getStatus())) {
            throw BusinessException.badRequest("离职员工不能发放红包");
        }
        RedPacket packet = new RedPacket();
        packet.setEmployeeId(target.getId());
        packet.setAmount(request.getAmount());
        packet.setMessage(request.getMessage());
        packet.setSenderId(managerId);
        packet.setReadFlag(false);
        redPacketMapper.insert(packet);
        return redPacketMapper.findVoById(packet.getId());
    }

    @Override
    public List<RedPacketVo> listMine(Long employeeId) {
        return redPacketMapper.selectByEmployee(employeeId);
    }

    @Override
    public List<RedPacketVo> listSent(Long managerId, Long employeeId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return redPacketMapper.selectByEmployeeForManager(employeeId, safeLimit);
    }

    @Override
    public void markRead(Long employeeId, Long id) {
        if (redPacketMapper.markRead(id, employeeId) == 0) {
            throw BusinessException.notFound("红包不存在");
        }
    }
}
