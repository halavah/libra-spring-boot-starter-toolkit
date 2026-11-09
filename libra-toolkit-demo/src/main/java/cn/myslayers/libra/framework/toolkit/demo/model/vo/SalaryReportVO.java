package cn.myslayers.libra.framework.toolkit.demo.model.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 薪资报表VO
 * 用于测试复杂统计报表、多维度薪资分析等场景
 */
public class SalaryReportVO {

    private String period;              // 报表期间
    private Date generateTime;          // 生成时间
    private String reportType;          // 报表类型

    // 总体统计
    private SalaryOverview overview;

    // 部门薪资详情
    private List<DepartmentSalaryDetail> departmentDetails;

    // 职级薪资分析
    private List<LevelSalaryAnalysis> levelAnalysis;

    // 薪资趋势
    private List<SalaryTrendData> trends;

    // 异常数据
    private List<SalaryAnomalyData> anomalies;

    // 内部类：薪资总览统计
    public static class SalaryOverview {
        private Integer totalEmployees;     // 总员工数
        private BigDecimal totalSalary;     // 总薪资
        private BigDecimal averageSalary;   // 平均薪资
        private BigDecimal medianSalary;    // 中位数薪资
        private BigDecimal maxSalary;       // 最高薪资
        private BigDecimal minSalary;       // 最低薪资
        private BigDecimal totalBonus;      // 总奖金
        private BigDecimal totalAllowance;  // 总补贴
        private BigDecimal totalDeduction;  // 总扣款

        public SalaryOverview() {
        }

        // Getter 和 Setter
        public Integer getTotalEmployees() {
            return totalEmployees;
        }

        public void setTotalEmployees(Integer totalEmployees) {
            this.totalEmployees = totalEmployees;
        }

        public BigDecimal getTotalSalary() {
            return totalSalary;
        }

        public void setTotalSalary(BigDecimal totalSalary) {
            this.totalSalary = totalSalary;
        }

        public BigDecimal getAverageSalary() {
            return averageSalary;
        }

        public void setAverageSalary(BigDecimal averageSalary) {
            this.averageSalary = averageSalary;
        }

        public BigDecimal getMedianSalary() {
            return medianSalary;
        }

        public void setMedianSalary(BigDecimal medianSalary) {
            this.medianSalary = medianSalary;
        }

        public BigDecimal getMaxSalary() {
            return maxSalary;
        }

        public void setMaxSalary(BigDecimal maxSalary) {
            this.maxSalary = maxSalary;
        }

        public BigDecimal getMinSalary() {
            return minSalary;
        }

        public void setMinSalary(BigDecimal minSalary) {
            this.minSalary = minSalary;
        }

        public BigDecimal getTotalBonus() {
            return totalBonus;
        }

        public void setTotalBonus(BigDecimal totalBonus) {
            this.totalBonus = totalBonus;
        }

        public BigDecimal getTotalAllowance() {
            return totalAllowance;
        }

        public void setTotalAllowance(BigDecimal totalAllowance) {
            this.totalAllowance = totalAllowance;
        }

        public BigDecimal getTotalDeduction() {
            return totalDeduction;
        }

        public void setTotalDeduction(BigDecimal totalDeduction) {
            this.totalDeduction = totalDeduction;
        }

        @Override
        public String toString() {
            return "SalaryOverview{" +
                    "totalEmployees=" + totalEmployees +
                    ", totalSalary=" + totalSalary +
                    ", averageSalary=" + averageSalary +
                    ", medianSalary=" + medianSalary +
                    ", maxSalary=" + maxSalary +
                    ", minSalary=" + minSalary +
                    ", totalBonus=" + totalBonus +
                    ", totalAllowance=" + totalAllowance +
                    ", totalDeduction=" + totalDeduction +
                    '}';
        }
    }

    // 内部类：部门薪资详情
    public static class DepartmentSalaryDetail {
        private Integer departmentId;       // 部门ID
        private String departmentName;      // 部门名称
        private Integer employeeCount;      // 员工数量
        private BigDecimal totalSalary;     // 部门总薪资
        private BigDecimal averageSalary;   // 部门平均薪资
        private BigDecimal budgetAmount;    // 预算金额
        private BigDecimal budgetUsage;     // 预算使用率
        private List<PositionSalaryInfo> positionBreakdown; // 职位薪资分解

        public DepartmentSalaryDetail() {
        }

        // Getter 和 Setter
        public Integer getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Integer departmentId) {
            this.departmentId = departmentId;
        }

        public String getDepartmentName() {
            return departmentName;
        }

        public void setDepartmentName(String departmentName) {
            this.departmentName = departmentName;
        }

        public Integer getEmployeeCount() {
            return employeeCount;
        }

        public void setEmployeeCount(Integer employeeCount) {
            this.employeeCount = employeeCount;
        }

        public BigDecimal getTotalSalary() {
            return totalSalary;
        }

        public void setTotalSalary(BigDecimal totalSalary) {
            this.totalSalary = totalSalary;
        }

        public BigDecimal getAverageSalary() {
            return averageSalary;
        }

        public void setAverageSalary(BigDecimal averageSalary) {
            this.averageSalary = averageSalary;
        }

        public BigDecimal getBudgetAmount() {
            return budgetAmount;
        }

        public void setBudgetAmount(BigDecimal budgetAmount) {
            this.budgetAmount = budgetAmount;
        }

        public BigDecimal getBudgetUsage() {
            return budgetUsage;
        }

        public void setBudgetUsage(BigDecimal budgetUsage) {
            this.budgetUsage = budgetUsage;
        }

        public List<PositionSalaryInfo> getPositionBreakdown() {
            return positionBreakdown;
        }

        public void setPositionBreakdown(List<PositionSalaryInfo> positionBreakdown) {
            this.positionBreakdown = positionBreakdown;
        }

        @Override
        public String toString() {
            return "DepartmentSalaryDetail{" +
                    "departmentId=" + departmentId +
                    ", departmentName='" + departmentName + '\'' +
                    ", employeeCount=" + employeeCount +
                    ", totalSalary=" + totalSalary +
                    ", averageSalary=" + averageSalary +
                    ", budgetAmount=" + budgetAmount +
                    ", budgetUsage=" + budgetUsage +
                    ", positionBreakdown=" + positionBreakdown +
                    '}';
        }
    }

    // 内部类：职级薪资分析
    public static class LevelSalaryAnalysis {
        private String level;               // 职级
        private Integer employeeCount;      // 该职级员工数
        private BigDecimal averageSalary;   // 平均薪资
        private BigDecimal minSalary;       // 最低薪资
        private BigDecimal maxSalary;       // 最高薪资
        private BigDecimal salaryRange;     // 薪资区间
        private Double growthRate;          // 增长率

        public LevelSalaryAnalysis() {
        }

        // Getter 和 Setter
        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public Integer getEmployeeCount() {
            return employeeCount;
        }

        public void setEmployeeCount(Integer employeeCount) {
            this.employeeCount = employeeCount;
        }

        public BigDecimal getAverageSalary() {
            return averageSalary;
        }

        public void setAverageSalary(BigDecimal averageSalary) {
            this.averageSalary = averageSalary;
        }

        public BigDecimal getMinSalary() {
            return minSalary;
        }

        public void setMinSalary(BigDecimal minSalary) {
            this.minSalary = minSalary;
        }

        public BigDecimal getMaxSalary() {
            return maxSalary;
        }

        public void setMaxSalary(BigDecimal maxSalary) {
            this.maxSalary = maxSalary;
        }

        public BigDecimal getSalaryRange() {
            return salaryRange;
        }

        public void setSalaryRange(BigDecimal salaryRange) {
            this.salaryRange = salaryRange;
        }

        public Double getGrowthRate() {
            return growthRate;
        }

        public void setGrowthRate(Double growthRate) {
            this.growthRate = growthRate;
        }

        @Override
        public String toString() {
            return "LevelSalaryAnalysis{" +
                    "level='" + level + '\'' +
                    ", employeeCount=" + employeeCount +
                    ", averageSalary=" + averageSalary +
                    ", minSalary=" + minSalary +
                    ", maxSalary=" + maxSalary +
                    ", salaryRange=" + salaryRange +
                    ", growthRate=" + growthRate +
                    '}';
        }
    }

    // 内部类：薪资趋势数据
    public static class SalaryTrendData {
        private String period;              // 期间
        private BigDecimal totalAmount;     // 总金额
        private BigDecimal averageAmount;   // 平均金额
        private Integer employeeCount;      // 员工数
        private Double changeRate;          // 变化率

        public SalaryTrendData() {
        }

        // Getter 和 Setter
        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
        }

        public BigDecimal getAverageAmount() {
            return averageAmount;
        }

        public void setAverageAmount(BigDecimal averageAmount) {
            this.averageAmount = averageAmount;
        }

        public Integer getEmployeeCount() {
            return employeeCount;
        }

        public void setEmployeeCount(Integer employeeCount) {
            this.employeeCount = employeeCount;
        }

        public Double getChangeRate() {
            return changeRate;
        }

        public void setChangeRate(Double changeRate) {
            this.changeRate = changeRate;
        }

        @Override
        public String toString() {
            return "SalaryTrendData{" +
                    "period='" + period + '\'' +
                    ", totalAmount=" + totalAmount +
                    ", averageAmount=" + averageAmount +
                    ", employeeCount=" + employeeCount +
                    ", changeRate=" + changeRate +
                    '}';
        }
    }

    // 内部类：薪资异常数据
    public static class SalaryAnomalyData {
        private Long employeeId;            // 员工ID
        private String employeeName;        // 员工姓名
        private String anomalyType;         // 异常类型
        private String description;         // 异常描述
        private BigDecimal expectedAmount;  // 预期金额
        private BigDecimal actualAmount;    // 实际金额
        private BigDecimal variance;        // 差异

        public SalaryAnomalyData() {
        }

        // Getter 和 Setter
        public Long getEmployeeId() {
            return employeeId;
        }

        public void setEmployeeId(Long employeeId) {
            this.employeeId = employeeId;
        }

        public String getEmployeeName() {
            return employeeName;
        }

        public void setEmployeeName(String employeeName) {
            this.employeeName = employeeName;
        }

        public String getAnomalyType() {
            return anomalyType;
        }

        public void setAnomalyType(String anomalyType) {
            this.anomalyType = anomalyType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getExpectedAmount() {
            return expectedAmount;
        }

        public void setExpectedAmount(BigDecimal expectedAmount) {
            this.expectedAmount = expectedAmount;
        }

        public BigDecimal getActualAmount() {
            return actualAmount;
        }

        public void setActualAmount(BigDecimal actualAmount) {
            this.actualAmount = actualAmount;
        }

        public BigDecimal getVariance() {
            return variance;
        }

        public void setVariance(BigDecimal variance) {
            this.variance = variance;
        }

        @Override
        public String toString() {
            return "SalaryAnomalyData{" +
                    "employeeId=" + employeeId +
                    ", employeeName='" + employeeName + '\'' +
                    ", anomalyType='" + anomalyType + '\'' +
                    ", description='" + description + '\'' +
                    ", expectedAmount=" + expectedAmount +
                    ", actualAmount=" + actualAmount +
                    ", variance=" + variance +
                    '}';
        }
    }

    // 内部类：职位薪资信息
    public static class PositionSalaryInfo {
        private String position;            // 职位
        private Integer count;              // 人数
        private BigDecimal averageSalary;   // 平均薪资

        public PositionSalaryInfo() {
        }

        // Getter 和 Setter
        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public BigDecimal getAverageSalary() {
            return averageSalary;
        }

        public void setAverageSalary(BigDecimal averageSalary) {
            this.averageSalary = averageSalary;
        }

        @Override
        public String toString() {
            return "PositionSalaryInfo{" +
                    "position='" + position + '\'' +
                    ", count=" + count +
                    ", averageSalary=" + averageSalary +
                    '}';
        }
    }

    // 默认构造函数
    public SalaryReportVO() {
    }

    // 主要字段的Getter和Setter方法
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Date getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(Date generateTime) {
        this.generateTime = generateTime;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public SalaryOverview getOverview() {
        return overview;
    }

    public void setOverview(SalaryOverview overview) {
        this.overview = overview;
    }

    public List<DepartmentSalaryDetail> getDepartmentDetails() {
        return departmentDetails;
    }

    public void setDepartmentDetails(List<DepartmentSalaryDetail> departmentDetails) {
        this.departmentDetails = departmentDetails;
    }

    public List<LevelSalaryAnalysis> getLevelAnalysis() {
        return levelAnalysis;
    }

    public void setLevelAnalysis(List<LevelSalaryAnalysis> levelAnalysis) {
        this.levelAnalysis = levelAnalysis;
    }

    public List<SalaryTrendData> getTrends() {
        return trends;
    }

    public void setTrends(List<SalaryTrendData> trends) {
        this.trends = trends;
    }

    public List<SalaryAnomalyData> getAnomalies() {
        return anomalies;
    }

    public void setAnomalies(List<SalaryAnomalyData> anomalies) {
        this.anomalies = anomalies;
    }

    @Override
    public String toString() {
        return "SalaryReportVO{" +
                "period='" + period + '\'' +
                ", generateTime=" + generateTime +
                ", reportType='" + reportType + '\'' +
                ", overview=" + overview +
                ", departmentDetails=" + departmentDetails +
                ", levelAnalysis=" + levelAnalysis +
                ", trends=" + trends +
                ", anomalies=" + anomalies +
                '}';
    }
}