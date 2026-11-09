package cn.myslayers.libra.framework.toolkit.demo.model.dto;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 部门管理DTO
 * 用于测试层级结构操作、批量管理等场景
 */
public class DepartmentManageDTO {

    private String name;                // 部门名称
    private String code;                // 部门编码
    private Integer parentId;           // 父部门ID
    private String description;         // 部门描述
    private Long managerId;             // 部门经理ID
    private String location;            // 办公地点
    private Date establishDate;         // 成立日期
    private String operation;           // 操作类型：CREATE, UPDATE, DELETE, MOVE
    private List<Integer> childIds;     // 子部门ID列表（批量操作）
    private Map<String, Object> extraInfo; // 扩展信息
    private String remarks;             // 备注

    // 默认构造函数
    public DepartmentManageDTO() {
    }

    // 便捷构造函数
    public DepartmentManageDTO(String name, String code, String operation) {
        this.name = name;
        this.code = code;
        this.operation = operation;
    }

    // Getter 和 Setter 方法
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

    public Date getEstablishDate() {
        return establishDate;
    }

    public void setEstablishDate(Date establishDate) {
        this.establishDate = establishDate;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public List<Integer> getChildIds() {
        return childIds;
    }

    public void setChildIds(List<Integer> childIds) {
        this.childIds = childIds;
    }

    public Map<String, Object> getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(Map<String, Object> extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "DepartmentManageDTO{" +
                "name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", parentId=" + parentId +
                ", description='" + description + '\'' +
                ", managerId=" + managerId +
                ", location='" + location + '\'' +
                ", establishDate=" + establishDate +
                ", operation='" + operation + '\'' +
                ", childIds=" + childIds +
                ", extraInfo=" + extraInfo +
                ", remarks='" + remarks + '\'' +
                '}';
    }
}