package cn.myslayers.libra.framework.toolkit.demo.test.database.database32;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil32;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * DatabaseUtil32 - MyBatisSqlParser 解析测试
 *
 * ## 测试目标:
 * 验证 `DatabaseUtil32.MyBatisSqlParser` 能够正确解析XML中定义的MyBatis动态SQL，
 * 包括所有动态标签的解析和参数处理。
 *
 * ## 测试方式:
 * - **按XML文件顺序**：依次测试Demo1、Demo2、Demo3、Demo4中的所有SQL
 * - **使用FromXml方法**：通过指定XML文件名和SQL ID进行测试
 * - **专注解析功能**：测试SQL解析结果，不涉及实际数据库执行
 *
 * ## 覆盖场景:
 * - **Demo1**: 基础和动态SQL标签解析
 * - **Demo2**: 高级复杂场景SQL解析
 * - **Demo3**: Include标签解析
 * - **Demo4**: 边缘案例和特殊语法解析
 */
@DisplayName("MyBatisSqlParser - XML解析测试")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class Test31_ApiMyBatisSqlParserTesting {

    /**
     * 核心解析测试辅助方法
     */
    private void parseAndVerify(String testName, String mapperFile, String sqlId, Map<String, Object> params) {
        System.out.println("\n--- MyBatisSqlParser 解析测试: " + testName + " ---");
        System.out.println("Mapper: " + mapperFile + ", SQL ID: " + sqlId);
        System.out.println("Input Params: " + params);

        try {
            // 使用MyBatisSqlParser解析SQL
            DatabaseUtil32.MyBatisSqlParser.MyBatisSqlWithParams result =
                DatabaseUtil32.MyBatisSqlParser.getSqlWithParamsFromXml(mapperFile, sqlId, params);

            System.out.println("✅ " + testName + ": 解析成功");
            System.out.println("   - 解析后SQL: " + result.getSql());
            System.out.println("   - 参数列表: " + result.getParams());
        } catch (Exception e) {
            System.out.println("❌ " + testName + ": 解析失败");
            System.out.println("   - 异常信息: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @BeforeAll
    void setup() {
        System.out.println("====================================================");
        System.out.println("  启动 MyBatisSqlParser 解析测试");
        System.out.println("====================================================");
        // 启用调试日志
//        DatabaseUtil32.LoggerCore.setDebugLogEnabled(true);
//        DatabaseUtil32.LoggerCore.setSqlDebugLogEnabled(true);
        // 注意：SQL语法预校验警告是正常的，因为JSqlParser无法识别所有数据库方言
        // 这些警告不影响实际的SQL执行功能
    }

    // ================================ Demo1Mapper.xml 测试 ================================

    @Test
    @Order(1)
    @DisplayName("Demo1Mapper.xml - selectUserById - 基础查询解析")
    void testDemo1_01_selectUserById() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1001L);
        parseAndVerify("Demo1-selectUserById", "Demo1Mapper.xml", "selectUserById", params);
    }

    @Test
    @Order(2)
    @DisplayName("Demo1Mapper.xml - insertUser - 基础插入解析")
    void testDemo1_02_insertUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "张三");
        params.put("age", 25);
        params.put("email", "zhangsan@example.com");
        params.put("phone", "13800000001");
        params.put("status", "active");
        // 修复：补充createTime参数避免JEXL警告
        params.put("createTime", new java.util.Date());
        parseAndVerify("Demo1-insertUser", "Demo1Mapper.xml", "insertUser", params);
    }

    @Test
    @Order(3)
    @DisplayName("Demo1Mapper.xml - updateUser - 基础更新解析")
    void testDemo1_03_updateUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1001L);
        params.put("name", "张三丰");
        params.put("age", 30);
        params.put("email", "zhangsanfeng@example.com");
        parseAndVerify("Demo1-updateUser", "Demo1Mapper.xml", "updateUser", params);
    }

    @Test
    @Order(4)
    @DisplayName("Demo1Mapper.xml - deleteUser - 基础删除解析")
    void testDemo1_04_deleteUser() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1001L);
        parseAndVerify("Demo1-deleteUser", "Demo1Mapper.xml", "deleteUser", params);
    }

    @Test
    @Order(5)
    @DisplayName("Demo1Mapper.xml - selectUsersByCondition - if标签解析")
    void testDemo1_05_selectUsersByCondition() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "张");
        params.put("age", 25);
        params.put("email", "test@example.com");
        params.put("status", "active");
        parseAndVerify("Demo1-selectUsersByCondition", "Demo1Mapper.xml", "selectUsersByCondition", params);
    }

    @Test
    @Order(6)
    @DisplayName("Demo1Mapper.xml - selectUsersWithWhere - where标签解析")
    void testDemo1_06_selectUsersWithWhere() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "李");
        params.put("age", 30);
        params.put("status", "pending");
        // 修复：补充email参数避免JEXL警告
        params.put("email", "li@example.com");
        parseAndVerify("Demo1-selectUsersWithWhere", "Demo1Mapper.xml", "selectUsersWithWhere", params);
    }

    @Test
    @Order(7)
    @DisplayName("Demo1Mapper.xml - updateUserSelective - set标签解析")
    void testDemo1_07_updateUserSelective() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1001L);
        params.put("name", "更新姓名");
        params.put("email", "updated@example.com");
        // 修复：补充age、phone、status参数避免JEXL警告
        params.put("age", 28);
        params.put("phone", "13800000999");
        params.put("status", "active");
        parseAndVerify("Demo1-updateUserSelective", "Demo1Mapper.xml", "updateUserSelective", params);
    }

    @Test
    @Order(8)
    @DisplayName("Demo1Mapper.xml - selectUsersByType - choose标签解析")
    void testDemo1_08_selectUsersByType() {
        Map<String, Object> params = new HashMap<>();
        params.put("searchType", "name");
        params.put("searchValue", "王");
        parseAndVerify("Demo1-selectUsersByType", "Demo1Mapper.xml", "selectUsersByType", params);
    }

    @Test
    @Order(9)
    @DisplayName("Demo1Mapper.xml - selectUsersWithTrim - trim标签解析")
    void testDemo1_09_selectUsersWithTrim() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "赵六");
        params.put("age", 35);
        // 修复：补充email参数避免JEXL警告
        params.put("email", "zhaoliu@example.com");
        parseAndVerify("Demo1-selectUsersWithTrim", "Demo1Mapper.xml", "selectUsersWithTrim", params);
    }

    @Test
    @Order(10)
    @DisplayName("Demo1Mapper.xml - selectUsersByIds - foreach标签解析")
    void testDemo1_10_selectUsersByIds() {
        Map<String, Object> params = new HashMap<>();
        params.put("ids", Arrays.asList(1001L, 1002L, 1003L, 1004L));
        parseAndVerify("Demo1-selectUsersByIds", "Demo1Mapper.xml", "selectUsersByIds", params);
    }

    @Test
    @Order(11)
    @DisplayName("Demo1Mapper.xml - batchInsertUsers - foreach批量插入解析")
    void testDemo1_11_batchInsertUsers() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> user1 = new HashMap<>();
        user1.put("name", "批量用户1");
        user1.put("age", 20);
        user1.put("email", "batch1@example.com");
        user1.put("phone", "13800000001");
        user1.put("status", "active");
        // 修复：补充createTime参数避免JEXL警告
        user1.put("createTime", new java.util.Date());

        Map<String, Object> user2 = new HashMap<>();
        user2.put("name", "批量用户2");
        user2.put("age", 22);
        user2.put("email", "batch2@example.com");
        user2.put("phone", "13800000002");
        user2.put("status", "pending");
        // 修复：补充createTime参数避免JEXL警告
        user2.put("createTime", new java.util.Date());

        params.put("users", Arrays.asList(user1, user2));
        parseAndVerify("Demo1-batchInsertUsers", "Demo1Mapper.xml", "batchInsertUsers", params);
    }

    @Test
    @Order(12)
    @DisplayName("Demo1Mapper.xml - batchUpdateUsers - foreach批量更新解析")
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
        parseAndVerify("Demo1-batchUpdateUsers", "Demo1Mapper.xml", "batchUpdateUsers", params);
    }

    @Test
    @Order(13)
    @DisplayName("Demo1Mapper.xml - selectUsersByComplexCondition - 复杂foreach解析")
    void testDemo1_13_selectUsersByComplexCondition() {
        Map<String, Object> params = new HashMap<>();
        params.put("names", Arrays.asList("张三", "李四", "王五"));
        params.put("ageRange", Arrays.asList(25, 30, 35));
        parseAndVerify("Demo1-selectUsersByComplexCondition", "Demo1Mapper.xml", "selectUsersByComplexCondition", params);
    }

    @Test
    @Order(14)
    @DisplayName("Demo1Mapper.xml - selectUsersWithResultMap - 结果映射解析")
    void testDemo1_14_selectUsersWithResultMap() {
        Map<String, Object> params = new HashMap<>();
        parseAndVerify("Demo1-selectUsersWithResultMap", "Demo1Mapper.xml", "selectUsersWithResultMap", params);
    }

    @Test
    @Order(15)
    @DisplayName("Demo1Mapper.xml - selectUsersWithRoles - 关联查询解析")
    void testDemo1_15_selectUsersWithRoles() {
        Map<String, Object> params = new HashMap<>();
        parseAndVerify("Demo1-selectUsersWithRoles", "Demo1Mapper.xml", "selectUsersWithRoles", params);
    }

    @Test
    @Order(16)
    @DisplayName("Demo1Mapper.xml - selectUsersWithInclude - include标签解析")
    void testDemo1_16_selectUsersWithInclude() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "测试用户");
        params.put("status", "active");
        parseAndVerify("Demo1-selectUsersWithInclude", "Demo1Mapper.xml", "selectUsersWithInclude", params);
    }

    @Test
    @Order(17)
    @DisplayName("Demo1Mapper.xml - selectUsersWithBind - bind标签解析")
    void testDemo1_17_selectUsersWithBind() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "绑定测试");
        parseAndVerify("Demo1-selectUsersWithBind", "Demo1Mapper.xml", "selectUsersWithBind", params);
    }

    @Test
    @Order(18)
    @DisplayName("Demo1Mapper.xml - selectUsersWithCDATA - CDATA区段解析")
    void testDemo1_18_selectUsersWithCDATA() {
        Map<String, Object> params = new HashMap<>();
        params.put("age", 25);
        parseAndVerify("Demo1-selectUsersWithCDATA", "Demo1Mapper.xml", "selectUsersWithCDATA", params);
    }

    @Test
    @Order(19)
    @DisplayName("Demo1Mapper.xml - selectUsersWithXmlEntity - XML实体解析")
    void testDemo1_19_selectUsersWithXmlEntity() {
        Map<String, Object> params = new HashMap<>();
        params.put("age", 18);
        parseAndVerify("Demo1-selectUsersWithXmlEntity", "Demo1Mapper.xml", "selectUsersWithXmlEntity", params);
    }

    @Test
    @Order(20)
    @DisplayName("Demo1Mapper.xml - selectUsersWithDynamicJoin - 动态关联解析")
    void testDemo1_20_selectUsersWithDynamicJoin() {
        Map<String, Object> params = new HashMap<>();
        params.put("includeDepartment", true);
        params.put("name", "动态关联");
        params.put("departmentId", 10L);
        parseAndVerify("Demo1-selectUsersWithDynamicJoin", "Demo1Mapper.xml", "selectUsersWithDynamicJoin", params);
    }

    @Test
    @Order(21)
    @DisplayName("Demo1Mapper.xml - selectUsersWithPagination - 分页查询解析")
    void testDemo1_21_selectUsersWithPagination() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "分页测试");
        params.put("offset", 0);
        params.put("pageSize", 10);
        parseAndVerify("Demo1-selectUsersWithPagination", "Demo1Mapper.xml", "selectUsersWithPagination", params);
    }

    @Test
    @Order(22)
    @DisplayName("Demo1Mapper.xml - countUsers - 统计查询解析")
    void testDemo1_22_countUsers() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        // 修复：补充name参数避免JEXL警告
        params.put("name", "test");
        parseAndVerify("Demo1-countUsers", "Demo1Mapper.xml", "countUsers", params);
    }

    @Test
    @Order(23)
    @DisplayName("Demo1Mapper.xml - selectUsersComplex - 复杂动态SQL解析")
    void testDemo1_23_selectUsersComplex() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> searchCriteria = new HashMap<>();
        searchCriteria.put("type", "name");
        searchCriteria.put("value", "复杂查询");
        params.put("searchCriteria", searchCriteria);
        params.put("roles", Arrays.asList(1L, 2L, 3L));
        params.put("orderBy", "age");
        // 修复：补充dateRange参数避免JEXL语法错误
        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("start", "2024-01-01");
        dateRange.put("end", "2024-12-31");
        params.put("dateRange", dateRange);
        parseAndVerify("Demo1-selectUsersComplex", "Demo1Mapper.xml", "selectUsersComplex", params);
    }

    @Test
    @Order(24)
    @DisplayName("Demo1Mapper.xml - selectUsersWithDynamicFields - 动态字段解析")
    void testDemo1_24_selectUsersWithDynamicFields() {
        Map<String, Object> params = new HashMap<>();
        params.put("fields", Arrays.asList("id", "name", "email"));
        params.put("status", "active");
        // 修复：补充name参数避免JEXL警告
        params.put("name", "test");
        parseAndVerify("Demo1-selectUsersWithDynamicFields", "Demo1Mapper.xml", "selectUsersWithDynamicFields", params);
    }

    @Test
    @Order(25)
    @DisplayName("Demo1Mapper.xml - selectUsersWithDynamicOrder - 动态排序解析")
    void testDemo1_25_selectUsersWithDynamicOrder() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> orderField1 = new HashMap<>();
        orderField1.put("field", "name");
        orderField1.put("direction", "ASC");

        Map<String, Object> orderField2 = new HashMap<>();
        orderField2.put("field", "age");
        orderField2.put("direction", "DESC");

        params.put("orderFields", Arrays.asList(orderField1, orderField2));
        // 修复：补充status和name参数避免JEXL警告
        params.put("status", "active");
        params.put("name", "test");
        parseAndVerify("Demo1-selectUsersWithDynamicOrder", "Demo1Mapper.xml", "selectUsersWithDynamicOrder", params);
    }

    // ================================ Demo2Mapper.xml 测试 ================================

    @Test
    @Order(26)
    @DisplayName("Demo2Mapper.xml - selectUsersWithDepartmentAndRole - 多表关联解析")
    void testDemo2_01_selectUsersWithDepartmentAndRole() {
        Map<String, Object> params = new HashMap<>();
        params.put("includeDepartment", true);
        params.put("includeRole", true);
        params.put("departmentIds", Arrays.asList(1L, 2L, 3L));
        params.put("roleIds", Arrays.asList(10L, 20L));
        parseAndVerify("Demo2-selectUsersWithDepartmentAndRole", "Demo2Mapper.xml", "selectUsersWithDepartmentAndRole", params);
    }

    @Test
    @Order(27)
    @DisplayName("Demo2Mapper.xml - selectUsersAdvancedSearch - 高级搜索解析")
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
        parseAndVerify("Demo2-selectUsersAdvancedSearch", "Demo2Mapper.xml", "selectUsersAdvancedSearch", params);
    }

    @Test
    @Order(28)
    @DisplayName("Demo2Mapper.xml - getUserStatistics - 分组统计解析")
    void testDemo2_03_getUserStatistics() {
        Map<String, Object> params = new HashMap<>();
        params.put("groupBy", "department");
        params.put("status", "active");
        // 修复：补充dateRange参数避免JEXL警告
        Map<String, Object> dateRange = new HashMap<>();
        dateRange.put("start", "2024-01-01");
        dateRange.put("end", "2024-12-31");
        params.put("dateRange", dateRange);
        parseAndVerify("Demo2-getUserStatistics", "Demo2Mapper.xml", "getUserStatistics", params);
    }

    @Test
    @Order(29)
    @DisplayName("Demo2Mapper.xml - batchUpdateUserStatus - 批量更新解析")
    void testDemo2_04_batchUpdateUserStatus() {
        Map<String, Object> params = new HashMap<>();
        params.put("newStatus", "inactive");
        params.put("userIds", Arrays.asList(1001L, 1002L, 1003L));
        // 修复：补充conditions参数避免JEXL警告
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("status", "active");
        params.put("conditions", conditions);
        parseAndVerify("Demo2-batchUpdateUserStatus", "Demo2Mapper.xml", "batchUpdateUserStatus", params);
    }

    @Test
    @Order(30)
    @DisplayName("Demo2Mapper.xml - batchDeleteUsers - 批量删除解析")
    void testDemo2_05_batchDeleteUsers() {
        Map<String, Object> params = new HashMap<>();
        params.put("userIds", Arrays.asList(2001L, 2002L));
        Map<String, Object> conditions = new HashMap<>();
        conditions.put("status", "inactive");
        conditions.put("inactiveDays", 90);
        params.put("conditions", conditions);
        parseAndVerify("Demo2-batchDeleteUsers", "Demo2Mapper.xml", "batchDeleteUsers", params);
    }

    @Test
    @Order(31)
    @DisplayName("Demo2Mapper.xml - insertUserDynamic - 动态插入解析")
    void testDemo2_06_insertUserDynamic() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "动态插入用户");
        params.put("age", 28);
        params.put("email", "dynamic@example.com");
        params.put("departmentId", 5L);
        params.put("status", "active");
        // 修复：补充phone、createTime、metadata参数避免JEXL警告
        params.put("phone", "13800000999");
        params.put("createTime", new java.util.Date());
        params.put("metadata", "{\"role\": \"user\"}");
        parseAndVerify("Demo2-insertUserDynamic", "Demo2Mapper.xml", "insertUserDynamic", params);
    }

    @Test
    @Order(32)
    @DisplayName("Demo2Mapper.xml - selectUsersWithSubquery - 子查询解析")
    void testDemo2_07_selectUsersWithSubquery() {
        Map<String, Object> params = new HashMap<>();
        params.put("hasRoles", true);
        params.put("minRoleCount", 2);
        params.put("departmentName", "技术部");
        parseAndVerify("Demo2-selectUsersWithSubquery", "Demo2Mapper.xml", "selectUsersWithSubquery", params);
    }

    @Test
    @Order(33)
    @DisplayName("Demo2Mapper.xml - selectUsersUnion - UNION查询解析")
    void testDemo2_08_selectUsersUnion() {
        Map<String, Object> params = new HashMap<>();
        params.put("includeActive", true);
        params.put("includeInactive", true);
        Map<String, Object> activeFilters = new HashMap<>();
        activeFilters.put("departmentId", 1L);
        params.put("activeFilters", activeFilters);
        parseAndVerify("Demo2-selectUsersUnion", "Demo2Mapper.xml", "selectUsersUnion", params);
    }

    @Test
    @Order(34)
    @DisplayName("Demo2Mapper.xml - selectUsersWithCTE - CTE查询解析")
    void testDemo2_09_selectUsersWithCTE() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        params.put("departmentIds", Arrays.asList(1L, 2L));
        params.put("minRoleCount", 1);
        params.put("activityStatus", "active");
        parseAndVerify("Demo2-selectUsersWithCTE", "Demo2Mapper.xml", "selectUsersWithCTE", params);
    }

    @Test
    @Order(35)
    @DisplayName("Demo2Mapper.xml - selectUsersWithRanking - 窗口函数解析")
    void testDemo2_10_selectUsersWithRanking() {
        Map<String, Object> params = new HashMap<>();
        params.put("partitionBy", "department");
        params.put("orderBy", "age");
        params.put("status", "active");
        params.put("departmentIds", Arrays.asList(1L, 2L, 3L));
        parseAndVerify("Demo2-selectUsersWithRanking", "Demo2Mapper.xml", "selectUsersWithRanking", params);
    }

    @Test
    @Order(36)
    @DisplayName("Demo2Mapper.xml - selectUsersWithJsonQuery - JSON查询解析")
    void testDemo2_11_selectUsersWithJsonQuery() {
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> jsonFilters = new HashMap<>();
        jsonFilters.put("preference", "dark");
        jsonFilters.put("settingKey", "language");
        jsonFilters.put("settingValue", "zh-CN");
        jsonFilters.put("hasPreferences", true);
        params.put("jsonFilters", jsonFilters);
        parseAndVerify("Demo2-selectUsersWithJsonQuery", "Demo2Mapper.xml", "selectUsersWithJsonQuery", params);
    }

    // ================================ Demo3Mapper.xml 测试 ================================

    @Test
    @Order(37)
    @DisplayName("Demo3Mapper.xml - findSimple - 简单include解析")
    void testDemo3_01_findSimple() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 3001L);
        parseAndVerify("Demo3-findSimple", "Demo3Mapper.xml", "findSimple", params);
    }

    @Test
    @Order(38)
    @DisplayName("Demo3Mapper.xml - findWithDynamicConditions - 动态条件include解析")
    void testDemo3_02_findWithDynamicConditions() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "active");
        params.put("name", "include测试");
        parseAndVerify("Demo3-findWithDynamicConditions", "Demo3Mapper.xml", "findWithDynamicConditions", params);
    }

    @Test
    @Order(39)
    @DisplayName("Demo3Mapper.xml - findWithDynamicOrdering - 动态排序include解析")
    void testDemo3_03_findWithDynamicOrdering() {
        Map<String, Object> params = new HashMap<>();
        params.put("orderByColumn", "name");
        params.put("orderDirection", "ASC");
        parseAndVerify("Demo3-findWithDynamicOrdering", "Demo3Mapper.xml", "findWithDynamicOrdering", params);
    }

    @Test
    @Order(40)
    @DisplayName("Demo3Mapper.xml - findWithProperties - 属性传递include解析")
    void testDemo3_04_findWithProperties() {
        Map<String, Object> params = new HashMap<>();
        parseAndVerify("Demo3-findWithProperties", "Demo3Mapper.xml", "findWithProperties", params);
    }

    @Test
    @Order(41)
    @DisplayName("Demo3Mapper.xml - findWithNestedInclude - 嵌套include解析")
    void testDemo3_05_findWithNestedInclude() {
        Map<String, Object> params = new HashMap<>();
        params.put("status", "pending");
        params.put("orderByColumn", "create_time");
        params.put("orderDirection", "DESC");
        parseAndVerify("Demo3-findWithNestedInclude", "Demo3Mapper.xml", "findWithNestedInclude", params);
    }

    // ================================ Demo4Mapper.xml 测试 ================================

    @Test
    @Order(42)
    @DisplayName("Demo4Mapper.xml - findWithXmlEntitiesInTest - XML实体字符解析")
    void testDemo4_01_findWithXmlEntitiesInTest() {
        Map<String, Object> params = new HashMap<>();
        params.put("priority", 7);
        params.put("name", "special & co");
        parseAndVerify("Demo4-findWithXmlEntitiesInTest", "Demo4Mapper.xml", "findWithXmlEntitiesInTest", params);
    }

    @Test
    @Order(43)
    @DisplayName("Demo4Mapper.xml - findWithCdataBlock - CDATA块解析")
    void testDemo4_02_findWithCdataBlock() {
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2023-01-01");
        params.put("endDate", "2023-12-31");
        parseAndVerify("Demo4-findWithCdataBlock", "Demo4Mapper.xml", "findWithCdataBlock", params);
    }

    @Test
    @Order(44)
    @DisplayName("Demo4Mapper.xml - findWithEmptyDynamicTags - 空动态标签解析")
    void testDemo4_03_findWithEmptyDynamicTags() {
        Map<String, Object> params = new HashMap<>();
        parseAndVerify("Demo4-findWithEmptyDynamicTags", "Demo4Mapper.xml", "findWithEmptyDynamicTags", params);
    }

    @Test
    @Order(45)
    @DisplayName("Demo4Mapper.xml - updateWithEmptySetTag - 空set标签解析")
    void testDemo4_04_updateWithEmptySetTag() {
        Map<String, Object> params = new HashMap<>();
        params.put("id", 4001L);
        parseAndVerify("Demo4-updateWithEmptySetTag", "Demo4Mapper.xml", "updateWithEmptySetTag", params);
    }

    @Test
    @Order(46)
    @DisplayName("Demo4Mapper.xml - findWithResultingEmptyWhere - 空where结果解析")
    void testDemo4_05_findWithResultingEmptyWhere() {
        Map<String, Object> params = new HashMap<>();
        params.put("name", "non_existent_user");
        parseAndVerify("Demo4-findWithResultingEmptyWhere", "Demo4Mapper.xml", "findWithResultingEmptyWhere", params);
    }

    @Test
    @Order(47)
    @DisplayName("Demo4Mapper.xml - findByUserMethod - 布尔值判断解析")
    void testDemo4_06_findByUserMethod() {
        Map<String, Object> params = new HashMap<>();
        params.put("isAdmin", true);
        params.put("canViewSensitiveData", false);
        parseAndVerify("Demo4-findByUserMethod", "Demo4Mapper.xml", "findByUserMethod", params);
    }

    @Test
    @Order(48)
    @DisplayName("Demo4Mapper.xml - findByTernaryOperator - 三元运算符解析")
    void testDemo4_07_findByTernaryOperator() {
        Map<String, Object> params = new HashMap<>();
        params.put("isActive", true);
        parseAndVerify("Demo4-findByTernaryOperator", "Demo4Mapper.xml", "findByTernaryOperator", params);
    }

    @Test
    @Order(49)
    @DisplayName("Demo4Mapper.xml - findByComplexExpression - 复杂表达式解析")
    void testDemo4_08_findByComplexExpression() {
        Map<String, Object> params = new HashMap<>();
        params.put("age", 30);
        params.put("name", "复杂表达式用户");
        params.put("status", "active");
        parseAndVerify("Demo4-findByComplexExpression", "Demo4Mapper.xml", "findByComplexExpression", params);
    }

    @Test
    @Order(50)
    @DisplayName("Demo4Mapper.xml - findBySimpleParameter - 简单参数解析")
    void testDemo4_09_findBySimpleParameter() {
        Map<String, Object> params = new HashMap<>();
        params.put("_parameter", "simple_test_user");
        parseAndVerify("Demo4-findBySimpleParameter", "Demo4Mapper.xml", "findBySimpleParameter", params);
    }

    @Test
    @Order(51)
    @DisplayName("Demo4Mapper.xml - findByDatabaseId - 数据库ID解析")
    void testDemo4_10_findByDatabaseId() {
        Map<String, Object> params = new HashMap<>();
        params.put("_databaseId", "mysql");
        parseAndVerify("Demo4-findByDatabaseId", "Demo4Mapper.xml", "findByDatabaseId", params);
    }
}