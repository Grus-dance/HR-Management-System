package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.UserAccount;

/**
 * 登录账号数据访问。
 */
@Mapper
public interface AccountMapper {

    UserAccount findByEmployeeId(@Param("employeeId") Long employeeId);

    int insert(UserAccount account);

    int updatePassword(@Param("employeeId") Long employeeId,
                       @Param("passwordHash") String passwordHash,
                       @Param("mustChangePassword") boolean mustChangePassword);

    int updateLastLogin(@Param("employeeId") Long employeeId);
}
