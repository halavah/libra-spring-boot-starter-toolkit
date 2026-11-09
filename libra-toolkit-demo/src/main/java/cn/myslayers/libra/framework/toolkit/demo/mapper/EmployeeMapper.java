package cn.myslayers.libra.framework.toolkit.demo.mapper;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.EmployeeQueryDTO;
import cn.myslayers.libra.framework.toolkit.demo.model.dto.EmployeeCreateDTO;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Employee;
import cn.myslayers.libra.framework.toolkit.demo.model.vo.EmployeeListVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 员工Mapper接口
 * 对应 EmployeeMapper.xml 中的所有方法
 * 与 DatabaseUtil41 配合使用，不依赖 MyBatis 框架
 */
public interface EmployeeMapper {

    /**
     * 根据ID查询员工
     * @param id 员工ID
     * @return 员工实体
     */
    Employee selectEmployeeById(Long id);

    /**
     * 复杂条件查询员工列表
     * @param queryDTO 查询条件DTO
     * @return 员工列表VO
     */
    List<EmployeeListVO> selectEmployeesByCondition(EmployeeQueryDTO queryDTO);

    /**
     * 简单列表查询
     * @return 员工列表VO
     */
    List<EmployeeListVO> selectEmployeeList();

    /**
     * 根据部门ID列表查询员工
     * @param departmentIds 部门ID列表
     * @return 员工实体列表
     */
    List<Employee> selectEmployeesByDepartments(List<Integer> departmentIds);

    /**
     * 插入员工
     * @param employeeCreateDTO 员工创建DTO
     * @return 影响行数
     */
    int insertEmployee(EmployeeCreateDTO employeeCreateDTO);

    /**
     * 批量插入员工
     * @param employees 员工列表
     * @return 影响行数
     */
    int batchInsertEmployees(List<EmployeeCreateDTO> employees);

    /**
     * 更新员工信息
     * @param employee 员工实体
     * @return 影响行数
     */
    int updateEmployee(Employee employee);

    /**
     * 批量更新员工状态
     * @param status 状态
     * @param ids ID列表
     * @return 影响行数
     */
    int batchUpdateEmployeeStatus(@Param("status") String status, @Param("ids") List<Long> ids);

    /**
     * 删除员工
     * @param id 员工ID
     * @return 影响行数
     */
    int deleteEmployee(Long id);

    /**
     * 统计各部门员工数量
     * @return 统计结果Map
     */
    List<Map<String, Object>> countEmployeesByDepartment();

    /**
     * 根据技能查询员工
     * @param skills 技能列表
     * @return 员工实体列表
     */
    List<Employee> selectEmployeesBySkills(List<String> skills);
}