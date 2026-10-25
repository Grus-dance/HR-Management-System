package org.example.service;

import org.example.dto.RedPacketRequest;
import org.example.vo.RedPacketVo;

import java.util.List;

/**
 * 私发红包服务。
 */
public interface BonusService {

    RedPacketVo send(Long managerId, RedPacketRequest request);

    List<RedPacketVo> listMine(Long employeeId);

    List<RedPacketVo> listSent(Long managerId, Long employeeId, int limit);

    void markRead(Long employeeId, Long id);
}
