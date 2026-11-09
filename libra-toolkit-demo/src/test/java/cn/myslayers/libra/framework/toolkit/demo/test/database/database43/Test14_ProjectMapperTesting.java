package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test14_ProjectMapperTesting - ProjectMapper.xml 强类型测试")
public class Test14_ProjectMapperTesting {

    // ==================== DTO/VO/Entity 内部定义 ====================

    public static class Project {
        private Long id;
        private String name;
        private String description;
        private Date startDate;
        private Date endDate;
        private BigDecimal budget;
        private String status;
        private Long managerId;
        private Long departmentId;

        // Getters & Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
        public BigDecimal getBudget() { return budget; }
        public void setBudget(BigDecimal budget) { this.budget = budget; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getManagerId() { return managerId; }
        public void setManagerId(Long managerId) { this.managerId = managerId; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

        @Override
        public String toString() { return JSON.toJSONString(this); }
    }

    public static class ProjectProgressVO extends Project {
        private String managerName;
        private String departmentName;

        // Getters & Setters
        public String getManagerName() { return managerName; }
        public void setManagerName(String managerName) { this.managerName = managerName; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    }

    public static class ProjectAssignDTO {
        private String projectName;
        private String status;
        private Long managerId;
        private Long departmentId;
        private Date startDate;
        private Date endDate;
        private BigDecimal budgetMin;
        private BigDecimal budgetMax;

        // Getters & Setters
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getManagerId() { return managerId; }
        public void setManagerId(Long managerId) { this.managerId = managerId; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
        public BigDecimal getBudgetMin() { return budgetMin; }
        public void setBudgetMin(BigDecimal budgetMin) { this.budgetMin = budgetMin; }
        public BigDecimal getBudgetMax() { return budgetMax; }
        public void setBudgetMax(BigDecimal budgetMax) { this.budgetMax = budgetMax; }
    }

    public static class ProjectMember {
        private Long projectId;
        private Long employeeId;
        private String role;
        private Date joinDate;
        private Double contributionRate;

        // Getters & Setters
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Date getJoinDate() { return joinDate; }
        public void setJoinDate(Date joinDate) { this.joinDate = joinDate; }
        public Double getContributionRate() { return contributionRate; }
        public void setContributionRate(Double rate) { this.contributionRate = rate; }
    }

    // ==================== 测试生命周期 ====================

    @BeforeAll
    static void setUp() {
        System.out.println("=== Test14_ProjectMapperTesting 开始测试 ===");

        // 开启日志
//        DatabaseUtil43.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil43.LoggerCore.setSqlDebugLogEnabled(true);

        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `department`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_milestone`;");

        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS project (project_id BIGINT PRIMARY KEY AUTO_INCREMENT, project_name VARCHAR(255), description VARCHAR(1000), start_date DATE, end_date DATE, budget DECIMAL(15, 2), status VARCHAR(50), manager_id BIGINT, department_id BIGINT, created_time TIMESTAMP, updated_time TIMESTAMP);");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS employee (employee_id BIGINT PRIMARY KEY, name VARCHAR(255), department_id BIGINT, email VARCHAR(255), position VARCHAR(255));");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS department (department_id BIGINT PRIMARY KEY, department_name VARCHAR(255));");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS project_employee (project_id BIGINT, employee_id BIGINT, role VARCHAR(255), join_date DATE, contribution_rate DOUBLE, PRIMARY KEY (project_id, employee_id));");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS project_milestone (milestone_id BIGINT PRIMARY KEY AUTO_INCREMENT, project_id BIGINT, milestone_name VARCHAR(255), description VARCHAR(1000), target_date DATE, completion_date DATE, status VARCHAR(50), priority VARCHAR(50));");
        System.out.println("已创建测试表结构。");
    }

    @AfterAll
    static void tearDown() {
        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `department`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_milestone`;");

        System.out.println("=== Test14_ProjectMapperTesting 测试完成 ===");
    }

    @BeforeEach
    void prepareData() {
        DatabaseUtil43.executeDML("DELETE FROM project;");
        DatabaseUtil43.executeDML("DELETE FROM employee;");
        DatabaseUtil43.executeDML("DELETE FROM department;");
        DatabaseUtil43.executeDML("DELETE FROM project_employee;");
        DatabaseUtil43.executeDML("DELETE FROM project_milestone;");

        DatabaseUtil43.executeDML("INSERT INTO department (department_id, department_name) VALUES (1, '研发部'), (2, '市场部');");
        DatabaseUtil43.executeDML("INSERT INTO employee (employee_id, name, department_id) VALUES (101, '张三', 1), (102, '李四', 1);");
        DatabaseUtil43.executeDML("INSERT INTO project (project_id, project_name, status, budget, manager_id, department_id, description) VALUES (1, '核心系统重构', 'ACTIVE', 500000.00, 101, 1, NULL);");
        DatabaseUtil43.executeDML("INSERT INTO project (project_id, project_name, status, budget, manager_id, department_id) VALUES (2, '新功能A上线', 'COMPLETED', 100000.00, 102, 1);");
        DatabaseUtil43.executeDML("INSERT INTO project_employee (project_id, employee_id, role) VALUES (1, 101, '项目经理'), (1, 102, '核心开发');");
    }

    // ==================== 测试方法 ====================

    @Test
    @Order(1)
    @DisplayName("insertProject - 创建项目")
    void testInsertProject() {
        System.out.println("\n--- [Order 1] 测试 insertProject ---");
        Project newProject = new Project();
        newProject.setName("移动端App开发");
        newProject.setStatus("PLANNING");
        newProject.setBudget(new BigDecimal("300000.00"));
        newProject.setManagerId(101L);
        newProject.setDepartmentId(1L);
        newProject.setStartDate(new Date());

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("insertProject", newProject);
        System.out.println("插入影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));
    }

    @Test
    @Order(2)
    @DisplayName("selectProjectById - 根据ID查询项目")
    void testSelectProjectById() {
        System.out.println("\n--- [Order 2] 测试 selectProjectById ---");
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", 1L);
        Project project = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectProjectById", params, Project.class);
        System.out.println("查询结果: " + project);
        System.out.println("验证查询结果是否非空: " + (project != null));
        if (project != null) {
            System.out.println("验证项目名称是否为'核心系统重构': " + "核心系统重构".equals(project.getName()));
        }
    }

    @Test
    @Order(3)
    @DisplayName("updateProject - 更新项目信息")
    void testUpdateProject() {
        System.out.println("\n--- [Order 3] 测试 updateProject ---");
        Project projectToUpdate = new Project();
        projectToUpdate.setId(1L);
        projectToUpdate.setStatus("SUSPENDED");
        projectToUpdate.setDescription("项目暂停，等待资源");

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("updateProject", projectToUpdate);
        System.out.println("更新影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", 1L);
        Project updatedProject = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectProjectById", params, Project.class);
        System.out.println("更新后查询结果: " + updatedProject);
        if (updatedProject != null) {
            System.out.println("验证更新后状态是否为'SUSPENDED': " + "SUSPENDED".equals(updatedProject.getStatus()));
        }
    }

    @Test
    @Order(4)
    @DisplayName("selectProjectList - 多条件查询项目列表")
    void testSelectProjectList() {
        System.out.println("\n--- [Order 4] 测试 selectProjectList ---");
        ProjectAssignDTO query = new ProjectAssignDTO();
        query.setStatus("ACTIVE");
        query.setDepartmentId(1L);

        List<ProjectProgressVO> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectProjectList", query, ProjectProgressVO.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证查询结果数量是否为1: " + (result != null && result.size() == 1));
        if (result != null && !result.isEmpty()) {
            System.out.println("验证项目名称是否为'核心系统重构': " + "核心系统重构".equals(result.get(0).getName()));
        }
    }

    @Test
    @Order(5)
    @DisplayName("addProjectMember & selectProjectMembers - 添加和查询项目成员")
    void testAddAndSelectProjectMembers() {
        System.out.println("\n--- [Order 5] 测试 addProjectMember & selectProjectMembers ---");
        ProjectMember newMember = new ProjectMember();
        newMember.setProjectId(2L);
        newMember.setEmployeeId(101L);
        newMember.setRole("技术顾问");
        newMember.setJoinDate(new Date());

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("addProjectMember", newMember);
        System.out.println("添加成员影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", 2L);
        List<Map> members = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectProjectMembers", params, Map.class);
        System.out.println("查询成员结果: " + members);
        System.out.println("验证查询成员结果数量是否为1: " + (members != null && members.size() == 1));
        if (members != null && !members.isEmpty()) {
            System.out.println("验证成员角色是否为'技术顾问': " + "技术顾问".equals(members.get(0).get("project_role")));
        }
    }

    @Test
    @Order(6)
    @DisplayName("getProjectStatistics - 获取项目统计信息")
    void testGetProjectStatistics() {
        System.out.println("\n--- [Order 6] 测试 getProjectStatistics ---");
        Map<String, Object> stats = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("getProjectStatistics", null, Map.class);
        System.out.println("统计结果: " + JSON.toJSONString(stats));
        System.out.println("验证统计结果是否非空: " + (stats != null));
        if (stats != null) {
            System.out.println("验证总项目数是否为2: " + Long.valueOf(2L).equals(stats.get("total_projects")));
            System.out.println("验证活跃项目数是否为1: " + Long.valueOf(1L).equals(stats.get("active_projects")));
            System.out.println("验证已完成项目数是否为1: " + Long.valueOf(1L).equals(stats.get("completed_projects")));
        }
    }

    @Test
    @Order(7)
    @DisplayName("deleteProject - 删除项目")
    void testDeleteProject() {
        System.out.println("\n--- [Order 7] 测试 deleteProject ---");
        Map<String, Object> params = new HashMap<>();
        params.put("projectId", 2L);
        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("deleteProject", params);
        System.out.println("删除影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));

        params.put("projectId", 2L);
        Project deletedProject = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectProjectById", params, Project.class);
        System.out.println("验证删除后查询结果是否为null: " + (deletedProject == null));
    }
}
