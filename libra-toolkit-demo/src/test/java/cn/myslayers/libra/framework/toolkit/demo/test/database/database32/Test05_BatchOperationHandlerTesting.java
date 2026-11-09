package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32.BatchOperationHandler;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32.BatchOperationHandler.BatchResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 批量操作及MERGE语法兼容性测试类
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("数据库批量操作与MERGE语法综合测试")
public class Test05_BatchOperationHandlerTesting {

    private static final Logger logger = LoggerFactory.getLogger(Test05_BatchOperationHandlerTesting.class);

    // 测试数据库名称
    private static final String TEST_H2_DB = "test_h2_batch_merge";
    private static final String TEST_ORACLE_DB = "eas-datasource";

    @BeforeEach
    void setUp() {
        logger.info("=== 开始批量操作与MERGE语法综合测试 ===");
//        DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);

        try {
            // 注册H2内存数据库
            DatabaseUtil32.DataSourceCore.register(
                TEST_H2_DB,
                "jdbc:h2:mem:" + TEST_H2_DB + System.currentTimeMillis() + ";DB_CLOSE_DELAY=-1;MODE=MySQL",
                "sa",
                "",
                "org.h2.Driver"
            );

            // 创建测试表
            try {
                // 批量操作测试表
                DatabaseUtil32.CommonSqlExecutor.executeDDL(TEST_H2_DB,
                    "CREATE TABLE IF NOT EXISTS test_batch_table (" +
                        "id INT PRIMARY KEY, " +
                        "name VARCHAR(100), " +
                        "age INT, " +
                        "status VARCHAR(50), " +
                        "last_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")"
                    );

                // 注释测试表
                DatabaseUtil32.CommonSqlExecutor.executeDDL(TEST_H2_DB,
                    "CREATE TABLE IF NOT EXISTS test_comment_table (" +
                        "id INT PRIMARY KEY, " +
                        "name VARCHAR(100), " +
                        "description TEXT" +
                        ")");

                // 性能测试表
                DatabaseUtil32.CommonSqlExecutor.executeDDL(TEST_H2_DB,
                    "CREATE TABLE IF NOT EXISTS performance_test (" +
                        "id INT PRIMARY KEY, " +
                        "data1 VARCHAR(200), " +
                        "data2 DOUBLE, " +
                        "data3 VARCHAR(50), " +
                        "data4 TIMESTAMP" +
                        ")");

                // UPSERT测试表
                DatabaseUtil32.CommonSqlExecutor.executeDDL(TEST_H2_DB,
                    "CREATE TABLE IF NOT EXISTS test_upsert_table (" +
                        "id INT PRIMARY KEY, " +
                        "name VARCHAR(100), " +
                        "age INT, " +
                        "status VARCHAR(50)" +
                        ")");

                // H2复合键UPSERT测试表
                DatabaseUtil32.CommonSqlExecutor.executeDDL(TEST_H2_DB,
                    "CREATE TABLE IF NOT EXISTS test_h2_upsert (" +
                        "id INT, " +
                        "category VARCHAR(50), " +
                        "title VARCHAR(200), " +
                        "content TEXT, " +
                        "score DOUBLE, " +
                        "PRIMARY KEY (id, category)" +
                        ")");

                // 默认数据库测试表
                DatabaseUtil32.CommonSqlExecutor.executeDDL(TEST_H2_DB,
                    "CREATE TABLE IF NOT EXISTS default_db_test (" +
                        "id INT PRIMARY KEY, " +
                        "name VARCHAR(100), " +
                        "value DOUBLE" +
                        ")");

                // H2 MERGE语法兼容性测试表
                DatabaseUtil32.CommonSqlExecutor.executeDDL(TEST_H2_DB,
                    "CREATE TABLE IF NOT EXISTS test_users (" +
                        "id BIGINT PRIMARY KEY, " +
                        "name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(200), " +
                        "age INT, " +
                        "created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                logger.info("✓ 所有测试表创建完成");
            } catch (Exception e) {
                logger.error("创建测试表失败", e);
            }

            logger.info("✓ H2测试数据库初始化完成: " + TEST_H2_DB);
        } catch (Exception e) {
            logger.error("H2数据库初始化失败", e);
        }
    }

    @AfterEach
    void tearDown() {
        logger.info("=== 清理测试环境 ===");

        // 清理H2数据库
        try {
            // 删除测试表
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, "DROP TABLE IF EXISTS test_batch_table", new HashMap<>());
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, "DROP TABLE IF EXISTS test_comment_table", new HashMap<>());
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, "DROP TABLE IF EXISTS performance_test", new HashMap<>());
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, "DROP TABLE IF EXISTS test_upsert_table", new HashMap<>());
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, "DROP TABLE IF EXISTS test_h2_upsert", new HashMap<>());
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, "DROP TABLE IF EXISTS default_db_test", new HashMap<>());
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, "DROP TABLE IF EXISTS test_users", new HashMap<>());


            // 关闭数据库连接
            DatabaseUtil32.DataSourceCore.closeDatabase(TEST_H2_DB);

            logger.info("✓ H2测试数据库清理完成");
        } catch (Exception e) {
            logger.error("清理H2数据库失败", e);
        }
    }

    // ==================== 核心2：H2 UPSERT 语法兼容性  ====================

    @Test
    @Order(1)
    @DisplayName("测试H2批量UPSERT操作")
    void testH2BatchUpsert() {
        logger.info(">>> 测试H2批量UPSERT操作");

        // 准备测试数据
        String tableName = "test_h2_upsert";
        String[] keyColumns = {"id", "category"};
        String[] valueColumns = {"title", "content", "score"};

        List<Map<String, Object>> entities = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            Map<String, Object> entity = new HashMap<>();
            entity.put("id", i);
            entity.put("category", "CAT" + (i % 5 + 1));
            entity.put("title", "H2测试标题" + i);
            entity.put("content", "这是H2数据库的测试内容" + i);
            entity.put("score", 80.0 + (i % 20));
            entities.add(entity);
        }

        // 执行批量UPSERT
        BatchResult result = BatchOperationHandler.batchUpsertWithDbName(TEST_H2_DB, tableName, entities, keyColumns, valueColumns);

        // 验证结果
        System.out.println("检查H2批量UPSERT结果是否不为null: " + (result != null));
        System.out.println("检查总记录数 | 预期: 30 | 实际: " + (result != null ? result.getTotalRecords() : "null"));
        logger.info("H2批量UPSERT结果: " + result.toString());
    }

    @Test
    @Order(2)
    @DisplayName("测试MySQL兼容的H2批量UPSERT操作")
    void testMySQLCompatibleH2BatchUpsert() {
        logger.info(">>> 测试MySQL兼容的H2批量UPSERT操作");

        // 准备测试数据
        String tableName = "test_upsert_table";
        String[] keyColumns = {"id"};
        String[] valueColumns = {"name", "age", "status"};

        List<Map<String, Object>> entities = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            Map<String, Object> entity = new HashMap<>();
            entity.put("id", i);
            entity.put("name", "测试用户" + i);
            entity.put("age", 25 + (i % 30));
            entity.put("status", i % 3 == 0 ? "premium" : "basic");
            entities.add(entity);
        }

        // 执行批量UPSERT（H2会使用类似Oracle的MERGE语法）
        BatchResult result = BatchOperationHandler.batchUpsertWithDbName(TEST_H2_DB, tableName, entities, keyColumns, valueColumns);

        // 验证结果
        System.out.println("检查MySQL兼容H2批量UPSERT结果是否不为null: " + (result != null));
        System.out.println("检查总记录数 | 预期: 20 | 实际: " + (result != null ? result.getTotalRecords() : "null"));
        logger.info("MySQL兼容H2批量UPSERT结果: " + result.toString());
    }

    // ==================== 核心2：H2 MERGE 语法兼容性  ====================

    // 创建用户数据Map的辅助方法
    private Map<String, Object> createUserMap(Long id, String name, String email, Integer age) {
        Map<String, Object> user = new HashMap<>();
        user.put("id", id);
        user.put("name", name);
        user.put("email", email);
        user.put("age", age);
        return user;
    }

    @Test
    @Order(3)
    @DisplayName("测试H2特有的MERGE INTO KEY语法")
    void testH2MergeIntoKeySyntax() {
        logger.info(">>> 测试H2 MERGE INTO KEY语法");

        // H2特有的MERGE INTO KEY语法
        String mergeSql = "MERGE INTO test_users KEY(id) " +
            "VALUES (1, 'Alice', 'alice@example.com', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try {
            int result = DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, mergeSql, new HashMap<>());
            System.out.println("检查MERGE操作是否成功执行: " + (result >= 0));
            logger.info("✓ H2 MERGE INTO KEY语法测试通过，影响行数: " + result);

            // 验证数据是否正确插入
            List<Map<String, Object>> users = DatabaseUtil32.SimpleSqlExecutor.selectList(
                TEST_H2_DB, "SELECT * FROM test_users WHERE id = 1", new HashMap<>());
            System.out.println("检查是否插入一条记录 | 预期: 1 | 实际: " + users.size());
            System.out.println("检查用户名是否正确 | 预期: Alice | 实际: " + users.get(0).get("NAME"));
        } catch (Exception e) {
            logger.error("H2 MERGE INTO KEY语法测试失败", e);
        }
    }

    @Test
    @Order(4)
    @DisplayName("测试标准的MERGE USING语法")
    void testStandardMergeUsingSyntax() {
        logger.info(">>> 测试标准MERGE USING语法");

        // 标准SQL MERGE USING语法
        String mergeSql = "MERGE INTO test_users AS target " +
            "USING (SELECT 2 as id, 'Bob' as name, 'bob@example.com' as email, 30 as age) AS source " +
            "ON target.id = source.id " +
            "WHEN MATCHED THEN " +
            "UPDATE SET name = source.name, email = source.email, age = source.age, updated_time = CURRENT_TIMESTAMP " +
            "WHEN NOT MATCHED THEN " +
            "INSERT (id, name, email, age, created_time, updated_time) " +
            "VALUES (source.id, source.name, source.email, source.age, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try {
            int result = DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, mergeSql, new HashMap<>());
            System.out.println("检查MERGE操作是否成功执行: " + (result >= 0));
            logger.info("✓ 标准MERGE USING语法测试通过，影响行数: " + result);

            // 验证数据是否正确插入
            List<Map<String, Object>> users = DatabaseUtil32.SimpleSqlExecutor.selectList(
                TEST_H2_DB, "SELECT * FROM test_users WHERE id = 2", new HashMap<>());
            System.out.println("检查是否插入一条记录 | 预期: 1 | 实际: " + users.size());
            System.out.println("检查用户名是否正确 | 预期: Bob | 实际: " + users.get(0).get("NAME"));
        } catch (Exception e) {
            logger.error("标准MERGE USING语法测试失败", e);
        }
    }

    @Test
    @Order(5)
    @DisplayName("测试批量MERGE操作")
    void testBatchMergeOperations() {
        logger.info(">>> 测试批量MERGE操作");

        // 准备测试数据
        List<Map<String, Object>> testData = Arrays.asList(
            createUserMap(3L, "Charlie", "charlie@example.com", 28),
            createUserMap(4L, "Diana", "diana@example.com", 32),
            createUserMap(5L, "Eve", "eve@example.com", 26)
        );

        try {
            // 使用批量UPSERT功能
            BatchResult result =
                BatchOperationHandler.batchUpsertWithDbName(
                    TEST_H2_DB,
                    "test_users",
                    testData,
                    new String[]{"id"},
                    new String[]{"name", "email", "age"}
                );

            System.out.println("检查批量MERGE操作是否成功: " + result.isSuccess());
            System.out.println("检查成功处理记录数 | 预期: 3 | 实际: " + result.getSuccessRecords());
            logger.info("✓ 批量MERGE操作测试通过: " + result);

            // 验证数据是否正确插入
            List<Map<String, Object>> users = DatabaseUtil32.SimpleSqlExecutor.selectList(
                TEST_H2_DB, "SELECT * FROM test_users WHERE id IN (3, 4, 5) ORDER BY id", new HashMap<>());
            System.out.println("检查是否插入3条记录 | 预期: 3 | 实际: " + users.size());
        } catch (Exception e) {
            logger.error("批量MERGE操作测试失败", e);
        }
    }

    @Test
    @Order(6)
    @DisplayName("测试MERGE操作的更新功能")
    void testMergeUpdateFunctionality() {
        logger.info(">>> 测试MERGE更新功能");

        // 先插入一条记录
        String insertSql = "INSERT INTO test_users (id, name, email, age) " +
            "VALUES (6, 'Frank', 'frank@example.com', 35)";

        try {
            DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, insertSql, new HashMap<>());

            // 使用MERGE更新这条记录
            String mergeSql = "MERGE INTO test_users KEY(id) " +
                "VALUES (6, 'Franklin', 'franklin@example.com', 36, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

            int result = DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, mergeSql, new HashMap<>());
            System.out.println("检查MERGE更新操作是否成功执行: " + (result >= 0));
            logger.info("✓ MERGE更新操作测试通过，影响行数: " + result);

            // 验证数据是否正确更新
            List<Map<String, Object>> users = DatabaseUtil32.SimpleSqlExecutor.selectList(
                TEST_H2_DB, "SELECT * FROM test_users WHERE id = 6", new HashMap<>());
            System.out.println("检查是否有一条记录 | 预期: 1 | 实际: " + users.size());
            System.out.println("检查用户名是否已更新 | 预期: Franklin | 实际: " + users.get(0).get("NAME"));
            System.out.println("检查邮箱是否已更新 | 预期: franklin@example.com | 实际: " + users.get(0).get("EMAIL"));
        } catch (Exception e) {
            logger.error("MERGE更新功能测试失败", e);
        }
    }

    @Test
    @Order(7)
    @DisplayName("测试复杂MERGE语句的兼容性")
    void testComplexMergeWallCompatibility() {
        logger.info(">>> 测试复杂MERGE语句的兼容性");

        // 包含复杂条件和函数的MERGE语句
        String complexMergeSql = "MERGE INTO test_users AS target " +
            "USING (" +
            "SELECT 7 as id, " +
            "UPPER('grace') as name, " +
            "CONCAT('grace', '@', 'example.com') as email, " +
            "CASE WHEN 1=1 THEN 29 ELSE 0 END as age" +
            ") AS source " +
            "ON target.id = source.id " +
            "WHEN MATCHED THEN " +
            "UPDATE SET " +
            "name = source.name, " +
            "email = source.email, " +
            "age = source.age + 1, " +
            "updated_time = CURRENT_TIMESTAMP " +
            "WHEN NOT MATCHED THEN " +
            "INSERT (id, name, email, age, created_time, updated_time) " +
            "VALUES (source.id, source.name, source.email, source.age, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

        try {
            int result = DatabaseUtil32.SimpleSqlExecutor.execute(TEST_H2_DB, complexMergeSql, new HashMap<>());
            System.out.println("检查复杂MERGE操作是否成功执行: " + (result >= 0));
            logger.info("✓ 复杂MERGE语句兼容性测试通过，影响行数: " + result);

            // 验证复杂逻辑是否正确执行
            List<Map<String, Object>> users = DatabaseUtil32.SimpleSqlExecutor.selectList(
                TEST_H2_DB, "SELECT * FROM test_users WHERE id = 7", new HashMap<>());
            System.out.println("检查是否插入一条记录 | 预期: 1 | 实际: " + users.size());
            System.out.println("检查用户名是否被转换为大写 | 预期: GRACE | 实际: " + users.get(0).get("NAME"));
            System.out.println("检查邮箱是否正确拼接 | 预期: grace@example.com | 实际: " + users.get(0).get("EMAIL"));
        } catch (Exception e) {
            logger.error("复杂MERGE语句兼容性测试失败", e);
        }
    }
}
