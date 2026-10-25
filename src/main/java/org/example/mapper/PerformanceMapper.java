package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.PerformanceRecord;
import org.example.vo.PerformanceVo;

import java.util.List;

/**
 * 业绩记录数据访问。
 */
@Mapper
public interface PerformanceMapper {

    PerformanceRecord findById(@Param("id") Long id);

    PerformanceRecord findByEmployeeAndPeriod(@Param("employeeId") Long employeeId,
                                              @Param("period") String period);

    List<PerformanceVo> selectByEmployee(@Param("employeeId") Long employeeId);

    List<PerformanceVo> selectPage(@Param("status") String status,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    long countPage(@Param("status") String status);

    long countPending();

    int insert(PerformanceRecord record);

    int updateSelf(@Param("id") Long id,
                   @Param("employeeId") Long employeeId,
                   @Param("score") java.math.BigDecimal score,
                   @Param("selfDesc") String selfDesc);

    int resubmit(@Param("id") Long id,
                 @Param("employeeId") Long employeeId,
                 @Param("score") java.math.BigDecimal score,
                 @Param("selfDesc") String selfDesc);

    int deletePending(@Param("id") Long id, @Param("employeeId") Long employeeId);

    int review(@Param("id") Long id,
               @Param("status") String status,
               @Param("comment") String comment,
               @Param("reviewerId") Long reviewerId);

    List<PerformanceRecord> selectTrend(@Param("employeeIds") List<Long> employeeIds,
                                        @Param("fromMonth") String fromMonth);
}
