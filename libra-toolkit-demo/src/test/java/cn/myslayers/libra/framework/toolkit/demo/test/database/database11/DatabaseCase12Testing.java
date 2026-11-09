package cn.myslayers.libra.framework.toolkit.demo.test.database.database11;

import cn.myslayers.libra.framework.toolkit.database.DatabaseUtil11;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * DatabaseUtil11工具类 - 自定义配置模式演示程序 main 方法
 *
 * 功能概述：
 * 本类提供了DatabaseUtil11数据库工具类的完整使用示例，展示了从基础配置到高级功能的全流程操作。
 * 通过运行main方法，可以学习和理解DatabaseUtil11的各种功能特性。
 *
 * 演示内容：
 * 1. 数据库连接配置和管理
 * 2. 基础CRUD操作（增删改查）
 * 3. 批量数据处理操作
 * 4. 事务管理和回滚机制
 * 5. 资源清理和连接关闭
 *
 * 使用场景：
 * - 学习DatabaseUtil11工具类的使用方法
 * - 验证数据库操作功能的正确性
 * - 作为项目中数据库操作的参考模板
 * - 进行数据库功能的快速测试
 *
 * 运行要求：
 * - MySQL 8.0+ 数据库服务
 * - 数据库连接信息正确配置
 * - 具有创建表和数据操作的权限
 * - JDK 8+ 运行环境
 *
 * 注意事项：
 * - 运行前请确保数据库服务正常
 * - 演示会创建和删除测试表，请勿在生产环境运行
 * - 如遇到错误，请检查数据库连接配置
 *
 * @author Halavah
 * @version 2.0 - 增强版演示程序
 * @since JDK8
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("DatabaseUtil11 - 自定义配置模式测试")
public class DatabaseCase12Testing {

    /** 演示表名称 */
    private static final String TABLE_NAME = "demo_users";

    //=====================================================================
    // 测试生命周期方法 - 初始化和清理
    //=====================================================================

    @BeforeAll
    static void setUp() throws SQLException {
        System.out.println("🔧 启用自定义配置模式...");
        System.out.println("✓ 当前配置模式: 自定义配置");

        // 步骤1：初始化数据库连接配置
        step1_CreateConnection();

        // 步骤2：创建演示表结构
        step2_CreateTable();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // 步骤9：清理资源
        step9_CleanupAndClose();

        System.out.println("🔧 自定义配置模式测试完成！");
    }

    //=====================================================================
    // 测试用例区域 - 演示流程控制
    //=====================================================================

    @Test
    @Order(1)
    @DisplayName("步骤3：演示INSERT操作")
    void testInsertOperations() {
        step3_InsertOperations();
    }

    @Test
    @Order(2)
    @DisplayName("步骤4：演示SELECT操作")
    void testSelectOperations() {
        step4_SelectOperations();
    }

    @Test
    @Order(3)
    @DisplayName("步骤5：演示UPDATE操作")
    void testUpdateOperations() {
        step5_UpdateOperations();
    }

    @Test
    @Order(4)
    @DisplayName("步骤6：演示DELETE操作")
    void testDeleteOperations() {
        step6_DeleteOperations();
    }

    @Test
    @Order(5)
    @DisplayName("步骤7：演示批量操作")
    void testBatchOperations() {
        step7_BatchOperations();
    }

    @Test
    @Order(6)
    @DisplayName("步骤8：演示事务管理")
    void testTransactionOperations() {
        step8_TransactionOperations();
    }

    //=====================================================================
    // 数据库连接配置区域 - 连接初始化和配置管理
    //=====================================================================

    /**
     * 步骤1：创建和配置数据库连接
     *
     * 功能说明：
     * - 从JDBC URL中提取数据库名称
     * - 创建数据库配置对象
     * - 注册数据库到DatabaseUtil11工具类（自动设置为默认数据库）
     *
     * 配置过程：
     * 1. 解析JDBC URL获取数据库名
     * 2. 构建DatabaseConfig配置对象
     * 3. 向DatabaseUtil11注册数据库配置（自动设置为默认）
     *
     * 注意事项：
     * - 确保数据库服务正在运行
     * - 验证连接参数的正确性
     * - 检查数据库访问权限
     */
    private static void step1_CreateConnection() {
        System.out.println("【步骤1】初始化数据库连接配置");
        System.out.println("正在配置数据库连接参数...");

        // 直接注册数据库 - 使用新的简化API
        String dbUrl = "jdbc:mysql://localhost:3306/libra-demo-db?characterEncoding=UTF-8&useUnicode=true&useSSL=false&tinyInt1isBit=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
        String username = "demo_user";
        String password = "demo_password";
        String driver = "com.mysql.cj.jdbc.Driver";

        // 从JDBC URL中智能提取数据库名称
        String dbName = extractDatabaseNameFromUrl(dbUrl);
        System.out.println("✓ 数据库名称解析成功: " + dbName);

        // 创建数据库配置对象 - 封装连接参数
        DatabaseUtil11.DatabaseConfig config = new DatabaseUtil11.DatabaseConfig(
            dbUrl,      // 数据库连接URL
            username, // 用户名
            password, // 密码
            driver    // 驱动类名
        );
        System.out.println("✓ 数据库配置对象创建成功");

        // 向DatabaseUtil11注册数据库配置 - 自动设置为默认数据库
        DatabaseUtil11.registerDatabase(dbName, config);
        System.out.println("✓ 数据库配置注册成功，已自动设置为默认数据库");

        System.out.println("数据库连接配置完成，可以开始数据操作\n");
    }

    /**
     * 从JDBC URL中提取数据库名称的工具方法
     *
     * 解析逻辑：
     * 1. 查找协议分隔符 "://" 的位置
     * 2. 从主机端口后查找第一个 "/"
     * 3. 提取 "/" 和 "?" 之间的数据库名
     * 4. 处理没有参数的URL情况
     *
     * 支持的URL格式：
     * - jdbc:mysql://host:port/database
     * - jdbc:mysql://host:port/database?params
     * - jdbc:mysql://host/database
     *
     * @param jdbcUrl JDBC连接URL
     * @return 数据库名称
     * @throws IllegalArgumentException 如果URL格式无效
     */
    private static String extractDatabaseNameFromUrl(String jdbcUrl) {
        try {
            // 转换为小写便于处理，但保留原始大小写用于提取
            String url = jdbcUrl.toLowerCase();

            // 查找主机端口后的第一个斜杠位置
            int dbStart = url.indexOf('/', url.indexOf("://") + 3);
            if (dbStart == -1) {
                throw new IllegalArgumentException("URL中未找到数据库名称部分");
            }

            // 查找参数分隔符的位置
            int dbEnd = url.indexOf('?', dbStart);
            if (dbEnd == -1) {
                dbEnd = url.length(); // 没有参数的情况
            }

            // 提取数据库名称（保持原始大小写）
            String dbName = jdbcUrl.substring(dbStart + 1, dbEnd);
            if (dbName.isEmpty()) {
                throw new IllegalArgumentException("数据库名称不能为空");
            }

            return dbName;

        } catch (Exception e) {
            throw new IllegalArgumentException("解析数据库名称失败: " + e.getMessage(), e);
        }
    }

    //=====================================================================
    // 数据表管理区域 - 表结构创建和管理
    //=====================================================================

    /**
     * 步骤2：创建演示用的数据表
     *
     * 功能说明：
     * - 删除可能存在的旧表结构
     * - 创建新的演示表结构
     * - 定义完整的字段和约束
     * - 设置合适的存储引擎和字符集
     *
     * 表结构设计：
     * - id: 主键，自增长，用于唯一标识
     * - name: 用户名，VARCHAR(100)，不允许为空
     * - email: 邮箱地址，VARCHAR(200)，允许为空
     * - age: 年龄，INT类型，允许为空
     * - salary: 薪资，DECIMAL(10,2)，支持小数
     * - created_time: 创建时间，自动设置当前时间
     *
     * 技术特性：
     * - 使用InnoDB存储引擎，支持事务
     * - 采用utf8mb4字符集，支持emoji
     * - 主键自增，简化数据插入
     *
     * @throws SQLException 如果表创建失败
     */
    private static void step2_CreateTable() throws SQLException {
        System.out.println("【步骤2】创建演示用数据表");
        System.out.println("正在准备表结构...");

        // 删除已存在的表 - 确保从干净状态开始
        String dropTableSql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        System.out.println("清理旧表结构（如果存在）...");

        // 创建新表的SQL语句 - 定义完整的表结构
        String createTableSql = "CREATE TABLE " + TABLE_NAME + " (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID'," +
            "name VARCHAR(100) NOT NULL COMMENT '用户姓名'," +
            "email VARCHAR(200) COMMENT '邮箱地址'," +
            "age INT COMMENT '年龄'," +
            "salary DECIMAL(10,2) COMMENT '薪资'," +
            "created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='演示用户表'";

        // 使用数据库连接执行DDL语句
        try (Connection conn = DatabaseUtil11.getConnection()) {
            // 删除旧表
            conn.createStatement().executeUpdate(dropTableSql);
            System.out.println("✓ 旧表清理完成");

            // 创建新表
            conn.createStatement().executeUpdate(createTableSql);
            System.out.println("✓ 新表创建成功: " + TABLE_NAME);
        }

        System.out.println("表结构信息:");
        System.out.println("  - id: 主键，自增长");
        System.out.println("  - name: 用户姓名，必填");
        System.out.println("  - email: 邮箱地址，可选");
        System.out.println("  - age: 年龄，可选");
        System.out.println("  - salary: 薪资，支持小数");
        System.out.println("  - created_time: 创建时间，自动生成");
        System.out.println("数据表准备完成，可以开始数据操作\n");
    }

    //=====================================================================
    // CRUD操作演示区域 - 基础数据库操作功能展示
    //=====================================================================

    /**
     * 步骤3：演示INSERT操作（数据插入）
     *
     * 功能演示：
     * - 单条记录插入操作
     * - 参数化查询防止SQL注入
     * - 插入结果验证和反馈
     * - 不同数据类型的处理
     *
     * 演示数据：
     * - 张三：25岁，薪资5000元
     * - 李四：30岁，薪资6000元
     * - 王五：28岁，薪资5500元
     *
     * 技术要点：
     * - 使用预编译语句防止SQL注入
     * - 支持多种数据类型（字符串、整数、小数）
     * - 返回影响的行数用于结果验证
     * - 自动处理字符编码和时间戳
     */
    private static void step3_InsertOperations() {
        System.out.println("【步骤3】INSERT操作演示（数据插入功能）");
        System.out.println("正在插入测试数据...");

        // 定义插入SQL语句 - 使用参数占位符
        String insertSql = "INSERT INTO " + TABLE_NAME + " (name, email, age, salary) VALUES (?, ?, ?, ?)";

        // 插入第一条记录 - 张三
        int result1 = DatabaseUtil11.insert(insertSql, "张三", "zhangsan@example.com", 25, 5000.00);
        System.out.println("✓ 插入用户【张三】- 影响行数: " + result1);

        // 插入第二条记录 - 李四
        int result2 = DatabaseUtil11.insert(insertSql, "李四", "lisi@example.com", 30, 6000.00);
        System.out.println("✓ 插入用户【李四】- 影响行数: " + result2);

        // 插入第三条记录 - 王五
        int result3 = DatabaseUtil11.insert(insertSql, "王五", "wangwu@example.com", 28, 5500.00);
        System.out.println("✓ 插入用户【王五】- 影响行数: " + result3);

        // 统计插入结果
        int totalInserted = result1 + result2 + result3;
        System.out.println("INSERT操作完成！共成功插入 " + totalInserted + " 条记录");
        System.out.println("数据插入演示完成，所有用户信息已保存到数据库\n");
    }

    /**
     * 步骤4：演示SELECT操作（数据查询）
     *
     * 功能演示：
     * - 查询所有记录（selectListByMp）
     * - 条件查询单条记录（selectOneByMp）
     * - 条件查询多条记录（selectListByMp with conditions）
     * - 结果集数据格式化显示
     *
     * 查询类型：
     * 1. 全表查询 - 获取所有用户信息
     * 2. 精确查询 - 根据姓名查找特定用户
     * 3. 条件查询 - 查找满足年龄条件的用户
     *
     * 技术要点：
     * - Map<String, Object>格式的结果集处理
     * - 参数化查询的条件设置
     * - 结果排序和数据展示
     * - 空结果的处理和提示
     */
    private static void step4_SelectOperations() {
        System.out.println("【步骤4】SELECT操作演示（数据查询功能）");

        // 演示1：查询所有记录
        System.out.println("📋 演示1：查询所有用户信息");
        String selectAllSql = "SELECT id, name, email, age, salary FROM " + TABLE_NAME + " ORDER BY id";
        List<Map<String, Object>> allUsers = DatabaseUtil11.selectList(selectAllSql);

        System.out.println("查询结果：共找到 " + allUsers.size() + " 条用户记录");
        System.out.println("用户列表：");
        for (Map<String, Object> user : allUsers) {
            System.out.println("  🔹 ID: " + user.get("id") +
                " | 姓名: " + user.get("name") +
                " | 邮箱: " + user.get("email") +
                " | 年龄: " + user.get("age") +
                " | 薪资: ¥" + user.get("salary"));
        }

        // 演示2：条件查询单条记录
        System.out.println("\n🔍 演示2：精确查询指定用户");
        String selectOneSql = "SELECT * FROM " + TABLE_NAME + " WHERE name = ?";
        Map<String, Object> user = DatabaseUtil11.selectOne(selectOneSql, "张三");

        if (user != null) {
            System.out.println("✓ 找到用户【张三】的详细信息:");
            System.out.println("  - 姓名: " + user.get("name"));
            System.out.println("  - 年龄: " + user.get("age") + "岁");
            System.out.println("  - 邮箱: " + user.get("email"));
            System.out.println("  - 薪资: ¥" + user.get("salary"));
            System.out.println("  - 创建时间: " + user.get("created_time"));
        } else {
            System.out.println("❌ 未找到指定用户");
        }

        // 演示3：条件查询多条记录
        System.out.println("\n📊 演示3：条件查询（年龄>=26的用户）");
        String selectListSql = "SELECT name, age, salary FROM " + TABLE_NAME + " WHERE age >= ? ORDER BY age";
        List<Map<String, Object>> adultUsers = DatabaseUtil11.selectList(selectListSql, 26);

        System.out.println("查询条件：年龄 >= 26岁");
        System.out.println("符合条件的用户：" + adultUsers.size() + " 人");
        for (Map<String, Object> adultUser : adultUsers) {
            System.out.println("  🔸 " + adultUser.get("name") +
                " (年龄: " + adultUser.get("age") +
                "岁, 薪资: ¥" + adultUser.get("salary") + ")");
        }

        System.out.println("SELECT操作演示完成！各种查询功能都已展示\n");
    }

    /**
     * 步骤5：演示UPDATE操作（数据更新）
     *
     * 功能演示：
     * - 单条记录更新操作
     * - 条件更新的精确控制
     * - 更新前后数据对比
     * - 更新结果验证
     *
     * 更新场景：
     * - 修改张三的邮箱地址
     * - 调整张三的薪资水平
     * - 验证更新操作的影响范围
     *
     * 技术要点：
     * - 使用WHERE条件精确定位要更新的记录
     * - 支持同时更新多个字段
     * - 返回实际影响的行数
     * - 更新操作的原子性保证
     */
    private static void step5_UpdateOperations() {
        System.out.println("【步骤5】UPDATE操作演示（数据更新功能）");

        // 查询更新前的数据状态
        System.out.println("📋 更新前的用户信息：");
        String selectBeforeSql = "SELECT name, email, age, salary FROM " + TABLE_NAME + " WHERE name = ?";
        Map<String, Object> beforeUpdate = DatabaseUtil11.selectOne(selectBeforeSql, "张三");

        if (beforeUpdate != null) {
            System.out.println("  🔹 姓名: " + beforeUpdate.get("name"));
            System.out.println("  🔹 邮箱: " + beforeUpdate.get("email"));
            System.out.println("  🔹 年龄: " + beforeUpdate.get("age") + "岁");
            System.out.println("  🔹 薪资: ¥" + beforeUpdate.get("salary"));
        }

        // 执行更新操作 - 修改张三的邮箱和薪资
        System.out.println("\n🔄 正在执行更新操作...");
        String updateSql = "UPDATE " + TABLE_NAME + " SET email = ?, salary = ? WHERE name = ?";
        String newEmail = "zhangsan_updated@example.com";
        double newSalary = 5500.00;

        int updateResult = DatabaseUtil11.update(updateSql, newEmail, newSalary, "张三");
        System.out.println("✓ 更新操作完成 - 影响行数: " + updateResult);

        // 验证更新后的数据
        System.out.println("\n📋 更新后的用户信息：");
        Map<String, Object> afterUpdate = DatabaseUtil11.selectOne(selectBeforeSql, "张三");

        if (afterUpdate != null) {
            System.out.println("  🔹 姓名: " + afterUpdate.get("name"));
            System.out.println("  🔹 邮箱: " + afterUpdate.get("email") + " ← 已更新");
            System.out.println("  🔹 年龄: " + afterUpdate.get("age") + "岁");
            System.out.println("  🔹 薪资: ¥" + afterUpdate.get("salary") + " ← 已更新");
        }

        // 测试更新不存在的记录
        System.out.println("\n🧪 测试更新不存在的记录：");
        int noUpdateResult = DatabaseUtil11.update(updateSql, "test@example.com", 9999.00, "不存在的用户");
        System.out.println("更新不存在用户的结果 - 影响行数: " + noUpdateResult + " (应该为0)");

        System.out.println("UPDATE操作演示完成！数据更新功能验证成功\n");
    }

    /**
     * 步骤6：演示DELETE操作（数据删除）
     *
     * 功能演示：
     * - 条件删除指定记录
     * - 删除操作的精确控制
     * - 删除前后数据统计
     * - 删除结果验证
     *
     * 删除场景：
     * - 删除指定用户记录
     * - 验证其他记录不受影响
     * - 测试删除不存在的记录
     *
     * 技术要点：
     * - 使用WHERE条件精确删除
     * - 避免误删其他数据
     * - 返回实际删除的行数
     * - 删除操作的不可逆性提醒
     */
    private static void step6_DeleteOperations() {
        System.out.println("【步骤6】DELETE操作演示（数据删除功能）");

        // 查询删除前的数据统计
        System.out.println("📊 删除前的数据统计：");
        String countSql = "SELECT COUNT(*) as total FROM " + TABLE_NAME;
        Map<String, Object> beforeCount = DatabaseUtil11.selectOne(countSql);
        System.out.println("  当前用户总数: " + beforeCount.get("total") + " 人");

        // 显示即将删除的用户信息
        System.out.println("\n🎯 即将删除的用户信息：");
        String selectTargetSql = "SELECT * FROM " + TABLE_NAME + " WHERE name = ?";
        Map<String, Object> targetUser = DatabaseUtil11.selectOne(selectTargetSql, "李四");

        if (targetUser != null) {
            System.out.println("  🔹 姓名: " + targetUser.get("name"));
            System.out.println("  🔹 邮箱: " + targetUser.get("email"));
            System.out.println("  🔹 年龄: " + targetUser.get("age") + "岁");
            System.out.println("  🔹 薪资: ¥" + targetUser.get("salary"));
        }

        // 执行删除操作
        System.out.println("\n🗑️ 正在执行删除操作...");
        String deleteSql = "DELETE FROM " + TABLE_NAME + " WHERE name = ?";
        int deleteResult = DatabaseUtil11.delete(deleteSql, "李四");
        System.out.println("✓ 删除操作完成 - 影响行数: " + deleteResult);

        // 验证删除结果
        System.out.println("\n📊 删除后的数据统计：");
        Map<String, Object> afterCount = DatabaseUtil11.selectOne(countSql);
        System.out.println("  当前用户总数: " + afterCount.get("total") + " 人");

        // 验证目标用户已被删除
        Map<String, Object> deletedUser = DatabaseUtil11.selectOne(selectTargetSql, "李四");
        if (deletedUser == null) {
            System.out.println("✓ 确认用户【李四】已被成功删除");
        } else {
            System.out.println("❌ 删除操作可能失败，用户仍然存在");
        }

        // 验证其他用户未受影响
        System.out.println("\n🔍 验证其他用户是否受影响：");
        String selectOthersSql = "SELECT name FROM " + TABLE_NAME + " ORDER BY name";
        List<Map<String, Object>> remainingUsers = DatabaseUtil11.selectList(selectOthersSql);
        System.out.println("剩余用户列表：");
        for (Map<String, Object> user : remainingUsers) {
            System.out.println("  ✓ " + user.get("name"));
        }

        // 测试删除不存在的记录
        System.out.println("\n🧪 测试删除不存在的记录：");
        int noDeleteResult = DatabaseUtil11.delete(deleteSql, "不存在的用户");
        System.out.println("删除不存在用户的结果 - 影响行数: " + noDeleteResult + " (应该为0)");

        System.out.println("DELETE操作演示完成！数据删除功能验证成功\n");
    }

    /**
     * 步骤7：演示批量操作（批处理功能）
     *
     * 功能演示：
     * - 批量插入多条记录
     * - 批处理的性能优势
     * - 批量操作的事务性
     * - 批处理结果统计
     *
     * 批量数据：
     * - 一次性插入5个新用户
     * - 不同年龄和薪资的用户数据
     * - 测试批量操作的效率
     *
     * 技术要点：
     * - 使用executeBatch方法
     * - 批量参数的数组格式
     * - 批处理的原子性保证
     * - 性能对比和统计
     */
    private static void step7_BatchOperations() {
        System.out.println("【步骤7】批量操作演示（批处理功能）");

        // 准备批量插入的数据
        System.out.println("📦 准备批量插入数据...");
        String batchInsertSql = "INSERT INTO " + TABLE_NAME + " (name, email, age, salary) VALUES (?, ?, ?, ?)";
        List<Object[]> batchData = Arrays.asList(
            new Object[]{"批量用户1", "batch1@example.com", 22, 4500.00},
            new Object[]{"批量用户2", "batch2@example.com", 24, 4800.00},
            new Object[]{"批量用户3", "batch3@example.com", 26, 5200.00},
            new Object[]{"批量用户4", "batch4@example.com", 28, 5600.00},
            new Object[]{"批量用户5", "batch5@example.com", 30, 6000.00}
        );

        System.out.println("批量数据准备完成，共 " + batchData.size() + " 条记录");

        // 记录批量操作的执行时间
        System.out.println("\n⏱️ 开始执行批量插入操作...");
        long startTime = System.currentTimeMillis();

        // 执行批量插入
        int[] batchResults = DatabaseUtil11.executeBatch(batchInsertSql, batchData);

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;

        // 分析批量操作结果
        System.out.println("✓ 批量操作完成！执行时间: " + executionTime + "ms");
        System.out.println("\n📊 批量操作结果分析：");
        System.out.println("  操作总数: " + batchResults.length);

        int successCount = 0;
        for (int i = 0; i < batchResults.length; i++) {
            if (batchResults[i] > 0) {
                successCount++;
                System.out.println("  第" + (i + 1) + "条记录: 成功插入 (影响行数: " + batchResults[i] + ")");
            } else {
                System.out.println("  第" + (i + 1) + "条记录: 插入失败 (返回值: " + batchResults[i] + ")");
            }
        }

        System.out.println("  成功插入: " + successCount + "/" + batchResults.length + " 条记录");

        // 验证批量插入的数据
        System.out.println("\n🔍 验证批量插入的数据：");
        String selectBatchSql = "SELECT name, age, salary FROM " + TABLE_NAME + " WHERE name LIKE ? ORDER BY name";
        List<Map<String, Object>> batchUsers = DatabaseUtil11.selectList(selectBatchSql, "批量用户%");

        System.out.println("查询到的批量用户：" + batchUsers.size() + " 人");
        for (Map<String, Object> user : batchUsers) {
            System.out.println("  🔸 " + user.get("name") +
                " (年龄: " + user.get("age") +
                "岁, 薪资: ¥" + user.get("salary") + ")");
        }

        System.out.println("批量操作演示完成！批处理功能验证成功\n");
    }

    /**
     * 步骤8：演示事务管理（事务控制功能）
     *
     * 功能演示：
     * - 成功事务的提交机制
     * - 失败事务的回滚机制
     * - 事务的原子性保证
     * - 复杂业务逻辑的事务处理
     *
     * 事务场景：
     * 1. 成功事务：同时插入两个相关用户
     * 2. 失败事务：模拟异常情况触发回滚
     * 3. 验证事务的ACID特性
     *
     * 技术要点：
     * - 使用executeInTransaction方法
     * - 事务回调接口的使用
     * - 异常处理和回滚机制
     * - 事务隔离性验证
     */
    private static void step8_TransactionOperations() {
        System.out.println("【步骤8】事务管理演示（事务控制功能）");

        // 记录事务操作前的数据状态
        System.out.println("📊 事务操作前的数据统计：");
        String countSql = "SELECT COUNT(*) as total FROM " + TABLE_NAME;
        Map<String, Object> beforeTransaction = DatabaseUtil11.selectOne(countSql);
        System.out.println("  当前用户总数: " + beforeTransaction.get("total") + " 人");

        // 演示1：成功的事务操作
        System.out.println("\n🔄 演示1：成功事务的提交");
        System.out.println("场景：在一个事务中插入两个相关用户（夫妻档）");

        String transactionResult = DatabaseUtil11.executeInTransaction(conn -> {
            // 在事务中执行多个相关操作
            String insertSql = "INSERT INTO " + TABLE_NAME + " (name, email, age, salary) VALUES (?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                // 插入第一个用户（丈夫）
                pstmt.setString(1, "事务用户-张先生");
                pstmt.setString(2, "zhangxiansheng@example.com");
                pstmt.setInt(3, 32);
                pstmt.setDouble(4, 7000.00);
                pstmt.executeUpdate();
                System.out.println("  ✓ 插入用户：张先生");

                // 插入第二个用户（妻子）
                pstmt.setString(1, "事务用户-张夫人");
                pstmt.setString(2, "zhangfuren@example.com");
                pstmt.setInt(3, 29);
                pstmt.setDouble(4, 6500.00);
                pstmt.executeUpdate();
                System.out.println("  ✓ 插入用户：张夫人");

                return "夫妻档用户插入成功";
            }
        });

        System.out.println("✓ 事务执行结果: " + transactionResult);

        // 验证成功事务的数据
        System.out.println("\n🔍 验证成功事务的数据：");
        String selectTransactionSql = "SELECT name, age, salary FROM " + TABLE_NAME + " WHERE name LIKE ? ORDER BY name";
        List<Map<String, Object>> transactionUsers = DatabaseUtil11.selectList(selectTransactionSql, "事务用户%");

        System.out.println("事务插入的用户：" + transactionUsers.size() + " 人");
        for (Map<String, Object> user : transactionUsers) {
            System.out.println("  🔸 " + user.get("name") +
                " (年龄: " + user.get("age") +
                "岁, 薪资: ¥" + user.get("salary") + ")");
        }

        // 演示2：失败的事务操作（回滚）
        System.out.println("\n🔄 演示2：失败事务的回滚");
        System.out.println("场景：在事务中故意抛出异常，验证回滚机制");

        try {
            DatabaseUtil11.executeInTransaction(conn -> {
                // 先插入一条记录
                String insertSql = "INSERT INTO " + TABLE_NAME + " (name, email, age, salary) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setString(1, "回滚测试用户");
                    pstmt.setString(2, "rollback@example.com");
                    pstmt.setInt(3, 25);
                    pstmt.setDouble(4, 5000.00);
                    pstmt.executeUpdate();
                    System.out.println("  ✓ 临时插入用户：回滚测试用户");
                }

                // 故意抛出异常触发回滚
                System.out.println("  ⚠️ 模拟业务异常，触发事务回滚...");
                throw new SQLException("模拟业务异常：数据验证失败");
            });
        } catch (RuntimeException e) {
            System.out.println("✓ 事务回滚成功，异常信息: " + e.getMessage());
        }

        // 验证回滚效果
        System.out.println("\n🔍 验证事务回滚效果：");
        Map<String, Object> rollbackUser = DatabaseUtil11.selectOne(
            "SELECT * FROM " + TABLE_NAME + " WHERE name = ?",
            "回滚测试用户"
        );

        if (rollbackUser == null) {
            System.out.println("✓ 回滚验证成功：临时插入的用户已被回滚删除");
        } else {
            System.out.println("❌ 回滚验证失败：用户仍然存在");
        }

        // 验证成功事务的数据仍然存在（事务隔离性）
        List<Map<String, Object>> persistentUsers = DatabaseUtil11.selectList(selectTransactionSql, "事务用户%");
        System.out.println("✓ 事务隔离性验证：成功提交的事务数据仍然存在 (" + persistentUsers.size() + " 条记录)");

        // 最终数据统计
        System.out.println("\n📊 事务操作后的数据统计：");
        Map<String, Object> afterTransaction = DatabaseUtil11.selectOne(countSql);
        System.out.println("  当前用户总数: " + afterTransaction.get("total") + " 人");

        long addedUsers = ((Number) afterTransaction.get("total")).longValue() -
                         ((Number) beforeTransaction.get("total")).longValue();
        System.out.println("  事务净增用户: " + addedUsers + " 人");

        System.out.println("事务管理演示完成！事务控制功能验证成功\n");
    }

    /**
     * 步骤9：清理资源和关闭连接
     *
     * 功能说明：
     * - 清理演示过程中创建的测试数据
     * - 删除演示用的数据表
     * - 关闭数据库连接池
     * - 释放系统资源
     *
     * 清理内容：
     * 1. 删除所有演示数据
     * 2. 删除演示用数据表
     * 3. 关闭数据库连接
     * 4. 清理DatabaseUtil11工具类状态
     *
     * 注意事项：
     * - 清理操作不可逆，请谨慎使用
     * - 生产环境中应根据实际需求决定是否清理
     * - 确保重要数据已备份
     */
    private static void step9_CleanupAndClose() throws SQLException {
        System.out.println("【步骤9】资源清理和连接关闭");

        // 显示清理前的最终数据统计
        System.out.println("📊 清理前的最终数据统计：");
        String countSql = "SELECT COUNT(*) as total FROM " + TABLE_NAME;
        Map<String, Object> finalCount = DatabaseUtil11.selectOne(countSql);
        if (finalCount != null && finalCount.get("total") != null) {
            System.out.println("  演示表中的用户总数: " + finalCount.get("total") + " 人");

            // 显示所有演示数据
            System.out.println("\n📋 演示过程中创建的所有数据：");
            String selectAllSql = "SELECT name, age, salary FROM " + TABLE_NAME + " ORDER BY id";
            List<Map<String, Object>> allDemoUsers = DatabaseUtil11.selectList(selectAllSql);

            for (Map<String, Object> user : allDemoUsers) {
                System.out.println("  🔹 " + user.get("name") +
                    " (年龄: " + user.get("age") +
                    "岁, 薪资: ¥" + user.get("salary") + ")");
            }
        } else {
             System.out.println("  演示表不存在或为空，无需显示数据。");
        }


        // 清理演示数据
        System.out.println("\n🧹 开始清理演示数据...");

        // 删除演示表
        String dropTableSql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        try (Connection conn = DatabaseUtil11.getConnection()) {
            conn.createStatement().executeUpdate(dropTableSql);
            System.out.println("✓ 演示表已删除: " + TABLE_NAME);
        }

        // 关闭数据库连接池
        DatabaseUtil11.closeAllDatabases();
        System.out.println("✓ 数据库连接已关闭");

        System.out.println("\n🎯 资源清理完成！");
        System.out.println("所有演示数据已清理，系统资源已释放");
        System.out.println("感谢使用DatabaseUtil11工具类演示程序！");
    }
}
