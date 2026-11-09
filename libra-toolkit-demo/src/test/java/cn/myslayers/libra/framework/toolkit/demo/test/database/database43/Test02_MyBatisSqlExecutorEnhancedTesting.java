package cn.myslayers.libra.framework.toolkit.demo.test.database.database43;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil43.MyBatisSqlExecutorEnhanced;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

/**
 * Created by halavah on 2025-07-19 11:15
 */
// 使用Junit5进行测试，并按Order注解排序
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("MyBatisSqlExecutorEnhanced 强类型对象参数执行器测试")
public class Test02_MyBatisSqlExecutorEnhancedTesting {

    private static final String TEST_DB = "default_mysql1";

    // ==================== 全局设置 ====================

    @BeforeAll
    static void setUp() {
        System.out.println("=== MyBatisSqlExecutorEnhanced 强类型对象参数执行器测试 ===");
        // 开启调试日志查看详细执行过程
        DatabaseUtil43.LoggerCore.setDebugLogEnabled(true);
        DatabaseUtil43.LoggerCore.setSqlDebugLogEnabled(true);
    }

    @AfterAll
    static void tearDown() {
        System.out.println("\n✅ 所有 MyBatisSqlExecutorEnhanced 测试完成！");
    }

    // ==================== 测试数据类 ====================

    public static class QueryParam {
        private String name;
        private Integer age;
        private String department;
        private Boolean active;

        public QueryParam() {}

        public QueryParam(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public QueryParam(String name, Integer age, String department, Boolean active) {
            this.name = name;
            this.age = age;
            this.department = department;
            this.active = active;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        @Override
        public String toString() {
            return "QueryParam{name='" + name + "', age=" + age + ", department='" + department + "', active=" + active + "}";
        }
    }

    public static class ResultItem {
        private Integer id;
        private String name;
        private Integer age;
        private String email;
        private String department;
        private Boolean active;

        public ResultItem() {}

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        @Override
        public String toString() {
            return "ResultItem{id=" + id + ", name='" + name + "', age=" + age + 
                   ", email='" + email + "', department='" + department + "', active=" + active + "}";
        }
    }

    // ==================== API1：直接SQL测试 ====================

    @Test
    @Order(1)
    @DisplayName("测试直接SQL - selectListByMp（强类型对象参数）")
    void testDirectSqlSelectList() {
        System.out.println("\n--- 测试1：直接SQL - selectListByMp ---");
        try {
            String sql = "SELECT 1 as id, 'Alice' as name, 28 as age, 'alice@company.com' as email, 'IT' as department, true as active " +
                        "UNION ALL SELECT 2, 'Bob', 32, 'bob@company.com', 'HR', true " +
                        "UNION ALL SELECT 3, 'Charlie', 25, 'charlie@company.com', 'IT', false " +
                        "WHERE age >= #{age} AND department = #{department}";

            QueryParam param = new QueryParam("IT员工", 25, "IT", true);

            List<ResultItem> results = MyBatisSqlExecutorEnhanced.selectListByMpEnhanced(TEST_DB, sql, param, ResultItem.class);

            System.out.println("查询结果：");
            System.out.println("  结果数量: " + results.size());
            results.forEach(item -> System.out.println("    " + item));

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试直接SQL - selectOneByMp（强类型对象参数）")
    void testDirectSqlSelectOne() {
        System.out.println("\n--- 测试2：直接SQL - selectOneByMp ---");
        try {
            String sql = "SELECT 1 as id, 'John Doe' as name, 30 as age, 'john@company.com' as email, 'IT' as department, true as active";

            QueryParam param = new QueryParam("John", 30, "IT", true);

            ResultItem result = MyBatisSqlExecutorEnhanced.selectOneByMpEnhanced(TEST_DB, sql, param, ResultItem.class);

            System.out.println("查询结果：");
            if (result != null) {
                System.out.println("  " + result);
            } else {
                System.out.println("  无结果");
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(3)
    @DisplayName("测试直接SQL - executeByMp（强类型对象参数）")
    void testDirectSqlExecute() {
        System.out.println("\n--- 测试3：直接SQL - executeByMp ---");
        try {
            // 模拟插入操作（实际不会真的插入，只是测试SQL解析和参数绑定）
            String sql = "SELECT 1 as affected_rows WHERE #{name} IS NOT NULL AND #{age} > 0";

            QueryParam param = new QueryParam("新员工", 25, "IT", true);

            int affectedRows = MyBatisSqlExecutorEnhanced.executeByMpEnhanced(TEST_DB, sql, param);

            System.out.println("执行结果：");
            System.out.println("  影响行数: " + affectedRows);

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== API2：指定XML文件测试 ====================

    @Test
    @Order(4)
    @DisplayName("测试指定XML文件 - selectListFromXmlByMp")
    void testXmlFileSelectList() {
        System.out.println("\n--- 测试4：指定XML文件 - selectListFromXmlByMp ---");
        try {
            QueryParam param = new QueryParam("测试员工", 25, "IT", true);

            // 测试XML文件（会因为文件不存在或SQL ID不存在而失败，但可以验证方法调用）
            try {
                List<ResultItem> results = MyBatisSqlExecutorEnhanced.selectListFromXmlByMpEnhanced(
                    TEST_DB, "EmployeeMapper.xml", "findEmployees", param, ResultItem.class);
                
                System.out.println("XML文件查询成功（如果XML和SQL ID存在）");
                System.out.println("  结果数量: " + results.size());
                
            } catch (RuntimeException e) {
                System.out.println("预期错误（XML文件或SQL ID不存在）: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(5)
    @DisplayName("测试指定XML文件 - selectOneFromXmlByMp")
    void testXmlFileSelectOne() {
        System.out.println("\n--- 测试5：指定XML文件 - selectOneFromXmlByMp ---");
        try {
            QueryParam param = new QueryParam("单个员工", 30, "HR", true);

            try {
                ResultItem result = MyBatisSqlExecutorEnhanced.selectOneFromXmlByMpEnhanced(
                    TEST_DB, "EmployeeMapper.xml", "findEmployeeById", param, ResultItem.class);
                
                System.out.println("XML文件单条查询成功");
                System.out.println("  结果: " + result);
                
            } catch (RuntimeException e) {
                System.out.println("预期错误（XML文件或SQL ID不存在）: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(6)
    @DisplayName("测试指定XML文件 - executeFromXmlByMp")
    void testXmlFileExecute() {
        System.out.println("\n--- 测试6：指定XML文件 - executeFromXmlByMp ---");
        try {
            QueryParam param = new QueryParam("更新员工", 30, "IT", true);

            try {
                int affectedRows = MyBatisSqlExecutorEnhanced.executeFromXmlByMpEnhanced(
                    TEST_DB, "EmployeeMapper.xml", "updateEmployee", param);
                
                System.out.println("XML文件执行成功");
                System.out.println("  影响行数: " + affectedRows);
                
            } catch (RuntimeException e) {
                System.out.println("预期错误（XML文件或SQL ID不存在）: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== API3：自动搜索XML测试 ====================

    @Test
    @Order(7)
    @DisplayName("测试自动搜索XML - selectListFromXmlAutoByMp")
    void testXmlAutoSelectList() {
        System.out.println("\n--- 测试7：自动搜索XML - selectListFromXmlAutoByMp ---");
        try {
            QueryParam param = new QueryParam("搜索员工", 25, "IT", true);

            try {
                List<ResultItem> results = MyBatisSqlExecutorEnhanced.selectListFromXmlAutoByMpEnhanced(
                    TEST_DB, "findAllEmployees", param, ResultItem.class);
                
                System.out.println("自动搜索XML查询成功");
                System.out.println("  结果数量: " + results.size());
                
            } catch (RuntimeException e) {
                System.out.println("预期错误（SQL ID不存在）: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(8)
    @DisplayName("测试自动搜索XML - selectOneFromXmlAutoByMp")
    void testXmlAutoSelectOne() {
        System.out.println("\n--- 测试8：自动搜索XML - selectOneFromXmlAutoByMp ---");
        try {
            QueryParam param = new QueryParam("单个员工", 30, "HR", true);

            try {
                ResultItem result = MyBatisSqlExecutorEnhanced.selectOneFromXmlAutoByMpEnhanced(
                    TEST_DB, "findEmployeeByName", param, ResultItem.class);
                
                System.out.println("自动搜索XML单条查询成功");
                System.out.println("  结果: " + result);
                
            } catch (RuntimeException e) {
                System.out.println("预期错误（SQL ID不存在）: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(9)
    @DisplayName("测试自动搜索XML - executeFromXmlAutoByMp")
    void testXmlAutoExecute() {
        System.out.println("\n--- 测试9：自动搜索XML - executeFromXmlAutoByMp ---");
        try {
            QueryParam param = new QueryParam("执行操作", 25, "IT", true);

            try {
                int affectedRows = MyBatisSqlExecutorEnhanced.executeFromXmlAutoByMpEnhanced(
                    TEST_DB, "insertEmployee", param);
                
                System.out.println("自动搜索XML执行成功");
                System.out.println("  影响行数: " + affectedRows);
                
            } catch (RuntimeException e) {
                System.out.println("预期错误（SQL ID不存在）: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 静态方法测试 ====================

    @Test
    @Order(10)
    @DisplayName("测试静态方法调用（通过 DatabaseUtil43）")
    void testStaticMethods() {
        System.out.println("\n--- 测试10：静态方法调用 ---");
        try {
            String sql = "SELECT 1 as id, 'Static Test' as name, 28 as age, 'static@test.com' as email, 'TEST' as department, true as active";
            QueryParam param = new QueryParam("静态测试", 28, "TEST", true);

            // 测试静态方法版本
            List<ResultItem> results = DatabaseUtil43.selectListByMpEnhanced(sql, param, ResultItem.class);
            System.out.println("静态方法查询结果数量: " + results.size());

            ResultItem singleResult = DatabaseUtil43.selectOneByMpEnhanced(sql, param, ResultItem.class);
            System.out.println("静态方法单条查询结果: " + singleResult);

            int affectedRows = DatabaseUtil43.executeByMpEnhanced("SELECT 1 WHERE #{name} IS NOT NULL", param);
            System.out.println("静态方法执行结果: " + affectedRows);

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 链式访问器测试 ====================

    @Test
    @Order(11)
    @DisplayName("测试链式访问器调用")
    void testAccessorMethods() {
        System.out.println("\n--- 测试11：链式访问器调用 ---");
        try {
            String sql = "SELECT 1 as id, 'Accessor Test' as name, 30 as age, 'accessor@test.com' as email, 'CHAIN' as department, true as active";
            QueryParam param = new QueryParam("链式测试", 30, "CHAIN", true);

            // 测试链式访问器
            List<ResultItem> results = DatabaseUtil43.accessor()
                .selectListByMpEnhanced(sql, param, ResultItem.class);
            System.out.println("链式访问器查询结果数量: " + results.size());

            ResultItem singleResult = DatabaseUtil43.accessor()
                .selectOneByMpEnhanced(sql, param, ResultItem.class);
            System.out.println("链式访问器单条查询结果: " + singleResult);

            DatabaseUtil43.accessor()
                .executeByMpEnhanced("SELECT 1 WHERE #{name} IS NOT NULL", param)
                .result();
            System.out.println("链式访问器执行完成");

        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== 异常和边界测试 ====================

    @Test
    @Order(12)
    @DisplayName("测试异常和边界情况")
    void testExceptionsAndEdgeCases() {
        System.out.println("\n--- 测试12：异常和边界情况 ---");
        
        // 测试null参数
        try {
            String sql = "SELECT 1 as id, 'Null Test' as name, 25 as age, 'null@test.com' as email";
            List<ResultItem> results = MyBatisSqlExecutorEnhanced.selectListByMpEnhanced(TEST_DB, sql, null, ResultItem.class);
            System.out.println("null参数测试成功，结果数量: " + results.size());
        } catch (Exception e) {
            System.out.println("null参数测试失败: " + e.getMessage());
        }

        // 测试无效SQL
        try {
            String invalidSql = "INVALID SQL STATEMENT #{name}";
            QueryParam param = new QueryParam("测试", 25);
            List<ResultItem> results = MyBatisSqlExecutorEnhanced.selectListByMpEnhanced(TEST_DB, invalidSql, param, ResultItem.class);
            System.out.println("无效SQL测试：不应该到达这里");
        } catch (Exception e) {
            System.out.println("无效SQL测试预期失败: " + e.getMessage());
        }

        // 测试无效数据库
        try {
            String sql = "SELECT 1 as id, 'Invalid DB Test' as name";
            QueryParam param = new QueryParam("测试", 25);
            List<ResultItem> results = MyBatisSqlExecutorEnhanced.selectListByMpEnhanced("invalid_db", sql, param, ResultItem.class);
            System.out.println("无效数据库测试：不应该到达这里");
        } catch (Exception e) {
            System.out.println("无效数据库测试预期失败: " + e.getMessage());
        }

        // 测试复杂对象参数
        try {
            String sql = "SELECT 1 as id, 'Complex Test' as name, #{age} as age, 'complex@test.com' as email WHERE #{active} = true";
            QueryParam complexParam = new QueryParam("复杂对象", 35, "复杂部门", true);
            List<ResultItem> results = MyBatisSqlExecutorEnhanced.selectListByMpEnhanced(TEST_DB, sql, complexParam, ResultItem.class);
            System.out.println("复杂对象参数测试成功，结果数量: " + results.size());
        } catch (Exception e) {
            System.out.println("复杂对象参数测试失败: " + e.getMessage());
        }
    }
} 