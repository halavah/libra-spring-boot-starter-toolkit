package cn.myslayers.libra.framework.toolkit.demo.model.entity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 项目实体类
 * 用于测试多表关联、项目协作、时间序列等复杂场景
 */
public class Project {

    private Long id;                    // 项目ID
    private String name;                // 项目名称
    private String code;                // 项目编码
    private String description;         // 项目描述
    private Long managerId;             // 项目经理ID
    private Integer departmentId;       // 所属部门
    private String status;              // 状态：PLANNING, RUNNING, PAUSED, COMPLETED, CANCELLED
    private String priority;            // 优先级：HIGH, MEDIUM, LOW
    private BigDecimal budget;          // 项目预算
    private Date startDate;             // 开始日期
    private Date endDate;               // 结束日期
    private Date actualStartDate;       // 实际开始日期
    private Date actualEndDate;         // 实际结束日期
    private Integer progress;           // 进度百分比
    private List<String> tags;          // 项目标签
    private Date createdTime;           // 创建时间
    private Date updatedTime;           // 更新时间

    // 默认构造函数
    public Project() {
    }

    // 便捷构造函数
    public Project(String name, String code, Long managerId, Integer departmentId) {
        this.name = name;
        this.code = code;
        this.managerId = managerId;
        this.departmentId = departmentId;
    }

    // Getter 和 Setter 方法
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
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

    public Date getActualStartDate() {
        return actualStartDate;
    }

    public void setActualStartDate(Date actualStartDate) {
        this.actualStartDate = actualStartDate;
    }

    public Date getActualEndDate() {
        return actualEndDate;
    }

    public void setActualEndDate(Date actualEndDate) {
        this.actualEndDate = actualEndDate;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", managerId=" + managerId +
                ", departmentId=" + departmentId +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", budget=" + budget +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", actualStartDate=" + actualStartDate +
                ", actualEndDate=" + actualEndDate +
                ", progress=" + progress +
                ", tags=" + tags +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Project project = (Project) o;

        return id != null ? id.equals(project.id) : project.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}