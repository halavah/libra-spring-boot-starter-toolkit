package cn.myslayers.libra.framework.toolkit.demo.model.entity;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.Address;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 员工实体类
 * 用于测试基础对象映射、嵌套对象、集合类型等功能
 */
public class Employee {

    // 基础字段
    private Long id;                    // 员工ID
    private String employeeNo;          // 员工编号
    private String name;                // 姓名
    private String email;               // 邮箱
    private String phone;               // 电话
    private Integer departmentId;       // 部门ID
    private String position;            // 职位
    private String level;               // 职级：P1-P10
    private BigDecimal baseSalary;      // 基础薪资
    private Date hireDate;              // 入职日期
    private String status;              // 状态：ACTIVE, INACTIVE, RESIGNED

    // 集合类型字段
    private List<String> skills;        // 技能标签

    // 嵌套对象字段
    private Address address;            // 地址信息（嵌套对象）

    // 时间戳字段
    private Date createdTime;           // 创建时间
    private Date updatedTime;           // 更新时间

    // 默认构造函数
    public Employee() {
    }

    // 全参数构造函数
    public Employee(Long id, String employeeNo, String name, String email, String phone,
                   Integer departmentId, String position, String level, BigDecimal baseSalary,
                   Date hireDate, String status, List<String> skills, Address address,
                   Date createdTime, Date updatedTime) {
        this.id = id;
        this.employeeNo = employeeNo;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.departmentId = departmentId;
        this.position = position;
        this.level = level;
        this.baseSalary = baseSalary;
        this.hireDate = hireDate;
        this.status = status;
        this.skills = skills;
        this.address = address;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
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

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
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

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", employeeNo='" + employeeNo + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", departmentId=" + departmentId +
                ", position='" + position + '\'' +
                ", level='" + level + '\'' +
                ", baseSalary=" + baseSalary +
                ", hireDate=" + hireDate +
                ", status='" + status + '\'' +
                ", skills=" + skills +
                ", address=" + address +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Employee employee = (Employee) o;

        return id != null ? id.equals(employee.id) : employee.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}