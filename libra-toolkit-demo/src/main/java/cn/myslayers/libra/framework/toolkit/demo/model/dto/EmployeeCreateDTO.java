package cn.myslayers.libra.framework.toolkit.demo.model.dto;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.Address;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 员工创建DTO
 * 用于测试嵌套对象创建、参数验证等场景
 */
public class EmployeeCreateDTO {

    private String employeeNo;          // 员工编号
    private String name;                // 姓名
    private String email;               // 邮箱
    private String phone;               // 电话
    private Integer departmentId;       // 部门ID
    private String position;            // 职位
    private String level;               // 职级
    private BigDecimal baseSalary;      // 基础薪资
    private Date hireDate;              // 入职日期
    private List<String> skills;        // 技能列表
    private Address address;            // 地址信息（嵌套对象）
    private String remarks;             // 备注

    // 默认构造函数
    public EmployeeCreateDTO() {
    }

    // 必填字段构造函数
    public EmployeeCreateDTO(String employeeNo, String name, String email, Integer departmentId) {
        this.employeeNo = employeeNo;
        this.name = name;
        this.email = email;
        this.departmentId = departmentId;
    }

    // Getter 和 Setter 方法
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "EmployeeCreateDTO{" +
                "employeeNo='" + employeeNo + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", departmentId=" + departmentId +
                ", position='" + position + '\'' +
                ", level='" + level + '\'' +
                ", baseSalary=" + baseSalary +
                ", hireDate=" + hireDate +
                ", skills=" + skills +
                ", address=" + address +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}