package cn.myslayers.libra.framework.toolkit.demo.test.database.database11;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil11;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DatabaseUtil11工具类 - YAML配置模式演示程序 main 方法
 *
 * 主要功能：
 * 1. 演示YAML配置模式的初始化流程
 * 2. 展示基础数据库操作（增删改查）
 * 3. 演示批量操作的使用方法
 * 4. 展示事务管理的使用方式
 * 5. 演示多数据源的切换操作
 *
 * 配置优势：
 * - 配置与代码分离，便于维护
 * - 支持多环境配置（dev、test、prod）
 * - 配置变更无需重新编译
 * - 符合Spring Boot规范
 *
 * 运行要求：
 * - 需要配置application-dev.yml文件
 * - 确保数据库连接正常
 * - 建议运行前备份重要数据
 *
 * @author Halavah
 * @version 3.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil11 - YAML配置模式测试")
public class DatabaseCase13Testing {

    /** 演示表名称 */
    private static final String TABLE_NAME = "yaml_demo_users";

    @BeforeAll
    static void setUp() {
        System.out.println(repeat("=", 80));
        System.out.println("🎯 DatabaseUtil11工具类 - YAML配置模式演示程序 v3.0");
        System.out.println(repeat("=", 80));
        try {
            // 步骤1：初始化YAML配置
            step1_InitYamlConfig();
            // 步骤2：创建演示表
            step2_CreateTable();
        } catch (Exception e) {
            System.err.println("\n❌ 测试初始化失败: " + e.getMessage());
            e.printStackTrace();
            // 如果初始化失败，后续测试无法进行，抛出异常
            throw new RuntimeException("测试初始化失败", e);
        }
    }

    @AfterAll
    static void tearDown() {
        try {
            // 步骤8：清理资源
            step8_Cleanup();
            System.out.println("\n✅ 演示程序执行完成！");
        } catch (Exception e) {
            System.err.println("\n❌ 资源清理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    @Order(1)
    @DisplayName("步骤3：基础插入操作演示")
    void testInsertOperations() {
        step3_InsertOperations();
    }

    @Test
    @Order(2)
    @DisplayName("步骤4：查询操作演示")
    void testQueryOperations() {
        step4_QueryOperations();
    }

    @Test
    @Order(3)
    @DisplayName("步骤5：更新操作演示")
    void testUpdateOperations() {
        step5_UpdateOperations();
    }

    @Test
    @Order(4)
    @DisplayName("步骤6：批量操作演示")
    void testBatchOperations() {
        step6_BatchOperations();
    }

    @Test
    @Order(5)
    @DisplayName("步骤7：事务管理演示")
    void testTransactionDemo() {
        step7_TransactionDemo();
    }

    /**
     * 步骤1：初始化YAML配置
     * 作用：设置YAML配置模式并初始化默认数据源
     * 用法：内部调用，演示YAML配置的初始化过程
     */
    private static void step1_InitYamlConfig() {
        System.out.println("\n📋 步骤1：初始化YAML配置");
        System.out.println(repeat("-", 50));

        try {
            // 注册数据库并设置为默认数据库
            DatabaseUtil11.registerDatabaseFromYml("master");

            // 显示当前配置状态
            System.out.println("🔧 配置模式: YAML配置");
            System.out.println("📊 默认数据库: " + DatabaseUtil11.getDefaultDatabase());
            System.out.println("✅ YAML配置初始化成功");

        } catch (Exception e) {
            throw new RuntimeException("YAML配置初始化失败", e);
        }
    }

    /**
     * 步骤2：创建演示表
     * 作用：创建用于演示的数据表，如果已存在则先删除
     * 用法：内部调用，准备演示环境
     */
    private static void step2_CreateTable() {
        System.out.println("\n🏗️ 步骤2：创建演示表");
        System.out.println(repeat("-", 50));

        try {
            // 删除已存在的表
            String dropSql = "DROP TABLE IF EXISTS " + TABLE_NAME;
            DatabaseUtil11.update(dropSql);
            System.out.println("🗑️ 删除旧表: " + TABLE_NAME);

            // 创建新表
            String createSql = "CREATE TABLE " + TABLE_NAME + " (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "age INT NOT NULL, " +
                    "email VARCHAR(100), " +
                    "config_mode VARCHAR(20) DEFAULT 'YAML', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            DatabaseUtil11.update(createSql);
            System.out.println("✅ 创建新表: " + TABLE_NAME);
            System.out.println("📋 表结构: id, name, age, email, config_mode, created_at");

        } catch (Exception e) {
            throw new RuntimeException("创建演示表失败", e);
        }
    }

    /**
     * 步骤3：基础插入操作演示
     * 作用：演示单条和多条数据的插入操作
     * 用法：内部调用，展示INSERT操作的使用方法
     */
    private static void step3_InsertOperations() {
        System.out.println("\n📝 步骤3：基础插入操作演示");
        System.out.println(repeat("-", 50));

        try {
            // 插入单条记录
            String insertSql = "INSERT INTO " + TABLE_NAME +
                    " (name, age, email, config_mode) VALUES (?, ?, ?, ?)";

            int rows1 = DatabaseUtil11.insert(insertSql, "张三", 25, "zhangsan@example.com", "YAML");
            System.out.println("➕ 插入用户: 张三, 影响行数: " + rows1);

            int rows2 = DatabaseUtil11.insert(insertSql, "李四", 30, "lisi@example.com", "YAML");
            System.out.println("➕ 插入用户: 李四, 影响行数: " + rows2);

            int rows3 = DatabaseUtil11.insert(insertSql, "王五", 28, "wangwu@example.com", "YAML");
            System.out.println("➕ 插入用户: 王五, 影响行数: " + rows3);

            System.out.println("✅ 基础插入操作完成，共插入 " + (rows1 + rows2 + rows3) + " 条记录");

        } catch (Exception e) {
            throw new RuntimeException("插入操作失败", e);
        }
    }

    /**
     * 步骤4：查询操作演示
     * 作用：演示各种查询操作，包括单条查询和多条查询
     * 用法：内部调用，展示SELECT操作的使用方法
     */
    private static void step4_QueryOperations() {
        System.out.println("\n🔍 步骤4：查询操作演示");
        System.out.println(repeat("-", 50));

        try {
            // 查询所有记录
            List<Map<String, Object>> allUsers = DatabaseUtil11.selectList(
                    "SELECT * FROM " + TABLE_NAME + " ORDER BY id");
            System.out.println("📊 查询所有用户，共 " + allUsers.size() + " 条记录:");

            for (Map<String, Object> user : allUsers) {
                System.out.println("   用户ID: " + user.get("id") +
                        ", 姓名: " + user.get("name") +
                        ", 年龄: " + user.get("age") +
                        ", 邮箱: " + user.get("email"));
            }

            // 条件查询
            List<Map<String, Object>> youngUsers = DatabaseUtil11.selectList(
                    "SELECT * FROM " + TABLE_NAME + " WHERE age < ? ORDER BY age",
                    30);
            System.out.println("\n🎯 查询年龄小于30的用户，共 " + youngUsers.size() + " 条记录:");

            for (Map<String, Object> user : youngUsers) {
                System.out.println("   " + user.get("name") + " - " + user.get("age") + "岁");
            }

            // 单条查询
            Map<String, Object> singleUser = DatabaseUtil11.selectOne(
                    "SELECT * FROM " + TABLE_NAME + " WHERE name = ?", "张三");
            if (singleUser != null) {
                System.out.println("\n👤 查询单个用户（张三）:");
                System.out.println("   ID: " + singleUser.get("id") +
                        ", 年龄: " + singleUser.get("age") +
                        ", 配置模式: " + singleUser.get("config_mode"));
            }

            System.out.println("✅ 查询操作演示完成");

        } catch (Exception e) {
            throw new RuntimeException("查询操作失败", e);
        }
    }

    /**
     * 步骤5：更新操作演示
     * 作用：演示数据更新操作，包括单条和批量更新
     * 用法：内部调用，展示UPDATE操作的使用方法
     */
    private static void step5_UpdateOperations() {
        System.out.println("\n✏️ 步骤5：更新操作演示");
        System.out.println(repeat("-", 50));

        try {
            // 更新单条记录
            int updateRows = DatabaseUtil11.update(
                    "UPDATE " + TABLE_NAME + " SET age = ?, email = ? WHERE name = ?",
                    26, "zhangsan_new@example.com", "张三");
            System.out.println("📝 更新张三的信息，影响行数: " + updateRows);

            // 批量更新
            int batchUpdateRows = DatabaseUtil11.update(
                    "UPDATE " + TABLE_NAME + " SET config_mode = ? WHERE age >= ?",
                    "YAML_BATCH", 26);
            System.out.println("📝 批量更新配置模式，影响行数: " + batchUpdateRows);

            // 验证更新结果
            Map<String, Object> updatedUser = DatabaseUtil11.selectOne(
                    "SELECT * FROM " + TABLE_NAME + " WHERE name = ?", "张三");
            if (updatedUser != null) {
                System.out.println("✅ 验证更新结果:");
                System.out.println("   张三新年龄: " + updatedUser.get("age"));
                System.out.println("   张三新邮箱: " + updatedUser.get("email"));
                System.out.println("   配置模式: " + updatedUser.get("config_mode"));
            }

            System.out.println("✅ 更新操作演示完成");

        } catch (Exception e) {
            throw new RuntimeException("更新操作失败", e);
        }
    }

    /**
     * 步骤6：批量操作演示
     * 作用：演示批量插入操作，提高数据处理效率
     * 用法：内部调用，展示批量操作的使用方法
     */
    private static void step6_BatchOperations() {
        System.out.println("\n📦 步骤6：批量操作演示");
        System.out.println(repeat("-", 50));

        try {
            // 准备批量插入数据
            List<Object[]> batchParams = new ArrayList<>();
            String[] names = {"赵六", "钱七", "孙八", "李九", "周十"};
            int[] ages = {22, 35, 29, 31, 27};

            for (int i = 0; i < names.length; i++) {
                batchParams.add(new Object[]{
                        names[i],
                        ages[i],
                        names[i].toLowerCase() + "@batch.com",
                        "YAML_BATCH"
                });
            }

            // 执行批量插入
            String batchSql = "INSERT INTO " + TABLE_NAME +
                    " (name, age, email, config_mode) VALUES (?, ?, ?, ?)";
            int[] batchResults = DatabaseUtil11.executeBatch(batchSql, batchParams);

            System.out.println("📦 批量插入 " + batchParams.size() + " 条记录");
            System.out.println("📊 批量操作结果: " + java.util.Arrays.toString(batchResults));

            // 统计总记录数
            Map<String, Object> countResult = DatabaseUtil11.selectOne(
                    "SELECT COUNT(*) as total FROM " + TABLE_NAME);
            System.out.println("📈 当前表中总记录数: " + countResult.get("total"));

            System.out.println("✅ 批量操作演示完成");

        } catch (Exception e) {
            throw new RuntimeException("批量操作失败", e);
        }
    }

    /**
     * 步骤7：事务管理演示
     * 作用：演示事务的提交和回滚机制
     * 用法：内部调用，展示事务管理的使用方法
     */
    private static void step7_TransactionDemo() {
        System.out.println("\n🔄 步骤7：事务管理演示");
        System.out.println(repeat("-", 50));

        try {
            // 事务操作演示（成功提交）
            String result = DatabaseUtil11.executeInTransaction(conn -> {
                // 在事务中执行多个操作
                DatabaseUtil11.insert("INSERT INTO " + TABLE_NAME +
                                " (name, age, email, config_mode) VALUES (?, ?, ?, ?)",
                        "事务用户1", 33, "transaction1@example.com", "TRANSACTION");

                DatabaseUtil11.insert("INSERT INTO " + TABLE_NAME +
                                " (name, age, email, config_mode) VALUES (?, ?, ?, ?)",
                        "事务用户2", 34, "transaction2@example.com", "TRANSACTION");

                DatabaseUtil11.update("UPDATE " + TABLE_NAME +
                                " SET config_mode = ? WHERE name = ?",
                        "TRANSACTION_UPDATE", "事务用户1");

                return "事务操作成功完成";
            });

            System.out.println("✅ 事务提交成功: " + result);

            // 验证事务结果
            List<Map<String, Object>> transactionUsers = DatabaseUtil11.selectList(
                    "SELECT * FROM " + TABLE_NAME + " WHERE config_mode LIKE 'TRANSACTION%'");
            System.out.println("📊 事务中创建的用户数量: " + transactionUsers.size());

            // 事务回滚演示（模拟错误）
            try {
                DatabaseUtil11.executeInTransaction(conn -> {
                    DatabaseUtil11.insert("INSERT INTO " + TABLE_NAME +
                                    " (name, age, email, config_mode) VALUES (?, ?, ?, ?)",
                            "回滚用户", 40, "rollback@example.com", "ROLLBACK");

                    // 模拟错误，触发回滚
                    throw new RuntimeException("模拟业务异常，触发事务回滚");
                });
            } catch (Exception e) {
                System.out.println("🔄 事务回滚演示: " + e.getMessage());
            }

            // 验证回滚结果
            Map<String, Object> rollbackUser = DatabaseUtil11.selectOne(
                    "SELECT * FROM " + TABLE_NAME + " WHERE name = ?", "回滚用户");
            if (rollbackUser == null) {
                System.out.println("✅ 事务回滚成功：回滚用户未被插入");
            }

            System.out.println("✅ 事务管理演示完成");

        } catch (Exception e) {
            throw new RuntimeException("事务管理演示失败", e);
        }
    }

    /**
     * 步骤8：清理资源
     * 作用：清理演示过程中创建的表和关闭数据库连接
     * 用法：内部调用，释放演示程序占用的资源
     */
    private static void step8_Cleanup() {
        System.out.println("\n🧹 步骤8：清理资源");
        System.out.println(repeat("-", 50));

        try {
            // 删除演示表
            String dropSql = "DROP TABLE IF EXISTS " + TABLE_NAME;
            DatabaseUtil11.update(dropSql);
            System.out.println("🗑️ 删除演示表: " + TABLE_NAME);

            // 关闭数据库连接
            DatabaseUtil11.closeAllDatabases();
            System.out.println("🔒 关闭所有数据库连接");

            System.out.println("✅ 资源清理完成");

        } catch (Exception e) {
            System.out.println("⚠️ 资源清理出现异常: " + e.getMessage());
        }
    }

    /**
     * 自定义字符串重复方法，兼容JDK8
     * 作用：重复指定字符串指定次数，替代JDK11的String.repeat()方法
     * 用法：repeat("=", 50) 输出50个等号
     *
     * @param str 要重复的字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    private static String repeat(String str, int count) {
        if (str == null || count <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
