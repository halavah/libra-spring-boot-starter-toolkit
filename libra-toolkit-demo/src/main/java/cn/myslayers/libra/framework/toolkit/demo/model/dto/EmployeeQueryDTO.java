package cn.myslayers.libra.framework.toolkit.demo.model.dto;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.DateRange;
import java.math.BigDecimal;
import java.util.List;

/**
 * 员工查询DTO
 * 用于测试复杂对象入参映射，包含嵌套对象和集合类型
 */
public class EmployeeQueryDTO {

    // 基础查询条件
    private String name;                // 姓名（模糊查询）
    private String email;               // 邮箱
    private String phone;               // 电话
    private String employeeNo;          // 员工编号

    // 集合类型查询条件
    private List<Integer> departmentIds; // 部门ID列表
    private List<String> positions;     // 职位列表
    private List<String> levels;        // 职级列表
    private List<String> skills;        // 技能标签
    private List<String> statuses;      // 状态列表

    // 范围查询条件
    private DateRange hireDateRange;    // 入职日期范围
    private SalaryRange salaryRange;    // 薪资范围
    private Integer minAge;             // 最小年龄
    private Integer maxAge;             // 最大年龄

    // 地址相关查询
    private String location;            // 工作地点
    private String province;            // 省份
    private String city;                // 城市

    // 嵌套对象 - 薪资范围
    public static class SalaryRange {
        private BigDecimal minSalary;   // 最低薪资
        private BigDecimal maxSalary;   // 最高薪资

        public SalaryRange() {
        }

        public SalaryRange(BigDecimal minSalary, BigDecimal maxSalary) {
            this.minSalary = minSalary;
            this.maxSalary = maxSalary;
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

        @Override
        public String toString() {
            return "SalaryRange{" +
                    "minSalary=" + minSalary +
                    ", maxSalary=" + maxSalary +
                    '}';
        }
    }

    // 默认构造函数
    public EmployeeQueryDTO() {
    }

    // Getter 和 Setter 方法
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

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public List<Integer> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(List<Integer> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public List<String> getPositions() {
        return positions;
    }

    public void setPositions(List<String> positions) {
        this.positions = positions;
    }

    public List<String> getLevels() {
        return levels;
    }

    public void setLevels(List<String> levels) {
        this.levels = levels;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }

    public DateRange getHireDateRange() {
        return hireDateRange;
    }

    public void setHireDateRange(DateRange hireDateRange) {
        this.hireDateRange = hireDateRange;
    }

    public SalaryRange getSalaryRange() {
        return salaryRange;
    }

    public void setSalaryRange(SalaryRange salaryRange) {
        this.salaryRange = salaryRange;
    }

    public Integer getMinAge() {
        return minAge;
    }

    public void setMinAge(Integer minAge) {
        this.minAge = minAge;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        return "EmployeeQueryDTO{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", employeeNo='" + employeeNo + '\'' +
                ", departmentIds=" + departmentIds +
                ", positions=" + positions +
                ", levels=" + levels +
                ", skills=" + skills +
                ", statuses=" + statuses +
                ", hireDateRange=" + hireDateRange +
                ", salaryRange=" + salaryRange +
                ", minAge=" + minAge +
                ", maxAge=" + maxAge +
                ", location='" + location + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}