package cn.myslayers.libra.framework.toolkit.demo.model.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 薪资实体类
 * 用于测试复杂计算、数值类型、精度处理等场景
 */
public class Salary {

    private Long id;                    // 薪资记录ID
    private Long employeeId;            // 员工ID
    private String period;              // 薪资期间：2024-01
    private BigDecimal baseSalary;      // 基础工资
    private BigDecimal performanceBonus; // 绩效奖金
    private BigDecimal allowance;       // 补贴
    private BigDecimal overtime;        // 加班费
    private BigDecimal deduction;       // 扣款
    private BigDecimal totalAmount;     // 总额
    private BigDecimal taxAmount;       // 税额
    private BigDecimal actualAmount;    // 实发金额
    private String status;              // 状态：DRAFT, APPROVED, PAID
    private Date payDate;               // 发放日期
    private String remarks;             // 备注
    private Map<String, BigDecimal> extraAmounts; // 其他金额项
    private Date createdTime;           // 创建时间
    private Date updatedTime;           // 更新时间

    // 默认构造函数
    public Salary() {
    }

    // 便捷构造函数
    public Salary(Long employeeId, String period, BigDecimal baseSalary) {
        this.employeeId = employeeId;
        this.period = period;
        this.baseSalary = baseSalary;
    }

    // Getter 和 Setter 方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public BigDecimal getBaseSalary() {
        return baseSalary;
    }

    public void setBaseSalary(BigDecimal baseSalary) {
        this.baseSalary = baseSalary;
    }

    public BigDecimal getPerformanceBonus() {
        return performanceBonus;
    }

    public void setPerformanceBonus(BigDecimal performanceBonus) {
        this.performanceBonus = performanceBonus;
    }

    public BigDecimal getAllowance() {
        return allowance;
    }

    public void setAllowance(BigDecimal allowance) {
        this.allowance = allowance;
    }

    public BigDecimal getOvertime() {
        return overtime;
    }

    public void setOvertime(BigDecimal overtime) {
        this.overtime = overtime;
    }

    public BigDecimal getDeduction() {
        return deduction;
    }

    public void setDeduction(BigDecimal deduction) {
        this.deduction = deduction;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getPayDate() {
        return payDate;
    }

    public void setPayDate(Date payDate) {
        this.payDate = payDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Map<String, BigDecimal> getExtraAmounts() {
        return extraAmounts;
    }

    public void setExtraAmounts(Map<String, BigDecimal> extraAmounts) {
        this.extraAmounts = extraAmounts;
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
        return "Salary{" +
                "id=" + id +
                ", employeeId=" + employeeId +
                ", period='" + period + '\'' +
                ", baseSalary=" + baseSalary +
                ", performanceBonus=" + performanceBonus +
                ", allowance=" + allowance +
                ", overtime=" + overtime +
                ", deduction=" + deduction +
                ", totalAmount=" + totalAmount +
                ", taxAmount=" + taxAmount +
                ", actualAmount=" + actualAmount +
                ", status='" + status + '\'' +
                ", payDate=" + payDate +
                ", remarks='" + remarks + '\'' +
                ", extraAmounts=" + extraAmounts +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Salary salary = (Salary) o;

        return id != null ? id.equals(salary.id) : salary.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}