package cn.myslayers.libra.framework.toolkit.demo.model.vo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 项目进度VO
 * 用于测试项目进度展示、任务统计等场景
 */
public class ProjectProgressVO {

    private Long id;                    // 项目ID
    private String name;                // 项目名称
    private String code;                // 项目编码
    private String status;              // 状态
    private String priority;            // 优先级
    private BigDecimal budget;          // 项目预算
    private Date startDate;             // 开始日期
    private Date endDate;               // 结束日期
    private Integer progress;           // 进度百分比
    private String managerName;         // 项目经理姓名
    private String departmentName;      // 所属部门
    private Integer memberCount;        // 成员数量
    private List<String> tags;          // 项目标签
    private ProgressDetail detail;      // 进度详情

    // 内部类：进度详情
    public static class ProgressDetail {
        private Integer totalTasks;      // 总任务数
        private Integer completedTasks;  // 已完成任务数
        private Integer pendingTasks;    // 待处理任务数
        private BigDecimal budgetUsed;   // 已用预算
        private Integer daysRemaining;   // 剩余天数
        private String riskLevel;        // 风险等级

        public ProgressDetail() {
        }

        public ProgressDetail(Integer totalTasks, Integer completedTasks, Integer pendingTasks) {
            this.totalTasks = totalTasks;
            this.completedTasks = completedTasks;
            this.pendingTasks = pendingTasks;
        }

        // Getter 和 Setter
        public Integer getTotalTasks() {
            return totalTasks;
        }

        public void setTotalTasks(Integer totalTasks) {
            this.totalTasks = totalTasks;
        }

        public Integer getCompletedTasks() {
            return completedTasks;
        }

        public void setCompletedTasks(Integer completedTasks) {
            this.completedTasks = completedTasks;
        }

        public Integer getPendingTasks() {
            return pendingTasks;
        }

        public void setPendingTasks(Integer pendingTasks) {
            this.pendingTasks = pendingTasks;
        }

        public BigDecimal getBudgetUsed() {
            return budgetUsed;
        }

        public void setBudgetUsed(BigDecimal budgetUsed) {
            this.budgetUsed = budgetUsed;
        }

        public Integer getDaysRemaining() {
            return daysRemaining;
        }

        public void setDaysRemaining(Integer daysRemaining) {
            this.daysRemaining = daysRemaining;
        }

        public String getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(String riskLevel) {
            this.riskLevel = riskLevel;
        }

        @Override
        public String toString() {
            return "ProgressDetail{" +
                    "totalTasks=" + totalTasks +
                    ", completedTasks=" + completedTasks +
                    ", pendingTasks=" + pendingTasks +
                    ", budgetUsed=" + budgetUsed +
                    ", daysRemaining=" + daysRemaining +
                    ", riskLevel='" + riskLevel + '\'' +
                    '}';
        }
    }

    // 默认构造函数
    public ProjectProgressVO() {
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

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public ProgressDetail getDetail() {
        return detail;
    }

    public void setDetail(ProgressDetail detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "ProjectProgressVO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", budget=" + budget +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", progress=" + progress +
                ", managerName='" + managerName + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", memberCount=" + memberCount +
                ", tags=" + tags +
                ", detail=" + detail +
                '}';
    }
}