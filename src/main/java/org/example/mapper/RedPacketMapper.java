package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.RedPacket;
import org.example.vo.RedPacketVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 私发红包数据访问。
 */
@Mapper
public interface RedPacketMapper {

    int insert(RedPacket packet);

    RedPacketVo findVoById(@Param("id") Long id);

    List<RedPacketVo> selectByEmployee(@Param("employeeId") Long employeeId);

    List<RedPacketVo> selectByEmployeeForManager(@Param("employeeId") Long employeeId,
                                                  @Param("limit") int limit);

    int markRead(@Param("id") Long id, @Param("employeeId") Long employeeId);

    BigDecimal sumByMonth(@Param("employeeId") Long employeeId, @Param("month") String month);

    long countUnread(@Param("employeeId") Long employeeId);
}
