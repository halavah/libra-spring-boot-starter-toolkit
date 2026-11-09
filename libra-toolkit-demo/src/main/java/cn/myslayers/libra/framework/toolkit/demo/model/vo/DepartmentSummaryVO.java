package cn.myslayers.libra.framework.toolkit.demo.model.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 部门汇总VO
 * 用于测试层级结构显示、统计数据等场景
 */
public class DepartmentSummaryVO {

    private Integer id;                 // 部门ID
    private String name;                // 部门名称
    private String code;                // 部门编码
    private String location;            // 办公地点
    private Integer level;              // 部门层级
    private String status;              // 状态
    private Date establishDate;         // 成立日期
    private String managerName;         // 经理姓名
    private Integer employeeCount;      // 员工数量
    private Integer projectCount;       // 项目数量
    private List<SubDepartment> children; // 子部门列表

    // 统计信息
    private DepartmentStatistics statistics;

    // 绩效信息
    private DepartmentPerformance performance;

    // 内部类：子部门信息
    public static class SubDepartment {
        private Integer id;
        private String name;
        private String code;
        private Integer employeeCount;

        public SubDepartment() {
        }

        public SubDepartment(Integer id, String name, String code) {
            this.id = id;
            this.name = name;
            this.code = code;
        }

        // Getter 和 Setter
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Integer getEmployeeCount() {
            return employeeCount;
        }

        public void setEmployeeCount(Integer employeeCount) {
            this.employeeCount = employeeCount;
        }

        @Override
        public String toString() {
            return "SubDepartment{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", code='" + code + '\'' +
                    ", employeeCount=" + employeeCount +
                    '}';
        }
    }

    // 内部类：部门统计信息
    public static class DepartmentStatistics {
        private Integer totalEmployees;     // 总员工数
        private Integer activeEmployees;    // 在职员工数
        private Integer resignedEmployees;  // 离职员工数
        private BigDecimal totalSalary;     // 总薪资
        private BigDecimal averageSalary;   // 平均薪资
        private Integer projectCount;       // 项目数量
        private Map<String, Integer> positionDistribution; // 职位分布
        private Map<String, Integer> levelDistribution;    // 职级分布

        public DepartmentStatistics() {
        }

        // Getter 和 Setter
        public Integer getTotalEmployees() {
            return totalEmployees;
        }

        public void setTotalEmployees(Integer totalEmployees) {
            this.totalEmployees = totalEmployees;
        }

        public Integer getActiveEmployees() {
            return activeEmployees;
        }

        public void setActiveEmployees(Integer activeEmployees) {
            this.activeEmployees = activeEmployees;
        }

        public Integer getResignedEmployees() {
            return resignedEmployees;
        }

        public void setResignedEmployees(Integer resignedEmployees) {
            this.resignedEmployees = resignedEmployees;
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

        public Integer getProjectCount() {
            return projectCount;
        }

        public void setProjectCount(Integer projectCount) {
            this.projectCount = projectCount;
        }

        public Map<String, Integer> getPositionDistribution() {
            return positionDistribution;
        }

        public void setPositionDistribution(Map<String, Integer> positionDistribution) {
            this.positionDistribution = positionDistribution;
        }

        public Map<String, Integer> getLevelDistribution() {
            return levelDistribution;
        }

        public void setLevelDistribution(Map<String, Integer> levelDistribution) {
            this.levelDistribution = levelDistribution;
        }

        @Override
        public String toString() {
            return "DepartmentStatistics{" +
                    "totalEmployees=" + totalEmployees +
                    ", activeEmployees=" + activeEmployees +
                    ", resignedEmployees=" + resignedEmployees +
                    ", totalSalary=" + totalSalary +
                    ", averageSalary=" + averageSalary +
                    ", projectCount=" + projectCount +
                    ", positionDistribution=" + positionDistribution +
                    ", levelDistribution=" + levelDistribution +
                    '}';
        }
    }

    // 内部类：部门绩效信息
    public static class DepartmentPerformance {
        private BigDecimal averageScore;     // 平均绩效分数
        private String overallGrade;         // 整体绩效等级
        private Integer completedProjects;   // 已完成项目数
        private Integer ongoingProjects;     // 进行中项目数
        private BigDecimal budgetUtilization; // 预算利用率

        public DepartmentPerformance() {
        }

        // Getter 和 Setter
        public BigDecimal getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(BigDecimal averageScore) {
            this.averageScore = averageScore;
        }

        public String getOverallGrade() {
            return overallGrade;
        }

        public void setOverallGrade(String overallGrade) {
            this.overallGrade = overallGrade;
        }

        public Integer getCompletedProjects() {
            return completedProjects;
        }

        public void setCompletedProjects(Integer completedProjects) {
            this.completedProjects = completedProjects;
        }

        public Integer getOngoingProjects() {
            return ongoingProjects;
        }

        public void setOngoingProjects(Integer ongoingProjects) {
            this.ongoingProjects = ongoingProjects;
        }

        public BigDecimal getBudgetUtilization() {
            return budgetUtilization;
        }

        public void setBudgetUtilization(BigDecimal budgetUtilization) {
            this.budgetUtilization = budgetUtilization;
        }

        @Override
        public String toString() {
            return "DepartmentPerformance{" +
                    "averageScore=" + averageScore +
                    ", overallGrade='" + overallGrade + '\'' +
                    ", completedProjects=" + completedProjects +
                    ", ongoingProjects=" + ongoingProjects +
                    ", budgetUtilization=" + budgetUtilization +
                    '}';
        }
    }

    // 默认构造函数
    public DepartmentSummaryVO() {
    }

    // Getter 和 Setter 方法
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getEstablishDate() {
        return establishDate;
    }

    public void setEstablishDate(Date establishDate) {
        this.establishDate = establishDate;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Integer getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(Integer employeeCount) {
        this.employeeCount = employeeCount;
    }

    public Integer getProjectCount() {
        return projectCount;
    }

    public void setProjectCount(Integer projectCount) {
        this.projectCount = projectCount;
    }

    public List<SubDepartment> getChildren() {
        return children;
    }

    public void setChildren(List<SubDepartment> children) {
        this.children = children;
    }

    public DepartmentStatistics getStatistics() {
        return statistics;
    }

    public void setStatistics(DepartmentStatistics statistics) {
        this.statistics = statistics;
    }

    public DepartmentPerformance getPerformance() {
        return performance;
    }

    public void setPerformance(DepartmentPerformance performance) {
        this.performance = performance;
    }

    @Override
    public String toString() {
        return "DepartmentSummaryVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", location='" + location + '\'' +
                ", level=" + level +
                ", status='" + status + '\'' +
                ", establishDate=" + establishDate +
                ", managerName='" + managerName + '\'' +
                ", employeeCount=" + employeeCount +
                ", projectCount=" + projectCount +
                ", children=" + children +
                ", statistics=" + statistics +
                ", performance=" + performance +
                '}';
    }
}