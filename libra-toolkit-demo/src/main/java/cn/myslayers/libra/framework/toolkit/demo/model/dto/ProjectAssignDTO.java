package cn.myslayers.libra.framework.toolkit.demo.model.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 项目分配DTO
 * 用于测试项目人员分配、权限管理等场景
 */
public class ProjectAssignDTO {

    private Long projectId;             // 项目ID
    private String projectName;         // 项目名称（用于查询条件）
    private String projectCode;         // 项目编码
    private String status;              // 项目状态（用于查询条件）
    private Long managerId;             // 项目经理ID（用于查询条件）
    private Integer departmentId;       // 部门ID（用于查询条件）
    private BigDecimal budgetMin;       // 预算最小值（用于查询条件）
    private BigDecimal budgetMax;       // 预算最大值（用于查询条件）
    private List<Long> memberIds;       // 成员ID列表
    private List<String> roleTypes;     // 角色类型列表
    private Date startDate;             // 分配开始日期
    private Date endDate;               // 分配结束日期
    private BigDecimal budgetAllocation; // 预算分配
    private String priority;            // 优先级
    private List<String> permissions;   // 权限列表
    private String assignType;          // 分配类型：ASSIGN, REASSIGN, REMOVE
    private String remarks;             // 备注

    // 默认构造函数
    public ProjectAssignDTO() {
    }

    // 便捷构造函数
    public ProjectAssignDTO(Long projectId, List<Long> memberIds, String assignType) {
        this.projectId = projectId;
        this.memberIds = memberIds;
        this.assignType = assignType;
    }

    // Getter 和 Setter 方法
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public BigDecimal getBudgetMin() {
        return budgetMin;
    }

    public void setBudgetMin(BigDecimal budgetMin) {
        this.budgetMin = budgetMin;
    }

    public BigDecimal getBudgetMax() {
        return budgetMax;
    }

    public void setBudgetMax(BigDecimal budgetMax) {
        this.budgetMax = budgetMax;
    }

    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    public List<String> getRoleTypes() {
        return roleTypes;
    }

    public void setRoleTypes(List<String> roleTypes) {
        this.roleTypes = roleTypes;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getBudgetAllocation() {
        return budgetAllocation;
    }

    public void setBudgetAllocation(BigDecimal budgetAllocation) {
        this.budgetAllocation = budgetAllocation;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public String getAssignType() {
        return assignType;
    }

    public void setAssignType(String assignType) {
        this.assignType = assignType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "ProjectAssignDTO{" +
                "projectId=" + projectId +
                ", projectCode='" + projectCode + '\'' +
                ", memberIds=" + memberIds +
                ", roleTypes=" + roleTypes +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", budgetAllocation=" + budgetAllocation +
                ", priority='" + priority + '\'' +
                ", permissions=" + permissions +
                ", assignType='" + assignType + '\'' +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}