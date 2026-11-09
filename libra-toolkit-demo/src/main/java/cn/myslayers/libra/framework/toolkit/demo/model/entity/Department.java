package cn.myslayers.libra.framework.toolkit.demo.model.entity;

import java.util.Date;
import java.util.Map;

/**
 * 部门实体类
 * 用于测试层级结构、递归查询、动态属性等复杂场景
 */
public class Department {

    private Integer id;                 // 部门ID
    private String name;                // 部门名称
    private String code;                // 部门编码
    private Integer parentId;           // 父部门ID
    private String description;         // 部门描述
    private Long managerId;             // 部门经理ID
    private String location;            // 办公地点
    private Integer level;              // 部门层级
    private String status;              // 状态：ACTIVE, INACTIVE
    private Date establishDate;         // 成立日期
    private Map<String, Object> extraInfo; // 扩展信息
    private Date createdTime;           // 创建时间
    private Date updatedTime;           // 更新时间

    // 默认构造函数
    public Department() {
    }

    // 便捷构造函数
    public Department(String name, String code, Integer parentId) {
        this.name = name;
        this.code = code;
        this.parentId = parentId;
    }

    // 全参数构造函数
    public Department(Integer id, String name, String code, Integer parentId, String description,
                     Long managerId, String location, Integer level, String status, Date establishDate,
                     Map<String, Object> extraInfo, Date createdTime, Date updatedTime) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.parentId = parentId;
        this.description = description;
        this.managerId = managerId;
        this.location = location;
        this.level = level;
        this.status = status;
        this.establishDate = establishDate;
        this.extraInfo = extraInfo;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    // Getter 和 Setter 方法
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getEstablishDate() {
        return establishDate;
    }

    public void setEstablishDate(Date establishDate) {
        this.establishDate = establishDate;
    }

    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Map<String, Object> extraInfo) {
        this.extraInfo = extraInfo;
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
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", parentId=" + parentId +
                ", description='" + description + '\'' +
                ", managerId=" + managerId +
                ", location='" + location + '\'' +
                ", level=" + level +
                ", status='" + status + '\'' +
                ", establishDate=" + establishDate +
                ", extraInfo=" + extraInfo +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Department that = (Department) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}