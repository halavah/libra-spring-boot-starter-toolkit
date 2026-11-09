package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43.ObjectMapper;
import cn.myslayers.libra.framework.toolkit.demo.model.dto.EmployeeQueryDTO;
import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.Address;
import cn.myslayers.libra.framework.toolkit.demo.model.dto.common.DateRange;
import cn.myslayers.libra.framework.toolkit.demo.model.entity.Employee;
import cn.myslayers.libra.framework.toolkit.demo.model.vo.EmployeeDetailVO;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Created by halavah on 2025-07-19 09:21
 */
// 使用Junit5进行测试，并按Order注解排序
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil43.ObjectMapper 全方位测试")
public class Test01_ObjectMapperTesting {

    // ==================== 全局设置 ====================

    @BeforeAll
    static void setUp() {
        System.out.println("=== DatabaseUtil43 全方位入参、出参识别测试 ===");
        // 禁用调试日志以保持输出整洁
        DatabaseUtil43.LoggerCore.setDebugLogEnabled(false);
    }

    @AfterAll
    static void tearDown() {
        System.out.println("\n✅ 所有测试完成！");
    }

    // ==================== 测试方法组 ====================

    /**
     * 1. 基础类型测试
     */
    @Test
    @Order(1)
    @DisplayName("1. 基础类型测试")
    void testSimpleTypes() {
        System.out.println("\n--- 1. 基础类型测试 ---");

        // 字符串
        testParameterConversion("String测试", "张三", String.class);

        // 数值类型
        testParameterConversion("Integer测试", 123, Integer.class);
        testParameterConversion("Long测试", 123456789L, Long.class);
        testParameterConversion("BigDecimal测试", new java.math.BigDecimal("12345.67"), java.math.BigDecimal.class);

        // 布尔类型
        testParameterConversion("Boolean测试", true, Boolean.class);

        // 日期类型
        testParameterConversion("Date测试", new java.util.Date(), java.util.Date.class);
        testParameterConversion("LocalDateTime测试", java.time.LocalDateTime.now(), java.time.LocalDateTime.class);

        System.out.println("✅ 基础类型测试完成");
    }

    /**
     * 2. 简单对象测试
     */
    @Test
    @Order(2)
    @DisplayName("2. 简单对象测试")
    void testSimpleObjects() {
        System.out.println("\n--- 2. 简单对象测试 ---");

        // 创建简单Employee对象
        Employee employee = createSimpleEmployee();

        // 入参转换测试
        System.out.println("📥 Employee入参转换测试:");
        Map<String, Object> paramMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(employee);
        System.out.println("参数Map大小: " + paramMap.size());
        paramMap.forEach((key, value) -> System.out.println("  " + key + " = " + value + " (" + (value != null ? value.getClass().getSimpleName() : "null") + ")"));

        // 出参转换测试
        System.out.println("📤 Map到Employee出参转换测试:");
        Map<String, Object> resultMap = createEmployeeResultMap();
        Employee convertedEmployee = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(resultMap, Employee.class);
        System.out.println("转换结果: " + (convertedEmployee != null ? "成功" : "失败"));
        if (convertedEmployee != null) {
            System.out.println("  员工姓名: " + convertedEmployee.getName());
            System.out.println("  员工编号: " + convertedEmployee.getEmployeeNo());
            System.out.println("  基础薪资: " + convertedEmployee.getBaseSalary());
        }

        System.out.println("✅ 简单对象测试完成");
    }

    /**
     * 3. 复杂嵌套对象测试
     */
    @Test
    @Order(3)
    @DisplayName("3. 复杂嵌套对象测试")
    void testComplexNestedObjects() {
        System.out.println("\n--- 3. 复杂嵌套对象测试 ---");

        // 创建复杂EmployeeQueryDTO对象
        EmployeeQueryDTO queryDTO = createComplexEmployeeQueryDTO();

        // 入参转换测试
        System.out.println("📥 EmployeeQueryDTO入参转换测试:");
        Map<String, Object> paramMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(queryDTO);
        System.out.println("参数Map大小: " + paramMap.size());

        // 检查嵌套属性访问
        boolean hasNestedAccess = paramMap.containsKey("hireDateRange.startDate") ||
                                 paramMap.containsKey("salaryRange.minSalary");
        System.out.println("嵌套属性访问支持: " + (hasNestedAccess ? "✅ 支持" : "❌ 不支持"));

        // 出参转换测试 - EmployeeDetailVO
        System.out.println("📤 复杂EmployeeDetailVO出参转换测试:");
        Map<String, Object> complexResultMap = createComplexEmployeeDetailVOResultMap();
        EmployeeDetailVO detailVO = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(complexResultMap, EmployeeDetailVO.class);
        System.out.println("转换结果: " + (detailVO != null ? "成功" : "失败"));
        if (detailVO != null) {
            System.out.println("  员工姓名: " + detailVO.getName());
            System.out.println("  部门信息: " + (detailVO.getDepartment() != null ? detailVO.getDepartment().getName() : "null"));
            System.out.println("  项目数量: " + (detailVO.getProjects() != null ? detailVO.getProjects().size() : 0));
        }

        System.out.println("✅ 复杂嵌套对象测试完成");
    }

    /**
     * 4. 集合类型测试
     */
    @Test
    @Order(4)
    @DisplayName("4. 集合类型测试")
    void testCollectionTypes() {
        System.out.println("\n--- 4. 集合类型测试 ---");

        // List<String>测试
        List<String> skills = Arrays.asList("Java", "Spring", "MyBatis", "MySQL");
        System.out.println("📥 List<String>入参转换测试:");
        Map<String, Object> listParamMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(skills);
        System.out.println("参数Map: " + listParamMap);

        // List<Integer>测试
        List<Integer> departmentIds = Arrays.asList(101, 102, 103);
        System.out.println("📥 List<Integer>入参转换测试:");
        Map<String, Object> intListParamMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(departmentIds);
        System.out.println("参数Map: " + intListParamMap);

        // List<Employee>测试
        List<Employee> employees = Arrays.asList(createSimpleEmployee(), createSimpleEmployee());
        System.out.println("📥 List<Employee>入参转换测试:");
        Map<String, Object> empListParamMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(employees);
        System.out.println("参数Map大小: " + empListParamMap.size());

        // 出参List转换测试
        System.out.println("📤 List结果集转换测试:");
        List<Map<String, Object>> resultMapList = createEmployeeListResultMaps();
        List<Employee> convertedEmployees = ObjectMapper.ResultSetToObjectConverter.convertMapListToObjectList(resultMapList, Employee.class);
        System.out.println("转换结果数量: " + convertedEmployees.size());

        System.out.println("✅ 集合类型测试完成");
    }

    /**
     * 5. 泛型集合测试
     */
    @Test
    @Order(5)
    @DisplayName("5. 泛型集合测试")
    void testGenericCollections() {
        System.out.println("\n--- 5. 泛型集合测试 ---");

        // TypeReference测试
        System.out.println("📤 TypeReference泛型转换测试:");
        List<Map<String, Object>> resultMapList = createEmployeeListResultMaps();

        // 使用TypeReference转换List<Employee>
        com.fasterxml.jackson.core.type.TypeReference<List<Employee>> typeRef =
            new com.fasterxml.jackson.core.type.TypeReference<List<Employee>>() {};

        // 注意：这里需要适配单个Map转换，因为convertMapListToObjectListWithType期望的是单个Map
        for (Map<String, Object> resultMap : resultMapList) {
            Employee emp = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(resultMap, Employee.class);
            System.out.println("泛型转换结果: " + (emp != null ? emp.getName() : "null"));
        }

        System.out.println("✅ 泛型集合测试完成");
    }

    /**
     * 6. Map类型测试
     */
    @Test
    @Order(6)
    @DisplayName("6. Map类型测试")
    void testMapTypes() {
        System.out.println("\n--- 6. Map类型测试 ---");

        // 简单Map测试
        Map<String, Object> simpleMap = new HashMap<>();
        simpleMap.put("name", "张三");
        simpleMap.put("age", 30);
        simpleMap.put("salary", new java.math.BigDecimal("8000.00"));

        System.out.println("📥 简单Map入参转换测试:");
        Map<String, Object> paramMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(simpleMap);
        System.out.println("转换结果: " + paramMap);

        // 嵌套Map测试
        Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("user", simpleMap);
        Map<String, Object> deptMap = new HashMap<>();
        deptMap.put("id", 101);
        deptMap.put("name", "研发部");
        nestedMap.put("department", deptMap);

        System.out.println("📥 嵌套Map入参转换测试:");
        Map<String, Object> nestedParamMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(nestedMap);
        System.out.println("转换结果大小: " + nestedParamMap.size());

        // Map出参测试
        System.out.println("📤 Map出参转换测试:");
        Map<String, Object> resultMap = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(simpleMap, Map.class);
        System.out.println("Map转换结果: " + resultMap);

        System.out.println("✅ Map类型测试完成");
    }

    /**
     * 7. 混合复杂对象测试
     */
    @Test
    @Order(7)
    @DisplayName("7. 混合复杂对象测试")
    void testMixedComplexObjects() {
        System.out.println("\n--- 7. 混合复杂对象测试 ---");

        // 创建混合复杂对象
        Map<String, Object> mixedObject = new HashMap<>();
        mixedObject.put("employee", createSimpleEmployee());
        mixedObject.put("queryCondition", createComplexEmployeeQueryDTO());
        mixedObject.put("skills", Arrays.asList("Java", "Spring", "Vue"));
        Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("version", "1.0");
        metadataMap.put("source", "test");
        mixedObject.put("metadata", metadataMap);

        System.out.println("📥 混合复杂对象入参转换测试:");
        Map<String, Object> paramMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(mixedObject);
        System.out.println("参数Map大小: " + paramMap.size());

        // 调试：打印所有参数key
        System.out.println("所有参数key:");
        paramMap.keySet().forEach(key -> System.out.println("  - " + key));

        // 检查多种嵌套属性
        boolean hasEmployeeNested = paramMap.containsKey("employee.name") || paramMap.containsKey("employee.employeeNo");
        boolean hasQueryNested = paramMap.containsKey("queryCondition.name") || paramMap.containsKey("queryCondition.departmentIds");
        boolean hasMetadataNested = paramMap.containsKey("metadata.version") || paramMap.containsKey("metadata.source");

        System.out.println("包含employee嵌套属性: " + hasEmployeeNested);
        System.out.println("包含queryCondition嵌套属性: " + hasQueryNested);
        System.out.println("包含metadata嵌套属性: " + hasMetadataNested);
        System.out.println("整体嵌套属性支持: " + (hasEmployeeNested || hasQueryNested || hasMetadataNested));

        System.out.println("✅ 混合复杂对象测试完成");
    }

    /**
     * 8. 字段命名转换测试
     */
    @Test
    @Order(8)
    @DisplayName("8. 字段命名转换测试")
    void testFieldNamingConversion() {
        System.out.println("\n--- 8. 字段命名转换测试 ---");

        // 下划线命名的结果Map
        Map<String, Object> underscoreMap = new HashMap<>();
        underscoreMap.put("employee_no", "EMP001");
        underscoreMap.put("name", "张三");  // 修复：使用name而不是full_name
        underscoreMap.put("base_salary", new java.math.BigDecimal("8000.00"));
        underscoreMap.put("hire_date", new java.util.Date());
        underscoreMap.put("department_id", 101);

        System.out.println("📤 下划线命名转换测试:");
        Employee employee = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(underscoreMap, Employee.class);
        System.out.println("转换结果: " + (employee != null ? "成功" : "失败"));
        if (employee != null) {
            System.out.println("  员工编号(employeeNo): " + employee.getEmployeeNo());
            System.out.println("  姓名(name): " + employee.getName());
            System.out.println("  基础薪资(baseSalary): " + employee.getBaseSalary());
            System.out.println("  部门ID(departmentId): " + employee.getDepartmentId());
            System.out.println("  入职日期(hireDate): " + employee.getHireDate());
        }

        // 调试：打印字段映射缓存内容
        System.out.println("🔍 字段映射缓存状态:");
        Map<String, Integer> cacheStats = ObjectMapper.getCacheStats();
        System.out.println("缓存统计: " + cacheStats);

        // 大写命名的结果Map
        Map<String, Object> upperCaseMap = new HashMap<>();
        upperCaseMap.put("EMPLOYEE_NO", "EMP002");
        upperCaseMap.put("NAME", "李四");
        upperCaseMap.put("BASE_SALARY", new java.math.BigDecimal("9000.00"));

        System.out.println("📤 大写命名转换测试:");
        Employee employee2 = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(upperCaseMap, Employee.class);
        System.out.println("转换结果: " + (employee2 != null ? "成功" : "失败"));
        if (employee2 != null) {
            System.out.println("  员工编号: " + employee2.getEmployeeNo());
            System.out.println("  姓名: " + employee2.getName());
        }

        System.out.println("✅ 字段命名转换测试完成");
    }

    /**
     * 9. 异常情况处理测试
     */
    @Test
    @Order(9)
    @DisplayName("9. 异常情况处理测试")
    void testExceptionHandling() {
        System.out.println("\n--- 9. 异常情况处理测试 ---");

        // null值测试
        System.out.println("📥 null值入参测试:");
        Map<String, Object> nullParamMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(null);
        System.out.println("null转换结果: " + nullParamMap + " (大小: " + nullParamMap.size() + ")");

        System.out.println("📤 null值出参测试:");
        Employee nullEmployee = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(null, Employee.class);
        System.out.println("null转换结果: " + nullEmployee);

        // 空Map测试
        System.out.println("📤 空Map出参测试:");
        Employee emptyEmployee = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(new HashMap<>(), Employee.class);
        System.out.println("空Map转换结果: " + emptyEmployee);

        // 类型不匹配测试
        System.out.println("📤 类型不匹配测试:");
        Map<String, Object> wrongTypeMap = new HashMap<>();
        wrongTypeMap.put("id", "123");  // 可转换的字符串数字
        wrongTypeMap.put("name", "测试员工");
        wrongTypeMap.put("baseSalary", "8888.88");  // 可转换的字符串数字
        wrongTypeMap.put("departmentId", "999");
        Employee wrongTypeEmployee = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(wrongTypeMap, Employee.class);
        System.out.println("类型不匹配转换结果: " + (wrongTypeEmployee != null ? "成功(容错)" : "失败"));
        if (wrongTypeEmployee != null) {
            System.out.println("  转换后ID: " + wrongTypeEmployee.getId());
            System.out.println("  转换后姓名: " + wrongTypeEmployee.getName());
            System.out.println("  转换后薪资: " + wrongTypeEmployee.getBaseSalary());
        }

        System.out.println("✅ 异常情况处理测试完成");
    }

    /**
     * 10. 性能压力测试
     */
    @Test
    @Order(10)
    @DisplayName("10. 性能压力测试")
    void testPerformance() {
        System.out.println("\n--- 10. 性能压力测试 ---");

        int testCount = 1000;
        Employee testEmployee = createSimpleEmployee();
        Map<String, Object> testResultMap = createEmployeeResultMap();

        // 入参转换性能测试
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < testCount; i++) {
            ObjectMapper.ObjectToParameterConverter.convertToParameterMap(testEmployee);
        }
        long paramConvertTime = System.currentTimeMillis() - startTime;

        // 出参转换性能测试
        startTime = System.currentTimeMillis();
        for (int i = 0; i < testCount; i++) {
            ObjectMapper.ResultSetToObjectConverter.convertMapToObject(testResultMap, Employee.class);
        }
        long resultConvertTime = System.currentTimeMillis() - startTime;

        System.out.println("📊 性能测试结果:");
        System.out.println("  入参转换 " + testCount + " 次耗时: " + paramConvertTime + "ms (平均: " + (paramConvertTime * 1.0 / testCount) + "ms/次)");
        System.out.println("  出参转换 " + testCount + " 次耗时: " + resultConvertTime + "ms (平均: " + (resultConvertTime * 1.0 / testCount) + "ms/次)");

        // 缓存统计
        Map<String, Integer> cacheStats = ObjectMapper.getCacheStats();
        System.out.println("📈 缓存统计: " + cacheStats);

        System.out.println("✅ 性能压力测试完成");
    }

    // ==================== 辅助方法：创建测试数据 ====================

    private <T> void testParameterConversion(String testName, T input, Class<T> expectedType) {
        System.out.println("📥 " + testName + ":");
        Map<String, Object> paramMap = ObjectMapper.ObjectToParameterConverter.convertToParameterMap(input);
        System.out.println("  输入: " + input + " (" + input.getClass().getSimpleName() + ")");
        System.out.println("  转换: " + paramMap);

        // 逆向转换测试
        if (paramMap.size() > 0) {
            T converted = ObjectMapper.ResultSetToObjectConverter.convertMapToObject(paramMap, expectedType);
            System.out.println("  逆向: " + converted + " (成功: " + (converted != null) + ")");
        }
    }

    private Employee createSimpleEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeNo("EMP001");
        employee.setName("张三");
        employee.setEmail("zhangsan@company.com");
        employee.setPhone("13800138000");
        employee.setDepartmentId(101);
        employee.setPosition("软件工程师");
        employee.setLevel("P5");
        employee.setBaseSalary(new java.math.BigDecimal("8000.00"));
        employee.setHireDate(new java.util.Date());
        employee.setStatus("ACTIVE");
        employee.setSkills(Arrays.asList("Java", "Spring", "MyBatis"));

        // 创建嵌套地址对象
        Address address = new Address();
        address.setProvince("广东省");
        address.setCity("深圳市");
        address.setDistrict("南山区");
        address.setDetailAddress("科技园南区");
        employee.setAddress(address);

        return employee;
    }

    private EmployeeQueryDTO createComplexEmployeeQueryDTO() {
        EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();
        queryDTO.setName("张");
        queryDTO.setDepartmentIds(Arrays.asList(101, 102, 103));
        queryDTO.setPositions(Arrays.asList("工程师", "架构师"));
        queryDTO.setSkills(Arrays.asList("Java", "Spring"));

        // 创建日期范围
        DateRange dateRange = new DateRange();
        dateRange.setStartDate(new java.util.Date(System.currentTimeMillis() - 365 * 24 * 60 * 60 * 1000L)); // 一年前
        dateRange.setEndDate(new java.util.Date());
        queryDTO.setHireDateRange(dateRange);

        // 创建薪资范围
        EmployeeQueryDTO.SalaryRange salaryRange = new EmployeeQueryDTO.SalaryRange();
        salaryRange.setMinSalary(new java.math.BigDecimal("5000"));
        salaryRange.setMaxSalary(new java.math.BigDecimal("15000"));
        queryDTO.setSalaryRange(salaryRange);

        return queryDTO;
    }

    private Map<String, Object> createEmployeeResultMap() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", 1L);
        resultMap.put("employeeNo", "EMP001");
        resultMap.put("name", "张三");
        resultMap.put("email", "zhangsan@company.com");
        resultMap.put("phone", "13800138000");
        resultMap.put("departmentId", 101);
        resultMap.put("position", "软件工程师");
        resultMap.put("level", "P5");
        resultMap.put("baseSalary", new java.math.BigDecimal("8000.00"));
        resultMap.put("hireDate", new java.util.Date());
        resultMap.put("status", "ACTIVE");
        return resultMap;
    }

    private Map<String, Object> createComplexEmployeeDetailVOResultMap() {
        Map<String, Object> resultMap = createEmployeeResultMap();

        // 添加部门信息
        Map<String, Object> department = new HashMap<>();
        department.put("id", 101);
        department.put("name", "研发部");
        department.put("code", "DEV");
        department.put("location", "深圳");
        resultMap.put("department", department);

        // 添加项目信息
        List<Map<String, Object>> projects = new ArrayList<>();
        Map<String, Object> project1 = new HashMap<>();
        project1.put("id", 1001);
        project1.put("name", "电商系统");
        project1.put("status", "进行中");
        projects.add(project1);
        resultMap.put("projects", projects);

        return resultMap;
    }

    private List<Map<String, Object>> createEmployeeListResultMaps() {
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            Map<String, Object> empMap = new HashMap<>();
            empMap.put("id", (long) i);
            empMap.put("employeeNo", "EMP00" + i);
            empMap.put("name", "员工" + i);
            empMap.put("email", "emp" + i + "@company.com");
            empMap.put("baseSalary", new java.math.BigDecimal(8000 + i * 1000));
            resultList.add(empMap);
        }

        return resultList;
    }
}
