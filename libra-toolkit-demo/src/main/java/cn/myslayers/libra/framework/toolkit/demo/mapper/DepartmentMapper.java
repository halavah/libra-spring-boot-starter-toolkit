package cn.myslayers.libra.framework.toolkit.demo.mapper;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.DepartmentManageDTO;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Department;
import cn.myslayers.libra.framework.toolkit.demo.model.vo.DepartmentSummaryVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 部门Mapper接口
 * 对应 DepartmentMapper.xml 中的所有方法
 * 与 DatabaseUtil41 配合使用，不依赖 MyBatis 框架
 */
public interface DepartmentMapper {

    /**
     * 根据ID查询部门
     * @param id 部门ID
     * @return 部门实体
     */
    Department selectDepartmentById(Integer id);

    /**
     * 查询所有子部门（递归）
     * @param parentId 父部门ID
     * @return 子部门列表
     */
    List<Department> selectChildDepartments(Integer parentId);

    /**
     * 复杂条件查询部门
     * @param departmentManageDTO 查询条件DTO
     * @return 部门汇总VO列表
     */
    List<DepartmentSummaryVO> selectDepartmentsByCondition(DepartmentManageDTO departmentManageDTO);

    /**
     * 批量插入部门
     * @param departments 部门列表
     * @return 影响行数
     */
    int batchInsertDepartments(List<Department> departments);

    /**
     * 批量更新部门状态
     * @param status 状态
     * @param ids 部门ID列表
     * @return 影响行数
     */
    int batchUpdateDepartmentStatus(@Param("status") String status, @Param("ids") List<Integer> ids);

    /**
     * 移动部门（包含子部门层级调整）
     * @param departmentId 部门ID
     * @param newParentId 新父部门ID
     * @param newLevel 新层级
     * @return 影响行数
     */
    int moveDepartmentWithChildren(@Param("departmentId") Integer departmentId,
                                   @Param("newParentId") Integer newParentId,
                                   @Param("newLevel") Integer newLevel);

    /**
     * 查询部门统计信息
     * @return 部门统计VO列表
     */
    List<DepartmentSummaryVO.DepartmentStatistics> selectDepartmentStatistics();

    /**
     * 根据扩展信息查询部门
     * @param conditions 条件Map
     * @return 部门实体列表
     */
    List<Department> selectDepartmentsByExtraInfo(Map<String, Object> conditions);

    /**
     * 插入部门
     * @param department 部门实体
     * @return 影响行数
     */
    int insertDepartment(Department department);

    /**
     * 更新部门
     * @param department 部门实体
     * @return 影响行数
     */
    int updateDepartment(Department department);

    /**
     * 删除部门（软删除）
     * @param id 部门ID
     * @return 影响行数
     */
    int deleteDepartment(Integer id);
}