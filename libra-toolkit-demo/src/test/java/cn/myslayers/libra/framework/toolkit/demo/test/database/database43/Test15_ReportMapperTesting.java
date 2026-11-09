package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test15_ReportMapperTesting - ReportMapper.xml 强类型测试")
public class Test15_ReportMapperTesting {

    // ==================== DTO/VO/Entity 内部定义 ====================

    public static class ReportQueryDTO {
        private Date startDate;
        private Date endDate;
        private Long departmentId;
        private String dimensionType; // for sales analysis
        private Long projectId;

        // Getters & Setters
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        public String getDimensionType() { return dimensionType; }
        public void setDimensionType(String dimensionType) { this.dimensionType = dimensionType; }
    }

    public static class ReportDataVO {
        private String reportTitle;
        private String reportDate;
        private Long totalEmployees;
        private Long totalDepartments;
        private Long activeProjects;
        private BigDecimal avgSalary;
        private BigDecimal monthlyRevenue;
        private Long monthlyOrders;

        // Getters & Setters
        public String getReportTitle() { return reportTitle; }
        public void setReportTitle(String reportTitle) { this.reportTitle = reportTitle; }
        public String getReportDate() { return reportDate; }
        public void setReportDate(String reportDate) { this.reportDate = reportDate; }
        public Long getTotalEmployees() { return totalEmployees; }
        public void setTotalEmployees(Long totalEmployees) { this.totalEmployees = totalEmployees; }
        public Long getTotalDepartments() { return totalDepartments; }
        public void setTotalDepartments(Long totalDepartments) { this.totalDepartments = totalDepartments; }
        public Long getActiveProjects() { return activeProjects; }
        public void setActiveProjects(Long activeProjects) { this.activeProjects = activeProjects; }
        public BigDecimal getAvgSalary() { return avgSalary; }
        public void setAvgSalary(BigDecimal avgSalary) { this.avgSalary = avgSalary; }
        public BigDecimal getMonthlyRevenue() { return monthlyRevenue; }
        public void setMonthlyRevenue(BigDecimal monthlyRevenue) { this.monthlyRevenue = monthlyRevenue; }
        public Long getMonthlyOrders() { return monthlyOrders; }
        public void setMonthlyOrders(Long monthlyOrders) { this.monthlyOrders = monthlyOrders; }

        @Override
        public String toString() { return JSON.toJSONString(this); }
    }

    // ==================== 测试生命周期 ====================

    @BeforeAll
    static void setUp() {
        System.out.println("=== Test15_ReportMapperTesting 开始测试 ===");

        // 开启日志
//        DatabaseUtil43.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil43.LoggerCore.setSqlDebugLogEnabled(true);

        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `department`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `orders`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `salary`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_milestone`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_expense`;");

        // 创建所有报表依赖的表
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS department (department_id BIGINT PRIMARY KEY, department_name VARCHAR(255), status VARCHAR(50), manager_name VARCHAR(255), budget DECIMAL(15,2));");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS employee (employee_id BIGINT PRIMARY KEY, name VARCHAR(255), department_id BIGINT, status VARCHAR(50), base_salary DECIMAL(15,2), performance_rating VARCHAR(50), hire_date DATE, created_time TIMESTAMP);");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS project (project_id BIGINT PRIMARY KEY, project_name VARCHAR(255), department_id BIGINT, status VARCHAR(50), budget DECIMAL(15,2), start_date DATE, end_date DATE, created_time TIMESTAMP);");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS orders (order_id BIGINT PRIMARY KEY, total_amount DECIMAL(15,2), final_amount DECIMAL(15,2), order_date DATE, status VARCHAR(50), payment_method VARCHAR(50), customer_phone VARCHAR(50), created_time TIMESTAMP);");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS salary (salary_id BIGINT PRIMARY KEY, employee_id BIGINT, net_salary DECIMAL(15,2), pay_date DATE, status VARCHAR(50));");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS project_employee (project_id BIGINT, employee_id BIGINT, contribution_rate DOUBLE, PRIMARY KEY (project_id, employee_id));");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS project_milestone (milestone_id BIGINT PRIMARY KEY, project_id BIGINT, status VARCHAR(50));");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS project_expense (expense_id BIGINT PRIMARY KEY, project_id BIGINT, expense_amount DECIMAL(15,2));");
        System.out.println("已创建测试表结构。");
    }

    @AfterAll
    static void tearDown() {
        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `department`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `orders`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `salary`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_milestone`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `project_expense`;");

        System.out.println("=== Test15_ReportMapperTesting 测试完成 ===");
    }

    @BeforeEach
    void prepareData() {
        // 清理数据
        DatabaseUtil43.executeDML("DELETE FROM department;");
        DatabaseUtil43.executeDML("DELETE FROM employee;");
        DatabaseUtil43.executeDML("DELETE FROM project;");
        DatabaseUtil43.executeDML("DELETE FROM orders;");
        DatabaseUtil43.executeDML("DELETE FROM salary;");
        DatabaseUtil43.executeDML("DELETE FROM project_employee;");
        DatabaseUtil43.executeDML("DELETE FROM project_milestone;");
        DatabaseUtil43.executeDML("DELETE FROM project_expense;");

        // 插入数据
        DatabaseUtil43.executeDML("INSERT INTO department (department_id, department_name, status, budget) VALUES (1, '研发部', 'ACTIVE', 1000000.00);");
        DatabaseUtil43.executeDML("INSERT INTO employee (employee_id, name, department_id, status, base_salary, hire_date, created_time) VALUES (101, '张三', 1, 'ACTIVE', 20000.00, '2022-01-01', NOW());");
        DatabaseUtil43.executeDML("INSERT INTO employee (employee_id, name, department_id, status, base_salary, hire_date, created_time) VALUES (102, '李四', 1, 'INACTIVE', 15000.00, '2022-02-01', NOW());");
        DatabaseUtil43.executeDML("INSERT INTO project (project_id, project_name, department_id, status, budget, start_date, end_date, created_time) VALUES (1, '核心系统', 1, 'RUNNING', 500000.00, '2023-01-01', '2023-12-31', NOW());");
        DatabaseUtil43.executeDML("INSERT INTO orders (order_id, total_amount, final_amount, order_date, status, created_time) VALUES (1, 1000.00, 900.00, '2023-10-10', 'DELIVERED', NOW());");
        DatabaseUtil43.executeDML("INSERT INTO salary (salary_id, employee_id, net_salary, pay_date, status) VALUES (1, 101, 18000.00, '2023-10-15', 'PAID');");
    }

    // ==================== 测试方法 ====================

    @Test
    @Order(1)
    @DisplayName("selectBusinessOverview - 业务概览报表")
    void testSelectBusinessOverview() {
        System.out.println("\n--- [Order 1] 测试 selectBusinessOverview ---");
        ReportDataVO result = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectBusinessOverview", null, ReportDataVO.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证查询结果是否非空: " + (result != null));
        if (result != null) {
            System.out.println("验证总员工数是否为1: " + Long.valueOf(1L).equals(result.getTotalEmployees()));
            System.out.println("验证活跃项目数是否为1: " + Long.valueOf(1L).equals(result.getActiveProjects()));
        }
    }

    @Test
    @Order(2)
    @DisplayName("selectEmployeeStatistics - 员工统计报表")
    void testSelectEmployeeStatistics() {
        System.out.println("\n--- [Order 2] 测试 selectEmployeeStatistics ---");
        ReportQueryDTO query = new ReportQueryDTO();
        query.setDepartmentId(1L);

        List<Map> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectEmployeeStatistics", query, Map.class);
        System.out.println("查询结果: " + JSON.toJSONString(result));
        System.out.println("验证结果列表是否非空: " + (result != null && !result.isEmpty()));
        if (result != null && !result.isEmpty()) {
            Map<String, Object> deptStats = result.get(0);
            System.out.println("验证总员工数是否为2: " + Long.valueOf(2L).equals(deptStats.get("employee_count")));
            System.out.println("验证活跃员工数是否为1: " + Long.valueOf(1L).equals(deptStats.get("active_employees")));
        }
    }

    @Test
    @Order(3)
    @DisplayName("selectFinancialReport - 财务分析报表")
    void testSelectFinancialReport() {
        System.out.println("\n--- [Order 3] 测试 selectFinancialReport ---");
        ReportQueryDTO query = new ReportQueryDTO();
        // 设置一个包含所有数据的日期范围
        Calendar cal = Calendar.getInstance();
        cal.set(2023, Calendar.JANUARY, 1);
        query.setStartDate(cal.getTime());
        cal.set(2023, Calendar.DECEMBER, 31);
        query.setEndDate(cal.getTime());
        query.setDimensionType("ALL");

        Map<String, Object> result = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectFinancialReport", query, Map.class);
        System.out.println("查询结果: " + JSON.toJSONString(result));
        System.out.println("验证查询结果是否非空: " + (result != null));
        if (result != null) {
            System.out.println("验证总收入是否为900.00: " + (new BigDecimal("900.00").compareTo(new BigDecimal(result.get("total_revenue").toString())) == 0));
            System.out.println("验证总薪资成本是否为18000.00: " + (new BigDecimal("18000.00").compareTo(new BigDecimal(result.get("total_salary_cost").toString())) == 0));
        }
    }

    @Test
    @Order(4)
    @DisplayName("selectProjectPerformanceReport - 项目绩效报表")
    void testSelectProjectPerformanceReport() {
        System.out.println("\n--- [Order 4] 测试 selectProjectPerformanceReport ---");
        ReportQueryDTO query = new ReportQueryDTO();
        query.setProjectId(1L);

        List<Map> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectProjectPerformanceReport", query, Map.class);
        System.out.println("查询结果: " + JSON.toJSONString(result));
        System.out.println("验证结果列表是否非空: " + (result != null && !result.isEmpty()));
        if (result != null && !result.isEmpty()) {
            Map<String, Object> projectReport = result.get(0);
            System.out.println("验证项目名称是否为'核心系统': " + "核心系统".equals(projectReport.get("project_name")));
            System.out.println("验证项目预算是否为500000.00: " + (new BigDecimal("500000.00").compareTo(new BigDecimal(projectReport.get("budget").toString())) == 0));
        }
    }
}
