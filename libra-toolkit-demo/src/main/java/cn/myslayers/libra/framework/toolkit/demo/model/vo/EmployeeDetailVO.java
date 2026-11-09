package cn.myslayers.libra.framework.toolkit.demo.model.vo;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.Address;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 员工详情VO
 * 用于测试嵌套对象结果映射、关联数据展示等场景
 */
public class EmployeeDetailVO {

    private Long id;                    // 员工ID
    private String employeeNo;          // 员工编号
    private String name;                // 姓名
    private String email;               // 邮箱
    private String phone;               // 电话
    private String position;            // 职位
    private String level;               // 职级
    private BigDecimal baseSalary;      // 基础薪资
    private Date hireDate;              // 入职日期
    private String status;              // 状态
    private List<String> skills;        // 技能列表
    private Address address;            // 地址信息（嵌套对象）

    // 关联信息
    private DepartmentInfo department;  // 部门信息
    private ManagerInfo manager;        // 经理信息
    private List<ProjectInfo> projects; // 参与项目列表

    // 高级统计信息
    private PerformanceSummary performance; // 绩效汇总
    private SalarySummary salary;       // 薪资汇总

    // 内部类：部门信息
    public static class DepartmentInfo {
        private Integer id;
        private String name;
        private String code;
        private String location;

        public DepartmentInfo() {
        }

        public DepartmentInfo(Integer id, String name, String code) {
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

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return "DepartmentInfo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", code='" + code + '\'' +
                    ", location='" + location + '\'' +
                    '}';
        }
    }

    // 内部类：经理信息
    public static class ManagerInfo {
        private Long id;
        private String name;
        private String position;

        public ManagerInfo() {
        }

        public ManagerInfo(Long id, String name, String position) {
            this.id = id;
            this.name = name;
            this.position = position;
        }

        // Getter 和 Setter
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }

        @Override
        public String toString() {
            return "ManagerInfo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", position='" + position + '\'' +
                    '}';
        }
    }

    // 内部类：项目信息
    public static class ProjectInfo {
        private Long id;
        private String name;
        private String code;
        private String status;
        private String role;

        public ProjectInfo() {
        }

        public ProjectInfo(Long id, String name, String code, String status) {
            this.id = id;
            this.name = name;
            this.code = code;
            this.status = status;
        }

        // Getter 和 Setter
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        @Override
        public String toString() {
            return "ProjectInfo{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", code='" + code + '\'' +
                    ", status='" + status + '\'' +
                    ", role='" + role + '\'' +
                    '}';
        }
    }

    // 内部类：绩效汇总信息
    public static class PerformanceSummary {
        private BigDecimal latestScore;      // 最新绩效分数
        private String latestGrade;          // 最新绩效等级
        private BigDecimal averageScore;     // 平均绩效分数
        private Integer evaluationCount;     // 评估次数
        private Date lastEvaluationDate;     // 最后评估日期

        public PerformanceSummary() {
        }

        public PerformanceSummary(BigDecimal latestScore, String latestGrade, BigDecimal averageScore) {
            this.latestScore = latestScore;
            this.latestGrade = latestGrade;
            this.averageScore = averageScore;
        }

        // Getter 和 Setter
        public BigDecimal getLatestScore() {
            return latestScore;
        }

        public void setLatestScore(BigDecimal latestScore) {
            this.latestScore = latestScore;
        }

        public String getLatestGrade() {
            return latestGrade;
        }

        public void setLatestGrade(String latestGrade) {
            this.latestGrade = latestGrade;
        }

        public BigDecimal getAverageScore() {
            return averageScore;
        }

        public void setAverageScore(BigDecimal averageScore) {
            this.averageScore = averageScore;
        }

        public Integer getEvaluationCount() {
            return evaluationCount;
        }

        public void setEvaluationCount(Integer evaluationCount) {
            this.evaluationCount = evaluationCount;
        }

        public Date getLastEvaluationDate() {
            return lastEvaluationDate;
        }

        public void setLastEvaluationDate(Date lastEvaluationDate) {
            this.lastEvaluationDate = lastEvaluationDate;
        }

        @Override
        public String toString() {
            return "PerformanceSummary{" +
                    "latestScore=" + latestScore +
                    ", latestGrade='" + latestGrade + '\'' +
                    ", averageScore=" + averageScore +
                    ", evaluationCount=" + evaluationCount +
                    ", lastEvaluationDate=" + lastEvaluationDate +
                    '}';
        }
    }

    // 内部类：薪资汇总信息
    public static class SalarySummary {
        private BigDecimal currentSalary;        // 当前薪资
        private BigDecimal lastYearTotal;        // 去年总薪资
        private BigDecimal yearToDateTotal;      // 年初至今总薪资
        private Integer salaryAdjustmentCount;   // 薪资调整次数
        private Date lastAdjustmentDate;         // 最后调整日期

        public SalarySummary() {
        }

        public SalarySummary(BigDecimal currentSalary, BigDecimal lastYearTotal, BigDecimal yearToDateTotal) {
            this.currentSalary = currentSalary;
            this.lastYearTotal = lastYearTotal;
            this.yearToDateTotal = yearToDateTotal;
        }

        // Getter 和 Setter
        public BigDecimal getCurrentSalary() {
            return currentSalary;
        }

        public void setCurrentSalary(BigDecimal currentSalary) {
            this.currentSalary = currentSalary;
        }

        public BigDecimal getLastYearTotal() {
            return lastYearTotal;
        }

        public void setLastYearTotal(BigDecimal lastYearTotal) {
            this.lastYearTotal = lastYearTotal;
        }

        public BigDecimal getYearToDateTotal() {
            return yearToDateTotal;
        }

        public void setYearToDateTotal(BigDecimal yearToDateTotal) {
            this.yearToDateTotal = yearToDateTotal;
        }

        public Integer getSalaryAdjustmentCount() {
            return salaryAdjustmentCount;
        }

        public void setSalaryAdjustmentCount(Integer salaryAdjustmentCount) {
            this.salaryAdjustmentCount = salaryAdjustmentCount;
        }

        public Date getLastAdjustmentDate() {
            return lastAdjustmentDate;
        }

        public void setLastAdjustmentDate(Date lastAdjustmentDate) {
            this.lastAdjustmentDate = lastAdjustmentDate;
        }

        @Override
        public String toString() {
            return "SalarySummary{" +
                    "currentSalary=" + currentSalary +
                    ", lastYearTotal=" + lastYearTotal +
                    ", yearToDateTotal=" + yearToDateTotal +
                    ", salaryAdjustmentCount=" + salaryAdjustmentCount +
                    ", lastAdjustmentDate=" + lastAdjustmentDate +
                    '}';
        }
    }

    // 默认构造函数
    public EmployeeDetailVO() {
    }

    // 主要字段的Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public DepartmentInfo getDepartment() {
        return department;
    }

    public void setDepartment(DepartmentInfo department) {
        this.department = department;
    }

    public ManagerInfo getManager() {
        return manager;
    }

    public void setManager(ManagerInfo manager) {
        this.manager = manager;
    }

    public List<ProjectInfo> getProjects() {
        return projects;
    }

    public void setProjects(List<ProjectInfo> projects) {
        this.projects = projects;
    }

    public PerformanceSummary getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceSummary performance) {
        this.performance = performance;
    }

    public SalarySummary getSalary() {
        return salary;
    }

    public void setSalary(SalarySummary salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "EmployeeDetailVO{" +
                "id=" + id +
                ", employeeNo='" + employeeNo + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", position='" + position + '\'' +
                ", level='" + level + '\'' +
                ", baseSalary=" + baseSalary +
                ", hireDate=" + hireDate +
                ", status='" + status + '\'' +
                ", skills=" + skills +
                ", address=" + address +
                ", department=" + department +
                ", manager=" + manager +
                ", projects=" + projects +
                ", performance=" + performance +
                ", salary=" + salary +
                '}';
    }
}