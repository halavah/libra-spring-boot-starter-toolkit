package cn.myslayers.libra.framework.toolkit.demo.model.dto;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.DateRange;
import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.PageQuery;
import java.util.List;
import java.util.Map;

/**
 * 报表查询DTO
 * 用于测试复杂报表查询、统计分析等场景
 */
public class ReportQueryDTO {

    private String reportType;          // 报表类型：EMPLOYEE, SALARY, PROJECT, ORDER
    private DateRange dateRange;        // 查询日期范围（嵌套对象）
    private PageQuery pageQuery;        // 分页查询（嵌套对象）
    private List<Integer> departmentIds; // 部门ID列表
    private Integer departmentId;        // 单个部门ID（用于查询条件）
    private String departmentStatus;     // 部门状态（用于查询条件）
    private String payDateStart;        // 薪资支付开始日期
    private String payDateEnd;          // 薪资支付结束日期
    private List<String> groupByFields; // 分组字段
    private List<String> orderByFields; // 排序字段
    private Map<String, Object> filterConditions; // 过滤条件映射
    private List<String> aggregateFields; // 聚合字段
    private String aggregateType;       // 聚合类型：SUM, COUNT, AVG, MAX, MIN
    private Boolean includeSubDepts;    // 是否包含子部门
    private String exportFormat;        // 导出格式：EXCEL, PDF, CSV
    private String customSql;           // 自定义SQL
    private Map<String, Object> parameters; // 自定义参数

    // 默认构造函数
    public ReportQueryDTO() {
    }

    // 便捷构造函数
    public ReportQueryDTO(String reportType, DateRange dateRange) {
        this.reportType = reportType;
        this.dateRange = dateRange;
    }

    // Getter 和 Setter 方法
    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public PageQuery getPageQuery() {
        return pageQuery;
    }

    public void setPageQuery(PageQuery pageQuery) {
        this.pageQuery = pageQuery;
    }

    public List<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentStatus() {
        return departmentStatus;
    }

    public void setDepartmentStatus(String departmentStatus) {
        this.departmentStatus = departmentStatus;
    }

    public String getPayDateStart() {
        return payDateStart;
    }

    public void setPayDateStart(String payDateStart) {
        this.payDateStart = payDateStart;
    }

    public String getPayDateEnd() {
        return payDateEnd;
    }

    public void setPayDateEnd(String payDateEnd) {
        this.payDateEnd = payDateEnd;
    }

    public List<String> getGroupByFields() {
        return groupByFields;
    }

    public void setGroupByFields(List<String> groupByFields) {
        this.groupByFields = groupByFields;
    }

    public List<String> getOrderByFields() {
        return orderByFields;
    }

    public void setOrderByFields(List<String> orderByFields) {
        this.orderByFields = orderByFields;
    }

    public Map<String, Object> getFilterConditions() {
        return filterConditions;
    }

    public void setFilterConditions(Map<String, Object> filterConditions) {
        this.filterConditions = filterConditions;
    }

    public List<String> getAggregateFields() {
        return aggregateFields;
    }

    public void setAggregateFields(List<String> aggregateFields) {
        this.aggregateFields = aggregateFields;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public Boolean getIncludeSubDepts() {
        return includeSubDepts;
    }

    public void setIncludeSubDepts(Boolean includeSubDepts) {
        this.includeSubDepts = includeSubDepts;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getCustomSql() {
        return customSql;
    }

    public void setCustomSql(String customSql) {
        this.customSql = customSql;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "ReportQueryDTO{" +
                "reportType='" + reportType + '\'' +
                ", dateRange=" + dateRange +
                ", pageQuery=" + pageQuery +
                ", departmentIds=" + departmentIds +
                ", groupByFields=" + groupByFields +
                ", orderByFields=" + orderByFields +
                ", filterConditions=" + filterConditions +
                ", aggregateFields=" + aggregateFields +
                ", aggregateType='" + aggregateType + '\'' +
                ", includeSubDepts=" + includeSubDepts +
                ", exportFormat='" + exportFormat + '\'' +
                ", customSql='" + customSql + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}