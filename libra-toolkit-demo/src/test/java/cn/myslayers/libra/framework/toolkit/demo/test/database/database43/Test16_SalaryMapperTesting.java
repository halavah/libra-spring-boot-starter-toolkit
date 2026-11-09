package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test16_SalaryMapperTesting - SalaryMapper.xml 强类型测试")
public class Test16_SalaryMapperTesting {
    
    // ==================== DTO/VO/Entity 内部定义 ====================

    public static class Salary {
        private Long id;
        private Long employeeId;
        private String period;
        private BigDecimal baseSalary;
        private BigDecimal performanceBonus;
        private BigDecimal allowance;
        private BigDecimal deduction;
        private BigDecimal overtime;
        private BigDecimal totalAmount;
        private BigDecimal taxAmount;
        private BigDecimal actualAmount;
        private Date payDate;
        private String status;
        private String remarks;

        // Getters & Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }
        public BigDecimal getBaseSalary() { return baseSalary; }
        public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
        public BigDecimal getPerformanceBonus() { return performanceBonus; }
        public void setPerformanceBonus(BigDecimal performanceBonus) { this.performanceBonus = performanceBonus; }
        public BigDecimal getAllowance() { return allowance; }
        public void setAllowance(BigDecimal allowance) { this.allowance = allowance; }
        public BigDecimal getDeduction() { return deduction; }
        public void setDeduction(BigDecimal deduction) { this.deduction = deduction; }
        public BigDecimal getOvertime() { return overtime; }
        public void setOvertime(BigDecimal overtime) { this.overtime = overtime; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }
        public BigDecimal getActualAmount() { return actualAmount; }
        public void setActualAmount(BigDecimal actualAmount) { this.actualAmount = actualAmount; }
        public Date getPayDate() { return payDate; }
        public void setPayDate(Date payDate) { this.payDate = payDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }

        @Override
        public String toString() { return JSON.toJSONString(this); }
    }

    public static class SalaryCalculateDTO {
        private Long employeeId;
        private Date startDate;
        private Date endDate;
        private String status;

        // Getters & Setters
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class SalaryReportVO extends Salary {
        private String employeeName;
        private String employeeNo;
        private String departmentName;

        // Getters & Setters
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        public String getEmployeeNo() { return employeeNo; }
        public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    }

    // ==================== 测试生命周期 ====================

    @BeforeAll
    static void setUp() {
        System.out.println("=== Test16_SalaryMapperTesting 开始测试 ===");

        // 开启日志
//        DatabaseUtil43.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil43.LoggerCore.setSqlDebugLogEnabled(true);

        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `salary`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `department`;");

        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS salary (salary_id BIGINT PRIMARY KEY AUTO_INCREMENT, employee_id BIGINT, salary_period VARCHAR(255), base_salary DECIMAL(15,2), overtime_pay DECIMAL(15,2), bonus DECIMAL(15,2), allowance DECIMAL(15,2), deduction DECIMAL(15,2), total_amount DECIMAL(15,2), tax_amount DECIMAL(15,2), net_salary DECIMAL(15,2), pay_date DATE, status VARCHAR(50), remarks VARCHAR(1000), created_time TIMESTAMP, updated_time TIMESTAMP, commission DECIMAL(15,2), social_insurance DECIMAL(15,2), tax DECIMAL(15,2), pay_period_start DATE, pay_period_end DATE);");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS employee (employee_id BIGINT PRIMARY KEY, employee_name VARCHAR(255), employee_no VARCHAR(255), department_id BIGINT);");
        DatabaseUtil43.executeDDL("CREATE TABLE IF NOT EXISTS department (department_id BIGINT PRIMARY KEY, department_name VARCHAR(255));");
        System.out.println("已创建测试表结构。");
    }

    @AfterAll
    static void tearDown() {
        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `salary`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `department`;");

        System.out.println("=== Test16_SalaryMapperTesting 测试完成 ===");
    }

    @BeforeEach
    void prepareData() {
        DatabaseUtil43.executeDML("DELETE FROM salary;");
        DatabaseUtil43.executeDML("DELETE FROM employee;");
        DatabaseUtil43.executeDML("DELETE FROM department;");

        DatabaseUtil43.executeDML("INSERT INTO department (department_id, department_name) VALUES (1, '研发部'), (2, '市场部');");
        DatabaseUtil43.executeDML("INSERT INTO employee (employee_id, employee_name, department_id) VALUES (101, '张三', 1), (102, '李四', 1), (201, '王五', 2);");
        DatabaseUtil43.executeDML("INSERT INTO salary (salary_id, employee_id, net_salary, pay_date, status, bonus) VALUES (1, 101, 18000.00, '2023-10-15', 'PAID', 2000.00);");
        DatabaseUtil43.executeDML("INSERT INTO salary (salary_id, employee_id, net_salary, pay_date, status, bonus) VALUES (2, 101, 18500.00, '2023-09-15', 'PAID', 2500.00);");
        DatabaseUtil43.executeDML("INSERT INTO salary (salary_id, employee_id, net_salary, pay_date, status, bonus) VALUES (3, 102, 15000.00, '2023-10-15', 'PAID', 1000.00);");
        DatabaseUtil43.executeDML("INSERT INTO salary (salary_id, employee_id, net_salary, pay_date, status, bonus) VALUES (4, 201, 12000.00, '2023-10-15', 'DRAFT', 500.00);");
    }

    // ==================== 测试方法 ====================

    @Test
    @Order(1)
    @DisplayName("insertSalary - 创建薪资记录")
    void testInsertSalary() {
        System.out.println("\n--- [Order 1] 测试 insertSalary ---");
        Salary newSalary = new Salary();
        newSalary.setEmployeeId(201L);
        newSalary.setPeriod("2023-11");
        newSalary.setBaseSalary(new BigDecimal("10000.00"));
        newSalary.setPerformanceBonus(new BigDecimal("1500.00"));
        newSalary.setActualAmount(new BigDecimal("11500.00"));
        newSalary.setPayDate(new Date());
        newSalary.setStatus("DRAFT");

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("insertSalary", newSalary);
        System.out.println("插入影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));
    }

    @Test
    @Order(2)
    @DisplayName("selectSalaryById - 根据ID查询薪资")
    void testSelectSalaryById() {
        System.out.println("\n--- [Order 2] 测试 selectSalaryById ---");
        Map<String, Object> params = new HashMap<>();
        params.put("salaryId", 1L);
        Salary salary = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectSalaryById", params, Salary.class);
        System.out.println("查询结果: " + salary);
        System.out.println("验证查询结果是否非空: " + (salary != null));
        if (salary != null) {
            System.out.println("验证员工ID是否为101: " + Long.valueOf(101L).equals(salary.getEmployeeId()));
        }
    }

    @Test
    @Order(3)
    @DisplayName("selectSalaryHistory - 查询员工薪资历史")
    void testSelectSalaryHistory() {
        System.out.println("\n--- [Order 3] 测试 selectSalaryHistory ---");
        SalaryCalculateDTO query = new SalaryCalculateDTO();
        query.setEmployeeId(101L);
        query.setStatus("PAID");

        List<SalaryReportVO> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectSalaryHistory", query, SalaryReportVO.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证查询结果数量是否为2: " + (result != null && result.size() == 2));
        if (result != null && !result.isEmpty()) {
            System.out.println("验证员工姓名是否为'张三': " + "张三".equals(result.get(0).getEmployeeName()));
        }
    }

    @Test
    @Order(4)
    @DisplayName("updateSalary - 更新薪资状态")
    void testUpdateSalary() {
        System.out.println("\n--- [Order 4] 测试 updateSalary ---");
        Map<String, Object> params = new HashMap<>();
        params.put("salaryId", 4L);
        params.put("status", "PAID");

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("updateSalary", params);
        System.out.println("更新影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("salaryId", 4L);
        Salary updatedSalary = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectSalaryById", queryParams, Salary.class);
        System.out.println("更新后查询结果: " + updatedSalary);
        if (updatedSalary != null) {
            System.out.println("验证更新后状态是否为'PAID': " + "PAID".equals(updatedSalary.getStatus()));
        }
    }

    @Test
    @Order(5)
    @DisplayName("getDepartmentSalaryComparison - 部门薪资对比")
    void testGetDepartmentSalaryComparison() {
        System.out.println("\n--- [Order 5] 测试 getDepartmentSalaryComparison ---");
        Map<String, Object> params = new HashMap<>();
        params.put("status", "PAID");

        List<Map> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("getDepartmentSalaryComparison", params, Map.class);
        System.out.println("查询结果: " + JSON.toJSONString(result));
        System.out.println("验证结果数量是否为1: " + (result != null && result.size() == 1));
        if (result != null && !result.isEmpty()) {
            Map<String, Object> deptSalary = result.get(0);
            System.out.println("验证部门名称是否为'研发部': " + "研发部".equals(deptSalary.get("department_name")));
            System.out.println("验证薪资记录数是否为3: " + Long.valueOf(3L).equals(deptSalary.get("salary_records")));
        }
    }

    @Test
    @Order(6)
    @DisplayName("deleteSalary - 删除薪资记录")
    void testDeleteSalary() {
        System.out.println("\n--- [Order 6] 测试 deleteSalary ---");
        Map<String, Object> params = new HashMap<>();
        params.put("salaryId", 4L);
        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("deleteSalary", params);
        System.out.println("删除影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("salaryId", 4L);
        Salary deletedSalary = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectSalaryById", queryParams, Salary.class);
        System.out.println("验证删除后查询结果是否为null: " + (deletedSalary == null));
    }
}
