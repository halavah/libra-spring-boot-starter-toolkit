package cn.myslayers.libra.framework.toolkit.demo.test.database.database31;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.SimpleSqlParser.SimpleSqlParameterConverter;
import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil31.SimpleSqlParser.SimpleSqlWithParams;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil31 - 全面的XML文件SQL解析测试")
public class Test12_SimpleSqlParser_XmlFileSqlTesting {

    private static final String LIBRA_TEST1_XML = "libra-test1-sql.xml";
    private static final String LIBRA_TEST2_XML = "libra-test2-sql.xml";
    private static final String LIBRA_TEST3_XML = "libra-test3-sql.xml";

    @BeforeAll
    static void setUpAll() {
//        DatabaseUtil31.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil31.LoggerCore.setSqlDebugLogEnabled(true);
        // 可强制刷新的初始化方法
        DatabaseUtil31.SimpleSqlParser.initializeSqlCache(true);
        System.out.println("========== SimpleSqlParser 直接SQL执行测试开始 ==========");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("========== SimpleSqlParser 直接SQL执行测试结束 ==========");
    }

    private void printResult(String testName, SimpleSqlWithParams result) {
        System.out.println("--- " + testName + " ---");
        if (result != null) {
            System.out.println("Parsed SQL: " + result.getSql());
            System.out.println("Parameters: " + result.getParams().toString());
        } else {
            System.out.println("Result is null.");
        }
        System.out.println();
    }

    // ==================== libra-test1-sql.xml ====================

    @Test
    @Order(1)
    @DisplayName("[libra-test1-sql] 测试 selectBasicList")
    void testSelectBasicList() {
        Map<String, Object> params = new HashMap<>();
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectBasicList", params);
        printResult("[libra-test1-sql] selectBasicList", result);
    }

    @Test
    @Order(2)
    @DisplayName("[libra-test1-sql] 测试 selectBasicOne")
    void testSelectBasicOne() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectBasicOne", params);
        printResult("[libra-test1-sql] selectBasicOne", result);
    }

    @Test
    @Order(3)
    @DisplayName("[libra-test1-sql] 测试 selectWithCondition")
    void testSelectWithCondition() {
        Map<String, Object> params = new HashMap<>();
        params.put("minAge", 18);
        params.put("maxAge", 60);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectWithCondition", params);
        printResult("[libra-test1-sql] selectWithCondition", result);
    }

    @Test
    @Order(4)
    @DisplayName("[libra-test1-sql] 测试 selectWithNumbers")
    void testSelectWithNumbers() {
        Map<String, Object> params = new HashMap<>();
        params.put("minAge", 20);
        params.put("maxAge", 50);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectWithNumbers", params);
        printResult("[libra-test1-sql] selectWithNumbers", result);
    }

    @Test
    @Order(5)
    @DisplayName("[libra-test1-sql] 测试 selectWithParams")
    void testSelectWithParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "John Doe");
        params.put("age", 30);
        params.put("email", "john.doe@example.com");
        params.put("description", "A test user.");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectWithParams", params);
        printResult("[libra-test1-sql] selectWithParams", result);
    }

    @Test
    @Order(6)
    @DisplayName("[libra-test1-sql] 测试 dropTestTable")
    void testDropTestTable() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "temp_users");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "dropTestTable", params);
        printResult("[libra-test1-sql] dropTestTable", result);
    }

    @Test
    @Order(7)
    @DisplayName("[libra-test1-sql] 测试 createTestTable")
    void testCreateTestTable() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "temp_users");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "createTestTable", params);
        printResult("[libra-test1-sql] createTestTable", result);
    }

    @Test
    @Order(8)
    @DisplayName("[libra-test1-sql] 测试 insertUser")
    void testInsertUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "temp_users");
        params.put("name", "Jane Doe");
        params.put("email", "jane.doe@example.com");
        params.put("age", 28);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "insertUser", params);
        printResult("[libra-test1-sql] insertUser", result);
    }

    @Test
    @Order(9)
    @DisplayName("[libra-test1-sql] 测试 selectUsers")
    void testSelectUsers() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "temp_users");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectUsers", params);
        printResult("[libra-test1-sql] selectUsers", result);
    }

    @Test
    @Order(10)
    @DisplayName("[libra-test1-sql] 测试 selectUserById")
    void testSelectUserById() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "temp_users");
        params.put("id", 101);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectUserById", params);
        printResult("[libra-test1-sql] selectUserById", result);
    }

    @Test
    @Order(11)
    @DisplayName("[libra-test1-sql] 测试 updateUser")
    void testUpdateUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "temp_users");
        params.put("id", 101);
        params.put("name", "Jane Smith");
        params.put("email", "jane.smith@example.com");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "updateUser", params);
        printResult("[libra-test1-sql] updateUser", result);
    }

    @Test
    @Order(12)
    @DisplayName("[libra-test1-sql] 测试 deleteUser")
    void testDeleteUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "temp_users");
        params.put("id", 101);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "deleteUser", params);
        printResult("[libra-test1-sql] deleteUser", result);
    }

    @Test
    @Order(13)
    @DisplayName("[libra-test1-sql] 测试 selectBasicQuery")
    void testSelectBasicQuery() {
        Map<String, Object> params = new HashMap<>();
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectBasicQuery", params);
        printResult("[libra-test1-sql] selectBasicQuery", result);
    }

    @Test
    @Order(14)
    @DisplayName("[libra-test1-sql] 测试 selectWithNestedConditions")
    void testSelectWithNestedConditions() {
        Map<String, Object> params = new HashMap<>();
        params.put("userType", "VIP");
        params.put("hasDiscount", "true");
        params.put("region", "West");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectWithNestedConditions", params);
        printResult("[libra-test1-sql] selectWithNestedConditions", result);
    }

    @Test
    @Order(15)
    @DisplayName("[libra-test1-sql] 测试 createBatchTable")
    void testCreateBatchTable() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "batch_ops");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "createBatchTable", params);
        printResult("[libra-test1-sql] createBatchTable", result);
    }

    @Test
    @Order(16)
    @DisplayName("[libra-test1-sql] 测试 insertBatchUser")
    void testInsertBatchUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "batch_ops");
        params.put("name", "batch_user");
        params.put("email", "batch@example.com");
        params.put("age", 40);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "insertBatchUser", params);
        printResult("[libra-test1-sql] insertBatchUser", result);
    }

    @Test
    @Order(17)
    @DisplayName("[libra-test1-sql] 测试 batchInsert")
    void testBatchInsert() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "batch_ops");
        params.put("name", "batch_user_2");
        params.put("email", "batch2@example.com");
        params.put("batch_id", "B001");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "batchInsert", params);
        printResult("[libra-test1-sql] batchInsert", result);
    }

    @Test
    @Order(18)
    @DisplayName("[libra-test1-sql] 测试 batchSelect")
    void testBatchSelect() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "batch_ops");
        params.put("batchPrefix", "B");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "batchSelect", params);
        printResult("[libra-test1-sql] batchSelect", result);
    }

    @Test
    @Order(19)
    @DisplayName("[libra-test1-sql] 测试 batchUpdate")
    void testBatchUpdate() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "batch_ops");
        params.put("newName", "Updated Batch User");
        params.put("batchPrefix", "B");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "batchUpdate", params);
        printResult("[libra-test1-sql] batchUpdate", result);
    }

    @Test
    @Order(20)
    @DisplayName("[libra-test1-sql] 测试 selectBatchUsers")
    void testSelectBatchUsers() {
        Map<String, Object> params = new HashMap<>();
        params.put("batchId", "B001");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectBatchUsers", params);
        printResult("[libra-test1-sql] selectBatchUsers", result);
    }

    @Test
    @Order(21)
    @DisplayName("[libra-test1-sql] 测试 dropBatchTable")
    void testDropBatchTable() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "batch_ops");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "dropBatchTable", params);
        printResult("[libra-test1-sql] dropBatchTable", result);
    }

    @Test
    @Order(22)
    @DisplayName("[libra-test1-sql] 测试 createTransactionTable")
    void testCreateTransactionTable() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "tx_table");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "createTransactionTable", params);
        printResult("[libra-test1-sql] createTransactionTable", result);
    }

    @Test
    @Order(23)
    @DisplayName("[libra-test1-sql] 测试 insertTransactionUser")
    void testInsertTransactionUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "tx_table");
        params.put("name", "tx_user");
        params.put("email", "tx@example.com");
        params.put("age", 50);
        params.put("transactionId", 999);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "insertTransactionUser", params);
        printResult("[libra-test1-sql] insertTransactionUser", result);
    }

    @Test
    @Order(24)
    @DisplayName("[libra-test1-sql] 测试 selectTransactionUsers")
    void testSelectTransactionUsers() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "tx_table");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectTransactionUsers", params);
        printResult("[libra-test1-sql] selectTransactionUsers", result);
    }

    @Test
    @Order(25)
    @DisplayName("[libra-test1-sql] 测试 dropTransactionTable")
    void testDropTransactionTable() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "tx_table");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "dropTransactionTable", params);
        printResult("[libra-test1-sql] dropTransactionTable", result);
    }

    @Test
    @Order(26)
    @DisplayName("[libra-test1-sql] 测试 queryWithEmptyParams")
    void testQueryWithEmptyParams() {
        Map<String, Object> params = new HashMap<>();
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "queryWithEmptyParams", params);
        printResult("[libra-test1-sql] queryWithEmptyParams", result);
    }

    @Test
    @Order(27)
    @DisplayName("[libra-test1-sql] 测试 queryWithNullParams")
    void testQueryWithNullParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("nullParam", null);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "queryWithNullParams", params);
        printResult("[libra-test1-sql] queryWithNullParams", result);
    }

    @Test
    @Order(28)
    @DisplayName("[libra-test1-sql] 测试 queryWithLongString")
    void testQueryWithLongString() {
        Map<String, Object> params = new HashMap<>();
        params.put("longString", "a_very_long_string_for_testing_purposes");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "queryWithLongString", params);
        printResult("[libra-test1-sql] queryWithLongString", result);
    }

    @Test
    @Order(29)
    @DisplayName("[libra-test1-sql] 测试 queryWithSpecialChars")
    void testQueryWithSpecialChars() {
        Map<String, Object> params = new HashMap<>();
        params.put("specialChars", "!@#$%^&*()_+-=[]{}|;':,./<>?");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "queryWithSpecialChars", params);
        printResult("[libra-test1-sql] queryWithSpecialChars", result);
    }

    @Test
    @Order(30)
    @DisplayName("[libra-test1-sql] 测试 fastQuery")
    void testFastQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 200);
        params.put("region", "North");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "fastQuery", params);
        printResult("[libra-test1-sql] fastQuery", result);
    }

    @Test
    @Order(31)
    @DisplayName("[libra-test1-sql] 测试 complexQuery")
    void testComplexQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 300);
        params.put("region", "South");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "complexQuery", params);
        printResult("[libra-test1-sql] complexQuery", result);
    }

    @Test
    @Order(32)
    @DisplayName("[libra-test1-sql] 测试 selectFromDefault")
    void testSelectFromDefault() {
        Map<String, Object> params = new HashMap<>();
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectFromDefault", params);
        printResult("[libra-test1-sql] selectFromDefault", result);
    }

    @Test
    @Order(33)
    @DisplayName("[libra-test1-sql] 测试 selectWithDbInfo")
    void testSelectWithDbInfo() {
        Map<String, Object> params = new HashMap<>();
        params.put("currentDb", "test_db");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectWithDbInfo", params);
        printResult("[libra-test1-sql] selectWithDbInfo", result);
    }

    @Test
    @Order(34)
    @DisplayName("[libra-test1-sql] 测试 concurrentQuery")
    void testConcurrentQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("threadId", 1);
        params.put("queryId", 1001);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "concurrentQuery", params);
        printResult("[libra-test1-sql] concurrentQuery", result);
    }

    @Test
    @Order(35)
    @DisplayName("[libra-test1-sql] 测试 concurrentInsert")
    void testConcurrentInsert() {
        Map<String, Object> params = new HashMap<>();
        params.put("threadId", 2);
        params.put("operationType", "INSERT");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "concurrentInsert", params);
        printResult("[libra-test1-sql] concurrentInsert", result);
    }

    @Test
    @Order(36)
    @DisplayName("[libra-test1-sql] 测试 concurrentSafeQuery")
    void testConcurrentSafeQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("threadId", 3);
        params.put("queryId", 1002);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "concurrentSafeQuery", params);
        printResult("[libra-test1-sql] concurrentSafeQuery", result);
    }

    @Test
    @Order(37)
    @DisplayName("[libra-test1-sql] 测试 queryWithComplexTypes")
    void testQueryWithComplexTypes() {
        Map<String, Object> params = new HashMap<>();
        params.put("boolParam", true);
        params.put("numberParam", 123.45);
        params.put("stringParam", "complex");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "queryWithComplexTypes", params);
        printResult("[libra-test1-sql] queryWithComplexTypes", result);
    }

    @Test
    @Order(38)
    @DisplayName("[libra-test1-sql] 测试 queryWithExpressions")
    void testQueryWithExpressions() {
        Map<String, Object> params = new HashMap<>();
        params.put("score1", 80);
        params.put("score2", 90);
        params.put("threshold", 75);
        params.put("operator", ">");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "queryWithExpressions", params);
        printResult("[libra-test1-sql] queryWithExpressions", result);
    }

    @Test
    @Order(39)
    @DisplayName("[libra-test1-sql] 测试 queryWithNestedParams")
    void testQueryWithNestedParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("searchType", "user");
        params.put("includeInactive", false);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "queryWithNestedParams", params);
        printResult("[libra-test1-sql] queryWithNestedParams", result);
    }

    @Test
    @Order(40)
    @DisplayName("[libra-test1-sql] 测试 selectInvalidSql")
    void testSelectInvalidSql() {
        Map<String, Object> params = new HashMap<>();
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST1_XML, "selectInvalidSql", params);
        printResult("[libra-test1-sql] selectInvalidSql", result);
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
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST2_XML, "testBasicWith", params);
        printResult("[libra-test2-sql] testBasicWith", result);
    }

    @Test
    @Order(42)
    @DisplayName("[libra-test2-sql] 测试 testSimpleQuery")
    void testTestSimpleQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("plNos", "P03");
        params.put("equipmentNo", "E002");
        params.put("stopReasonNames", "ReasonC");
        params.put("startDate", "2023-02-01");
        params.put("endDate", "2023-02-28");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST2_XML, "testSimpleQuery", params);
        printResult("[libra-test2-sql] testSimpleQuery", result);
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
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST2_XML, "queryCjsbgzmsList", params);
        printResult("[libra-test2-sql] queryCjsbgzmsList", result);
    }

    // ==================== libra-test3-sql.xml ====================

    @Test
    @Order(44)
    @DisplayName("[libra-test3-sql] 测试 findUserById_simple")
    void testFindUserById_simple() {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", 999);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST3_XML, "findUserById_simple", params);
        printResult("[libra-test3-sql] findUserById_simple", result);
    }

    @Test
    @Order(45)
    @DisplayName("[libra-test3-sql] 测试 findUserWithStatus_simple_dynamic")
    void testFindUserWithStatus_simple_dynamic() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "inactive");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST3_XML, "findUserWithStatus_simple_dynamic", params);
        printResult("[libra-test3-sql] findUserWithStatus_simple_dynamic", result);
    }

    @Test
    @Order(46)
    @DisplayName("[libra-test3-sql] 测试 findProductInTable_simple_dynamic")
    void testFindProductInTable_simple_dynamic() {
        Map<String, Object> params = new HashMap<>();
        params.put("tableName", "archive_products");
        params.put("productId", 888);
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST3_XML, "findProductInTable_simple_dynamic", params);
        printResult("[libra-test3-sql] findProductInTable_simple_dynamic", result);
    }

    @Test
    @Order(47)
    @DisplayName("[libra-test3-sql] 测试 findUserWithNestedIf")
    void testFindUserWithNestedIf() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "pending");
        params.put("name", "%test%");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST3_XML, "findUserWithNestedIf", params);
        printResult("[libra-test3-sql] findUserWithNestedIf", result);
    }

    @Test
    @Order(48)
    @DisplayName("[libra-test3-sql] 测试 findUserWithQuotedTest")
    void testFindUserWithQuotedTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST3_XML, "findUserWithQuotedTest", params);
        printResult("[libra-test3-sql] findUserWithQuotedTest", result);
    }

    @Test
    @Order(49)
    @DisplayName("[libra-test3-sql] 测试 findUserWithComplexCondition")
    void testFindUserWithComplexCondition() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "complex_user");
        SimpleSqlWithParams result = SimpleSqlParameterConverter.convertXmlFile(LIBRA_TEST3_XML, "findUserWithComplexCondition", params);
        printResult("[libra-test3-sql] findUserWithComplexCondition", result);
    }
}
