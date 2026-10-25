package org.example.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.entity.Employee;
import org.example.entity.WorkExperience;
import org.example.vo.EmployeeVo;
import org.example.vo.NamedValueVo;

import java.util.List;

/**
 * 员工档案数据访问。
 */
@Mapper
public interface EmployeeMapper {

    Employee findById(@Param("id") Long id);

    Employee findByEmpNo(@Param("empNo") String empNo);

    EmployeeVo findVoById(@Param("id") Long id);

    List<EmployeeVo> selectPage(@Param("keyword") String keyword,
                                @Param("department") String department,
                                @Param("status") String status,
                                @Param("offset") int offset,
                                @Param("size") int size);

    long countPage(@Param("keyword") String keyword,
                   @Param("department") String department,
                   @Param("status") String status);

    int insert(Employee employee);

    int updateProfile(@Param("id") Long id,
                      @Param("phone") String phone,
                      @Param("email") String email,
                      @Param("address") String address,
                      @Param("emergencyContact") String emergencyContact,
                      @Param("emergencyPhone") String emergencyPhone);

    int updateGrade(@Param("id") Long id, @Param("gradeCode") String gradeCode);

    List<WorkExperience> selectExperiences(@Param("employeeId") Long employeeId);

    List<EmployeeVo> findVosByIds(@Param("ids") List<Long> ids);

    List<String> selectDepartments();

    long countByStatus(@Param("status") String status);

    long countHiredBetween(@Param("start") String start, @Param("end") String end);

    List<NamedValueVo> countByDepartment();
}
