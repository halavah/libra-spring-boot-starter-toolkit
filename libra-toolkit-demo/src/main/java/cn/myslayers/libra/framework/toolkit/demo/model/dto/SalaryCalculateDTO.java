package cn.myslayers.libra.framework.toolkit.demo.model.dto;

import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.DateRange;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 薪资计算DTO
 * 用于测试复杂薪资计算、批量处理等场景
 */
public class SalaryCalculateDTO {

    private List<Long> employeeIds;     // 员工ID列表
    private String period;              // 计算期间：2024-01
    private DateRange dateRange;        // 日期范围（嵌套对象）
    private String calculationType;     // 计算类型：MONTHLY, BONUS, ALLOWANCE
    private BigDecimal baseAmount;      // 基础金额
    private BigDecimal bonusRate;       // 奖金比例
    private Map<String, BigDecimal> allowanceMap; // 补贴映射
    private Map<String, BigDecimal> deductionMap; // 扣款映射
    private Boolean includeTax;         // 是否含税
    private String taxPolicy;           // 税收政策
    private List<String> includeItems;  // 包含项目
    private List<String> excludeItems;  // 排除项目
    private String remarks;             // 备注

    // 默认构造函数
    public SalaryCalculateDTO() {
    }

    // 便捷构造函数
    public SalaryCalculateDTO(List<Long> employeeIds, String period, String calculationType) {
        this.employeeIds = employeeIds;
        this.period = period;
        this.calculationType = calculationType;
    }

    // Getter 和 Setter 方法
    public List<Long> getEmployeeIds() {
        return employeeIds;
    }

    public void setEmployeeIds(List<Long> employeeIds) {
        this.employeeIds = employeeIds;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public String getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getBonusRate() {
        return bonusRate;
    }

    public void setBonusRate(BigDecimal bonusRate) {
        this.bonusRate = bonusRate;
    }

    public Map<String, BigDecimal> getAllowanceMap() {
        return allowanceMap;
    }

    public void setAllowanceMap(Map<String, BigDecimal> allowanceMap) {
        this.allowanceMap = allowanceMap;
    }

    public Map<String, BigDecimal> getDeductionMap() {
        return deductionMap;
    }

    public void setDeductionMap(Map<String, BigDecimal> deductionMap) {
        this.deductionMap = deductionMap;
    }

    public Boolean getIncludeTax() {
        return includeTax;
    }

    public void setIncludeTax(Boolean includeTax) {
        this.includeTax = includeTax;
    }

    public String getTaxPolicy() {
        return taxPolicy;
    }

    public void setTaxPolicy(String taxPolicy) {
        this.taxPolicy = taxPolicy;
    }

    public List<String> getIncludeItems() {
        return includeItems;
    }

    public void setIncludeItems(List<String> includeItems) {
        this.includeItems = includeItems;
    }

    public List<String> getExcludeItems() {
        return excludeItems;
    }

    public void setExcludeItems(List<String> excludeItems) {
        this.excludeItems = excludeItems;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "SalaryCalculateDTO{" +
                "employeeIds=" + employeeIds +
                ", period='" + period + '\'' +
                ", dateRange=" + dateRange +
                ", calculationType='" + calculationType + '\'' +
                ", baseAmount=" + baseAmount +
                ", bonusRate=" + bonusRate +
                ", allowanceMap=" + allowanceMap +
                ", deductionMap=" + deductionMap +
                ", includeTax=" + includeTax +
                ", taxPolicy='" + taxPolicy + '\'' +
                ", includeItems=" + includeItems +
                ", excludeItems=" + excludeItems +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}