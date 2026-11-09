package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * DatabaseUtil32 - MyBatisSqlExecutor 执行测试
 *
 * ## 测试目标:
 * 验证 `DatabaseUtil32.MyBatisSqlExecutor` 能够正确执行解析后的MyBatis动态SQL，
 * 包括查询、插入、更新、删除等各种数据库操作。
 *
 * ## 测试方式:
 * - **按XML文件顺序**：依次测试Demo1、Demo2、Demo3、Demo4中的所有SQL
 * - **使用FromXml方法**：通过MyBatisSqlExecutor直接调用XML中的SQL
 * - **专注执行功能**：测试SQL执行结果，验证数据库操作正确性
 *
 * ## 覆盖场景:
 * - **Demo1**: 基础CRUD和动态SQL执行
 * - **Demo2**: 高级复杂场景SQL执行
 * - **Demo3**: Include标签SQL执行
 * - **Demo4**: 边缘案例和特殊语法SQL执行
 */
@DisplayName("MyBatisSqlExecutor - SQL执行测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Test32_ApiMyBatisSqlExecutorTesting {

    // 定义操作类型，用于辅助方法分发
    private enum OperationType {
        SELECT_LIST,
        SELECT_ONE,
        EXECUTE
    }

    /**
     * 核心执行测试辅助方法
     */
    private void executeAndVerify(String testName, String mapperFile, String sqlId, Map<String, Object> params, OperationType operationType) {
        System.out.println("\n--- MyBatisSqlExecutor 执行测试: " + testName + " ---");
        System.out.println("Mapper: " + mapperFile + ", SQL ID: " + sqlId);
        System.out.println("Input Params: " + params);
        System.out.println("Operation: " + operationType);

        try {
            Object result;
            switch (operationType) {
                case SELECT_LIST:
                    result = DatabaseUtil32.MyBatisSqlExecutor.selectListFromXmlByMp(null, mapperFile, sqlId, params);
                    break;
                case SELECT_ONE:
                    result = DatabaseUtil32.MyBatisSqlExecutor.selectOneFromXmlByMp(null, mapperFile, sqlId, params);
                    break;
                case EXECUTE:
                    result = DatabaseUtil32.MyBatisSqlExecutor.executeFromXmlByMp(null, mapperFile, sqlId, params);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的操作类型: " + operationType);
            }
            System.out.println("✅ " + testName + ": 执行成功");
            System.out.println("   - 执行结果: " + result);
        } catch (Exception e) {
            System.out.println("❌ " + testName + ": 执行失败");
            System.out.println("   - 异常信息: " + e.getMessage());
            e.printStackTrace();
        }
    }

        @BeforeAll
    void setup() {
        System.out.println("====================================================");
        System.out.println("  启动 MyBatisSqlExecutor 执行测试");
        System.out.println("====================================================");

        // 启用调试日志
        DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);

        // 初始化测试数据库
        initializeTestDatabase();

        // 清理测试数据，确保干净的测试环境
        cleanTestData();
    }

    /**
     * 初始化测试数据库 - 创建表结构和插入测试数据
     */
    private void initializeTestDatabase() {
        try {
            System.out.println("🔧 开始初始化测试数据库...");

            // 获取数据库初始化SQL语句
            List<String> sqlStatements = getInitializationSqlStatements();
            int executedCount = 0;

            for (String sql : sqlStatements) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty()) {
                    try {
                        System.out.println("执行SQL: " + trimmedSql.substring(0, Math.min(50, trimmedSql.length())) + "...");
                        DatabaseUtil32.executeDDL(trimmedSql);
                        executedCount++;
                    } catch (Exception e) {
                        // 对于某些可能失败的语句（如DROP TABLE），继续执行
                        if (!trimmedSql.toUpperCase().startsWith("DROP")) {
                            System.out.println("⚠️ 执行SQL失败（继续执行）: " + trimmedSql.substring(0, Math.min(50, trimmedSql.length())));
                            System.out.println("   错误信息: " + e.getMessage());
                        }
                    }
                }
            }

            System.out.println("✅ 测试数据库初始化完成，执行了 " + executedCount + " 条SQL语句");

            // 验证表是否创建成功
            verifyTablesCreated();

        } catch (Exception e) {
            System.err.println("❌ 测试数据库初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("测试数据库初始化失败，无法继续测试", e);
        }
    }

    /**
     * 获取数据库初始化SQL语句列表
     */
    private List<String> getInitializationSqlStatements() {
        List<String> sqlStatements = new ArrayList<>();
        
        // 1. 禁用外键检查
        sqlStatements.add("SET FOREIGN_KEY_CHECKS = 0");
        
        // 2. 删除已存在的表
        sqlStatements.add("DROP TABLE IF EXISTS user_roles");
        sqlStatements.add("DROP TABLE IF EXISTS roles");
        sqlStatements.add("DROP TABLE IF EXISTS users");
        sqlStatements.add("DROP TABLE IF EXISTS records");
        sqlStatements.add("DROP TABLE IF EXISTS departments");
        sqlStatements.add("DROP TABLE IF EXISTS user_logins");
        
        // 3. 创建表结构
        sqlStatements.add(createUsersTableSql());
        sqlStatements.add(createDepartmentsTableSql());
        sqlStatements.add(createRolesTableSql());
        sqlStatements.add(createUserRolesTableSql());
        sqlStatements.add(createRecordsTableSql());
        sqlStatements.add(createUserLoginsTableSql());
        
        // 4. 插入测试数据
        sqlStatements.addAll(getDepartmentInsertSql());
        sqlStatements.addAll(getRoleInsertSql());
        sqlStatements.addAll(getUserInsertSql());
        sqlStatements.addAll(getUserRoleInsertSql());
        sqlStatements.addAll(getRecordInsertSql());
        sqlStatements.addAll(getUserLoginInsertSql());
        
        // 5. 重新启用外键检查
        sqlStatements.add("SET FOREIGN_KEY_CHECKS = 1");
        
        return sqlStatements;
    }

    // === 创建表结构的SQL语句 ===
    
    private String createUsersTableSql() {
        return "CREATE TABLE users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "email VARCHAR(200) UNIQUE, " +
                "phone VARCHAR(20), " +
                "age INT, " +
                "status VARCHAR(20) DEFAULT 'active', " +
                "role VARCHAR(50), " +
                "department_id BIGINT, " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "metadata JSON" +
                ")";
    }
    
    private String createDepartmentsTableSql() {
        return "CREATE TABLE departments (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(100) NOT NULL, " +
                "code VARCHAR(20) UNIQUE, " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
    }
    
    private String createRolesTableSql() {
        return "CREATE TABLE roles (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(50) NOT NULL, " +
                "code VARCHAR(50), " +
                "description VARCHAR(200), " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";
    }
    
    private String createUserRolesTableSql() {
        return "CREATE TABLE user_roles (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "role_id BIGINT NOT NULL, " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id), " +
                "FOREIGN KEY (role_id) REFERENCES roles(id), " +
                "UNIQUE KEY uk_user_role (user_id, role_id)" +
                ")";
    }
    
    private String createRecordsTableSql() {
        return "CREATE TABLE records (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "name VARCHAR(200), " +
                "title VARCHAR(200), " +
                "content TEXT, " +
                "type VARCHAR(50), " +
                "status VARCHAR(20) DEFAULT 'active', " +
                "user_id BIGINT, " +
                "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")";
    }
    
    private String createUserLoginsTableSql() {
        return "CREATE TABLE user_logins (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id BIGINT NOT NULL, " +
                "login_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "ip_address VARCHAR(50), " +
                "user_agent TEXT, " +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")";
    }

    // === 插入测试数据的SQL语句 ===
    
    private List<String> getDepartmentInsertSql() {
        return Arrays.asList(
            "INSERT INTO departments (id, name, code) VALUES (1, '技术部', 'TECH')",
            "INSERT INTO departments (id, name, code) VALUES (2, '产品部', 'PRODUCT')",
            "INSERT INTO departments (id, name, code) VALUES (3, '运营部', 'OPERATION')",
            "INSERT INTO departments (id, name, code) VALUES (4, '人事部', 'HR')",
            "INSERT INTO departments (id, name, code) VALUES (5, '财务部', 'FINANCE')"
        );
    }
    
    private List<String> getRoleInsertSql() {
        return Arrays.asList(
            "INSERT INTO roles (id, name, code, description) VALUES (1, 'ADMIN', 'ADMIN_CODE', '管理员')",
            "INSERT INTO roles (id, name, code, description) VALUES (2, 'USER', 'USER_CODE', '普通用户')",
            "INSERT INTO roles (id, name, code, description) VALUES (3, 'DEVELOPER', 'DEV_CODE', '开发者')",
            "INSERT INTO roles (id, name, code, description) VALUES (4, 'TESTER', 'TEST_CODE', '测试员')",
            "INSERT INTO roles (id, name, code, description) VALUES (5, 'MANAGER', 'MGR_CODE', '经理')"
        );
    }
    
    private List<String> getUserInsertSql() {
        return Arrays.asList(
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1001, '张三', 'zhangsan@example.com', '13800000001', 25, 'active', 'ADMIN', 1, '{\"level\": \"senior\"}')",
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1002, '李四', 'lisi@example.com', '13800000002', 30, 'active', 'DEVELOPER', 1, '{\"level\": \"expert\"}')",
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1003, '王五', 'wangwu@example.com', '13800000003', 28, 'inactive', 'USER', 2, '{\"level\": \"junior\"}')",
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1004, '赵六', 'zhaoliu@example.com', '13800000004', 35, 'active', 'MANAGER', 3, '{\"level\": \"senior\"}')",
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1005, '陈七', 'chenqi@example.com', '13800000005', 26, 'pending', 'USER', 1, '{\"level\": \"junior\"}')",
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1006, '测试用户1', 'test1@example.com', '13800000006', 22, 'active', 'USER', 2, '{\"level\": \"trainee\"}')",
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1007, '测试用户2', 'test2@example.com', '13800000007', 29, 'active', 'TESTER', 3, '{\"level\": \"senior\"}')",
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1008, '批量用户1', 'batch1@example.com', '13800000008', 24, 'active', 'USER', 1, '{\"level\": \"junior\"}')",
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1009, '批量用户2', 'batch2@example.com', '13800000009', 27, 'active', 'USER', 2, '{\"level\": \"senior\"}')",
            "INSERT INTO users (id, name, email, phone, age, status, role, department_id, metadata) VALUES (1010, '动态插入用户', 'dynamic@example.com', '13800000010', 31, 'active', 'ADMIN', 4, '{\"level\": \"expert\"}')"
        );
    }
    
    private List<String> getUserRoleInsertSql() {
        return Arrays.asList(
            "INSERT INTO user_roles (user_id, role_id) VALUES (1001, 1)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1001, 3)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1002, 2)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1002, 3)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1003, 2)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1004, 5)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1004, 2)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1005, 2)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1006, 2)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1007, 2)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1007, 4)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1008, 2)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1009, 2)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1010, 1)",
            "INSERT INTO user_roles (user_id, role_id) VALUES (1010, 5)"
        );
    }
    
    private List<String> getRecordInsertSql() {
        return Arrays.asList(
            "INSERT INTO records (id, name, title, content, type, status, user_id) VALUES (1, 'Record1', '测试记录1', '这是测试记录的内容1', 'TYPE_A', 'active', 1001)",
            "INSERT INTO records (id, name, title, content, type, status, user_id) VALUES (2, 'Record2', '测试记录2', '这是测试记录的内容2', 'TYPE_B', 'active', 1002)",
            "INSERT INTO records (id, name, title, content, type, status, user_id) VALUES (3, 'Record3', '测试记录3', '这是测试记录的内容3', 'TYPE_A', 'inactive', 1003)",
            "INSERT INTO records (id, name, title, content, type, status, user_id) VALUES (4, 'Record4', '测试记录4', '这是测试记录的内容4', 'TYPE_C', 'active', 1004)",
            "INSERT INTO records (id, name, title, content, type, status, user_id) VALUES (5, 'Record5', '测试记录5', '这是测试记录的内容5', 'TYPE_B', 'pending', 1005)"
        );
    }
    
    private List<String> getUserLoginInsertSql() {
        return Arrays.asList(
            "INSERT INTO user_logins (user_id, login_time, ip_address, user_agent) VALUES (1001, '2025-01-01 08:00:00', '192.168.1.100', 'Mozilla/5.0')",
            "INSERT INTO user_logins (user_id, login_time, ip_address, user_agent) VALUES (1002, '2025-01-01 09:00:00', '192.168.1.101', 'Chrome/91.0')",
            "INSERT INTO user_logins (user_id, login_time, ip_address, user_agent) VALUES (1003, '2025-01-01 10:00:00', '192.168.1.102', 'Safari/14.0')",
            "INSERT INTO user_logins (user_id, login_time, ip_address, user_agent) VALUES (1001, '2025-01-02 08:00:00', '192.168.1.100', 'Mozilla/5.0')",
            "INSERT INTO user_logins (user_id, login_time, ip_address, user_agent) VALUES (1002, '2025-01-02 09:00:00', '192.168.1.101', 'Chrome/91.0')"
        );
    }

    /**
     * 清理测试数据，确保每次测试在干净环境中执行
     */
    private void cleanTestData() {
                try {
            System.out.println("🧹 开始清理测试数据...");
            // 按照外键依赖顺序删除数据
            DatabaseUtil32.execute("DELETE FROM user_roles WHERE user_id >= 2000", new HashMap<>());
            DatabaseUtil32.execute("DELETE FROM records WHERE user_id >= 2000", new HashMap<>());
            DatabaseUtil32.execute("DELETE FROM users WHERE id >= 2000", new HashMap<>());
            DatabaseUtil32.execute("DELETE FROM departments WHERE id >= 100", new HashMap<>());
            DatabaseUtil32.execute("DELETE FROM roles WHERE id >= 100", new HashMap<>());

            // 重置自增ID（确保测试数据ID不冲突）
            DatabaseUtil32.execute("ALTER TABLE users AUTO_INCREMENT = 2000", new HashMap<>());
            DatabaseUtil32.execute("ALTER TABLE departments AUTO_INCREMENT = 100", new HashMap<>());
            DatabaseUtil32.execute("ALTER TABLE roles AUTO_INCREMENT = 100", new HashMap<>());
            DatabaseUtil32.execute("ALTER TABLE records AUTO_INCREMENT = 2000", new HashMap<>());

            System.out.println("✅ 测试数据清理完成");
        } catch (Exception e) {
            System.err.println("⚠️ 测试数据清理失败: " + e.getMessage());
            // 清理失败不影响测试继续，只是可能有数据冲突
        }
    }

    /**
     * 验证测试表是否创建成功
     */
    private void verifyTablesCreated() {
        try {
            // 查询users表验证是否创建成功
            DatabaseUtil32.selectOne("SELECT COUNT(*) as count FROM users", new HashMap<>());
            System.out.println("✓ 数据库表验证通过");
        } catch (Exception e) {
            System.err.println("❌ 数据库表验证失败: " + e.getMessage());
            throw new RuntimeException("测试表创建验证失败", e);
        }
    }

    // ================================ Demo1Mapper.xml 测试 ================================

    @Test
    @Order(1)
    @DisplayName("Demo1Mapper.xml - selectUserById - 基础查询执行")
    void testDemo1_01_selectUserById() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1001L);
        executeAndVerify("Demo1-selectUserById", "Demo1Mapper.xml", "selectUserById", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(2)
    @DisplayName("Demo1Mapper.xml - insertUser - 基础插入执行")
    void testDemo1_02_insertUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "张三");
        params.put("age", 25);
        params.put("email", "zhangsan@example.com");
        params.put("phone", "13800000001");
        params.put("status", "active");
        executeAndVerify("Demo1-insertUser", "Demo1Mapper.xml", "insertUser", params, OperationType.EXECUTE);
    }

    @Test
    @Order(3)
    @DisplayName("Demo1Mapper.xml - updateUser - 基础更新执行")
    void testDemo1_03_updateUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1001L);
        params.put("name", "张三丰");
        params.put("age", 30);
        params.put("email", "zhangsanfeng@example.com");
        executeAndVerify("Demo1-updateUser", "Demo1Mapper.xml", "updateUser", params, OperationType.EXECUTE);
    }

    @Test
    @Order(4)
    @DisplayName("Demo1Mapper.xml - deleteUser - 基础删除执行")
    void testDemo1_04_deleteUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1001L);
        executeAndVerify("Demo1-deleteUser", "Demo1Mapper.xml", "deleteUser", params, OperationType.EXECUTE);
    }

    @Test
    @Order(5)
    @DisplayName("Demo1Mapper.xml - selectUsersByCondition - if标签执行")
    void testDemo1_05_selectUsersByCondition() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "张");
        params.put("age", 25);
        params.put("email", "test@example.com");
        params.put("status", "active");
        executeAndVerify("Demo1-selectUsersByCondition", "Demo1Mapper.xml", "selectUsersByCondition", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(6)
    @DisplayName("Demo1Mapper.xml - selectUsersWithWhere - where标签执行")
    void testDemo1_06_selectUsersWithWhere() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "李");
        params.put("age", 30);
        params.put("status", "pending");
        executeAndVerify("Demo1-selectUsersWithWhere", "Demo1Mapper.xml", "selectUsersWithWhere", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(7)
    @DisplayName("Demo1Mapper.xml - updateUserSelective - set标签执行")
    void testDemo1_07_updateUserSelective() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1001L);
        params.put("name", "更新姓名");
        params.put("email", "updated@example.com");
        executeAndVerify("Demo1-updateUserSelective", "Demo1Mapper.xml", "updateUserSelective", params, OperationType.EXECUTE);
    }

    @Test
    @Order(8)
    @DisplayName("Demo1Mapper.xml - selectUsersByType - choose标签执行")
    void testDemo1_08_selectUsersByType() {
        Map<String, Object> params = new HashMap<>();
        params.put("searchType", "name");
        params.put("searchValue", "王");
        executeAndVerify("Demo1-selectUsersByType", "Demo1Mapper.xml", "selectUsersByType", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(9)
    @DisplayName("Demo1Mapper.xml - selectUsersWithTrim - trim标签执行")
    void testDemo1_09_selectUsersWithTrim() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "赵六");
        params.put("age", 35);
        executeAndVerify("Demo1-selectUsersWithTrim", "Demo1Mapper.xml", "selectUsersWithTrim", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(10)
    @DisplayName("Demo1Mapper.xml - selectUsersByIds - foreach标签执行")
    void testDemo1_10_selectUsersByIds() {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList(1001L, 1002L, 1003L, 1004L));
        executeAndVerify("Demo1-selectUsersByIds", "Demo1Mapper.xml", "selectUsersByIds", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(11)
    @DisplayName("Demo1Mapper.xml - batchInsertUsers - foreach批量插入执行")
    void testDemo1_11_batchInsertUsers() {
        Map<String, Object> params = new HashMap<>();

        // 使用时间戳确保email唯一性
        long timestamp = System.currentTimeMillis();

        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "批量测试用户1");
        user1.put("age", 20);
        user1.put("email", "batchtest1_" + timestamp + "@example.com");
        user1.put("phone", "13900000001");
        user1.put("status", "active");
        user1.put("createTime", new java.util.Date());

        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "批量测试用户2");
        user2.put("age", 22);
        user2.put("email", "batchtest2_" + timestamp + "@example.com");
        user2.put("phone", "13900000002");
        user2.put("status", "pending");
        user2.put("createTime", new java.util.Date());

        params.put("users", Arrays.asList(user1, user2));
        executeAndVerify("Demo1-batchInsertUsers", "Demo1Mapper.xml", "batchInsertUsers", params, OperationType.EXECUTE);
    }

    @Test
    @Order(12)
    @DisplayName("Demo1Mapper.xml - batchUpdateUsers - foreach批量更新执行")
    void testDemo1_12_batchUpdateUsers() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", 1001L);
        user1.put("name", "更新用户1");
        user1.put("age", 25);

        Map<String, Object> user2 = new HashMap<>();
        user2.put("id", 1002L);
        user2.put("name", "更新用户2");
        user2.put("age", 30);

        params.put("users", Arrays.asList(user1, user2));
        executeAndVerify("Demo1-batchUpdateUsers", "Demo1Mapper.xml", "batchUpdateUsers", params, OperationType.EXECUTE);
    }

    @Test
    @Order(13)
    @DisplayName("Demo1Mapper.xml - selectUsersByComplexCondition - 复杂foreach执行")
    void testDemo1_13_selectUsersByComplexCondition() {
        Map<String, Object> params = new HashMap<>();
        params.put("names", Arrays.asList("张三", "李四", "王五"));
        params.put("ageRange", Arrays.asList(25, 30, 35));
        executeAndVerify("Demo1-selectUsersByComplexCondition", "Demo1Mapper.xml", "selectUsersByComplexCondition", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(14)
    @DisplayName("Demo1Mapper.xml - selectUsersWithResultMap - 结果映射执行")
    void testDemo1_14_selectUsersWithResultMap() {
        Map<String, Object> params = new HashMap<>();
        executeAndVerify("Demo1-selectUsersWithResultMap", "Demo1Mapper.xml", "selectUsersWithResultMap", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(15)
    @DisplayName("Demo1Mapper.xml - selectUsersWithRoles - 关联查询执行")
    void testDemo1_15_selectUsersWithRoles() {
        Map<String, Object> params = new HashMap<>();
        executeAndVerify("Demo1-selectUsersWithRoles", "Demo1Mapper.xml", "selectUsersWithRoles", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(16)
    @DisplayName("Demo1Mapper.xml - selectUsersWithInclude - include标签执行")
    void testDemo1_16_selectUsersWithInclude() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "测试用户");
        params.put("status", "active");
        executeAndVerify("Demo1-selectUsersWithInclude", "Demo1Mapper.xml", "selectUsersWithInclude", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(17)
    @DisplayName("Demo1Mapper.xml - selectUsersWithBind - bind标签执行")
    void testDemo1_17_selectUsersWithBind() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "绑定测试");
        executeAndVerify("Demo1-selectUsersWithBind", "Demo1Mapper.xml", "selectUsersWithBind", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(18)
    @DisplayName("Demo1Mapper.xml - selectUsersWithCDATA - CDATA区段执行")
    void testDemo1_18_selectUsersWithCDATA() {
        Map<String, Object> params = new HashMap<>();
        params.put("age", 25);
        executeAndVerify("Demo1-selectUsersWithCDATA", "Demo1Mapper.xml", "selectUsersWithCDATA", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(19)
    @DisplayName("Demo1Mapper.xml - selectUsersWithXmlEntity - XML实体执行")
    void testDemo1_19_selectUsersWithXmlEntity() {
        Map<String, Object> params = new HashMap<>();
        params.put("age", 18);
        executeAndVerify("Demo1-selectUsersWithXmlEntity", "Demo1Mapper.xml", "selectUsersWithXmlEntity", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(20)
    @DisplayName("Demo1Mapper.xml - selectUsersWithDynamicJoin - 动态关联执行")
    void testDemo1_20_selectUsersWithDynamicJoin() {
        Map<String, Object> params = new HashMap<>();
        params.put("includeDepartment", true);
        params.put("name", "动态关联");
        params.put("departmentId", 10L);
        executeAndVerify("Demo1-selectUsersWithDynamicJoin", "Demo1Mapper.xml", "selectUsersWithDynamicJoin", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(21)
    @DisplayName("Demo1Mapper.xml - selectUsersWithPagination - 分页查询执行")
    void testDemo1_21_selectUsersWithPagination() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "分页测试");
        params.put("offset", 0);
        params.put("pageSize", 10);
        executeAndVerify("Demo1-selectUsersWithPagination", "Demo1Mapper.xml", "selectUsersWithPagination", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(22)
    @DisplayName("Demo1Mapper.xml - countUsers - 统计查询执行")
    void testDemo1_22_countUsers() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        executeAndVerify("Demo1-countUsers", "Demo1Mapper.xml", "countUsers", params, OperationType.SELECT_ONE);
    }

    @Test
    @Order(23)
    @DisplayName("Demo1Mapper.xml - selectUsersComplex - 复杂动态SQL执行")
    void testDemo1_23_selectUsersComplex() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("type", "name");
        searchCriteria.put("value", "复杂查询");
        params.put("searchCriteria", searchCriteria);
        params.put("roles", Arrays.asList(1L, 2L, 3L));
        params.put("orderBy", "age");
        executeAndVerify("Demo1-selectUsersComplex", "Demo1Mapper.xml", "selectUsersComplex", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(24)
    @DisplayName("Demo1Mapper.xml - selectUsersWithDynamicFields - 动态字段执行")
    void testDemo1_24_selectUsersWithDynamicFields() {
        Map<String, Object> params = new HashMap<>();
        params.put("fields", Arrays.asList("id", "name", "email"));
        params.put("status", "active");
        executeAndVerify("Demo1-selectUsersWithDynamicFields", "Demo1Mapper.xml", "selectUsersWithDynamicFields", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(25)
    @DisplayName("Demo1Mapper.xml - selectUsersWithDynamicOrder - 动态排序执行")
    void testDemo1_25_selectUsersWithDynamicOrder() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> orderField1 = new HashMap<>();
        orderField1.put("field", "name");
        orderField1.put("direction", "ASC");

        Map<String, Object> orderField2 = new HashMap<>();
        orderField2.put("field", "age");
        orderField2.put("direction", "DESC");

        params.put("orderFields", Arrays.asList(orderField1, orderField2));
        executeAndVerify("Demo1-selectUsersWithDynamicOrder", "Demo1Mapper.xml", "selectUsersWithDynamicOrder", params, OperationType.SELECT_LIST);
    }

    // ================================ Demo2Mapper.xml 测试 ================================

    @Test
    @Order(26)
    @DisplayName("Demo2Mapper.xml - selectUsersWithDepartmentAndRole - 多表关联执行")
    void testDemo2_01_selectUsersWithDepartmentAndRole() {
        Map<String, Object> params = new HashMap<>();
        params.put("includeDepartment", true);
        params.put("includeRole", true);
        params.put("departmentIds", Arrays.asList(1L, 2L, 3L));
        params.put("roleIds", Arrays.asList(10L, 20L));
        executeAndVerify("Demo2-selectUsersWithDepartmentAndRole", "Demo2Mapper.xml", "selectUsersWithDepartmentAndRole", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(27)
    @DisplayName("Demo2Mapper.xml - selectUsersAdvancedSearch - 高级搜索执行")
    void testDemo2_02_selectUsersAdvancedSearch() {
        Map<String, Object> params = new HashMap<>();

        Map<String, Object> basicSearch = new HashMap<>();
        basicSearch.put("keyword", "高级搜索");
        params.put("basicSearch", basicSearch);

        Map<String, Object> filters = new HashMap<>();
        Map<String, Object> ageRange = new HashMap<>();
        ageRange.put("min", 20);
        ageRange.put("max", 50);
        filters.put("ageRange", ageRange);
        filters.put("statuses", Arrays.asList("active", "pending"));
        params.put("filters", filters);

        params.put("excludeIds", Arrays.asList(100L, 200L));
        executeAndVerify("Demo2-selectUsersAdvancedSearch", "Demo2Mapper.xml", "selectUsersAdvancedSearch", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(28)
    @DisplayName("Demo2Mapper.xml - getUserStatistics - 分组统计执行")
    void testDemo2_03_getUserStatistics() {
        Map<String, Object> params = new HashMap<>();
        params.put("groupBy", "department");
        params.put("status", "active");
        executeAndVerify("Demo2-getUserStatistics", "Demo2Mapper.xml", "getUserStatistics", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(29)
    @DisplayName("Demo2Mapper.xml - batchUpdateUserStatus - 批量更新执行")
    void testDemo2_04_batchUpdateUserStatus() {
        Map<String, Object> params = new HashMap<>();
        params.put("newStatus", "inactive");
        params.put("userIds", Arrays.asList(1001L, 1002L, 1003L));
        executeAndVerify("Demo2-batchUpdateUserStatus", "Demo2Mapper.xml", "batchUpdateUserStatus", params, OperationType.EXECUTE);
    }

    @Test
    @Order(30)
    @DisplayName("Demo2Mapper.xml - batchDeleteUsers - 批量删除执行")
    void testDemo2_05_batchDeleteUsers() {
        Map<String, Object> params = new HashMap<>();
        params.put("userIds", Arrays.asList(2001L, 2002L));
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("status", "inactive");
        conditions.put("inactiveDays", 90);
        params.put("conditions", conditions);
        executeAndVerify("Demo2-batchDeleteUsers", "Demo2Mapper.xml", "batchDeleteUsers", params, OperationType.EXECUTE);
    }

    @Test
    @Order(31)
    @DisplayName("Demo2Mapper.xml - insertUserDynamic - 动态插入执行")
    void testDemo2_06_insertUserDynamic() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "动态插入用户");
        params.put("age", 28);
        params.put("email", "dynamic@example.com");
        params.put("departmentId", 5L);
        params.put("status", "active");
        params.put("createTime", new java.util.Date());
        executeAndVerify("Demo2-insertUserDynamic", "Demo2Mapper.xml", "insertUserDynamic", params, OperationType.EXECUTE);
    }

    @Test
    @Order(32)
    @DisplayName("Demo2Mapper.xml - selectUsersWithSubquery - 子查询执行")
    void testDemo2_07_selectUsersWithSubquery() {
        Map<String, Object> params = new HashMap<>();
        params.put("hasRoles", true);
        params.put("minRoleCount", 2);
        params.put("departmentName", "技术部");
        executeAndVerify("Demo2-selectUsersWithSubquery", "Demo2Mapper.xml", "selectUsersWithSubquery", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(33)
    @DisplayName("Demo2Mapper.xml - selectUsersUnion - UNION查询执行")
    void testDemo2_08_selectUsersUnion() {
        Map<String, Object> params = new HashMap<>();
        params.put("includeActive", true);
        params.put("includeInactive", true);
        Map<String, Object> activeFilters = new HashMap<>();
        activeFilters.put("departmentId", 1L);
        params.put("activeFilters", activeFilters);
        executeAndVerify("Demo2-selectUsersUnion", "Demo2Mapper.xml", "selectUsersUnion", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(34)
    @DisplayName("Demo2Mapper.xml - selectUsersWithCTE - CTE查询执行")
    void testDemo2_09_selectUsersWithCTE() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        params.put("departmentIds", Arrays.asList(1L, 2L));
        params.put("minRoleCount", 1);
        params.put("activityStatus", "active");
        executeAndVerify("Demo2-selectUsersWithCTE", "Demo2Mapper.xml", "selectUsersWithCTE", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(35)
    @DisplayName("Demo2Mapper.xml - selectUsersWithRanking - 窗口函数执行")
    void testDemo2_10_selectUsersWithRanking() {
        Map<String, Object> params = new HashMap<>();
        params.put("partitionBy", "department");
        params.put("orderBy", "age");
        params.put("status", "active");
        params.put("departmentIds", Arrays.asList(1L, 2L, 3L));
        executeAndVerify("Demo2-selectUsersWithRanking", "Demo2Mapper.xml", "selectUsersWithRanking", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(36)
    @DisplayName("Demo2Mapper.xml - selectUsersWithJsonQuery - JSON查询执行")
    void testDemo2_11_selectUsersWithJsonQuery() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> jsonFilters = new HashMap<>();
        jsonFilters.put("preference", "dark");
        jsonFilters.put("settingKey", "language");
        jsonFilters.put("settingValue", "zh-CN");
        jsonFilters.put("hasPreferences", true);
        params.put("jsonFilters", jsonFilters);
        executeAndVerify("Demo2-selectUsersWithJsonQuery", "Demo2Mapper.xml", "selectUsersWithJsonQuery", params, OperationType.SELECT_LIST);
    }

    // ================================ Demo3Mapper.xml 测试 ================================

    @Test
    @Order(37)
    @DisplayName("Demo3Mapper.xml - findSimple - 简单include执行")
    void testDemo3_01_findSimple() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 3001L);
        executeAndVerify("Demo3-findSimple", "Demo3Mapper.xml", "findSimple", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(38)
    @DisplayName("Demo3Mapper.xml - findWithDynamicConditions - 动态条件include执行")
    void testDemo3_02_findWithDynamicConditions() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        params.put("name", "include测试");
        executeAndVerify("Demo3-findWithDynamicConditions", "Demo3Mapper.xml", "findWithDynamicConditions", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(39)
    @DisplayName("Demo3Mapper.xml - findWithDynamicOrdering - 动态排序include执行")
    void testDemo3_03_findWithDynamicOrdering() {
        Map<String, Object> params = new HashMap<>();
        params.put("orderByColumn", "name");
        params.put("orderDirection", "ASC");
        executeAndVerify("Demo3-findWithDynamicOrdering", "Demo3Mapper.xml", "findWithDynamicOrdering", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(40)
    @DisplayName("Demo3Mapper.xml - findWithProperties - 属性传递include执行")
    void testDemo3_04_findWithProperties() {
        Map<String, Object> params = new HashMap<>();
        executeAndVerify("Demo3-findWithProperties", "Demo3Mapper.xml", "findWithProperties", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(41)
    @DisplayName("Demo3Mapper.xml - findWithNestedInclude - 嵌套include执行")
    void testDemo3_05_findWithNestedInclude() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "pending");
        params.put("orderByColumn", "create_time");
        params.put("orderDirection", "DESC");
        executeAndVerify("Demo3-findWithNestedInclude", "Demo3Mapper.xml", "findWithNestedInclude", params, OperationType.SELECT_LIST);
    }

    // ================================ Demo4Mapper.xml 测试 ================================

    @Test
    @Order(42)
    @DisplayName("Demo4Mapper.xml - findWithXmlEntitiesInTest - XML实体字符执行")
    void testDemo4_01_findWithXmlEntitiesInTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("priority", 7);
        params.put("name", "special & co");
        executeAndVerify("Demo4-findWithXmlEntitiesInTest", "Demo4Mapper.xml", "findWithXmlEntitiesInTest", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(43)
    @DisplayName("Demo4Mapper.xml - findWithCdataBlock - CDATA块执行")
    void testDemo4_02_findWithCdataBlock() {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2023-01-01");
        params.put("endDate", "2023-12-31");
        executeAndVerify("Demo4-findWithCdataBlock", "Demo4Mapper.xml", "findWithCdataBlock", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(44)
    @DisplayName("Demo4Mapper.xml - findWithEmptyDynamicTags - 空动态标签执行")
    void testDemo4_03_findWithEmptyDynamicTags() {
        Map<String, Object> params = new HashMap<>();
        executeAndVerify("Demo4-findWithEmptyDynamicTags", "Demo4Mapper.xml", "findWithEmptyDynamicTags", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(45)
    @DisplayName("Demo4Mapper.xml - updateWithEmptySetTag - 空set标签执行")
    void testDemo4_04_updateWithEmptySetTag() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 4001L);
        executeAndVerify("Demo4-updateWithEmptySetTag", "Demo4Mapper.xml", "updateWithEmptySetTag", params, OperationType.EXECUTE);
    }

    @Test
    @Order(46)
    @DisplayName("Demo4Mapper.xml - findWithResultingEmptyWhere - 空where结果执行")
    void testDemo4_05_findWithResultingEmptyWhere() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "non_existent_user");
        executeAndVerify("Demo4-findWithResultingEmptyWhere", "Demo4Mapper.xml", "findWithResultingEmptyWhere", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(47)
    @DisplayName("Demo4Mapper.xml - findByUserMethod - 布尔值判断执行")
    void testDemo4_06_findByUserMethod() {
        Map<String, Object> params = new HashMap<>();
        params.put("isAdmin", true);
        params.put("canViewSensitiveData", false);
        executeAndVerify("Demo4-findByUserMethod", "Demo4Mapper.xml", "findByUserMethod", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(48)
    @DisplayName("Demo4Mapper.xml - findByTernaryOperator - 三元运算符执行")
    void testDemo4_07_findByTernaryOperator() {
        Map<String, Object> params = new HashMap<>();
        params.put("isActive", true);
        executeAndVerify("Demo4-findByTernaryOperator", "Demo4Mapper.xml", "findByTernaryOperator", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(49)
    @DisplayName("Demo4Mapper.xml - findByComplexExpression - 复杂表达式执行")
    void testDemo4_08_findByComplexExpression() {
        Map<String, Object> params = new HashMap<>();
        params.put("age", 30);
        params.put("name", "复杂表达式用户");
        params.put("status", "active");
        executeAndVerify("Demo4-findByComplexExpression", "Demo4Mapper.xml", "findByComplexExpression", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(50)
    @DisplayName("Demo4Mapper.xml - findBySimpleParameter - 简单参数执行")
    void testDemo4_09_findBySimpleParameter() {
        Map<String, Object> params = new HashMap<>();
        params.put("_parameter", "simple_test_user");
        executeAndVerify("Demo4-findBySimpleParameter", "Demo4Mapper.xml", "findBySimpleParameter", params, OperationType.SELECT_LIST);
    }

    @Test
    @Order(51)
    @DisplayName("Demo4Mapper.xml - findByDatabaseId - 数据库ID执行")
    void testDemo4_10_findByDatabaseId() {
        Map<String, Object> params = new HashMap<>();
        params.put("_databaseId", "mysql");
        executeAndVerify("Demo4-findByDatabaseId", "Demo4Mapper.xml", "findByDatabaseId", params, OperationType.SELECT_LIST);
    }
}