package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.math.BigDecimal;

/**
 * DepartmentMapper.xml 强类型测试类
 * 使用 ...Enhanced API 精确测试 parameterType 和 resultType
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test11_DepartmentMapperTesting - DepartmentMapper.xml 强类型测试")
public class Test11_DepartmentMapperTesting {

    // ==================== DTO/VO/Entity 内部定义 ====================
    // 为了测试独立，将XML中用到的所有Java类型在此定义为静态内部类

    public static class Department {
        private Integer id;
        private String name;
        private String code;
        private Integer parentId;
        private String description;
        private Integer managerId;
        private String location;
        private Integer level;
        private String status;
        private Date establishDate;
        private String extraInfo; // JSON 字符串

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public Integer getParentId() { return parentId; }
        public void setParentId(Integer parentId) { this.parentId = parentId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getManagerId() { return managerId; }
        public void setManagerId(Integer managerId) { this.managerId = managerId; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getEstablishDate() { return establishDate; }
        public void setEstablishDate(Date establishDate) { this.establishDate = establishDate; }
        public String getExtraInfo() { return extraInfo; }
        public void setExtraInfo(String extraInfo) { this.extraInfo = extraInfo; }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }

    public static class DepartmentManageDTO {
        private String name;
        private String code;
        private Integer parentId;
        private Integer managerId;
        private String location;
        private Integer level;
        private String status;
        private Date establishDateStart;
        private Date establishDateEnd;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public Integer getParentId() { return parentId; }
        public void setParentId(Integer parentId) { this.parentId = parentId; }
        public Integer getManagerId() { return managerId; }
        public void setManagerId(Integer managerId) { this.managerId = managerId; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public Integer getLevel() { return level; }
        public void setLevel(Integer level) { this.level = level; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Date getEstablishDateStart() { return establishDateStart; }
        public void setEstablishDateStart(Date date) { this.establishDateStart = date; }
        public Date getEstablishDateEnd() { return establishDateEnd; }
        public void setEstablishDateEnd(Date date) { this.establishDateEnd = date; }
    }

    public static class DepartmentSummaryVO {
        private Integer id;
        private String name;
        private String code;
        private String managerName;
        private Integer employeeCount;

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getManagerName() { return managerName; }
        public void setManagerName(String name) { this.managerName = name; }
        public Integer getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(Integer count) { this.employeeCount = count; }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }

        public static class DepartmentStatistics {
            private Integer id;
            private String name;
            private Long totalEmployees;
            private Long activeEmployees;
            private BigDecimal totalSalary;
            private BigDecimal averageSalary;

            // Getters and Setters
            public Integer getId() { return id; }
            public void setId(Integer id) { this.id = id; }
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public Long getTotalEmployees() { return totalEmployees; }
            public void setTotalEmployees(Long count) { this.totalEmployees = count; }
            public Long getActiveEmployees() { return activeEmployees; }
            public void setActiveEmployees(Long count) { this.activeEmployees = count; }
            public BigDecimal getTotalSalary() { return totalSalary; }
            public void setTotalSalary(BigDecimal salary) { this.totalSalary = salary; }
            public BigDecimal getAverageSalary() { return averageSalary; }
            public void setAverageSalary(BigDecimal salary) { this.averageSalary = salary; }

            @Override
            public String toString() {
                return JSON.toJSONString(this);
            }
        }
    }

    // ==================== 测试生命周期 ====================

    @BeforeAll
    static void setUp() {
        System.out.println("=== Test11_DepartmentMapperTesting 开始测试 ===");

        // 开启日志
//        DatabaseUtil43.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil43.LoggerCore.setSqlDebugLogEnabled(true);

        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_department`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_project`;");

        // 创建表结构
        String createDeptTable = "CREATE TABLE t_department (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), code VARCHAR(255), parent_id INT, " +
            "description VARCHAR(255), manager_id INT, location VARCHAR(255), level INT, status VARCHAR(50), " +
            "establish_date DATE, extra_info JSON, created_time TIMESTAMP, updated_time TIMESTAMP);";
        String createEmpTable = "CREATE TABLE t_employee (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255), department_id INT, status VARCHAR(50), base_salary DECIMAL(10, 2));";
        String createProjTable = "CREATE TABLE t_project (id INT, department_id INT, status VARCHAR(50));";

        DatabaseUtil43.executeDDL(createDeptTable);
        DatabaseUtil43.executeDDL(createEmpTable);
        DatabaseUtil43.executeDDL(createProjTable);
        System.out.println("已创建测试表结构。");
    }

    @AfterAll
    static void tearDown() {
        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_department`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_project`;");

        System.out.println("=== Test11_DepartmentMapperTesting 测试完成 ===");
    }

    @BeforeEach
    void prepareData() {
        // 清理数据
        DatabaseUtil43.executeDML("DELETE FROM t_department;");
        DatabaseUtil43.executeDML("DELETE FROM t_employee;");
        DatabaseUtil43.executeDML("DELETE FROM t_project;");
        // 插入基础数据
        DatabaseUtil43.executeDML("INSERT INTO t_department (id, name, code, parent_id, level, status) VALUES (1, '集团总部', 'HQ', NULL, 0, 'ACTIVE');");
        DatabaseUtil43.executeDML("INSERT INTO t_department (id, name, code, parent_id, level, status) VALUES (10, '研发中心', 'RD', 1, 1, 'ACTIVE');");
        DatabaseUtil43.executeDML("INSERT INTO t_department (id, name, code, parent_id, level, status) VALUES (11, '后端开发部', 'RD-BACK', 10, 2, 'ACTIVE');");
        DatabaseUtil43.executeDML("INSERT INTO t_employee (id, name, department_id, status, base_salary) VALUES (101, '张三', 11, 'ACTIVE', 20000.00);");
    }

    // ==================== 测试方法 ====================

    @Test
    @Order(1)
    @DisplayName("insertDepartment - 插入单个部门")
    public void testInsertDepartment() {
        System.out.println("\n--- [Order 1] 测试 insertDepartment ---");
        Department newDept = new Department();
        newDept.setName("法务部");
        newDept.setCode("LAW");
        newDept.setParentId(1);
        newDept.setLevel(1);
        newDept.setStatus("ACTIVE");
        newDept.setEstablishDate(new Date());
        newDept.setExtraInfo("{\"budget\": 50000}");

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("insertDepartment", newDept);
        System.out.println("插入影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));
    }

    @Test
    @Order(2)
    @DisplayName("selectDepartmentById - 根据ID查询部门")
    public void testSelectDepartmentById() {
        System.out.println("\n--- [Order 2] 测试 selectDepartmentById ---");
        Department dept = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectDepartmentById", 10, Department.class);
        System.out.println("查询结果: " + dept);
        System.out.println("验证查询结果是否非空: " + (dept != null));
        if (dept != null) {
            System.out.println("验证部门名称是否为'研发中心': " + "研发中心".equals(dept.getName()));
        }
    }

    @Test
    @Order(3)
    @DisplayName("updateDepartment - 更新部门信息")
    public void testUpdateDepartment() {
        System.out.println("\n--- [Order 3] 测试 updateDepartment ---");
        Department deptToUpdate = new Department();
        deptToUpdate.setId(10);
        deptToUpdate.setDescription("更新后的部门描述");
        deptToUpdate.setLocation("深圳");

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("updateDepartment", deptToUpdate);
        System.out.println("更新影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));

        Department updatedDept = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectDepartmentById", 10, Department.class);
        System.out.println("更新后查询结果: " + updatedDept);
        if (updatedDept != null) {
            System.out.println("验证更新后位置是否为'深圳': " + "深圳".equals(updatedDept.getLocation()));
        }
    }

    @Test
    @Order(4)
    @DisplayName("selectDepartmentsByCondition - 复杂条件查询")
    public void testSelectDepartmentsByCondition() {
        System.out.println("\n--- [Order 4] 测试 selectDepartmentsByCondition ---");
        DepartmentManageDTO query = new DepartmentManageDTO();
        query.setLevel(1);
        query.setStatus("ACTIVE");

        List<DepartmentSummaryVO> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectDepartmentsByCondition", query, DepartmentSummaryVO.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证列表是否非空: " + (result != null && !result.isEmpty()));
        if (result != null && !result.isEmpty()) {
            System.out.println("验证查询结果名称是否为'研发中心': " + "研发中心".equals(result.get(0).getName()));
        }
    }

    @Test
    @Order(5)
    @DisplayName("selectChildDepartments - 查询所有子部门")
    public void testSelectChildDepartments() {
        System.out.println("\n--- [Order 5] 测试 selectChildDepartments ---");
        List<Department> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectChildDepartments", 1, Department.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证子部门数量是否为2: " + (result != null && result.size() == 2));
    }

    @Test
    @Order(6)
    @DisplayName("batchInsertDepartments - 批量创建部门")
    public void testBatchInsertDepartments() {
        System.out.println("\n--- [Order 6] 测试 batchInsertDepartments ---");
        List<Department> newDepts = new ArrayList<>();
        Department dept1 = new Department();
        dept1.setName("市场部");
        dept1.setCode("MKT");
        dept1.setParentId(1);
        dept1.setLevel(1);
        newDepts.add(dept1);

        Department dept2 = new Department();
        dept2.setName("销售部");
        dept2.setCode("SALES");
        dept2.setParentId(1);
        dept2.setLevel(1);
        newDepts.add(dept2);

        Map<String, Object> params = new HashMap<>();
        params.put("departments", newDepts);

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("batchInsertDepartments", params);
        System.out.println("批量插入影响行数: " + result);
        System.out.println("验证批量插入影响行数是否为2: " + (result == 2));
    }

    @Test
    @Order(7)
    @DisplayName("batchUpdateDepartmentStatus - 批量更新部门状态")
    public void testBatchUpdateDepartmentStatus() {
        System.out.println("\n--- [Order 7] 测试 batchUpdateDepartmentStatus ---");
        Map<String, Object> params = new HashMap<>();
        params.put("status", "INACTIVE");
        params.put("ids", Arrays.asList(10, 11));

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("batchUpdateDepartmentStatus", params);
        System.out.println("批量更新影响行数: " + result);
        System.out.println("验证批量更新影响行数是否为2: " + (result == 2));
    }

    @Test
    @Order(8)
    @DisplayName("selectDepartmentsByExtraInfo - 根据JSON内容查询")
    public void testSelectDepartmentsByExtraInfo() {
        System.out.println("\n--- [Order 8] 测试 selectDepartmentsByExtraInfo ---");
        // 先更新一条记录，加入JSON信息
        DatabaseUtil43.executeDML("UPDATE t_department SET extra_info = '{\"type\":\"core\", \"priority\":\"high\"}' WHERE id = 10;");

        Map<String, Object> params = new HashMap<>();
        Map<String, String> extraConditions = new HashMap<>();
        extraConditions.put("type", "core");
        params.put("extraConditions", extraConditions);

        List<Department> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectDepartmentsByExtraInfo", params, Department.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证查询结果数量是否为1: " + (result != null && result.size() == 1));
        if (result != null && result.size() == 1) {
             System.out.println("验证查询结果ID是否为10: " + (Integer.valueOf(10).equals(result.get(0).getId())));
        }
    }

    @Test
    @Order(9)
    @DisplayName("deleteDepartment - 删除部门（软删除）")
    public void testDeleteDepartment() {
        System.out.println("\n--- [Order 9] 测试 deleteDepartment ---");
        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("deleteDepartment", 11);
        System.out.println("删除影响行数: " + result);
        System.out.println("验证删除影响行数是否为1: " + (result == 1));

        Department deletedDept = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectDepartmentById", 11, Department.class);
        System.out.println("删除后查询结果: " + deletedDept);
        if (deletedDept != null) {
            System.out.println("验证删除后状态是否为'DELETED': " + "DELETED".equals(deletedDept.getStatus()));
        }
    }
}
