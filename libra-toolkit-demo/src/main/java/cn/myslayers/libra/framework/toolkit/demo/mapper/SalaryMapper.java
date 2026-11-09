package cn.myslayers.libra.framework.toolkit.demo.mapper;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.SalaryCalculateDTO;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Salary;
import cn.myslayers.libra.framework.toolkit.demo.model.vo.SalaryReportVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 薪资Mapper接口
 * 对应 SalaryMapper.xml 中的所有方法
 * 与 DatabaseUtil41 配合使用，不依赖 MyBatis 框架
 */
public interface SalaryMapper {

    /**
     * 根据薪资ID查询详细信息
     * @param salaryId 薪资ID
     * @return 薪资实体
     */
    Salary selectSalaryById(Long salaryId);

    /**
     * 查询员工薪资历史记录
     * @param salaryCalculateDTO 薪资计算DTO
     * @return 薪资报表VO列表
     */
    List<SalaryReportVO> selectSalaryHistory(SalaryCalculateDTO salaryCalculateDTO);

    /**
     * 薪资列表查询（支持复杂过滤和聚合）
     * @param salaryCalculateDTO 薪资计算DTO
     * @return 薪资Map列表
     */
    List<Map<String, Object>> selectSalaryList(SalaryCalculateDTO salaryCalculateDTO);

    /**
     * 插入薪资记录
     * @param salary 薪资实体
     * @return 影响行数
     */
    int insertSalary(Salary salary);

    /**
     * 批量插入薪资记录
     * @param salaries 薪资列表
     * @return 影响行数
     */
    int batchInsertSalary(List<Salary> salaries);

    /**
     * 更新薪资记录
     * @param salary 薪资实体
     * @return 影响行数
     */
    int updateSalary(Salary salary);

    /**
     * 删除薪资记录
     * @param salaryId 薪资ID
     * @return 影响行数
     */
    int deleteSalary(Long salaryId);

    /**
     * 批量删除薪资记录
     * @param salaryIds 薪资ID列表
     * @return 影响行数
     */
    int deleteSalariesByIds(List<Long> salaryIds);

    /**
     * 薪资统计分析
     * @param period 统计期间
     * @return 统计结果Map列表
     */
    List<Map<String, Object>> getSalaryStatistics(String period);

    /**
     * 部门薪资对比分析
     * @param year 年份
     * @return 对比分析Map列表
     */
    List<Map<String, Object>> getDepartmentSalaryComparison(Integer year);

    /**
     * 薪资趋势分析（按月）
     * @param startMonth 开始月份
     * @param endMonth 结束月份
     * @return 趋势分析Map列表
     */
    List<Map<String, Object>> getSalaryTrendByMonth(@Param("startMonth") String startMonth, @Param("endMonth") String endMonth);

    /**
     * 员工薪资排名
     * @param departmentId 部门ID（可选）
     * @param limit 限制条数
     * @return 排名Map列表
     */
    List<Map<String, Object>> getEmployeeSalaryRanking(@Param("departmentId") Integer departmentId, @Param("limit") Integer limit);

    /**
     * 薪资异常检测
     * @param threshold 阈值
     * @return 异常数据Map列表
     */
    List<Map<String, Object>> detectSalaryAnomalies(@Param("threshold") Double threshold);

    /**
     * 薪资成本预算分析
     * @param year 年份
     * @param quarter 季度
     * @return 预算分析Map列表
     */
    List<Map<String, Object>> getSalaryCostBudgetAnalysis(@Param("year") Integer year, @Param("quarter") Integer quarter);
}