package cn.myslayers.libra.framework.toolkit.demo.model.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 员工列表VO
 * 用于测试列表数据展示、简化字段映射等场景
 */
public class EmployeeListVO {

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
    private String departmentName;      // 部门名称
    private String managerName;         // 经理姓名

    // 默认构造函数
    public EmployeeListVO() {
    }

    // 便捷构造函数
    public EmployeeListVO(Long id, String employeeNo, String name, String departmentName) {
        this.id = id;
        this.employeeNo = employeeNo;
        this.name = name;
        this.departmentName = departmentName;
    }

    // Getter 和 Setter 方法
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

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    @Override
    public String toString() {
        return "EmployeeListVO{" +
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
                ", departmentName='" + departmentName + '\'' +
                ", managerName='" + managerName + '\'' +
                '}';
    }
}