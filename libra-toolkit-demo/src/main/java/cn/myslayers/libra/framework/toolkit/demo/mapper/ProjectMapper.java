package cn.myslayers.libra.framework.toolkit.demo.mapper;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.ProjectAssignDTO;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Project;
import cn.myslayers.libra.framework.toolkit.demo.model.vo.ProjectProgressVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 项目Mapper接口
 * 对应 ProjectMapper.xml 中的所有方法
 * 与 DatabaseUtil41 配合使用，不依赖 MyBatis 框架
 */
public interface ProjectMapper {

    /**
     * 根据ID查询项目基础信息
     * @param projectId 项目ID
     * @return 项目实体
     */
    Project selectProjectById(Long projectId);

    /**
     * 项目列表查询（支持条件过滤）
     * @param projectAssignDTO 项目分配DTO
     * @return 项目进度VO列表
     */
    List<ProjectProgressVO> selectProjectList(ProjectAssignDTO projectAssignDTO);

    /**
     * 创建项目
     * @param project 项目实体
     * @return 影响行数
     */
    int insertProject(Project project);

    /**
     * 更新项目信息
     * @param project 项目实体
     * @return 影响行数
     */
    int updateProject(Project project);

    /**
     * 删除项目
     * @param projectId 项目ID
     * @return 影响行数
     */
    int deleteProject(Long projectId);

    /**
     * 批量删除项目
     * @param projectIds 项目ID列表
     * @return 影响行数
     */
    int deleteProjectsByIds(List<Long> projectIds);

    /**
     * 根据部门查询项目
     * @param departmentId 部门ID
     * @return 项目Map列表
     */
    List<Map<String, Object>> selectProjectsByDepartment(Integer departmentId);

    /**
     * 批量更新项目状态
     * @param status 状态
     * @param projectIds 项目ID列表
     * @return 影响行数
     */
    int batchUpdateProjectStatus(@Param("status") String status, @Param("projectIds") List<Long> projectIds);

    /**
     * 查询项目成员
     * @param projectId 项目ID
     * @return 项目成员Map列表
     */
    List<Map<String, Object>> selectProjectMembers(Long projectId);

    /**
     * 添加项目成员
     * @param projectId 项目ID
     * @param employeeId 员工ID
     * @param role 角色
     * @return 影响行数
     */
    int addProjectMember(@Param("projectId") Long projectId, @Param("employeeId") Long employeeId, @Param("role") String role);

    /**
     * 批量添加项目成员
     * @param members 成员列表
     * @return 影响行数
     */
    int batchAddProjectMembers(@Param("members") List<Map<String, Object>> members);

    /**
     * 移除项目成员
     * @param projectId 项目ID
     * @param employeeId 员工ID
     * @return 影响行数
     */
    int removeProjectMember(@Param("projectId") Long projectId, @Param("employeeId") Long employeeId);

    /**
     * 获取项目统计信息
     * @return 项目统计Map列表
     */
    List<Map<String, Object>> getProjectStatistics();

    /**
     * 项目预算分析
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 预算分析Map列表
     */
    List<Map<String, Object>> selectProjectBudgetAnalysis(@Param("startDate") String startDate, @Param("endDate") String endDate);

    /**
     * 项目时间线查询
     * @param projectId 项目ID
     * @return 时间线Map列表
     */
    List<Map<String, Object>> selectProjectTimeline(Long projectId);

    /**
     * 项目里程碑查询
     * @param projectId 项目ID
     * @return 里程碑Map列表
     */
    List<Map<String, Object>> selectProjectMilestones(Long projectId);

    /**
     * 项目详细报告
     * @param projectId 项目ID
     * @return 详细报告Map
     */
    Map<String, Object> selectProjectDetailReport(Long projectId);
}