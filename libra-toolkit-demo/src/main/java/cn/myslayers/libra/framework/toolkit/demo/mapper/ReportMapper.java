package cn.myslayers.libra.framework.toolkit.demo.mapper;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.ReportQueryDTO;
import cn.myslayers.libra.framework.toolkit.demo.model.vo.ReportDataVO;

import java.util.List;
import java.util.Map;

/**
 * 报表Mapper接口
 * 对应 ReportMapper.xml 中的所有方法
 * 与 DatabaseUtil41 配合使用，不依赖 MyBatis 框架
 */
public interface ReportMapper {

    /**
     * 业务概览报表
     * @param reportQueryDTO 报表查询DTO
     * @return 报表数据VO
     */
    ReportDataVO selectBusinessOverview(ReportQueryDTO reportQueryDTO);

    /**
     * 员工统计报表
     * @param reportQueryDTO 报表查询DTO
     * @return 员工统计Map列表
     */
    List<Map<String, Object>> selectEmployeeStatistics(ReportQueryDTO reportQueryDTO);

    /**
     * 财务报表
     * @param reportQueryDTO 报表查询DTO
     * @return 财务报表Map列表
     */
    List<Map<String, Object>> selectFinancialReport(ReportQueryDTO reportQueryDTO);

    /**
     * 项目绩效报表
     * @param reportQueryDTO 报表查询DTO
     * @return 项目绩效Map列表
     */
    List<Map<String, Object>> selectProjectPerformanceReport(ReportQueryDTO reportQueryDTO);

    /**
     * 销售分析报表
     * @param reportQueryDTO 报表查询DTO
     * @return 销售分析Map列表
     */
    List<Map<String, Object>> selectSalesAnalysisReport(ReportQueryDTO reportQueryDTO);

    /**
     * 员工绩效报表
     * @param reportQueryDTO 报表查询DTO
     * @return 员工绩效Map列表
     */
    List<Map<String, Object>> selectEmployeePerformanceReport(ReportQueryDTO reportQueryDTO);

    /**
     * 系统使用报表
     * @param reportQueryDTO 报表查询DTO
     * @return 系统使用Map列表
     */
    List<Map<String, Object>> selectSystemUsageReport(ReportQueryDTO reportQueryDTO);
}