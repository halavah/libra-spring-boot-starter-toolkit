package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.SimpleSqlExecutor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil31 - 全面的XML文件SQL执行器测试")
public class Test22_SimpleSqlExecutor_XmlFileSqlTesting {

    private static final String DB_NAME = "default_mysql1";
    private static final String LIBRA_TEST1_XML = "libra-test1-sql.xml";
    private static final String LIBRA_TEST2_XML = "libra-test2-sql.xml";
    private static final String LIBRA_TEST3_XML = "libra-test3-sql.xml";

    @BeforeAll
    static void setUpAll() {
//        DatabaseUtil31.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil31.LoggerCore.setSqlDebugLogEnabled(true);
        // 可强制刷新的初始化方法
        DatabaseUtil31.SimpleSqlParser.initializeSqlCache(true);
        System.out.println("========== SimpleSqlExecutor 全面XML文件执行器测试开始 ==========");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("========== SimpleSqlExecutor 全面XML文件执行器测试结束 ==========");
    }

    // 辅助方法：打印查询结果
    private void printQueryResult(String testName, List<Map<String, Object>> result) {
        System.out.println("--- " + testName + " ---");
        if (result == null) {
            System.out.println("Result: null");
        } else {
            System.out.println("Result (" + result.size() + " rows):");
            result.forEach(row -> System.out.println("  " + row));
        }
        System.out.println();
    }

    // 辅助方法：打印单条查询结果
    private void printSingleQueryResult(String testName, Map<String, Object> result) {
        System.out.println("--- " + testName + " ---");
        if (result == null) {
            System.out.println("Result: null");
        } else {
            System.out.println("Result: " + result);
        }
        System.out.println();
    }

    // 辅助方法：打印执行结果
    private void printExecuteResult(String testName, int affectedRows) {
        System.out.println("--- " + testName + " ---");
        System.out.println("Affected Rows: " + affectedRows);
        System.out.println();
    }

    // 辅助方法：生成唯一的表名
    private String getTempTableName() {
        return "temp_test_table_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }

    // ==================== libra-test1-sql.xml ====================

    @Test
    @Order(1)
    @DisplayName("[libra-test1-sql] 测试 selectBasicList")
    void testSelectBasicList() {
        List<Map<String, Object>> result = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST1_XML, "selectBasicList", new HashMap<>());
        printQueryResult("[libra-test1-sql] selectBasicList", result);
    }

    @Test
    @Order(2)
    @DisplayName("[libra-test1-sql] 测试 selectBasicOne")
    void testSelectBasicOne() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        Map<String, Object> result = SimpleSqlExecutor.selectOneFromXml(DB_NAME, LIBRA_TEST1_XML, "selectBasicOne", params);
        printSingleQueryResult("[libra-test1-sql] selectBasicOne", result);
    }

    @Test
    @Order(3)
    @DisplayName("[libra-test1-sql] 测试 selectWithCondition")
    void testSelectWithCondition() {
        Map<String, Object> params = new HashMap<>();
        params.put("minAge", 18);
        params.put("maxAge", 60);
        List<Map<String, Object>> result = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST1_XML, "selectWithCondition", params);
        printQueryResult("[libra-test1-sql] selectWithCondition", result);
    }

    @Test
    @Order(8)
    @DisplayName("[libra-test1-sql] 测试 DML (insert, select, update, delete)")
    void testDmlOperations() {
        String tableName = getTempTableName();
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);

        try {
            // 1. Create Table
            printExecuteResult("[libra-test1-sql] createTestTable", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "createTestTable", params));

            // 2. Insert User
            params.put("name", "Jane Doe");
            params.put("email", "jane.doe@example.com");
            params.put("age", 28);
            printExecuteResult("[libra-test1-sql] insertUser", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "insertUser", params));

            // 3. Select Users
            List<Map<String, Object>> users = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST1_XML, "selectUsers", params);
            printQueryResult("[libra-test1-sql] selectUsers after insert", users);
            int userId = (int) users.get(0).get("id");
            params.put("id", userId);

            // 4. Select User By Id
            Map<String, Object> user = SimpleSqlExecutor.selectOneFromXml(DB_NAME, LIBRA_TEST1_XML, "selectUserById", params);
            printSingleQueryResult("[libra-test1-sql] selectUserById", user);

            // 5. Update User
            params.put("name", "Jane Smith");
            params.put("email", "jane.smith@example.com");
            printExecuteResult("[libra-test1-sql] updateUser", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "updateUser", params));
            user = SimpleSqlExecutor.selectOneFromXml(DB_NAME, LIBRA_TEST1_XML, "selectUserById", params);
            printSingleQueryResult("[libra-test1-sql] selectUserById after update", user);

            // 6. Delete User
            printExecuteResult("[libra-test1-sql] deleteUser", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "deleteUser", params));
            user = SimpleSqlExecutor.selectOneFromXml(DB_NAME, LIBRA_TEST1_XML, "selectUserById", params);
            printSingleQueryResult("[libra-test1-sql] selectUserById after delete", user);

        } finally {
            // 7. Drop Table
            printExecuteResult("[libra-test1-sql] dropTestTable", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "dropTestTable", params));
        }
    }

    @Test
    @Order(14)
    @DisplayName("[libra-test1-sql] 测试 selectWithNestedConditions")
    void testSelectWithNestedConditions() {
        Map<String, Object> params = new HashMap<>();
        params.put("userType", "VIP");
        params.put("hasDiscount", "true");
        params.put("region", "West");
        List<Map<String, Object>> result = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST1_XML, "selectWithNestedConditions", params);
        printQueryResult("[libra-test1-sql] selectWithNestedConditions", result);
    }

    @Test
    @Order(15)
    @DisplayName("[libra-test1-sql] 测试 Batch DML (insert, select, update)")
    void testBatchOperations() {
        String tableName = getTempTableName();
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);

        try {
            // 1. Create Table
            printExecuteResult("[libra-test1-sql] createBatchTable", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "createBatchTable", params));

            // 2. Batch Insert
            params.put("name", "batch_user_1");
            params.put("email", "batch1@example.com");
            params.put("batch_id", "B001");
            printExecuteResult("[libra-test1-sql] batchInsert 1", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "batchInsert", params));
            params.put("name", "batch_user_2");
            params.put("email", "batch2@example.com");
            params.put("batch_id", "B002");
            printExecuteResult("[libra-test1-sql] batchInsert 2", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "batchInsert", params));

            // 3. Batch Select
            params.put("batchPrefix", "B");
            List<Map<String, Object>> users = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST1_XML, "batchSelect", params);
            printQueryResult("[libra-test1-sql] batchSelect after insert", users);

            // 4. Batch Update
            params.put("newName", "Updated Batch User");
            printExecuteResult("[libra-test1-sql] batchUpdate", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "batchUpdate", params));
            users = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST1_XML, "batchSelect", params);
            printQueryResult("[libra-test1-sql] batchSelect after update", users);

        } finally {
            // 5. Drop Table
            printExecuteResult("[libra-test1-sql] dropBatchTable", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "dropBatchTable", params));
        }
    }

    // ==================== libra-test2-sql.xml ====================

    @Test
    @Order(41)
    @DisplayName("[libra-test2-sql] 测试 testBasicWith")
    void testTestBasicWith() {
        Map<String, Object> params = new HashMap<>();
        params.put("plNos", "P01,P02");
        params.put("equipmentNo", "E001");
        params.put("stopReasonNames", "ReasonA,ReasonB");
        params.put("startDate", "2023-01-01");
        params.put("endDate", "2023-01-31");
        List<Map<String, Object>> result = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST2_XML, "testBasicWith", params);
        printQueryResult("[libra-test2-sql] testBasicWith", result);
    }

    @Test
    @Order(43)
    @DisplayName("[libra-test2-sql] 测试 queryCjsbgzmsList")
    void testQueryCjsbgzmsList() {
        Map<String, Object> params = new HashMap<>();
        params.put("plNos", "P01,P02,P03");
        params.put("equipmentNo", "E003");
        params.put("stopReasonNames", "ReasonA,ReasonC");
        params.put("startDate", "2023-01-01");
        params.put("endDate", "2023-03-31");
        List<Map<String, Object>> result = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST2_XML, "queryCjsbgzmsList", params);
        printQueryResult("[libra-test2-sql] queryCjsbgzmsList", result);
    }

    // ==================== libra-test3-sql.xml ====================

    @Test
    @Order(50)
    @DisplayName("[libra-test3-sql] 综合测试 (Create, Insert, Select, Drop)")
    void testLibraTest3Operations() {
        String tableName = "users_test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);

        try {
            // 1. Create users table
            Integer createResult = SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST3_XML, "createUsersTable", params);
            printExecuteResult("[libra-test3-sql] createUsersTable", createResult != null ? createResult : -1);

            // 2. Insert test data
            params.put("id", 1);
            params.put("name", "Alice");
            params.put("email", "alice@example.com");
            params.put("status", "active");
            Integer insertResult1 = SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST3_XML, "insertUser", params);
            printExecuteResult("[libra-test3-sql] insertUser 1", insertResult1 != null ? insertResult1 : -1);


            params.put("id", 2);
            params.put("name", "Bob");
            params.put("email", "bob@example.com");
            params.put("status", "inactive");
            Integer insertResult2 = SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST3_XML, "insertUser", params);
            printExecuteResult("[libra-test3-sql] insertUser 2", insertResult2 != null ? insertResult2 : -1);


            params.put("id", 999);
            params.put("name", "Test User");
            params.put("email", "test.user@example.com");
            params.put("status", "pending");
            Integer insertResult3 = SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST3_XML, "insertUser", params);
            printExecuteResult("[libra-test3-sql] insertUser 3", insertResult3 != null ? insertResult3 : -1);


            // 3. Run all find queries
            System.out.println("\n--- Running find queries on created table ---");

            // Test findUserById_simple
            Map<String, Object> findParams = new HashMap<>();
            findParams.put("tableName", tableName);
            findParams.put("userId", 999);
            Map<String, Object> resultOne = SimpleSqlExecutor.selectOneFromXml(DB_NAME, LIBRA_TEST3_XML, "findUserById_simple", findParams);
            printSingleQueryResult("[libra-test3-sql] findUserById_simple", resultOne);

            // Test findUserWithStatus_simple_dynamic
            findParams.clear();
            findParams.put("tableName", tableName);
            findParams.put("status", "inactive");
            List<Map<String, Object>> resultList = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST3_XML, "findUserWithStatus_simple_dynamic", findParams);
            printQueryResult("[libra-test3-sql] findUserWithStatus_simple_dynamic", resultList);

            // Test findUserWithNestedIf
            findParams.clear();
            findParams.put("tableName", tableName);
            findParams.put("status", "pending");
            findParams.put("name", "%Test%");
            resultList = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST3_XML, "findUserWithNestedIf", findParams);
            printQueryResult("[libra-test3-sql] findUserWithNestedIf", resultList);

            // Test findUserWithQuotedTest
            findParams.clear();
            findParams.put("tableName", tableName);
            findParams.put("status", "active"); // This param is used in the <if> but the query inside is static
            resultList = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST3_XML, "findUserWithQuotedTest", findParams);
            printQueryResult("[libra-test3-sql] findUserWithQuotedTest", resultList);

            // Test findUserWithComplexCondition
            findParams.clear();
            findParams.put("tableName", tableName);
            findParams.put("name", "Alice");
            resultList = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST3_XML, "findUserWithComplexCondition", findParams);
            printQueryResult("[libra-test3-sql] findUserWithComplexCondition", resultList);

        } finally {
            // 4. Drop users table
            Integer dropResult = SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST3_XML, "dropUsersTable", params);
            printExecuteResult("[libra-test3-sql] dropUsersTable", dropResult != null ? dropResult : -1);
        }
    }

    @Test
    @Order(46)
    @DisplayName("[libra-test3-sql] 测试 findProductInTable_simple_dynamic")
    void testFindProductInTable_simple_dynamic() {
        String tableName = "products_test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);

        try {
            // 1. Create products table
            Integer createResult = SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST3_XML, "createProductsTable", params);
            printExecuteResult("[libra-test3-sql] createProductsTable", createResult != null ? createResult : -1);

            // 2. Insert a product
            params.put("productId", 888);
            params.put("productName", "Test Product");
            params.put("price", 99.99);
            Integer insertResult = SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST3_XML, "insertProduct", params);
            printExecuteResult("[libra-test3-sql] insertProduct", insertResult != null ? insertResult : -1);

            // 3. Find the product
            List<Map<String, Object>> result = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST3_XML, "findProductInTable_simple_dynamic", params);
            printQueryResult("[libra-test3-sql] findProductInTable_simple_dynamic", result);

        } finally {
            // 4. Drop products table
            Integer dropResult = SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST3_XML, "dropProductsTable", params);
            printExecuteResult("[libra-test3-sql] dropProductsTable", dropResult != null ? dropResult : -1);
        }
    }

    // ==================== Remaining tests from libra-test1-sql.xml ====================

    @Test
    @Order(22)
    @DisplayName("[libra-test1-sql] 测试 Transaction DML")
    void testTransactionOperations() {
        String tableName = getTempTableName();
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", tableName);

        try {
            // 1. Create Table
            printExecuteResult("[libra-test1-sql] createTransactionTable", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "createTransactionTable", params));

            // 2. Insert Transaction User
            params.put("name", "tx_user");
            params.put("email", "tx@example.com");
            params.put("age", 50);
            params.put("transactionId", 999);
            printExecuteResult("[libra-test1-sql] insertTransactionUser", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "insertTransactionUser", params));

            // 3. Select Transaction Users
            List<Map<String, Object>> users = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST1_XML, "selectTransactionUsers", params);
            printQueryResult("[libra-test1-sql] selectTransactionUsers", users);

        } finally {
            // 4. Drop Table
            printExecuteResult("[libra-test1-sql] dropTransactionTable", SimpleSqlExecutor.executeFromXml(DB_NAME, LIBRA_TEST1_XML, "dropTransactionTable", params));
        }
    }

    @Test
    @Order(30)
    @DisplayName("[libra-test1-sql] 测试 fastQuery")
    void testFastQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 200);
        params.put("region", "North");
        List<Map<String, Object>> result = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST1_XML, "fastQuery", params);
        printQueryResult("[libra-test1-sql] fastQuery", result);
    }

    @Test
    @Order(31)
    @DisplayName("[libra-test1-sql] 测试 complexQuery")
    void testComplexQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 300);
        params.put("region", "South");
        List<Map<String, Object>> result = SimpleSqlExecutor.selectListFromXml(DB_NAME, LIBRA_TEST1_XML, "complexQuery", params);
        printQueryResult("[libra-test1-sql] complexQuery", result);
    }
}
