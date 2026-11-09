package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43;
import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * EmployeeMapper.xml 强类型测试类
 * 使用 ...Enhanced API 精确测试 parameterType 和 resultType
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Test12_EmployeeMapperTesting - EmployeeMapper.xml 强类型测试")
public class Test12_EmployeeMapperTesting {

    // ==================== DTO/VO/Entity 内部定义 ====================

    public static class Employee {
        private Long id;
        private String employeeNo;
        private String name;
        private String email;
        private String phone;
        private Integer departmentId;
        private String position;
        private String level;
        private BigDecimal baseSalary;
        private Date hireDate;
        private String status;
        private String skills; // 逗号分隔的字符串
        private String addressProvince;
        private String addressCity;
        private String addressDistrict;
        private String addressDetail;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getDepartmentId() { return departmentId; }
        public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public BigDecimal getBaseSalary() { return baseSalary; }
        public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getSkills() { return skills; }
        public void setSkills(String skills) { this.skills = skills; }
        public String getEmployeeNo() { return employeeNo; }
        public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Date getHireDate() { return hireDate; }
        public void setHireDate(Date hireDate) { this.hireDate = hireDate; }
        public String getAddressProvince() { return addressProvince; }
        public void setAddressProvince(String addressProvince) { this.addressProvince = addressProvince; }
        public String getAddressCity() { return addressCity; }
        public void setAddressCity(String city) { this.addressCity = city; }
        public String getAddressDistrict() { return addressDistrict; }
        public void setAddressDistrict(String addressDistrict) { this.addressDistrict = addressDistrict; }
        public String getAddressDetail() { return addressDetail; }
        public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }

    public static class EmployeeQueryDTO {
        private String name;
        private String email;
        private List<Integer> departmentIds;
        private List<String> positions;
        private List<String> levels;
        private List<String> statuses;
        private DateRange hireDateRange;
        private SalaryRange salaryRange;
        private String province;
        private String city;

        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public List<Integer> getDepartmentIds() { return departmentIds; }
        public void setDepartmentIds(List<Integer> departmentIds) { this.departmentIds = departmentIds; }
        public List<String> getPositions() { return positions; }
        public void setPositions(List<String> positions) { this.positions = positions; }
        public List<String> getLevels() { return levels; }
        public void setLevels(List<String> levels) { this.levels = levels; }
        public List<String> getStatuses() { return statuses; }
        public void setStatuses(List<String> statuses) { this.statuses = statuses; }
        public DateRange getHireDateRange() { return hireDateRange; }
        public void setHireDateRange(DateRange hireDateRange) { this.hireDateRange = hireDateRange; }
        public SalaryRange getSalaryRange() { return salaryRange; }
        public void setSalaryRange(SalaryRange salaryRange) { this.salaryRange = salaryRange; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
    }

    public static class DateRange {
        private Date startDate;
        private Date endDate;
        public Date getStartDate() { return startDate; }
        public void setStartDate(Date startDate) { this.startDate = startDate; }
        public Date getEndDate() { return endDate; }
        public void setEndDate(Date endDate) { this.endDate = endDate; }
    }

    public static class SalaryRange {
        private BigDecimal minSalary;
        private BigDecimal maxSalary;
        public BigDecimal getMinSalary() { return minSalary; }
        public void setMinSalary(BigDecimal minSalary) { this.minSalary = minSalary; }
        public BigDecimal getMaxSalary() { return maxSalary; }
        public void setMaxSalary(BigDecimal maxSalary) { this.maxSalary = maxSalary; }
    }

    public static class EmployeeListVO {
        private Long id;
        private String name;
        private String email;
        private String departmentName;
        private String position;
        private String fullAddress;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDepartmentName() { return departmentName; }
        public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getFullAddress() { return fullAddress; }
        public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

        @Override
        public String toString() {
            return JSON.toJSONString(this);
        }
    }

    public static class EmployeeCreateDTO {
        private String employeeNo;
        private String name;
        private String email;
        private String phone;
        private Integer departmentId;
        private String position;
        private String level;
        private BigDecimal baseSalary;
        private Date hireDate;
        private String status;
        private String skills;
        private Address address;

        // Getters and Setters
        public String getEmployeeNo() { return employeeNo; }
        public void setEmployeeNo(String employeeNo) { this.employeeNo = employeeNo; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public Integer getDepartmentId() { return departmentId; }
        public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public BigDecimal getBaseSalary() { return baseSalary; }
        public void setBaseSalary(BigDecimal baseSalary) { this.baseSalary = baseSalary; }
        public Date getHireDate() { return hireDate; }
        public void setHireDate(Date hireDate) { this.hireDate = hireDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getSkills() { return skills; }
        public void setSkills(String skills) { this.skills = skills; }
        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }
    }

    public static class Address {
        private String province;
        private String city;
        private String district;
        private String detailAddress;

        // Getters and Setters
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getDetailAddress() { return detailAddress; }
        public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    }

    // ==================== 测试生命周期 ====================

    @BeforeAll
    static void setUp() {
        System.out.println("=== Test12_EmployeeMapperTesting 开始测试 ===");

        // 开启日志
//        DatabaseUtil43.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil43.LoggerCore.setSqlDebugLogEnabled(true);

        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_department`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_project`;");

        // 创建表结构
        String createDeptTable = "CREATE TABLE IF NOT EXISTS t_department (id INT PRIMARY KEY, name VARCHAR(255));";
        String createEmpTable = "CREATE TABLE IF NOT EXISTS t_employee (" +
            "id BIGINT PRIMARY KEY AUTO_INCREMENT, employee_no VARCHAR(255), name VARCHAR(255), email VARCHAR(255), phone VARCHAR(255), " +
            "department_id INT, position VARCHAR(255), level VARCHAR(255), base_salary DECIMAL(10, 2), hire_date DATE, " +
            "status VARCHAR(50), skills VARCHAR(1000), address_province VARCHAR(255), address_city VARCHAR(255), " +
            "address_district VARCHAR(255), address_detail VARCHAR(255), created_time TIMESTAMP, updated_time TIMESTAMP);";

        DatabaseUtil43.executeDDL(createDeptTable);
        DatabaseUtil43.executeDDL(createEmpTable);
        System.out.println("已创建测试表结构。");
    }

    @AfterAll
    static void tearDown() {
        // 清理数据
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_department`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_employee`;");
        DatabaseUtil43.executeDML("DROP TABLE IF EXISTS `t_project`;");

        System.out.println("=== Test12_EmployeeMapperTesting 测试完成 ===");
    }

    @BeforeEach
    void prepareData() {
        DatabaseUtil43.executeDML("DELETE FROM t_employee;");
        DatabaseUtil43.executeDML("DELETE FROM t_department;");
        DatabaseUtil43.executeDML("INSERT INTO t_department (id, name) VALUES (1, '研发部'), (2, '产品部');");
        DatabaseUtil43.executeDML("INSERT INTO t_employee (id, name, email, department_id, position, level, base_salary, status, skills, address_province, address_city) VALUES " +
            "(1, '张三', 'zhangsan@test.com', 1, '后端工程师', 'P6', 20000.00, 'ACTIVE', 'Java,Spring,MySQL', '广东', '深圳')," +
            "(2, '李四', 'lisi@test.com', 1, '前端工程师', 'P6', 22000.00, 'ACTIVE', 'Vue,React,NodeJS', '广东', '广州')," +
            "(3, '王五', 'wangwu@test.com', 2, '产品经理', 'P7', 25000.00, 'INACTIVE', 'PRD,Axure', '北京', '北京');");
    }

    // ==================== 测试方法 ====================

    @Test
    @Order(1)
    @DisplayName("insertEmployee - 插入单个员工")
    void testInsertEmployee() {
        System.out.println("\n--- [Order 1] 测试 insertEmployee ---");
        EmployeeCreateDTO newEmp = new EmployeeCreateDTO();
        newEmp.setEmployeeNo("T0001");
        newEmp.setName("赵六");
        newEmp.setEmail("zhaoliu@test.com");
        newEmp.setDepartmentId(1);
        newEmp.setPosition("DevOps工程师");
        newEmp.setLevel("P5");
        newEmp.setBaseSalary(new BigDecimal("18000.00"));
        newEmp.setHireDate(new Date());
        newEmp.setSkills("Docker,Kubernetes,Jenkins");
        Address addr = new Address();
        addr.setProvince("上海");
        addr.setCity("上海");
        addr.setDistrict("浦东新区");
        addr.setDetailAddress("张江高科");
        newEmp.setAddress(addr);

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("insertEmployee", newEmp);
        System.out.println("插入影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));
    }

    @Test
    @Order(2)
    @DisplayName("selectEmployeeById - 根据ID查询员工")
    void testSelectEmployeeById() {
        System.out.println("\n--- [Order 2] 测试 selectEmployeeById ---");
        Employee emp = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectEmployeeById", 1L, Employee.class);
        System.out.println("查询结果: " + emp);
        System.out.println("验证查询结果是否非空: " + (emp != null));
        if (emp != null) {
            System.out.println("验证员工姓名是否为'张三': " + "张三".equals(emp.getName()));
        }
    }

    @Test
    @Order(3)
    @DisplayName("updateEmployee - 更新员工信息")
    void testUpdateEmployee() {
        System.out.println("\n--- [Order 3] 测试 updateEmployee ---");
        Employee empToUpdate = new Employee();
        empToUpdate.setId(1L);
        empToUpdate.setPosition("高级后端工程师");
        empToUpdate.setBaseSalary(new BigDecimal("25000.00"));

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("updateEmployee", empToUpdate);
        System.out.println("更新影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));

        Employee updatedEmp = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectEmployeeById", 1L, Employee.class);
        System.out.println("更新后查询结果: " + updatedEmp);
        if (updatedEmp != null) {
            System.out.println("验证更新后职位是否为'高级后端工程师': " + "高级后端工程师".equals(updatedEmp.getPosition()));
        }
    }

    @Test
    @Order(4)
    @DisplayName("selectEmployeesByCondition - 复杂条件查询")
    void testSelectEmployeesByCondition() {
        System.out.println("\n--- [Order 4] 测试 selectEmployeesByCondition ---");
        EmployeeQueryDTO query = new EmployeeQueryDTO();
        query.setDepartmentIds(Collections.singletonList(1));
        query.setPositions(Arrays.asList("后端工程师", "前端工程师"));
        query.setStatuses(Collections.singletonList("ACTIVE"));

        List<EmployeeListVO> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectEmployeesByCondition", query, EmployeeListVO.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证查询结果数量是否为2: " + (result != null && result.size() == 2));
    }

    @Test
    @Order(5)
    @DisplayName("selectEmployeesByDepartments - 根据部门列表查询")
    void testSelectEmployeesByDepartments() {
        System.out.println("\n--- [Order 5] 测试 selectEmployeesByDepartments ---");
        List<Employee> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("selectEmployeesByDepartments", Collections.singletonList(1), Employee.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证查询结果数量是否为2: " + (result != null && result.size() == 2));
    }

    @Test
    @Order(6)
    @DisplayName("batchUpdateEmployeeStatus - 批量更新员工状态")
    void testBatchUpdateEmployeeStatus() {
        System.out.println("\n--- [Order 6] 测试 batchUpdateEmployeeStatus ---");
        Map<String, Object> params = new HashMap<>();
        params.put("status", "RESIGNED");
        params.put("ids", Arrays.asList(1L, 2L));

        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("batchUpdateEmployeeStatus", params);
        System.out.println("批量更新影响行数: " + result);
        System.out.println("验证批量更新影响行数是否为2: " + (result == 2));
    }

    @Test
    @Order(7)
    @DisplayName("countEmployeesByDepartment - 统计各部门员工信息")
    void testCountEmployeesByDepartment() {
        System.out.println("\n--- [Order 7] 测试 countEmployeesByDepartment ---");
        List<Map> result = DatabaseUtil43.selectListFromXmlAutoByMpEnhanced("countEmployeesByDepartment", null, Map.class);
        System.out.println("查询结果: " + result);
        System.out.println("验证结果列表是否非空: " + (result != null && !result.isEmpty()));
        // 简单验证研发部的数据
        Map<String, Object> rdDept = null;
        if (result != null) {
            rdDept = result.stream().filter(m -> m.get("department_name").equals("研发部")).findFirst().orElse(null);
        }
        System.out.println("验证研发部数据是否存在: " + (rdDept != null));
        if (rdDept != null) {
            System.out.println("验证研发部员工数是否为2: " + Long.valueOf(2L).equals(rdDept.get("employee_count")));
        }
    }

    @Test
    @Order(8)
    @DisplayName("deleteEmployee - 删除员工")
    void testDeleteEmployee() {
        System.out.println("\n--- [Order 8] 测试 deleteEmployee ---");
        int result = DatabaseUtil43.executeFromXmlAutoByMpEnhanced("deleteEmployee", 3L);
        System.out.println("删除影响行数: " + result);
        System.out.println("验证影响行数是否为1: " + (result == 1));
        Employee deletedEmp = DatabaseUtil43.selectOneFromXmlAutoByMpEnhanced("selectEmployeeById", 3L, Employee.class);
        System.out.println("验证删除后查询结果为null: " + (deletedEmp == null));
    }
}
